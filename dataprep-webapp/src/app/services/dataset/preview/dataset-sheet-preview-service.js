/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.dataset.service:DatasetSheetPreviewService
 * @description Dataset sheet preview service
 */
export default function DatasetSheetPreviewService(DatasetService, $timeout) {
	'ngInject';

	const self = this;

    /**
     * @ngdoc property
     * @name currentMetadata
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The current metadata to preview
     * @type {Object}
     */
	self.currentMetadata = null;

    /**
     * @ngdoc property
     * @name selectedSheetName
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The loaded sheet name
     * @type {Object}
     */
	self.selectedSheetName = null;

    /**
     * @ngdoc property
     * @name grid
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The Slickgrid that will contains the data
     * @type {Object}
     */
	self.grid = null;

    /**
     * @ngdoc property
     * @name showModal
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The flag that display preview modal
     * @type {boolean}
     */
	self.showModal = false;

    /**
     * @ngdoc property
     * @name addPreparation
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The flag that indicates if the dataset draft is used to add a preparation
     * @type {boolean}
     */
	self.addPreparation = false;

    /**
     * @ngdoc property
     * @name preparationName
     * @propertyOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description The preparation to be created
     */
	self.preparationName = '';

    /**
     * @ngdoc method
     * @name display
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description Set the display flag to true
     */
	this.display = function () {
		self.showModal = true;
	};

    /**
     * @ngdoc method
     * @name resetGrid
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @description Remove all grid content
     */
	const resetGrid = function () {
		self.grid.setColumns([]);
		self.grid.setData([]);
	};

    /**
     * @ngdoc method
     * @name setGridData
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @param {object} data The new data containing columns and records
     * @description Set new grid content
     */
	const setGridData = function (data) {
		const columns = _.map(data.metadata.columns, function (col) {
			return {
				id: col.id,
				name: '<div class="grid-header">' +
                '<div class="grid-header-title dropdown-button ng-binding">' + col.name + '</div>' +
                '</div>',
				field: col.id,
				minWidth: 100,
			};
		});

		self.grid.setColumns(columns);
		self.grid.setData(data.records);
		self.grid.autosizeColumns();
		self.grid.render();
	};

    /**
     * @ngdoc method
     * @name loadPreview
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @param {object} dataset The dataset metadata to load
     * @param {boolean} addPreparation The dataset draft is used to add a preparation
     * @param {string} preparationName The preparation name
     * @description Set the metadata containing the dataset sheets, and get the preview of the default (last) selected sheet
     */
	this.loadPreview = function (dataset, addPreparation, preparationName) {
		self.currentMetadata = null;
		self.selectedSheetName = null;
		self.addPreparation = addPreparation;
		self.preparationName = preparationName;
		resetGrid();
		return DatasetService.getSheetPreview(dataset)
            .then(function (response) {
	self.currentMetadata = response.metadata;
	self.selectedSheetName = response.metadata.sheetName;
	$timeout(() => {
		setGridData(response);
	});
});
	};

    /**
     * @ngdoc method
     * @name loadSheet
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @param {string} sheetName The sheet name to load on the current dataset
     * @description Get and load the sheet preview
     */
	this.loadSheet = function (sheetName) {
		resetGrid();
		return DatasetService.getSheetPreview(self.currentMetadata, sheetName)
            .then(function (response) {
	setGridData(response);
});
	};

    /**
     * @ngdoc method
     * @name setDatasetSheet
     * @methodOf data-prep.services.dataset.service:DatasetSheetPreviewService
     * @param {string} sheetName The sheet name to set in the dataset
     * @description Select a sheet in the current dataset. We update the metadata
     */
	this.setDatasetSheet = function (sheetName) {
		return DatasetService.setDatasetSheet(self.currentMetadata, sheetName);
	};
}
