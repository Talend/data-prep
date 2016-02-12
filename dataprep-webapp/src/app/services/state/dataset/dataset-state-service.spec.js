/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('dataset state service', function(){
	'use strict';

	var dataset = {};

	beforeEach(angular.mock.module('data-prep.services.state'));
	beforeEach(angular.mock.module('data-prep.services.playground'));

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