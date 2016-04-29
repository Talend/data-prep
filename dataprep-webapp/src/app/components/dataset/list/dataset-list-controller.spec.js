/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset list controller', () => {

    let createController, scope, stateMock;

    const datasets = [
        {id: 'ec4834d9bc2af8', name: 'Customers (50 lines)'},
        {id: 'ab45f893d8e923', name: 'Us states'},
        {id: 'cf98d83dcb9437', name: 'Customers (1K lines)'}
    ];
    const theCurrentFolder = {path: 'folder-16/folder-1/sub-1', name: 'sub-1'};

    beforeEach(angular.mock.module('data-prep.dataset-list', ($provide) => {
        stateMock = {
            inventory: {
                datasets: [],
                currentFolder: theCurrentFolder,
                currentFolderContent: {
                    datasets: [datasets[0]]
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $componentController, $state, StateService, MessageService) => {
        scope = $rootScope.$new();

        createController = () => $componentController('datasetList', {$scope: scope});

        spyOn($state, 'go').and.returnValue();
        spyOn(MessageService, 'error').and.returnValue();
        spyOn(MessageService, 'success').and.returnValue();
        spyOn(StateService, 'setDatasetName').and.returnValue();
    }));

    describe('on init', () => {
        it('should get content of root folder', inject(($q, $stateParams, FolderService) => {
            //given
            $stateParams.folderPath = '';
            spyOn(FolderService, 'getContent').and.returnValue($q.when());

            //when
            const ctrl = createController();
            ctrl.$onInit();

            //then
            expect(FolderService.getContent).toHaveBeenCalled();

        }));

        it('should get content of router param', inject(($q, $stateParams, FolderService) => {
            //given
            $stateParams.folderPath = 'test';
            spyOn(FolderService, 'getContent').and.returnValue($q.when());

            //when
            const ctrl = createController();
            ctrl.$onInit();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith({path: 'test', name: 'test'});
        }));

        it('should get content of router param when path ends with /', inject(function ($q, $stateParams, FolderService) {
            //given
            $stateParams.folderPath = 'test/test1/';
            spyOn(FolderService, 'getContent').and.returnValue($q.when());

            //when
            const ctrl = createController();
            ctrl.$onInit();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith({path: 'test/test1/', name: 'test1'});
        }));

        it('should go back to root folder when request is failed', inject(function ($stateParams, $q, $state, FolderService) {
            //given
            $stateParams.folderPath = 'test/';
            spyOn(FolderService, 'getContent').and.returnValue($q.reject());

            //when
            const ctrl = createController();
            ctrl.$onInit();
            scope.$digest();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.index.datasets', {folderPath: ''});

        }));
    });

    describe('open preparation', () => {
        const preparation = {
            id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
            author: 'anonymousUser'
        };
    });

    describe('update dataset content', () => {
        it('should upload selected file', inject((UpdateWorkflowService) => {
            //given
            spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue();

            var ctrl = createController();
            var dataset = {id: '13b484a9380fa54c02'};
            var updatedDatasetFile = {};
            ctrl.updateDatasetFile = [updatedDatasetFile];

            //when
            ctrl.uploadUpdatedDatasetFile(dataset);

            //then
            expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith(updatedDatasetFile, dataset);
        }));
    });

    describe('remove dataset', () => {
        beforeEach(inject(($q, FolderService, TalendConfirmService, DatasetService) => {
            spyOn(FolderService, 'getContent').and.returnValue($q.when());
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when());
            spyOn(DatasetService, 'delete').and.returnValue($q.when());
        }));

        it('should ask confirmation before deletion', inject(($q, TalendConfirmService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);

            //then
            expect(TalendConfirmService.confirm).toHaveBeenCalledWith({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                type: 'dataset',
                name: 'Customers (50 lines)'
            });
        }));

        it('should remove dataset', inject(($q, DatasetService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);
            scope.$digest();

            //then
            expect(DatasetService.delete).toHaveBeenCalledWith(dataset);
        }));

        it('should show confirmation message', inject((MessageService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            //when
            ctrl.remove(dataset);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
                type: 'dataset',
                name: 'Customers (50 lines)'
            });
        }));

        it('should refresh folder content', inject((FolderService) => {
            //given
            const dataset = datasets[0];
            const ctrl = createController();

            expect(FolderService.getContent).not.toHaveBeenCalled();

            //when
            ctrl.remove(dataset);
            scope.$digest();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith(theCurrentFolder);
        }));
    });

    describe('rename dataset', () => {
        describe('invalid name', () => {
            it('should NOT rename with falsy name', inject(($q, DatasetService) => {
                //given
                const ctrl = createController();
                const name = 'dataset name';
                const dataset = {name: name};

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, '');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should NOT rename with blank name', inject(($q, DatasetService) => {
                //given
                const ctrl = createController();
                const name = 'dataset name';
                const dataset = {name: name};

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, ' ');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should NOT rename with an already existing name', inject(($q, DatasetService, MessageService) => {
                //given
                const ctrl = createController();
                const name = 'foo';
                const dataset = {name: name};

                spyOn(DatasetService, 'getDatasetByName').and.returnValue({id: 'ab45f893d8e923', name: 'Us states'});
                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, 'Us states');
                scope.$digest();

                //then
                expect(dataset.name).toBe(name);
                expect(DatasetService.update).not.toHaveBeenCalled();
                expect(MessageService.error).toHaveBeenCalledWith('DATASET_NAME_ALREADY_USED_TITLE', 'DATASET_NAME_ALREADY_USED');
            }));
        });

        describe('with current pending rename', () => {
            it('should NOT rename', inject(($q, DatasetService) => {
                //given
                const ctrl = createController();
                const dataset = {id: '461465'};
                const name = 'new dataset name';
                ctrl.renamingList.push(dataset);

                spyOn(DatasetService, 'update').and.returnValue($q.when());

                //when
                ctrl.rename(dataset, name);

                //then
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should show a warning', inject(($q, MessageService) => {
                //given
                const ctrl = createController();
                const dataset = {id: '461465'};
                const name = 'new dataset name';
                ctrl.renamingList.push(dataset);

                spyOn(MessageService, 'warning').and.returnValue();

                //when
                ctrl.rename(dataset, name);

                //then
                expect(MessageService.warning).toHaveBeenCalledWith(
                    'DATASET_CURRENTLY_RENAMING_TITLE',
                    'DATASET_CURRENTLY_RENAMING'
                );
            }));
        });

        it('should rename dataset', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const dataset = {name: 'my old name'};
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());

            //when
            ctrl.rename(dataset, name);

            //then
            expect(DatasetService.rename).toHaveBeenCalledWith(dataset, name);
        }));

        it('should show confirmation message', inject(($q, DatasetService, MessageService) => {
            //given
            const ctrl = createController();
            const dataset = {name: 'my old name'};
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());

            //when
            ctrl.rename(dataset, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('DATASET_RENAME_SUCCESS_TITLE', 'DATASET_RENAME_SUCCESS');
        }));

        it('should manage "renaming" list', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const dataset = {name: 'my old name'};
            const name = 'new dataset name';

            spyOn(DatasetService, 'rename').and.returnValue($q.when());
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(false);

            //when
            ctrl.rename(dataset, name);
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.renamingList.indexOf(dataset) > -1).toBe(false);
        }));
    });

    describe('certification', () => {
        beforeEach(inject(($q, FolderService, DatasetService) => {
            spyOn(FolderService, 'getContent').and.returnValue($q.when());
            spyOn(DatasetService, 'processCertification').and.returnValue($q.when());
        }));

        it('should process certification', inject((DatasetService) => {
            //given
            const ctrl = createController();

            //when
            ctrl.processCertification(datasets[0]);

            //then
            expect(DatasetService.processCertification).toHaveBeenCalledWith(datasets[0]);
        }));

        it('should update folder content', inject((FolderService) => {
            //given
            const ctrl = createController();

            expect(FolderService.getContent).not.toHaveBeenCalled();

            //when
            ctrl.processCertification(datasets[0]);
            scope.$digest();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith(theCurrentFolder);
        }));
    });

    describe('change folder', () => {
        it('should go to folder', inject(($state) => {
            //given
            const ctrl = createController();

            //when
            ctrl.goToFolder({path: '1/2', name: '2'});

            //then
            expect($state.go).toHaveBeenCalledWith('nav.index.datasets', {folderPath: '1/2'});
        }));
    });

    describe('rename folder', () => {
        beforeEach(inject(($q, FolderService) => {
            spyOn(FolderService, 'getContent').and.returnValue($q.when());
            spyOn(FolderService, 'rename').and.returnValue($q.when());
        }));

        it('should rename folder', inject((FolderService) => {
            //given
            const ctrl = createController();
            const folderToRename = {path: 'toto/1'};

            //when
            ctrl.renameFolder(folderToRename, '2');

            //then
            expect(FolderService.rename).toHaveBeenCalledWith('toto/1', 'toto/2');
        }));

        it('should refresh current folder', inject((FolderService) => {
            //given
            const ctrl = createController();
            const folderToRename = {path: 'toto/1'};

            expect(FolderService.getContent).not.toHaveBeenCalled();

            //when
            ctrl.renameFolder(folderToRename, '2');
            scope.$digest();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith(theCurrentFolder);
        }));
    });

    describe('remove folder', () => {
        beforeEach(inject(($q, FolderService) => {
            spyOn(FolderService, 'remove').and.returnValue($q.when());
            spyOn(FolderService, 'getContent').and.returnValue($q.when());
        }));

        it('should remove folder', inject((FolderService) => {
            //given
            const ctrl = createController();
            const folder = {path: 'toto'};

            //when
            ctrl.removeFolder(folder);

            //then
            expect(FolderService.remove).toHaveBeenCalledWith(folder.path);
        }));

        it('should refresh current folder', inject((FolderService) => {
            //given
            const ctrl = createController();
            const folder = {path: 'toto'};

            expect(FolderService.getContent).not.toHaveBeenCalled();

            //when
            ctrl.removeFolder(folder);
            scope.$digest();

            //then
            expect(FolderService.getContent).toHaveBeenCalledWith(theCurrentFolder);
        }));
    });

    describe('copy/move dataset', () => {
        const dataset = {name: 'my Dataset'};
        const folderDest = {name: 'my folder destination', path: '/folder1/folder2'};
        const name = {name: 'my new Dataset name'};

        beforeEach(inject(($q, FolderService) => {
            spyOn(FolderService, 'getContent').and.returnValue($q.when());
        }));

        it('should open copy/move form', () => {
            //given
            const dataset = {id: '4903d57a449cf75b606'};
            const ctrl = createController();
            ctrl.datasetCopyVisibility = false;
            ctrl.datasetToCopyMove = null;

            //when
            ctrl.openFolderSelection(dataset);

            //then
            expect(ctrl.datasetCopyVisibility).toBe(true);
            expect(ctrl.datasetToCopyMove).toBe(dataset);
        });

        describe('copy', () => {
            beforeEach(inject(($q, DatasetService) => {
                spyOn(DatasetService, 'clone').and.returnValue($q.when());
            }));

            it('should call clone function', inject((DatasetService) => {
                //given
                const ctrl = createController();
                expect(DatasetService.clone).not.toHaveBeenCalled();

                //when
                ctrl.clone(dataset, folderDest, name);

                //then
                expect(DatasetService.clone).toHaveBeenCalledWith(dataset, folderDest, name);
            }));

            it('should show success message on clone success', inject((MessageService) => {
                //given
                const ctrl = createController();
                expect(MessageService.success).not.toHaveBeenCalled();

                //when
                ctrl.clone(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(MessageService.success).toHaveBeenCalledWith('COPY_SUCCESS_TITLE', 'COPY_SUCCESS');
            }));

            it('should show refresh current folder content', inject((FolderService) => {
                //given
                const ctrl = createController();
                expect(FolderService.getContent).not.toHaveBeenCalledWith(stateMock.inventory.currentFolder);

                //when
                ctrl.clone(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(FolderService.getContent).toHaveBeenCalledWith(stateMock.inventory.currentFolder);
            }));

            it('should hide clone modal', () => {
                //given
                const ctrl = createController();

                //when
                ctrl.clone(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(ctrl.datasetCopyVisibility).toBe(false);
            });
        });

        describe('move', () => {
            beforeEach(inject(($q, DatasetService) => {
                spyOn(DatasetService, 'move').and.returnValue($q.when());
            }));

            it('should call move function', inject((DatasetService) => {
                //given
                const ctrl = createController();
                expect(DatasetService.move).not.toHaveBeenCalled();

                //when
                ctrl.move(dataset, folderDest, name);

                //then
                expect(DatasetService.move).toHaveBeenCalledWith(dataset, folderDest, name);
            }));

            it('should show success message on move success', inject((MessageService) => {
                //given
                const ctrl = createController();
                expect(MessageService.success).not.toHaveBeenCalled();

                //when
                ctrl.move(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(MessageService.success).toHaveBeenCalledWith('MOVE_SUCCESS_TITLE', 'MOVE_SUCCESS');
            }));

            it('should refresh current folder content', inject((FolderService) => {
                //given
                const ctrl = createController();
                expect(FolderService.getContent).not.toHaveBeenCalledWith(stateMock.inventory.currentFolder);

                //when
                ctrl.move(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(FolderService.getContent).toHaveBeenCalledWith(stateMock.inventory.currentFolder);
            }));

            it('should hide copy modal', () => {
                //given
                const ctrl = createController();

                //when
                ctrl.move(dataset, folderDest, name);
                scope.$digest();

                //then
                expect(ctrl.datasetCopyVisibility).toBe(false);
            });
        });
    });
});
