/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Header controller', () => {

    let createController, scope;

    beforeEach(angular.mock.module('data-prep.inventory-header'));

    beforeEach(inject(function ($rootScope, $controller, $q) {
        scope = $rootScope.$new();

        createController = () => {
            const ctrl = $controller(
                'InventoryHeaderCtrl',
                { $scope: scope }
            );
            ctrl.onFolderCreation = jasmine.createSpy('onFolderCreation').and.returnValue($q.when());
            ctrl.folderNameForm = { $commitViewValue: jasmine.createSpy('$commitViewValue') };
            return ctrl;
        };
    }));

    describe('open folder modal', () => {
        it('should reset folder name', inject(() => {
            // given
            const ctrl = createController();
            ctrl.folderName = 'toto';

            // when
            ctrl.openFolderModal();

            // then
            expect(ctrl.folderName).toBe('');
        }));

        it('should show folder name', inject(() => {
            // given
            const ctrl = createController();
            ctrl.folderNameModal = false;

            // when
            ctrl.openFolderModal();

            // then
            expect(ctrl.folderNameModal).toBe(true);
        }));
    });

    describe('add folder', () => {
        it('should force model sync', inject(() => {
            // given
            const ctrl = createController();
            expect(ctrl.folderNameForm.$commitViewValue).not.toHaveBeenCalled();

            // when
            ctrl.addFolder();

            // then
            expect(ctrl.folderNameForm.$commitViewValue).toHaveBeenCalled();
        }));

        it('should trigger folder creation callback', inject(() => {
            // given
            const ctrl = createController();
            ctrl.folderName = 'toto';
            expect(ctrl.onFolderCreation).not.toHaveBeenCalled();

            // when
            ctrl.addFolder();

            // then
            expect(ctrl.onFolderCreation).toHaveBeenCalledWith({ name: 'toto' });
        }));

        it('should close folder creation modal', inject(() => {
            // given
            const ctrl = createController();
            ctrl.folderNameModal = true;

            // when
            ctrl.addFolder();
            expect(ctrl.folderNameModal).toBe(true);
            scope.$digest();

            // then
            expect(ctrl.folderNameModal).toBe(false);
        }));
    });
});
