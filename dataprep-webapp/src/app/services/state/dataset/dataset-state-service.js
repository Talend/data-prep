/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const datasetState = {
	uploadingDataset: null,
	uploadSteps: [],
};

export function DatasetStateService($translate, ProgressService, PROGRESSION_DEFAULTS) {
	'ngInject';

	return {
		startUploadingDataset,
		startProfilingDataset,
		finishUploadingDataset,
	};

    // --------------------------------------------------------------------------------------------
    // ------------------------------------------UPLOADING DATASETS--------------------------------
    // --------------------------------------------------------------------------------------------
	function startUploadingDataset(dataset) {
		datasetState.uploadingDataset = dataset;
		initSteps(dataset);
	}

	function startProfilingDataset() {
		ProgressService.next();
	}

	function finishUploadingDataset() {
		datasetState.uploadingDataset = null;
		ProgressService.next();
	}

	function initSteps(dataset) {
		ProgressService.title = 'ADD_NEW_DATASET';
		ProgressService.steps = PROGRESSION_DEFAULTS.DATASET;
		ProgressService.progressionGetter = () => dataset.progress;
	}
}
