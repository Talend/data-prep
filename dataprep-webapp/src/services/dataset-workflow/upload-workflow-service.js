(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.datasetWorkflowService:UploadWorkflowService
     * @description UploadWorkflowService service. This service exposes functions to open the different types of dataset
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires data-prep.services.utils.service:MessageService
     * @requires data-prep.services.dataset.service:DatasetSheetPreviewService
     */
    function UploadWorkflowService($state, DatasetSheetPreviewService, MessageService, DatasetService) {
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
                $state.go('nav.home.datasets', {datasetid: dataset.id});
            }
        };
    }

    angular.module('data-prep.services.datasetWorkflowService')
        .service('UploadWorkflowService', UploadWorkflowService);
})();