/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import {
	HOME_DATASETS_ROUTE,
	PLAYGROUND_DATASET_ROUTE,
	PLAYGROUND_PREPARATION_ROUTE,
} from '../../../index-route';

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
export default function DatasetXlsPreviewCtrl($timeout, $state, state,
                                              DatasetSheetPreviewService, DatasetService,
                                              PlaygroundService, StateService, PreparationService) {
	'ngInject';

	const vm = this;
	vm.datasetSheetPreviewService = DatasetSheetPreviewService;

	/**
	 * @ngdoc method
	 * @name initGrid
	 * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
	 * @description [PRIVATE] Initialize the grid and set it in the service.
	 * The service will provide the data in it.
	 * This is called at controller creation
	 */
	vm.initGrid = function () {
		const options = {
			enableColumnReorder: false,
			editable: false,
			enableAddRow: false,
			enableCellNavigation: true,
			enableTextSelectionOnCells: false,
		};

		DatasetSheetPreviewService.grid = new Slick.Grid(
			'#datasetSheetPreviewGrid',
			[],
			[],
			options
		);
		DatasetSheetPreviewService.grid.registerPlugin(new Slick.AutoColumnSize());
	};

	/**
	 * @ngdoc method
	 * @name selectSheet
	 * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
	 * @description Load a sheet preview in the grid
	 */
	vm.selectSheet = function selectSheet() {
		return DatasetSheetPreviewService.loadSheet(vm.selectedSheetName);
	};

	/**
	 * @ngdoc method
	 * @name disableDatasetSheetConfirm
	 * @methodOf data-prep.dataset-xls-preview.controller:DatasetPreviewXlsCtrl
	 * @description Disable dataset Sheet confirm button
	 */
	vm.disableDatasetSheetConfirm = function disableDatasetSheetConfirm() {
		if (DatasetSheetPreviewService.addPreparation && vm.metadata) {
			return _.some(state.inventory.folder.content.preparations, { name: vm.metadata.name });
		}
		else {
			return false;
		}
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
				if (DatasetSheetPreviewService.addPreparation) {
					PreparationService
						.create(
							vm.metadata.id,
							DatasetSheetPreviewService.preparationName,
							state.inventory.folder.metadata.id
						)
						.then((prepid) => {
							$state.go(PLAYGROUND_PREPARATION_ROUTE, { prepid });
						});
				}
				else {
					StateService.setPreviousRoute(HOME_DATASETS_ROUTE);
					$state.go(PLAYGROUND_DATASET_ROUTE, { datasetid: vm.metadata.id });
				}
			});
	};
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
		get() {
			return this.datasetSheetPreviewService.showModal;
		},

		set(value) {
			this.datasetSheetPreviewService.showModal = value;
		},
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
		get() {
			return this.datasetSheetPreviewService.currentMetadata;
		},
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
		get() {
			return this.datasetSheetPreviewService.selectedSheetName;
		},

		set(newValue) {
			this.datasetSheetPreviewService.selectedSheetName = newValue;
		},
	});
