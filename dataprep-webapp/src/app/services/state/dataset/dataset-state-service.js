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
	uploadingSteps: [],
};

export function DatasetStateService() {
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
		nextStep();
	}

	function finishUploadingDataset() {
		datasetState.uploadingDataset = null;
		nextStep();
	}

	function initSteps(dataset) {
		datasetState.uploadingSteps = [
			{
				type: 'PROGRESSION',
				state: 'IN_PROGRESS',
				label: 'UPLOADING_FILE',
				getValue: () => dataset.progress,
			},
			{
				type: 'INFINITE',
				state: 'FUTURE',
				label: 'PROFILING_DATA',
			},
		];
	}

	function nextStep() {
		const index = datasetState.uploadingSteps.findIndex(step => step.state === 'IN_PROGRESS');
		if (datasetState.uploadingSteps[index + 1]) {
			datasetState.uploadingSteps[index].state = 'COMPLETE';
			datasetState.uploadingSteps[index + 1].state = 'IN_PROGRESS';
		}
		else {
			datasetState.uploadingSteps = [];
		}
	}
}
