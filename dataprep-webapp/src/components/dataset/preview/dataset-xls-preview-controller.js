(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description Dataset preview grid controller.
     * @requires data-prep.services.dataset.service:DatasetSheetPreviewService
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.folder.service:FolderService
     */
    function DatasetXlsPreviewCtrl(state, $timeout, DatasetSheetPreviewService, DatasetService, PlaygroundService, StateService, FolderService) {
        var vm = this;
        vm.datasetSheetPreviewService = DatasetSheetPreviewService;

        /**
         * @ngdoc method
         * @name initGrid
         * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
         * @description [PRIVATE] Initialize the grid and set it in the service. The service will provide the data in it.
         * This is called at controller creation
         */
        var initGrid = function() {
            var options = {
                enableColumnReorder: false,
                editable: false,
                enableAddRow: false,
                enableCellNavigation: true,
                enableTextSelectionOnCells: false
            };

            var grid = new Slick.Grid( '#datasetSheetPreviewGrid', [], [], options);
            DatasetSheetPreviewService.grid = grid;
        };

        /**
         * @ngdoc method
         * @name selectSheet
         * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
         * @description Load a sheet preview in the grid
         */
        vm.selectSheet = function() {
            return DatasetSheetPreviewService.loadSheet(vm.selectedSheetName);
        };

        /**
         * @ngdoc method
         * @name setDatasetSheet
         * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
         * @description Set the sheet in the dataset, update the dataset list, and hide the modal
         */
        vm.setDatasetSheet = function() {
            DatasetSheetPreviewService.setDatasetSheet(vm.selectedSheetName)
                .then(function(){
                    DatasetService.refreshDatasets();
                    FolderService.getContent(state.folder.currentFolder);
                })
                .then(function(){
                    vm.state = false;
                })
                .then(PlaygroundService.initPlayground.bind(null, vm.metadata))
                .then(StateService.showPlayground);
        };

        $timeout(initGrid);
    }

    /**
     * @ngdoc property
     * @name state
     * @propertyOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description The modal state
     * This list is bound to {@link data-prep.services.dataset.service:DatasetSheetPreviewService DatasetSheetPreviewService}.showModal
     */
    Object.defineProperty(DatasetXlsPreviewCtrl.prototype,
        'state', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datasetSheetPreviewService.showModal;
            },
            set: function(value) {
                this.datasetSheetPreviewService.showModal = value;
            }
        });

    /**
     * @ngdoc property
     * @name metadata
     * @propertyOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description The metadata to preview
     * This list is bound to {@link data-prep.services.dataset.service:DatasetSheetPreviewService DatasetSheetPreviewService}.currentMetadata
     */
    Object.defineProperty(DatasetXlsPreviewCtrl.prototype,
        'metadata', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datasetSheetPreviewService.currentMetadata;
            }
        });

    /**
     * @ngdoc property
     * @name selectedSheetName
     * @propertyOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description The selected sheet name
     * This list is bound to {@link data-prep.services.dataset.service:DatasetSheetPreviewService DatasetSheetPreviewService}.selectedSheetName
     */
    Object.defineProperty(DatasetXlsPreviewCtrl.prototype,
        'selectedSheetName', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datasetSheetPreviewService.selectedSheetName;
            },
            set: function (newValue) {
                this.datasetSheetPreviewService.selectedSheetName = newValue;
            }
        });

    angular.module('data-prep.dataset-xls-preview')
        .controller('DatasetXlsPreviewCtrl', DatasetXlsPreviewCtrl);

})();