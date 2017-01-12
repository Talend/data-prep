/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const DATASTORE_SUBMIT_SELECTOR = '#datastore-form [type="submit"]';

/**
 * @ngdoc controller
 * @name data-prep.dataset-import-tcomp:DatasetImportTcompCtrl
 * @description TCOMP Dataset Import controller
 */
export default class DatasetImportTcompCtrl {
	constructor($document, $timeout, $translate, DatasetService, MessageService, ImportService, UploadWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$timeout = $timeout;
		this.$translate = $translate;

		this.datasetService = DatasetService;
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
		const locationType = changes.locationType && changes.locationType.currentValue;
		if (item) {
			this.importService
				.getFormsByDatasetId(this.item.id)
				.then((response) => {
					const { data } = response;
					const { dataStoreFormData, dataSetFormData } = data;
					this._getDatastoreFormActions();
					this.datastoreForm = dataStoreFormData;
					this._getDatasetFormActions();
					this.datasetForm = dataSetFormData;
				})
				.catch(this._reset);
		}
		else if (locationType) {
			this.importService
				.importParameters(locationType)
				.then((response) => {
					const { data } = response;
					this._getDatastoreFormActions();
					this.datastoreForm = data;
				})
				.catch(this._reset);
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDatastoreFormActions
	 * @methodOf data-prep.dataset-import-tcomp:DatasetImportTcompCtrl
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
	 * @methodOf data-prep.dataset-import-tcomp:ImportService
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
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param definitionName ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatastoreFormChange(formData, definitionName, propertyName) {
		this.importService
			.refreshForm(propertyName, formData)
			.then((response) => {
				const { data } = response;
				this.datastoreForm = data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param definitionName ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, definitionName = this.locationType) {
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
					.then((response) => {
						this.datasetForm = response.data;
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
						this.importService
							.getDatasetForm(formData)
							.then((response) => {
								const { data } = response;
								this._getDatasetFormActions();
								this.datasetForm = data;
							});
					}
				});
		}
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormChange
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
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
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Create dataset with both forms data
	 * @param formsData Datastore and dataset properties
	 * @private
	 */
	_create(formsData) {
		return this.importService
			.createDataset(this.locationType, formsData)
			.then((response) => {
				const { data } = response;
				const { dataSetId } = data;
				return this.datasetService.getDatasetById(dataSetId);
			});
	}

	/**
	 * @ngdoc method
	 * @name _edit
	 * @methodOf data-prep.import.controller:ImportCtrl
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
