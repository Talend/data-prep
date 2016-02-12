/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const datasetState = {
    uploadingDatasets: []
};

export function DatasetStateService() {

    return {
        //uploading datasets
        startUploadingDataset: startUploadingDataset,
        finishUploadingDataset: finishUploadingDataset
    };

    //--------------------------------------------------------------------------------------------------------------
    //----------------------------------------------UPLOADING DATASETS----------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    function startUploadingDataset(dataset) {
        datasetState.uploadingDatasets.push(dataset);
    }

    function finishUploadingDataset(dataset) {
        datasetState.uploadingDatasets.splice(datasetState.uploadingDatasets.indexOf(dataset), 1);
    }
}