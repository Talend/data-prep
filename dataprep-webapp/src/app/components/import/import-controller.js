/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.import.controller:ImportCtrl
 * @description Import controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.datasetWorkflowService.service:UploadWorkflowService
 * @requires data-prep.services.datasetWorkflowService.service:UpdateWorkflowService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires talend.widget.service:TalendConfirmService
 */
export default class ImportCtrl {

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

		/** List of supported import type */
		this.importTypes = this.state.import.importTypes;

		/** Display/Hide the import parameters modal */
		this.showModal = false;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);

		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormCancel = this.onDatasetFormCancel.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------Import-----------------------------------------
	// --------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name startDefaultImport
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Start the default import process of a dataset.
	 */
	startDefaultImport() {
		const defaultExportType = _.find(this.importTypes, 'defaultImport', true) || this.importTypes[0];
		this.startImport(defaultExportType);
	}

	/**
	 * @ngdoc method
	 * @name startImport
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Start the import process of a dataset. Route the call to the right import method
	 * (local or remote) depending on the import type user choice.
	 */
	startImport(importType) {
		this.currentInputType = importType;
		if (importType.locationType) {
			switch (importType.locationType) {
			case 'local':
				this.$document.find('#datasetFile').eq(0).click();
				break;
			default:
				this.showModal = true;
				if (this.currentInputType.dynamic) {
					this._getDatastoreFormActions();
					this._getDatasetFormActions();

					this.ImportService.importParameters(this.currentInputType.locationType)
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
	 * @name _getDatastoreFormActions
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Cancel action for modal
	 */
	onDatasetFormCancel() {
		this.showModal = false;
		this.datastoreForm = null;
		this.dataStoreId = null;
		this.datasetForm = null;
	}

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
		const definitionName = formId || this.currentInputType.locationType;
		this.ImportService.refreshParameters(definitionName, propertyName, formData)
			.then((response) => {
				this.datastoreForm = response.data;
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
		const definitionName = formId || this.currentInputType.locationType;
		this.ImportService.testConnection(definitionName, uiSpecs && uiSpecs.formData)
			.then((response) => {
				this.dataStoreId = response.data && response.data.dataStoreId;
				if (!this.dataStoreId) {
					return null;
				}
				return this.ImportService.getDatasetForm(this.dataStoreId);
			})
			.then((datasetFormResponse) => {
				this.datasetForm = datasetFormResponse && datasetFormResponse.data;
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
		this.ImportService.refreshDatasetForm(this.dataStoreId, propertyName, formData)
			.then((response) => {
				this.datasetForm = response.data;
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
		this.ImportService.createDataset(this.dataStoreId, uiSpecs && uiSpecs.formData)
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
		const file = this.datasetFile ? this.datasetFile[0] : null;
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
				this.showModal = false;
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
		const file = this.datasetFile ? this.datasetFile[0] : null;
		const importType = this.currentInputType;
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
