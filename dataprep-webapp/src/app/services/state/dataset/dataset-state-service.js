/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

export function DatasetStateService() {
	return {
		startUploadingDataset,
		finishUploadingDataset,
	};

    // --------------------------------------------------------------------------------------------
    // ------------------------------------------UPLOADING DATASETS--------------------------------
    // --------------------------------------------------------------------------------------------
	function startUploadingDataset(dataset) {
		datasetState.uploadingDataset = dataset;
	}

	function finishUploadingDataset() {
		datasetState.uploadingDataset = null;
	}
}
