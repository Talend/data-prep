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
			// TODO
		}
		else if (locationType) {
			this.importService
				.importParameters(locationType)
				.then((response) => {
					this._getDatastoreFormActions();
					this.datastoreForm = response.data;
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
		this.stateService.hideImport();
		this.datastoreForm = null;
		this.datasetForm = null;
		this.datasetFormData = null;
		this.submitLock = false;
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
			this._onCreate(formsData);
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
		const $datastoreFormSubmit = this.$document.find('#datastore-form [type="submit"]').eq(0);
		if ($datastoreFormSubmit) {
			const { formData } = uiSpecs;
			this.datasetFormData = formData;
			const datastoreFormSubmitElm = $datastoreFormSubmit[0];
			datastoreFormSubmitElm.click();
		}
	}

	_onCreate(formsData) {
		this.importService
			.createDataset(this.locationType, formsData)
			.then((response) => {
				const { data } = response;
				const { dataSetId } = data;
				return this.datasetService.getDatasetById(dataSetId);
			})
			.then(this.uploadWorkflowService.openDataset)
			.then(() => this.stateService.hideImport())
			.finally(() => {
				this.datasetFormData = null;
				this.submitLock = false;
			});
	}
}
