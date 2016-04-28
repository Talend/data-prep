/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

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
export default function DatasetXlsPreviewCtrl($timeout, $state, state, DatasetSheetPreviewService, DatasetService, PlaygroundService, StateService) {
    'ngInject';

    var vm = this;
    vm.datasetSheetPreviewService = DatasetSheetPreviewService;

    /**
     * @ngdoc method
     * @name initGrid
     * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description [PRIVATE] Initialize the grid and set it in the service. The service will provide the data in it.
     * This is called at controller creation
     */
    var initGrid = function () {
        var options = {
            enableColumnReorder: false,
            editable: false,
            enableAddRow: false,
            enableCellNavigation: true,
            enableTextSelectionOnCells: false
        };

        DatasetSheetPreviewService.grid = new Slick.Grid('#datasetSheetPreviewGrid', [], [], options);
    };

    /**
     * @ngdoc method
     * @name selectSheet
     * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description Load a sheet preview in the grid
     */
    vm.selectSheet = function () {
        return DatasetSheetPreviewService.loadSheet(vm.selectedSheetName);
    };

    /**
     * @ngdoc method
     * @name setDatasetSheet
     * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
     * @description Set the sheet in the dataset, update the dataset list, and hide the modal
     */
    vm.setDatasetSheet = function () {
        DatasetSheetPreviewService.setDatasetSheet(vm.selectedSheetName)
            .then(() => {
                vm.visible = false;
            })
            .then(() => {
                StateService.setPreviousRoute('nav.index.datasets');
                $state.go('playground.dataset', {datasetid: vm.metadata.id});
            });
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
    'visible', {
        enumerable: true,
        configurable: false,
        get: function () {
            return this.datasetSheetPreviewService.showModal;
        },
        set: function (value) {
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