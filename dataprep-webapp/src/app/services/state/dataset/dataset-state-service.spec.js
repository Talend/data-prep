/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('dataset state service', function () {
	var dataset = { progress: 66 };
	let stateMock;

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.preferredLanguage('en');
	}));
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

	it('should remove the uploading dataset', inject((DatasetStateService, datasetState) => {
		//given
		DatasetStateService.startUploadingDataset(dataset);

		//when
		DatasetStateService.finishUploadingDataset(dataset);

		//then
		expect(datasetState.uploadingDataset).toBe(null);
	}));
});
