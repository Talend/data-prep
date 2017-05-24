/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('dataset state service', function () {
	var dataset = { progress: 66 };
	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.state'));
	beforeEach(inject(datasetState => {
		datasetState.uploadingDataset = null;
		datasetState.uploadSteps = [];
	}));

	it('should add an uploading dataset', inject((DatasetStateService, datasetState) => {
		//given
		expect(datasetState.uploadingDataset).toBe(null);

		//when
		DatasetStateService.startUploadingDataset(dataset);

		//then
		expect(datasetState.uploadingDataset).toBe(dataset);
	}));

	it('should build the proper steps when uploading', inject((DatasetStateService, datasetState) => {
		//given
		DatasetStateService.startUploadingDataset(dataset);

		//then
		expect(datasetState.uploadSteps[0].type).toBe('PROGRESSION');
		expect(datasetState.uploadSteps[0].state).toBe('IN_PROGRESS');
		expect(datasetState.uploadSteps[0].label).toBe('UPLOADING_FILE');
		expect(datasetState.uploadSteps[0].getValue()).toBe(66);

		expect(datasetState.uploadSteps[1].type).toBe('INFINITE');
		expect(datasetState.uploadSteps[1].state).toBe('FUTURE');
		expect(datasetState.uploadSteps[1].label).toBe('PROFILING_DATA');
	}));

	it('should build the proper steps when profiling', inject((DatasetStateService, datasetState) => {
		//given
		DatasetStateService.startUploadingDataset(dataset);
		DatasetStateService.startProfilingDataset();

		//then
		expect(datasetState.uploadSteps[0].type).toBe('PROGRESSION');
		expect(datasetState.uploadSteps[0].state).toBe('COMPLETE');
		expect(datasetState.uploadSteps[0].label).toBe('UPLOADING_FILE');

		expect(datasetState.uploadSteps[1].type).toBe('INFINITE');
		expect(datasetState.uploadSteps[1].state).toBe('IN_PROGRESS');
		expect(datasetState.uploadSteps[1].label).toBe('PROFILING_DATA');
	}));

	it('should remove the uploading dataset', inject((DatasetStateService, datasetState) => {
		//given
		DatasetStateService.startUploadingDataset(dataset);

		//when
		DatasetStateService.finishUploadingDataset(dataset);

		//then
		expect(datasetState.uploadingDataset).toBe(null);
	}));
});
