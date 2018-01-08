/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import { PLAYGROUND_DATASET_ROUTE } from './../../index-route';
/**
 * @ngdoc service
 * @name data-prep.services.datasetWorkflowService:UploadWorkflowService
 * @description UploadWorkflowService service. This service exposes functions to open the different types of dataset
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetSheetPreviewService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.utils.service:MessageService
 */
export default function UploadWorkflowService($state, $window, StateService, DatasetSheetPreviewService, DatasetService, MessageService) {
	'ngInject';

	const self = this;

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
     * @param {boolean} addPreparation The dataset draft is used to add a preparation
     * @param {string} preparationName The preparation name
     */
	this.openDraft = function openDraft(dataset, addPreparation, preparationName) {
		if (dataset.type === 'application/vnd.ms-excel') {
			DatasetSheetPreviewService.loadPreview(dataset, addPreparation, preparationName)
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
	this.openDataset = function openDataset(dataset, event) {
		if (dataset.draft) {
			self.openDraft(dataset, false, '');
		}
		else if (event && ((event.button === 0 && (event.ctrlKey || event.metaKey)) || event.button === 1)) {
			$window.open($state.href(PLAYGROUND_DATASET_ROUTE, { datasetid: dataset.id }), '_blank');
		}
		else {
			$state.go(PLAYGROUND_DATASET_ROUTE, { datasetid: dataset.id });
		}
	};
}
