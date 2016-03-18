/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('DatasetCopyMove controller', () => {
    let createController, scope, ctrl, $element;

    beforeEach(angular.mock.module('data-prep.dataset-copy-move'));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();
        $element = {};
        createController = () => {
            return $componentController('datasetCopyMove', {
                $scope: scope,
                $element: $element
            }, {
                dataset: {name: 'my ds name'},
                initialFolder: {
                    path: 'folder1',
                    name: 'my folder name'
                }
            });
        };
    }));

    describe('clone', () => {
        beforeEach(() => {
            ctrl = createController();
            ctrl.destinationFolder = {name: 'my folder name', path: '/parent/child'};
            ctrl.copyMoveForm = {};
            ctrl.copyMoveForm.$commitViewValue = jasmine.createSpy('$commitViewValue');
        });

        it('should call clone service', inject(($q) => {
            //given
            ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.when(true));
            expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
            expect(ctrl.onCopy).not.toHaveBeenCalled();

            //when
            ctrl.clone();

            //then
            expect(ctrl.copyMoveForm.$commitViewValue).toHaveBeenCalled();
            expect(ctrl.onCopy).toHaveBeenCalledWith({
                dataset: ctrl.dataset,
                destination: ctrl.destinationFolder,
                name: ctrl.newDsName
            });
        }));

        it('should manage cloning flag', inject(($q) => {
            //given
            ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.when(true));
            expect(ctrl.isCloningDs).toBeFalsy();

            //when
            ctrl.clone();
            expect(ctrl.isCloningDs).toBeTruthy();
            scope.$digest();

            //then
            expect(ctrl.isCloningDs).toBeFalsy();
        }));

        it('should reset flag and focus on input when clone fails', inject(($q) => {
            //given
            ctrl.onCopy = jasmine.createSpy('onCopy').and.returnValue($q.reject());
            ctrl._focusOnNameInput = jasmine.createSpy('_focusOnNameInput');

            //when
            ctrl.clone();
            expect(ctrl.isCloningDs).toBeTruthy();
            scope.$digest();

            //then
            expect(ctrl.isCloningDs).toBeFalsy();
            expect(ctrl._focusOnNameInput).toHaveBeenCalled();
        }));
    });

    describe('move', () => {
        beforeEach(() => {
            ctrl = createController();
            ctrl.destinationFolder = {name: 'my folder name', path: '/parent/child'};
            ctrl.copyMoveForm = {};
            ctrl.copyMoveForm.$commitViewValue = jasmine.createSpy('$commitViewValue');
        });

        it('should call move service', inject(($q) => {
            //given
            ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.when(true));
            expect(ctrl.copyMoveForm.$commitViewValue).not.toHaveBeenCalled();
            expect(ctrl.onMove).not.toHaveBeenCalled();

            //when
            ctrl.move();

            //then
            expect(ctrl.copyMoveForm.$commitViewValue).toHaveBeenCalled();
            expect(ctrl.onMove).toHaveBeenCalledWith({
                dataset: ctrl.dataset,
                destination: ctrl.destinationFolder,
                name: ctrl.newDsName
            });
        }));

        it('should manage moving flag', inject(($q) => {
            //given
            ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.when(true));
            expect(ctrl.isMovingDs).toBeFalsy();

            //when
            ctrl.move();
            expect(ctrl.isMovingDs).toBeTruthy();
            scope.$digest();

            //then
            expect(ctrl.isMovingDs).toBeFalsy();
        }));

        it('should reset flag and focus on input when clone fails', inject(($q) => {
            //given
            ctrl.onMove = jasmine.createSpy('onMove').and.returnValue($q.reject());
            ctrl._focusOnNameInput = jasmine.createSpy('_focusOnNameInput');

            //when
            ctrl.move();
            expect(ctrl.isMovingDs).toBeTruthy();
            scope.$digest();

            //then
            expect(ctrl.isMovingDs).toBeFalsy();
            expect(ctrl._focusOnNameInput).toHaveBeenCalled();
        }));
    });
});
