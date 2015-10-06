describe('Dataset list controller', function () {
    'use strict';

    var createController, scope;
    var datasets = [
        {id: 'ec4834d9bc2af8', name: 'Customers (50 lines)'},
        {id: 'ab45f893d8e923', name: 'Us states'},
        {id: 'cf98d83dcb9437', name: 'Customers (1K lines)'}
    ];
    var refreshedDatasets = [
        {id: 'ec4834d9bc2af8', name: 'Customers (50 lines)'},
        {id: 'ab45f893d8e923', name: 'Us states'}
    ];

    beforeEach(module('data-prep.dataset-list'));
    beforeEach(module('data-prep.services.onboarding'));
    beforeEach(module('data-prep.datagrid'));

    beforeEach(inject(function ($rootScope, $controller, $q, $state, DatasetService, PlaygroundService, MessageService, DatasetListSortService, StateService) {
        var datasetsValues = [datasets, refreshedDatasets];
        scope = $rootScope.$new();

        createController = function () {
            return $controller('DatasetListCtrl', {
                $scope: scope
            });
        };

        spyOn(DatasetService, 'processCertification').and.returnValue($q.when(true));
        spyOn(DatasetService, 'getDatasets').and.callFake(function () {
            return $q.when(datasetsValues.shift());
        });

        spyOn(DatasetListSortService, 'setSort').and.returnValue();
        spyOn(DatasetListSortService, 'setOrder').and.returnValue();

        spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when(true));
        spyOn(StateService, 'showPlayground').and.returnValue();
        spyOn(MessageService, 'error').and.returnValue();
        spyOn($state, 'go').and.returnValue();
    }));

    afterEach(inject(function($stateParams) {
        $stateParams.datasetid = null;
    }));

    it('should get dataset on creation', inject(function (DatasetService) {
        //when
        createController();
        scope.$digest();

        //then
        expect(DatasetService.getDatasets).toHaveBeenCalled();
    }));

    describe('dataset in query params load', function() {
        it('should init playground with the provided datasetId from url', inject(function ($stateParams, PlaygroundService, StateService) {
            //given
            $stateParams.datasetid = 'ab45f893d8e923';

            //when
            createController();
            scope.$digest();

            //then
            expect(PlaygroundService.initPlayground).toHaveBeenCalledWith(datasets[1]);
            expect(StateService.showPlayground).toHaveBeenCalled();
        }));

        it('should show error message when dataset id is not in users dataset', inject(function ($stateParams, PlaygroundService, MessageService, StateService) {
            //given
            $stateParams.datasetid = 'azerty';

            //when
            createController();
            scope.$digest();

            //then
            expect(PlaygroundService.initPlayground).not.toHaveBeenCalled();
            expect(StateService.showPlayground).not.toHaveBeenCalled();
            expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'dataset'});
        }));
    });

    describe('sort parameters', function() {

        describe('with dataset refresh success', function() {
            beforeEach(inject(function ($q, DatasetService) {
                spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when(true));
            }));

            it('should refresh dataset when sort is changed', inject(function ($q, DatasetService) {
                //given
                var ctrl = createController();
                ctrl.sortSelected = {id: 'date', name: 'DATE_SORT'};
                var newSort = {id: 'name', name: 'NAME_SORT'};

                //when
                ctrl.updateSortBy(newSort);

                //then
                expect(DatasetService.refreshDatasets).toHaveBeenCalledWith();
            }));

            it('should refresh dataset when order is changed', inject(function ($q, DatasetService) {
                //given
                var ctrl = createController();
                ctrl.selectedOrder = {id: 'desc', name: 'DESC_ORDER'};
                var newSortOrder = {id: 'asc', name: 'ASC_ORDER'};

                //when
                ctrl.updateSortOrder(newSortOrder);

                //then
                expect(DatasetService.refreshDatasets).toHaveBeenCalledWith();
            }));

            it('should not refresh dataset when requested sort is already the selected one', inject(function (DatasetService) {
                //given
                var ctrl = createController();
                var newSort = {id: 'name', name: 'NAME_SORT'};

                //when
                ctrl.updateSortBy(newSort);
                ctrl.updateSortBy(newSort);

                //then
                expect(DatasetService.refreshDatasets.calls.count()).toBe(1);
            }));

            it('should not refresh dataset when requested order is already the selected one', inject(function (DatasetService) {
                //given
                var ctrl = createController();
                var newSortOrder = {id: 'desc', name: 'ASC_ORDER'};

                //when
                ctrl.updateSortOrder(newSortOrder);
                ctrl.updateSortOrder(newSortOrder);

                //then
                expect(DatasetService.refreshDatasets.calls.count()).toBe(1);
            }));

            it('should update sort parameter', inject(function (DatasetService, DatasetListSortService) {
                //given
                var ctrl = createController();
                var newSort = {id: 'name', name: 'NAME'};

                //when
                ctrl.updateSortBy(newSort);

                //then
                expect(DatasetListSortService.setSort).toHaveBeenCalledWith('name');
            }));

            it('should update order parameter', inject(function (DatasetService, DatasetListSortService) {
                //given
                var ctrl = createController();
                var newSortOrder = {id: 'asc', name: 'ASC_ORDER'};

                //when
                ctrl.updateSortOrder(newSortOrder);

                //then
                expect(DatasetListSortService.setOrder).toHaveBeenCalledWith('asc');
            }));

        });

        describe('with dataset refresh failure', function() {
            beforeEach(inject(function ($q, DatasetService) {
                spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.reject(false));
            }));

            it('should set the old sort parameter', function () {
                //given
                var previousSelectedSort = {id: 'date', name: 'DATE'};
                var newSort =  {id: 'name', name: 'NAME_SORT'};

                var ctrl = createController();
                ctrl.sortSelected = previousSelectedSort;

                //when
                ctrl.updateSortBy(newSort);
                expect(ctrl.sortSelected).toBe(newSort);
                scope.$digest();

                //then
                expect(ctrl.sortSelected).toBe(previousSelectedSort);
            });

            it('should set the old order parameter', function () {
                //given
                var previousSelectedOrder = {id: 'desc', name: 'DESC'};
                var newSortOrder =  {id: 'asc', name: 'ASC_ORDER'};

                var ctrl = createController();
                ctrl.sortOrderSelected = previousSelectedOrder;

                //when
                ctrl.updateSortOrder(newSortOrder);
                expect(ctrl.sortOrderSelected).toBe(newSortOrder);
                scope.$digest();

                //then
                expect(ctrl.sortOrderSelected).toBe(previousSelectedOrder);
            });
        });
    });

    describe('already created', function () {
        var ctrl;

        beforeEach(inject(function ($rootScope, $q, MessageService, DatasetService, DatasetSheetPreviewService, TalendConfirmService) {
            ctrl = createController();
            scope.$digest();

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when(true));
            spyOn(DatasetService, 'delete').and.returnValue($q.when(true));
            spyOn(MessageService, 'success').and.returnValue();
            spyOn(DatasetSheetPreviewService, 'loadPreview').and.returnValue($q.when(true));
            spyOn(DatasetSheetPreviewService, 'display').and.returnValue($q.when(true));
            spyOn(DatasetService, 'toggleFavorite').and.returnValue($q.when(true));
            spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when(true));
        }));

        it('should delete dataset and show toast', inject(function ($q, MessageService, DatasetService, TalendConfirmService) {
            //given
            var dataset = datasets[0];


            //when
            ctrl.delete(dataset);
            scope.$digest();

            //then
            expect(TalendConfirmService.confirm).toHaveBeenCalledWith({disableEnter: true}, [ 'DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM' ], {type: 'dataset', name: 'Customers (50 lines)' });
            expect(DatasetService.delete).toHaveBeenCalledWith(dataset);
            expect(MessageService.success).toHaveBeenCalledWith('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {type: 'dataset', name: 'Customers (50 lines)'});
        }));

        it('should bind datasets getter to datasetListService.datasets', inject(function (DatasetService, DatasetListService) {
            //given
            DatasetListService.datasets = refreshedDatasets;

            //then
            expect(ctrl.datasets).toBe(refreshedDatasets);
        }));
    });

    describe('cloning dataset',function(){

        var ctrl;

        beforeEach(inject(function ($rootScope, $q, DatasetService, MessageService) {
            ctrl = createController();
            scope.$digest();

            spyOn(DatasetService, 'clone').and.returnValue($q.when(true));
            spyOn(MessageService, 'success').and.returnValue();
        }));

        it('clone must call clone service', inject(function ($q, DatasetService, MessageService) {
            //given
            var dataset = datasets[0];


            //when
            ctrl.clone(dataset);
            scope.$digest();

            //then
            expect(DatasetService.clone).toHaveBeenCalledWith(dataset);
            expect(MessageService.success).toHaveBeenCalledWith('CLONE_SUCCESS_TITLE', 'CLONE_SUCCESS');
        }));

    });

    describe('renaming dataset',function(){

        var ctrl;

        beforeEach(inject(function ($rootScope, $q, DatasetService, MessageService) {
            ctrl = createController();
            scope.$digest();

            spyOn(DatasetService, 'update').and.returnValue($q.when(true));
            spyOn(MessageService, 'success').and.returnValue();
        }));

        it('clone must call clone service', inject(function ($q, DatasetService, MessageService) {
            //given
            var dataset = datasets[0];


            //when
            ctrl.rename(dataset);
            scope.$digest();

            //then
            expect(ctrl.showChangeName).toBe(true);
            expect(ctrl.currentDataset).toBe(dataset);

            //when
            ctrl.doRename(dataset);
            scope.$digest();

            //then
            expect(ctrl.showChangeName).toBe(false);
            expect(DatasetService.update).toHaveBeenCalledWith(dataset);
            expect(MessageService.success).toHaveBeenCalledWith('RENAME_SUCCESS_TITLE', 'RENAME_SUCCESS');
        }));

    });
});
