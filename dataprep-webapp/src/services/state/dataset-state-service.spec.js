describe('dataset state service', function(){
	'use strict';

	var dataset = {};

	beforeEach(module('data-prep.services.state'));
	beforeEach(module('data-prep.services.playground'));

	it('should add 2 currently added datasets to be shown in the progress bar', inject(function (DatasetStateService, datasetState) {
		//given
		expect(datasetState.uploadingDatasets.length).toBe(0);

		//when
		DatasetStateService.startUploadingDataset(dataset);
		DatasetStateService.startUploadingDataset(dataset);

		//then
		expect(datasetState.uploadingDatasets.length).toBe(2);
	}));

	it('should remove 1 currently being added dataset among the 2 existing', inject(function (DatasetStateService, datasetState) {
		//given
		expect(datasetState.uploadingDatasets.length).toBe(2);

		//when
		DatasetStateService.finishUploadingDataset(dataset);

		//then
		expect(datasetState.uploadingDatasets.length).toBe(1);
	}));
});