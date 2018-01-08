/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const LIVE_LOCATION_TYPE = 'job';

const DATASTORE_SUBMIT_SELECTOR = '#datastore-form [type="submit"]';

/**
 * @ngdoc controller
 * @name data-prep.dataset-import:DatasetImportCtrl
 * @description Dataset Import controller
 */
export default class DatasetImportCtrl {
	constructor($document, $timeout, $translate, DatasetService, MessageService, ImportService, UploadWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$timeout = $timeout;
		this.$translate = $translate;

		this.importService = ImportService;
		this.messageService = MessageService;
		this.uploadWorkflowService = UploadWorkflowService;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);
		this._getDatastoreFormActions = this._getDatastoreFormActions.bind(this);
		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
		this._getDatasetFormActions = this._getDatasetFormActions.bind(this);

		this._create = this._create.bind(this);
		this._edit = this._edit.bind(this);
		this._reset = this._reset.bind(this);
		this._simulateDatastoreSubmit = this._simulateDatastoreSubmit.bind(this);
	}

	$onChanges(changes) {
		const item = changes.item && changes.item.currentValue;
		const locationType = (changes.locationType && changes.locationType.currentValue) || LIVE_LOCATION_TYPE;
		if (item) {
			this.importService
				.getFormsByDatasetId(this.item.id)
				.then(({ data }) => {
					const { dataStoreFormData, dataSetFormData } = data;
					const { properties } = dataStoreFormData;
					this.datastoreForm = null;
					this.datasetForm = null;
					this._getDatastoreFormActions(properties);
					this._getDatasetFormActions();
					this.$timeout(() => {
						this.datastoreForm = dataStoreFormData;
						this.datasetForm = dataSetFormData;
					});
				})
				.catch(this._reset);
		}
		else if (locationType) {
			this.importService
				.importParameters(locationType)
				.then(({ data }) => {
					const { properties } = data;
					this.datastoreForm = null;
					this._getDatastoreFormActions(properties);
					this.$timeout(() => {
						this.datastoreForm = data;
					});
					return properties;
				})
				.then((formData) => {
					const hasHiddenTestConnectionBtn = formData && !formData.tdp_isTestConnectionEnabled;
					if (hasHiddenTestConnectionBtn) {
						return this._initDatasetForm(formData);
					}
				})
				.catch(this._reset);
		}
	}

	/**
	 * @ngdoc method
	 * @name _initDatasetForm
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Initialize dataset form from datastore form data
	 * @param formData Datastore form data
	 * @returns {Promise}
	 * @private
	 */
	_initDatasetForm(formData) {
		return this.importService
			.getDatasetForm(formData)
			.then(({ data }) => {
				this.datasetForm = null;
				this._getDatasetFormActions();
				this.$timeout(() => {
					this.datasetForm = data;
				});
			});
	}

	/**
	 * @ngdoc method
	 * @name _getDatastoreFormActions
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Populates datastore form actions if they don't exist
	 */
	_getDatastoreFormActions(properties) {
		if (!this.datastoreFormActions) {
			this.datastoreFormActions = [{
				style: `info ${properties && !properties.tdp_isTestConnectionEnabled && 'sr-only'}`,
				type: 'submit',
				label: this.$translate.instant('DATASTORE_TEST_CONNECTION'),
			}];
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDatasetFormActions
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Populates dataset form actions if they don't exist
	 */
	_getDatasetFormActions() {
		if (!this.datasetFormActions) {
			this.datasetFormActions = [
				{
					style: 'default',
					type: 'button',
					onClick: this._reset,
					label: this.$translate.instant('CANCEL'),
				},
				{
					style: 'success',
					type: 'submit',
					label: this.$translate.instant(this.item ? 'EDIT_DATASET' : 'IMPORT_DATASET'),
				},
			];
		}
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormChange
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param definitionName ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatastoreFormChange(formData, definitionName = (this.locationType || LIVE_LOCATION_TYPE), propertyName) {
		this.importService
			.refreshForm(propertyName, formData)
			.then(({ data }) => {
				this.datastoreForm = null;
				this.$timeout(() => {
					this.datastoreForm = data;
				});
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param definitionName ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, definitionName = (this.locationType || LIVE_LOCATION_TYPE)) {
		const { formData } = uiSpecs;
		if (this.submitLock) {
			const formsData = {
				dataStoreProperties: formData,
				dataSetProperties: this.datasetFormData,
			};
			let controlledSubmitPromise;
			// Dataset form change
			if (this.currentPropertyName) {
				controlledSubmitPromise = this.importService
					.refreshForms(this.currentPropertyName, formsData)
					.then(({ data }) => {
						this.datasetForm = null;
						this.$timeout(() => {
							this.datasetForm = data;
						});
					});
			}
			// Dataset form submit
			else {
				const action = this.item ? this._edit : this._create;
				controlledSubmitPromise = action(formsData)
					.then(this.uploadWorkflowService.openDataset)
					.then(this._reset);
			}
			controlledSubmitPromise.finally(() => {
				this.currentPropertyName = null;
				this.submitLock = false;
			});
		}
		// Datastore form submit without submit button
		else if (this.datastoreForm && this.datastoreForm.properties && !this.datastoreForm.properties.tdp_isTestConnectionEnabled) {
			// From datastore form submit (i.e. submit with keyboard)
			return false;
		}
		// Datastore form submit
		else {
			this.importService
				.testConnection(definitionName, formData)
				.then(() => this.messageService.success(
					'DATASTORE_TEST_CONNECTION',
					'DATASTORE_CONNECTION_SUCCESSFUL'
				))
				.then(() => {
					if (!this.item && !this.datasetForm) {
						return this._initDatasetForm(formData);
					}
				});
		}
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormChange
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Dataset form change handler
	 * @param formData All data as form properties
	 * @param definitionName ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatasetFormChange(formData, definitionName, propertyName) {
		this.currentPropertyName = propertyName;
		this._simulateDatastoreSubmit(formData);
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormSubmit
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Dataset form submit handler
	 * @see onDatastoreFormSubmit
	 * @param uiSpecs
	 */
	onDatasetFormSubmit(uiSpecs) {
		const { formData } = uiSpecs;
		this._simulateDatastoreSubmit(formData);
	}

	/**
	 * @ngdoc method
	 * @name _simulateDatastoreSubmit
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Simulate datastore form submit after saving dataset form data
	 * Both forms need to be submitted so we have to put a latch in order to submit data store and data set forms data
	 * One way to do that, it's to trigger onClick event on data store form submit button
	 *  1. Second form submit -> save second form data
	 *  2. Trigger click event on first form submit button -> save first form data
	 *  3. Second form submit -> aggregate both forms data and send it
	 * @private
	 */
	_simulateDatastoreSubmit(formData) {
		this.submitLock = true;
		const $datastoreFormSubmit = this.$document.find(DATASTORE_SUBMIT_SELECTOR).eq(0);
		if ($datastoreFormSubmit.length) {
			this.datasetFormData = formData;
			const datastoreFormSubmitElm = $datastoreFormSubmit[0];
			datastoreFormSubmitElm.click();
		}
		else {
			this.submitLock = false;
		}
	}

	/**
	 * @ngdoc method
	 * @name _reset
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Reset state after submit
	 * @private
	 */
	_reset() {
		this.$timeout(() => {
			this.datastoreForm = null;
			this.datasetForm = null;
			this.datasetFormData = null;
			this.submitLock = false;
			this.currentPropertyName = null;
			this.importService.StateService.hideImport();
			this.importService.StateService.setCurrentImportItem(null);
		});
	}

	/**
	 * @ngdoc method
	 * @name _create
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Create dataset with both forms data
	 * @param formsData Datastore and dataset properties
	 * @private
	 */
	_create(formsData) {
		return this.importService
			.createDataset((this.locationType || LIVE_LOCATION_TYPE), formsData)
			.then(({ data }) => {
				const { dataSetId } = data;
				// temp dataset object to read its id
				return { id: dataSetId };
			});
	}

	/**
	 * @ngdoc method
	 * @name _edit
	 * @methodOf data-prep.dataset-import:DatasetImportCtrl
	 * @description Edit dataset with both forms data
	 * @param formsData Datastore and dataset properties
	 * @private
	 */
	_edit(formsData) {
		const itemId = this.item.id;
		return this.importService
			.editDataset(itemId, formsData)
			.then(() => this.item);
	}
}
