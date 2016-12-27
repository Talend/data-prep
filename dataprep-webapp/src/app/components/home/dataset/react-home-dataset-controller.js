/*  ============================================================================
 Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE
 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France
 ============================================================================*/

export default class HomeDatasetCtrl {

	constructor($document, $translate, state, StateService, UploadWorkflowService, UpdateWorkflowService, DatasetService, TalendConfirmService, ImportService) {
		'ngInject';
		this.$document = $document;
		this.$translate = $translate;
		this.state = state;
		this.StateService = StateService;
		this.UploadWorkflowService = UploadWorkflowService;
		this.UpdateWorkflowService = UpdateWorkflowService;
		this.DatasetService = DatasetService;
		this.TalendConfirmService = TalendConfirmService;
		this.ImportService = ImportService;

		this.updateDatasetFile = null;
		this.importDatasetFile = null;

		/** List of supported import type */
		this.importTypes = this.state.import.importTypes;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);

		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------Import-----------------------------------------
	// --------------------------------------------------------------------------------------------
	onFileChange () {
		this.UpdateWorkflowService.updateDataset(this.updateDatasetFile[0], this.state.inventory.datasetToUpdate);
	};

	/**
	 * @ngdoc method
	 * @name onDatastoreFormChange
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatastoreFormChange(formData, formId, propertyName) {
		const definitionName = formId || this.ImportService.currentInputType.locationType;
		this.ImportService.refreshParameters(definitionName, propertyName, formData)
			.then((response) => {
				this.ImportService.datastoreForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param formId ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, formId) {
		const definitionName = formId || this.ImportService.currentInputType.locationType;
		this.ImportService.testConnection(definitionName, uiSpecs && uiSpecs.formData)
			.then((response) => {
				this.ImportService.dataStoreId = response.data && response.data.dataStoreId;
				if (!this.ImportService.dataStoreId) {
					return null;
				}
				return this.ImportService.getDatasetForm(this.ImportService.dataStoreId);
			})
			.then((datasetFormResponse) => {
				this.ImportService.datasetForm = datasetFormResponse && datasetFormResponse.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormChange
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatasetFormChange(formData, formId, propertyName) {
		this.ImportService.refreshDatasetForm(this.ImportService.dataStoreId, propertyName, formData)
			.then((response) => {
				this.ImportService.datasetForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormSubmit
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs
	 */
	onDatasetFormSubmit(uiSpecs) {
		this.ImportService.createDataset(this.ImportService.dataStoreId, uiSpecs && uiSpecs.formData)
			.then((response) => {
				const dataSetId = response.data && response.data.dataSetId;
				this.DatasetService.getDatasetById(dataSetId).then(this.UploadWorkflowService.openDataset);
			});
	}

	/**
	 * @ngdoc method
	 * @name createDataset
	 * @description Create dataset using import parameters
	 * @param {object} file The file imported from local
	 * @param {string} name The dataset name
	 * @param {object} importType The import parameters
	 */
	createDataset(file, name, importType) {
		const params = this.DatasetService.getLocationParamIteration({}, importType.parameters);
		params.type = importType.locationType;
		params.name = name;

		const dataset = this.DatasetService.createDatasetInfo(file, name);
		this.StateService.startUploadingDataset(dataset);

		return this.DatasetService.create(params, importType.contentType, file)
			.progress((event) => {
				dataset.progress = parseInt((100.0 * event.loaded) / event.total, 10);
			})
			.then((event) => {
				this.DatasetService.getDatasetById(event.data).then(this.UploadWorkflowService.openDataset);
			})
			.catch(() => {
				dataset.error = true;
			})
			.finally(() => {
				this.StateService.finishUploadingDataset(dataset);
			});
	}

	/**
	 * @ngdoc method
	 * @name import
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Import step 1 - It checks if the dataset name is available
	 * If so : the dataset is created
	 * If not : the new name modal is shown
	 */
	import(importType) {
		const file = this.importDatasetFile ? this.importDatasetFile[0] : null;
		const datasetName = file ?
			file.name :
			_.find(importType.parameters, { name: 'name' }).value;

		// remove file extension and ask final name
		const name = datasetName.replace(/\.[^/.]+$/, '');

		return this.DatasetService.checkNameAvailability(name)
			// name available: we create the dataset
			.then(() => {
				this.createDataset(file, name, importType);
			})
			// name is not available, we ask for a new name
			.catch(() => {
				this.datasetName = name;
				this.datasetNameModal = true;
			})
			.finally(() => {
				this.StateService.setShowImportModal(false);
			});
	}

	/**
	 * @ngdoc method
	 * @name uploadDatasetName
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Import step 2 - name entered. It checks if the name is available
	 * If so : the dataset is created
	 * If not : the user has to choose to create a new one or the update the existing one
	 */
	onImportNameValidation() {
		const file = this.importDatasetFile ? this.importDatasetFile[0] : null;
		const importType = this.ImportService.currentInputType;
		const name = this.datasetName;

		return this.DatasetService.checkNameAvailability(name)
			// name still exists
			.then(() => {
				this.createDataset(file, name, importType);
			})
			// name still exists : we ask if user want to update it
			.catch(existingDataset => this.updateOrCreate(file, existingDataset, importType, name));
	}

	/**
	 * @ngdoc method
	 * @name updateOrCreate
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @param {object} file The dataset file
	 * @param {object} existingDataset The dataset to update
	 * @param {object} importType The import configuration
	 * @param {string} name The dataset name
	 * @description Import step 3 - Ask to create or update the existing dataset
	 * Create : get a unique name and create
	 * Update : update the content of the existing dataset
	 */
	updateOrCreate(file, existingDataset, importType, name) {
		return this.TalendConfirmService.confirm(null, ['UPDATE_EXISTING_DATASET'], { dataset: name })
			// user confirm : let's update the dataset
			.then(() => {
				this.UpdateWorkflowService.updateDataset(file, existingDataset);
			})
			// user dismiss : cancel
			// user select no : get unique name and create a new dataset
			.catch((cause) => {
				if (cause === 'dismiss') {
					return;
				}

				return this.DatasetService.getUniqueName(name)
					.then((name) => {
						return this.createDataset(
							file,
							name,
							importType
						);
					});
			});
	}
}
