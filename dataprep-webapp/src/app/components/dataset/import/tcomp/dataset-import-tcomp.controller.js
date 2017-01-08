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
	constructor($document, $translate, state, StateService, DatasetService, MessageService, ImportService, UploadWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$translate = $translate;

		this.state = state;

		this.datasetService = DatasetService;
		this.importService = ImportService;
		this.messageService = MessageService;
		this.stateService = StateService;
		this.uploadWorkflowService = UploadWorkflowService;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);

		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormCancel = this.onDatasetFormCancel.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
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
				});
		}
		else if (locationType) {
			this.importService
				.importParameters(locationType)
				.then((response) => {
					const { data } = response;
					this._getDatastoreFormActions();
					this.datastoreForm = data;
				});
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
					onClick: this.onDatasetFormCancel,
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
	 * @name onDatasetFormCancel
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Cancel action for modal
	 */
	onDatasetFormCancel() {
		this._reset();
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
	onDatastoreFormChange(formData, definitionName = this.locationType, propertyName) {
		this.importService
			.refreshForm(definitionName, propertyName, formData)
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
			if (this.item) {
				this._edit(formsData);
			}
			else {
				this._create(formsData);
			}
		}
		else {
			this.importService
				.testConnection(definitionName, formData)
				.then(() => this.messageService.success(
					'DATASTORE_TEST_CONNECTION',
					'DATASTORE_CONNECTION_SUCCESSFUL'
				))
				.then(() => {
					if (!this.item) {
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
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param definitionName ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatasetFormChange(formData, definitionName = this.locationType, propertyName) {
		this.importService.refreshForm(definitionName, propertyName, formData)
			.then((response) => {
				const { data } = response;
				this.datasetForm = data;
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
		this.submitLock = true;
		const $datastoreFormSubmit = this.$document.find(DATASTORE_SUBMIT_SELECTOR).eq(0);
		if ($datastoreFormSubmit.length) {
			const { formData } = uiSpecs;
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
		this.stateService.hideImport();
		this.stateService.setCurrentImportItem(null);
		this.datastoreForm = null;
		this.datasetForm = null;
		this.datasetFormData = null;
		this.submitLock = false;
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
		this.importService
			.createDataset(this.locationType, formsData)
			.then((response) => {
				const { data } = response;
				const { dataSetId } = data;
				return this.datasetService.getDatasetById(dataSetId);
			})
			.then(this.uploadWorkflowService.openDataset)
			.finally(this._reset);
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
		this.importService
			.editDataset(this.item.id, formsData)
			.finally(this._reset);
	}
}
