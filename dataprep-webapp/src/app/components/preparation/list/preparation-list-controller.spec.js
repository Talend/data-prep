/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation list controller', () => {
    'use strict';

    let createController, scope, stateMock;

    beforeEach(angular.mock.module('data-prep.preparation-list', ($provide) => {
        stateMock = {
            inventory: {
                folder: { metadata: { path: '/my/path' } }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($state, $q, $rootScope, $componentController, PreparationService, FolderService, MessageService, StateService) => {
        scope = $rootScope.$new();

        createController = () => {
            return $componentController('preparationList', {
                $scope: scope
            });
        };

        spyOn(PreparationService, 'copy').and.returnValue($q.when());
        spyOn(PreparationService, 'move').and.returnValue($q.when());
        spyOn(PreparationService, 'delete').and.returnValue($q.when());
        spyOn(PreparationService, 'setName').and.returnValue($q.when());
        spyOn(FolderService, 'refreshContent').and.returnValue($q.when());
        spyOn(FolderService, 'remove').and.returnValue($q.when());
        spyOn(FolderService, 'rename').and.returnValue($q.when());
        spyOn(StateService, 'setPreviousRoute').and.returnValue();
        spyOn(MessageService, 'success').and.returnValue(null);
        spyOn(MessageService, 'error').and.returnValue(null);
        spyOn($state, 'go').and.returnValue();
    }));

    describe('remove', () => {
        it('should show confirmation dialog', inject(($q, TalendConfirmService) => {
            //given
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());

            const ctrl = createController();
            const preparation = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                name: 'my preparation',
            };

            //when
            ctrl.remove(preparation);

            //then
            expect(TalendConfirmService.confirm).toHaveBeenCalledWith(
                { disableEnter: true },
                ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'],
                { type: 'preparation', name: preparation.name }
            );
        }));

        it('should do nothing on dialog cancel', inject(($q, TalendConfirmService, PreparationService, MessageService) => {
            //given
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.reject());

            const ctrl = createController();
            const preparation = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                name: 'my preparation',
            };

            //when
            ctrl.remove(preparation);
            scope.$digest();

            //then
            expect(PreparationService.delete).not.toHaveBeenCalled();
            expect(MessageService.success).not.toHaveBeenCalled();
        }));

        it('should remove preparation on dialog confirm', inject(($q, TalendConfirmService, PreparationService) => {
            //given
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());

            const ctrl = createController();
            const preparation = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                name: 'my preparation',
            };

            //when
            ctrl.remove(preparation);
            scope.$digest();

            //then
            expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
        }));

        it('should refresh folder content on dialog confirm', inject(($q, TalendConfirmService, FolderService) => {
            //given
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());

            const ctrl = createController();
            const preparation = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                name: 'my preparation',
            };

            //when
            ctrl.remove(preparation);
            scope.$digest();

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));

        it('should show success message on dialog confirm', inject(($q, TalendConfirmService, MessageService) => {
            //given
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());

            const ctrl = createController();
            const preparation = {
                id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                name: 'my preparation',
            };

            //when
            ctrl.remove(preparation);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith(
                'REMOVE_SUCCESS_TITLE',
                'REMOVE_SUCCESS',
                { type: 'preparation', name: preparation.name }
            );
        }));
    });

    describe('rename', () => {

        it('should call preparation service', inject((PreparationService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer', name: 'my old name' };
            const name = 'new preparation name';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(PreparationService.setName).toHaveBeenCalledWith(preparation.id, name);
        }));

        it('should refresh folder content', inject(($q, TalendConfirmService, FolderService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer', name: 'my old name' };
            const name = 'new preparation name';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));

        it('should show success message', inject((MessageService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer', name: 'my old name' };
            const name = 'new preparation name';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('PREPARATION_RENAME_SUCCESS_TITLE', 'PREPARATION_RENAME_SUCCESS');
        }));

        it('should not call preparation service with blank name', inject((PreparationService, MessageService) => {
            //given

            const ctrl = createController();
            const preparation = { id: 'foo_beer', name: 'my old name' };
            const name = ' ';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(PreparationService.setName).not.toHaveBeenCalled();
            expect(MessageService.success).not.toHaveBeenCalled();
        }));

        it('should not call preparation service with null name', inject((PreparationService, MessageService) => {
            //given

            const ctrl = createController();
            const preparation = { id: 'foo_beer', name: 'my old name' };

            //when
            ctrl.rename(preparation);
            scope.$digest();

            //then
            expect(PreparationService.setName).not.toHaveBeenCalled();
            expect(MessageService.success).not.toHaveBeenCalled();
        }));
    });

    describe('copy', () => {
        it('should init copy modal', inject(() => {
            // given
            const ctrl = createController();
            const preparation = { id: 'foo_beer' };

            expect(ctrl.preparationToCopyMove).toBeFalsy();
            expect(ctrl.copyMoveModal).toBeFalsy();

            // when
            ctrl.openCopyMoveModal(preparation);

            // then
            expect(ctrl.preparationToCopyMove).toBe(preparation);
            expect(ctrl.copyMoveModal).toBe(true);
        }));

        it('should call preparation service', inject((PreparationService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            //when
            ctrl.copy(preparation, folder, name);
            scope.$digest();

            //then
            expect(PreparationService.copy).toHaveBeenCalledWith(preparation.id, '/copy/path', 'new name');
        }));

        it('should refresh folder content', inject(($q, TalendConfirmService, FolderService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            //when
            ctrl.copy(preparation, folder, name);
            scope.$digest();

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));

        it('should show message', inject((MessageService) => {
            //given
            const ctrl = createController();
            const preparation = { id: 'foo_beer' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            expect(MessageService.success).not.toHaveBeenCalled();

            //when
            ctrl.copy(preparation,  folder, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('PREPARATION_COPYING_SUCCESS_TITLE', 'PREPARATION_COPYING_SUCCESS');
        }));
    });

    describe('move', () => {
        it('should call preparation service', inject((PreparationService) => {
            //given
            const ctrl = createController();
            const preparation = { id: '3567e18ff9147da98c53' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            //when
            ctrl.move(preparation, folder, name);
            scope.$digest();

            //then
            expect(PreparationService.move).toHaveBeenCalledWith(
                '3567e18ff9147da98c53', // preparation id
                '/my/path',             // current folder from state
                '/copy/path',           // destination
                'new name'              // new name
            );
        }));

        it('should refresh folder content', inject(($q, TalendConfirmService, FolderService) => {
            //given
            const ctrl = createController();
            const preparation = { id: '3567e18ff9147da98c53' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            //when
            ctrl.move(preparation, folder, name);
            scope.$digest();

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));

        it('should show message', inject((MessageService) => {
            //given
            const ctrl = createController();
            const preparation = { id: '3567e18ff9147da98c53' };
            const folder = { path: '/copy/path' };
            const name = 'new name';

            //when
            ctrl.move(preparation, folder, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('PREPARATION_MOVING_SUCCESS_TITLE', 'PREPARATION_MOVING_SUCCESS');
        }));
    });

    describe('go to folder', () => {
        it('should redirect to folder route', inject(($state) => {
            // given
            const folder = { path: '/my/new/folder' };
            const ctrl = createController();

            expect($state.go).not.toHaveBeenCalled();

            // when
            ctrl.goToFolder(folder);

            // then
            expect($state.go).toHaveBeenCalledWith('nav.index.preparations', { folderPath: folder.path });
        }));
    });

    describe('rename folder', () => {
        it('should call service', inject((FolderService) => {
            // given
            const folder = { path: '/my/folder' };
            const ctrl = createController();

            expect(FolderService.rename).not.toHaveBeenCalled();

            // when
            ctrl.renameFolder(folder, 'newFolder');

            // then
            expect(FolderService.rename).toHaveBeenCalledWith('/my/folder', '/my/newFolder');
        }));

        it('should refresh folder content', inject((FolderService) => {
            // given
            const folder = { path: '/my/folder' };
            const ctrl = createController();

            expect(FolderService.refreshContent).not.toHaveBeenCalled();

            // when
            ctrl.renameFolder(folder, 'newFolder');
            scope.$digest();

            // then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));
    });

    describe('remove folder', () => {
        it('should call service', inject((FolderService) => {
            // given
            const folder = { path: '/my/folder' };
            const ctrl = createController();

            expect(FolderService.remove).not.toHaveBeenCalled();

            // when
            ctrl.removeFolder(folder);

            // then
            expect(FolderService.remove).toHaveBeenCalledWith('/my/folder');
        }));

        it('should refresh folder content', inject((FolderService) => {
            // given
            const folder = { path: '/my/folder' };
            const ctrl = createController();

            expect(FolderService.refreshContent).not.toHaveBeenCalled();

            // when
            ctrl.removeFolder(folder);
            scope.$digest();

            // then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/path'); //path from state
        }));
    });
});
