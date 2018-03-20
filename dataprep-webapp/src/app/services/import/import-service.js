/*  ============================================================================
 Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE
 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France
 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.import.service:ImportService
 * @description Import service. This service provide the entry point to the backend import REST api.
 * @requires data-prep.services.import.service:ImportService
 */
export default class ImportService {
	constructor($document, $rootScope, state, DatasetService, ImportRestService, StateService, TalendConfirmService, UploadWorkflowService, UpdateWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$rootScope = $rootScope;

		this.state = state;

		this.DatasetService = DatasetService;
		this.ImportRestService = ImportRestService;
		this.StateService = StateService;
		this.TalendConfirmService = TalendConfirmService;
		this.UpdateWorkflowService = UpdateWorkflowService;
		this.UploadWorkflowService = UploadWorkflowService;

		this.currentInputType = null;
		this.updateDatasetFile = null;
		this.importDatasetFile = null;
	}

	manageLoader(method, args) {
		this.$rootScope.$emit('talend.loading.start');
		return method(...args)
			.finally(() => this.$rootScope.$emit('talend.loading.stop'));
	}

	/**
	 * @ngdoc method
	 * @name importParameters
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Fetch the available import parameters
	 * @returns {Promise}  The GET call promise
	 */
	importParameters(locationType) {
		return this.manageLoader(
			this.ImportRestService.importParameters,
			[locationType]
		);
	}

	/**
	 * @ngdoc method
	 * @name resetImport
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Reset import state
	 */
	resetImport() {
		this.StateService.setCurrentImportItem(null);
	}

	/**
	 * @ngdoc method
	 * @name testConnection
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Test connection to a datastore
	 * @returns {Promise} The POST call promise
	 */
	testConnection(definitionName, datastoreFormData) {
		return this.manageLoader(
			this.ImportRestService.testConnection,
			[definitionName, datastoreFormData]
		);
	}

	/**
	 * @ngdoc method
	 * @name getDatasetForm
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Get dataset form
	 * @returns {Promise} The POST call promise
	 */
	getDatasetForm(datastoreFormData) {
		return this.manageLoader(
			this.ImportRestService.getDatasetForm,
			[datastoreFormData]
		);
	}

	/**
	 * @ngdoc method
	 * @name refreshForm
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Refresh the form
	 * @returns {Promise}  The POST call promise
	 */
	refreshForm(propertyName, formData) {
		return this.manageLoader(
			this.ImportRestService.refreshForm,
			[propertyName, formData]
		);
	}

	/**
	 * @ngdoc method
	 * @name refreshForms
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Refresh the forms
	 * @returns {Promise}  The POST call promise
	 */
	refreshForms(propertyName, formsData) {
		return this.manageLoader(
			this.ImportRestService.refreshForms,
			[propertyName, formsData]
		);
	}

	/**
	 * @ngdoc method
	 * @name createDataset
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Create dataset for a datastore
	 * @returns {Promise} The POST call promise
	 */
	createDataset(definitionName, formsData) {
		return this.manageLoader(
			this.ImportRestService.createDataset,
			[definitionName, formsData]
		);
	}

	/**
	 * @ngdoc method
	 * @name getFormsByDatasetId
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Get filled datastore and dataset forms
	 * @returns {Promise} The POST call promise
	 */
	getFormsByDatasetId(datasetId) {
		return this.manageLoader(
			this.ImportRestService.getFormsByDatasetId,
			[datasetId]
		);
	}

	/**
	 * @ngdoc method
	 * @name editDataset
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Edit dataset for a datastore
	 * @returns {Promise} The POST call promise
	 */
	editDataset(datasetId, formsData) {
		return this.manageLoader(
			this.ImportRestService.editDataset,
			[datasetId, formsData]
		);
	}

