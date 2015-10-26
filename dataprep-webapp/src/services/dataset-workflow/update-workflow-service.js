(function () {
	'use strict';

	/**
	 * @ngdoc service
	 * @name data-prep.services.datasetWorkflowService:UpdateWorkflowService
	 * @description UpdateWorkflowService service. This service exposes functions to update a given dataset
	 * @requires data-prep.services.dataset.service:DatasetService
	 * @requires data-prep.services.utils.service:MessageService
	 * @requires data-prep.services.state.service:StateService
	 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
	 */
	function UpdateWorkflowService(StateService, MessageService, DatasetService, UploadWorkflowService) {

		/**
		 * @ngdoc method
		 * @name updateDataset
		 * @methodOf data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
		 * @param {object} file - the file to upload
		 * @param {object} existingDataset - the existing dataset
		 * @description [PRIVATE] Update existing dataset
		 */
		this.updateDataset = function updateDataset (file, existingDataset) {
			var dataset = DatasetService.createDatasetInfo(file, existingDataset.name, existingDataset.id);
			StateService.startUploadingDataset(dataset);

			DatasetService.update(dataset)
				.progress(function (event) {
					dataset.progress = parseInt(100.0 * event.loaded / event.total);
				})
				.then(function () {
					MessageService.success('DATASET_UPDATE_SUCCESS_TITLE', 'DATASET_UPDATE_SUCCESS', {dataset: dataset.name});

					//Force the update currentMetadata of the dataset
					StateService.resetPlayground();
					DatasetService.getDatasetById(dataset.id).then(UploadWorkflowService.openDataset);

				})
				.catch(function () {
					dataset.error = true;
				})
				.finally(function () {
					StateService.finishUploadingDataset(dataset);
				});
		};

	}

	angular.module('data-prep.services.datasetWorkflowService')
		.service('UpdateWorkflowService', UpdateWorkflowService);
})();