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
 * @name data-prep.services.import.service:ImportService
 * @description Import service. This service provide the entry point to the backend import REST api.
 * @requires data-prep.services.import.service:ImportService
 */
export default class ImportService {

	constructor($document, $rootScope, $translate, state, DatasetService, ImportRestService,
				StateService, TalendConfirmService, UploadWorkflowService, UpdateWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$rootScope = $rootScope;
		this.$translate = $translate;

		this.ImportRestService = ImportRestService;
		this.StateService = StateService;

		this.$document = $document;
		this.$translate = $translate;
		this.state = state;
		this.DatasetService = DatasetService;
		this.StateService = StateService;
		this.TalendConfirmService = TalendConfirmService;
		this.UpdateWorkflowService = UpdateWorkflowService;
		this.UploadWorkflowService = UploadWorkflowService;

		this.currentInputType = null;
		this.datastoreFormActions = null;
		this.datasetFormActions = null;
		this.dataStoreId = null;
		this.updateDatasetFile = null;
		this.importDatasetFile = null;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);

		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
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
	 * @name refreshParameters
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Refresh the available import parameters
	 * @returns {Promise}  The POST call promise
	 */
	refreshParameters(formId, propertyName, formData) {
		return this.manageLoader(
			this.ImportRestService.refreshParameters,
			[formId, propertyName, formData]
		);
	}

	/**
	 * @ngdoc method
	 * @name testConnection
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Test connection to a datastore
	 * @returns {Promise} The POST call promise
	 */
	testConnection(formId, formData) {
		return this.manageLoader(
			this.ImportRestService.testConnection,
			[formId, formData]
		);
	}

	/**
	 * @ngdoc method
	 * @name getDatasetForm
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Get dataset form properties
	 * @returns {Promise} The GET call promise
	 */
	getDatasetForm(datastoreId) {
		return this.manageLoader(
			this.ImportRestService.getDatasetForm,
			[datastoreId]
		);
	}

	/**
	 * @ngdoc method
	 * @name refreshDatasetForm
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Refresh the available dataset form parameters
	 * @returns {Promise}  The POST call promise
	 */
	refreshDatasetForm(datastoreId, propertyName, formData) {
		return this.manageLoader(
			this.ImportRestService.refreshDatasetForm,
			[datastoreId, propertyName, formData]
		);
	}

	/**
	 * @ngdoc method
	 * @name createDataset
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Create dataset for a datastore
	 * @returns {Promise} The POST call promise
	 */
	createDataset(datastoreId, formData) {
		return this.manageLoader(
			this.ImportRestService.createDataset,
			[datastoreId, formData]
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
			switch (importType.locationType) {
			case 'local':
				this.$document.find('#importDatasetFile').eq(0).click();
				break;
			default:
				this.StateService.showImport();
				if (this.currentInputType.dynamic) {
					this._getDatastoreFormActions();
					this._getDatasetFormActions();

					this.importParameters(this.currentInputType.locationType)
						.then((response) => {
							if (this._isTCOMP(importType.locationType)) {
								this.datastoreForm = response.data;
							}
							else {
								this.currentInputType.parameters = response.data;
							}
						});
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
	 * @methodOf data-prep.services.import.service:ImportService
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
				this.importDataset(file, name, importType);
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
	 * @name _getDatastoreFormActions
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Populates datastore form actions if they don't exist
	 */
	_getDatastoreFormActions() {
		if (!this.datastoreFormActions) {
			this.datastoreFormActions = [
				{
					style: 'info',
					type: 'submit',
					label: this.$translate.instant('DATASTORE_TEST_CONNECTION'),
				},
			];
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDatasetFormActions
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Populates dataset form actions if they don't exist
	 */
	_getDatasetFormActions() {
		if (!this.datasetFormActions) {
			this.datasetFormActions = [
				{
					style: 'default',
					type: 'button',
					onClick: this.onDatasetFormCancel,
					label: this.$translate.instant('CANCEL'),
				},
				{
					style: 'success',
					type: 'submit',
					label: this.$translate.instant('IMPORT_DATASET'),
				},
			];
		}
	}

	/**
	 * @ngdoc method
	 * @name _isTCOMP
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Know if location type comes from TCOMP
	 * @param locationType Import location type
	 * @returns {boolean} true if locationType starts with tcomp
	 */
	_isTCOMP(locationType) {
		return (locationType.indexOf('tcomp') === 0);
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormCancel
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Cancel action for modal
	 */
	onDatasetFormCancel() {
		this.StateService.hideImport();
		this.datastoreForm = null;
		this.dataStoreId = null;
		this.datasetForm = null;
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
	 * @name onDatastoreFormChange
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatastoreFormChange(formData, formId, propertyName) {
		const definitionName = formId || this.currentInputType.locationType;
		this.refreshParameters(definitionName, propertyName, formData)
			.then((response) => {
				this.datastoreForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param formId ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, formId) {
		const definitionName = formId || this.currentInputType.locationType;
		this.testConnection(definitionName, uiSpecs && uiSpecs.formData)
			.then((response) => {
				this.dataStoreId = response.data && response.data.dataStoreId;
				if (!this.dataStoreId) {
					return null;
				}
				return this.getDatasetForm(this.dataStoreId);
			})
			.then((datasetFormResponse) => {
				this.datasetForm = datasetFormResponse && datasetFormResponse.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormChange
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatasetFormChange(formData, formId, propertyName) {
		this.refreshDatasetForm(this.dataStoreId, propertyName, formData)
			.then((response) => {
				this.datasetForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormSubmit
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Datastore form change handler
	 * @param uiSpecs
	 */
	onDatasetFormSubmit(uiSpecs) {
		this.createDataset(this.dataStoreId, uiSpecs && uiSpecs.formData)
			.then((response) => {
				const dataSetId = response.data && response.data.dataSetId;
				return this.DatasetService.getDatasetById(dataSetId);
			})
			.then(this.UploadWorkflowService.openDataset)
			.then(() => this.StateService.hideImport());
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
						return this.importDataset(
							file,
							name,
							importType
						);
					});
			});
	}
}