	/**
	 * @ngdoc method
	 * @name startImport
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Start the import process of a dataset. Route the call to the right import method
	 * (local or remote) depending on the import type user choice.
	 */
	startImport(importType) {
		this.currentInputType = importType;
		if (importType.locationType) {
			this.isTCOMP = importType.locationType.indexOf('tcomp') === 0;
			if (this.isTCOMP) {
				this.StateService.showImport();
			}
			else {
				switch (importType.locationType) {
				case 'local':
					this.$document.find('#importDatasetFile').eq(0).click();
					break;
				default:
					this.StateService.showImport();
					if (this.currentInputType.dynamic) {
						this.importParameters(this.currentInputType.locationType)
							.then((response) => {
								this.currentInputType.parameters = response.data;
							});
					}
				}
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name importDataset
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Create dataset using import parameters
	 * @param {object} file The file imported from local
	 * @param {string} name The dataset name
	 * @param {object} importType The import parameters
	 */
	importDataset(file, name, importType) {
		const params = {
			...this.DatasetService.getLocationParamIteration({}, importType.parameters),
			type: importType.locationType,
			size: file.size,
			name,
		};

		const dataset = this.DatasetService.createDatasetInfo(file, name);
		this.StateService.startUploadingDataset(dataset);
		this.StateService.startProgress(this.state.progress.schemas.dataset, () => dataset.progress);

		return this.DatasetService.create(params, importType.contentType, file)
			.progress((event) => {
				const progress = parseInt((100.0 * event.loaded) / event.total, 10);
				if (dataset.progress !== progress && progress === 100) {
					this.StateService.nextProgress();
				}
				dataset.progress = progress;
			})
			.then((event) => {
				return this.DatasetService.getDatasetById(event.data);
			})
			.then((data) => {
				return this.currentPostImportAction ? this.currentPostImportAction(data) : this.UploadWorkflowService.openDataset(data);
			})
			.catch(() => {
				dataset.error = true;
			})
			.finally(() => {
				this.StateService.resetProgress();
				this.StateService.finishUploadingDataset();
			});
	}

	/**
	 * @ngdoc method
	 * @name import
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Import step 1 - It checks if the dataset name is available
	 * If so : the dataset is created
	 * If not : the new name modal is shown
	 */
	import(importType, postImportAction) {
		const file = this.importDatasetFile ? this.importDatasetFile[0] : null;
		const datasetName = file ?
			file.name :
			_.find(importType.parameters, { name: 'name' }).value;

		// remove file extension and ask final name
		const name = datasetName.replace(/\.[^/.]+$/, '');
		this.currentPostImportAction = postImportAction;

		return this.DatasetService
			.checkNameAvailability(name)
			// name available: we create the dataset
			.then(() => {
				return this.importDataset(file, name, importType);
			})
			// name is not available, we ask for a new name
			.catch(() => {
				this.datasetName = name;
				this.datasetNameModal = true;
			})
			.finally(() => {
				this.StateService.hideImport();
			});
	}

	/**
	 * @ngdoc method
	 * @name onFileChange
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Update dataset
	 */
	onFileChange() {
		this.UpdateWorkflowService.updateDataset(this.updateDatasetFile[0], this.state.inventory.datasetToUpdate);
	}

	/**
	 * @ngdoc method
	 * @name uploadDatasetName
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Import step 2 - name entered. It checks if the name is available
	 * If so : the dataset is created
	 * If not : the user has to choose to create a new one or the update the existing one
	 */
	onImportNameValidation() {
		const file = this.importDatasetFile ? this.importDatasetFile[0] : null;
		const importType = this.currentInputType;
		const name = this.datasetName;

		return this.DatasetService.checkNameAvailability(name)
		// name still exists
			.then(() => {
				this.importDataset(file, name, importType);
			})
			// name still exists : we ask if user want to update it
			.catch(existingDataset => this.updateOrCreate(file, existingDataset, importType, name));
	}

	/**
	 * @ngdoc method
	 * @name updateOrCreate
	 * @methodOf data-prep.services.import.service:ImportService
	 * @param {object} file The dataset file
	 * @param {object} existingDataset The dataset to update
	 * @param {object} importType The import configuration
	 * @param {string} name The dataset name
	 * @description Import step 3 - Ask to create or update the existing dataset
	 * Create : get a unique name and create
	 * Update : update the content of the existing dataset
	 */
	updateOrCreate(file, existingDataset, importType, name) {
		return this.TalendConfirmService.confirm(['UPDATE_EXISTING_DATASET'], { dataset: name })
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
						return this.importDataset(
							file,
							name,
							importType
						);
					});
			});
	}
}
