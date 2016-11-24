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

	constructor($document, $translate, state, StateService, UploadWorkflowService, UpdateWorkflowService, DatasetService, TalendConfirmService, ImportRestService) {
		'ngInject';

		this.$document = $document;
		this.$translate = $translate;
		this.state = state;
		this.StateService = StateService;
		this.UploadWorkflowService = UploadWorkflowService;
		this.UpdateWorkflowService = UpdateWorkflowService;
		this.DatasetService = DatasetService;
		this.TalendConfirmService = TalendConfirmService;
		this.ImportRestService = ImportRestService;

		/** List of supported import type */
		this.importTypes = this.state.import.importTypes;

		/** Display/Hide the import parameters modal */
		this.showModal = false;

		/** Display/Hide the import parameters modal */
		this.isFetchingParameters = false;
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
					this.isFetchingParameters = true;

					this.datastoreFormActions = [
						{
							style: 'success',
							type: 'submit',
							onClick: this.onDatastoreFormSubmit,
							label: this.$translate.instant('DATASTORE_TEST_CONNECTION'),
						},
					];

					this.onDatastoreFormChange = this.onFormChange.bind(this);

					this.onDatastoreFormSubmit = () => {};

					this.ImportRestService.importParameters(this.currentInputType.locationType)
						.then((response) => {
							if (this._isTCOMP(importType.locationType)) {
								this.currentInputType.datastoreForm = response.data;
							}
							else {
								this.currentInputType.parameters = response.data;
							}
						})
						.finally(() => {
							this.isFetchingParameters = false;
						});
				}
			}
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
	 * @name onCancel
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Cancel action for modal
	 */
	onCancel() {
		this.showModal = false;
		this.currentInputType = {};
	}

	/**
	 * @ngdoc method
	 * @name onFormChange
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Generic form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onFormChange(formData, formId, propertyName) {
		this.isFetchingParameters = true;
		this.ImportRestService.reimportParameters(formId, propertyName, formData)
			.then((response) => {
				this.currentInputType.datastoreForm = response.data;
			})
			.finally(() => {
				this.isFetchingParameters = false;
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
