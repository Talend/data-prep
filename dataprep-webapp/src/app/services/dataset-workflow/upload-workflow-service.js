/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.datasetWorkflowService:UploadWorkflowService
 * @description UploadWorkflowService service. This service exposes functions to open the different types of dataset
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetSheetPreviewService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.utils.service:MessageService
 */
export default function UploadWorkflowService($state, StateService, DatasetSheetPreviewService, DatasetService, MessageService) {
    'ngInject';

    var self = this;

    /**
     * @ngdoc method
     * @name openDraft
     * @methodOf data-prep.services.uploadWorkflowService:UploadWorkflowService
     * @description Draft management
     * <ul>
     *      <li>File type is not defined : display error, refresh dataset list</li>
     *      <li>File type is excel : redirect to schema selection</li>
     *      <li>File type defined but unknown : display error</li>
     * </ul>
     * @param {object} dataset The dataset draft to open
     */
    this.openDraft = function openDraft(dataset) {
        if (dataset.type === 'application/vnd.ms-excel') {
            DatasetSheetPreviewService.loadPreview(dataset)
                .then(DatasetSheetPreviewService.display);
        }
        else if (dataset.type) {
            MessageService.error('PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE', 'PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE');
        }
        else {
            DatasetService.refreshDatasets();
            MessageService.error('FILE_FORMAT_ANALYSIS_NOT_READY_TITLE', 'FILE_FORMAT_ANALYSIS_NOT_READY_CONTENT');
        }
    };

    /**
     * @ngdoc method
     * @name openDataset
     * @methodOf data-prep.services.uploadWorkflowService:UploadWorkflowService
     * @description Try to open a dataset. If it is a draft, we open the draft import wizard instead.
     * @param {object} dataset The dataset to open
     */
    this.openDataset = function openDataset(dataset) {
        if (dataset.draft) {
            self.openDraft(dataset);
        }
        else {
            StateService.setPreviousRoute('nav.index.datasets');
            $state.go('playground.dataset', {datasetid: dataset.id});
        }
    };
}