/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset xls preview controller', () => {
    'use strict';

    let createController;
    let scope;
    let gridElement;
    let stateMock;
    const content = { column: [], records: [] };

    beforeEach(angular.mock.module('data-prep.dataset-xls-preview', ($provide) => {
        stateMock = {
            inventory: {
                currentFolder: { path: 'HOME' },
                folder: {
                    metadata: {
                        id: 'HOME',
                    },
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $controller, $q, $timeout, $state,
        DatasetSheetPreviewService, DatasetService, PlaygroundService, StateService) => {
        scope = $rootScope.$new();

        createController = () => {
            const ctrl = $controller('DatasetXlsPreviewCtrl', {
                $scope: scope,
            });
            $timeout.flush();
            return ctrl;
        };

        //insert grid entry point
        gridElement = angular.element('<div id="datasetSheetPreviewGrid"></div>');
        angular.element('body').append(gridElement);

        //spies
        spyOn(DatasetSheetPreviewService, 'loadSheet').and.returnValue();
        spyOn(DatasetSheetPreviewService, 'setDatasetSheet').and.returnValue($q.when(true));
        spyOn(DatasetService, 'getContent').and.returnValue($q.when(content));
        spyOn(StateService, 'setPreviousRoute').and.returnValue();
        spyOn($state, 'go').and.returnValue();
    }));

    afterEach(() => {
        gridElement.remove();
    });

    describe('bindings', () => {
        it('should bind modal visibility flag getter to DatasetSheetPreviewService.showModal', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            expect(ctrl.visible).toBeFalsy();

            //when
            DatasetSheetPreviewService.showModal = true;

            //then
            expect(ctrl.visible).toBe(true);
        }));

        it('should bind modal visibility flag setter to DatasetSheetPreviewService.showModal', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            DatasetSheetPreviewService.showModal = true;

            //when
            ctrl.visible = false;

            //then
            expect(DatasetSheetPreviewService.showModal).toBe(false);
        }));

        it('should bind metadata getter to DatasetSheetPreviewService.currentMetadata', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            const metadata = { id: 'toto' };
            expect(ctrl.metadata).toBeFalsy();

            //when
            DatasetSheetPreviewService.currentMetadata = metadata;

            //then
            expect(ctrl.metadata).toBe(metadata);
        }));

        it('should bind selectedSheetName getter to DatasetSheetPreviewService.selectedSheetName', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            expect(ctrl.selectedSheetName).toBeFalsy();

            //when
            DatasetSheetPreviewService.selectedSheetName = 'my sheet';

            //then
            expect(ctrl.selectedSheetName).toBe('my sheet');
        }));

        it('should bind selectedSheetName setter to DatasetSheetPreviewService.selectedSheetName', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            DatasetSheetPreviewService.selectedSheetName = 'my sheet';

            //when
            ctrl.selectedSheetName = 'my new sheet';

            //then
            expect(DatasetSheetPreviewService.selectedSheetName).toBe('my new sheet');
        }));
    });

    describe('select sheet', () => {
        it('should load the selected sheet preview', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            ctrl.selectedSheetName = 'my sheet';

            //when
            ctrl.selectSheet();

            //then
            expect(DatasetSheetPreviewService.loadSheet).toHaveBeenCalledWith('my sheet');
        }));
    });

    describe('validation', () => {
        it('should set the selected sheet to the dataset', inject((DatasetSheetPreviewService) => {
            //given
            const ctrl = createController();
            ctrl.selectedSheetName = 'my sheet';

            //when
            ctrl.setDatasetSheet();

            //then
            expect(DatasetSheetPreviewService.setDatasetSheet).toHaveBeenCalledWith('my sheet');
        }));

        it('should hide modal', inject((DatasetSheetPreviewService) => {
            //given
            DatasetSheetPreviewService.currentMetadata = { id: 'toto' };
            const ctrl = createController();
            ctrl.visible = true;

            //when
            ctrl.setDatasetSheet();
            scope.$digest();

            //then
            expect(ctrl.visible).toBe(false);
        }));

        it('should open dataset', inject(($state, DatasetSheetPreviewService, StateService) => {
            //given
            const ctrl = createController();
            ctrl.selectedSheetName = 'my sheet';

            DatasetSheetPreviewService.currentMetadata = { id: '13aa256cf813a25d158' };

            //when
            ctrl.setDatasetSheet();
            scope.$digest();

            //then
            expect(StateService.setPreviousRoute).toHaveBeenCalledWith('home.datasets');
            expect($state.go).toHaveBeenCalledWith('playground.dataset', { datasetid: '13aa256cf813a25d158' });
        }));

        it('should open preparation', inject(($q, $state, DatasetSheetPreviewService, StateService, PreparationService) => {
            //given
            spyOn(PreparationService, 'create').and.returnValue($q.when('aaaa256cf813a25d158'));

            const ctrl = createController();
            ctrl.selectedSheetName = 'my sheet';

            DatasetSheetPreviewService.addPreparation = true;
            DatasetSheetPreviewService.preparationName = 'sheet Preparation';
            DatasetSheetPreviewService.currentMetadata = { id: '13aa256cf813a25d158', name: 'sheet' };

            //when
            ctrl.setDatasetSheet();
            scope.$digest();

            //then
            expect(PreparationService.create).toHaveBeenCalledWith('13aa256cf813a25d158', 'sheet Preparation', 'HOME');
            expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: 'aaaa256cf813a25d158' });
        }));
    });
});
