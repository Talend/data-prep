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
 * @name data-prep.dataset-edit:DatasetEditTcompCtrl
 * @description Inventory Share Config controller
 */
export default class DatasetEditTcompCtrl {
	constructor($translate, state, ImportService, MessageService) {
		'ngInject';

		this.$translate = $translate;
		this.state = state;
		this.importService = ImportService;
		this.messageService = MessageService;

		this.onDatastoreFormChange = this.onDatastoreFormChange.bind(this);
		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);

		this.onDatasetFormChange = this.onDatasetFormChange.bind(this);
		this.onDatasetFormCancel = this.onDatasetFormCancel.bind(this);
		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
	}

	$onChanges(changes) {
		const modelToEdit = changes.item && changes.item.currentValue;
		if (modelToEdit) {
			this._getDatastoreFormActions();
			this._getDatasetFormActions();
			this.importService.getDatastoreFormByDatasetId(modelToEdit.id)
				.then((response) => {
					this.datastoreForm = response.data;
				});
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDatastoreFormActions
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Populates datastore form actions if they don't exist
	 */
	_getDatastoreFormActions(isTestConnectionNeeded) {
		if (!this.datastoreFormActions) {
			this.datastoreFormActions = [
				{
					style: 'info',
					type: 'submit',
					label: this.$translate.instant(isTestConnectionNeeded ? 'DATASTORE_TEST_CONNECTION' : 'SUBMIT'),
				},
			];
		}
	}

	/**
	 * @ngdoc method
	 * @name _getDatasetFormActions
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
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
	 * @name onDatasetFormCancel
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Cancel action for modal
	 */
	onDatasetFormCancel() {
		this.datastoreForm = null;
		this.dataStoreId = null;
		this.datasetForm = null;
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormChange
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatastoreFormChange(formData, formId, propertyName) {
		const definitionName = formId || `tcomp-${formData['@definitionName']}`;
		this.importService.refreshParameters(definitionName, propertyName, formData)
			.then((response) => {
				this.datastoreForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param formId ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, formId) {
		const formData = uiSpecs && uiSpecs.formData;
		const definitionName = formId || `tcomp-${formData['@definitionName']}`;
		this.importService.testConnection(definitionName, formData)
			.then((response) => {
				this.dataStoreId = response.data && response.data.dataStoreId;
				if (!this.dataStoreId) {
					return null;
				}
				return this.importService.getDatasetForm(this.dataStoreId);
			})
			.then((datasetFormResponse) => {
				this.datasetForm = datasetFormResponse && datasetFormResponse.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormChange
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Datastore form change handler
	 * @param formData All data as form properties
	 * @param formId ID attached to the form
	 * @param propertyName Property which has triggered change handler
	 */
	onDatasetFormChange(formData, formId, propertyName) {
		this.importService.refreshDatasetForm(this.dataStoreId, propertyName, formData)
			.then((response) => {
				this.datasetForm = response.data;
			});
	}

	/**
	 * @ngdoc method
	 * @name onDatasetFormSubmit
	 * @methodOf data-prep.dataset-edit:DatasetEditTcompCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs
	 */
	onDatasetFormSubmit(uiSpecs) {
		this.importService.editDataset(this.dataStoreId, this.item.id, uiSpecs && uiSpecs.formData)
			.then(() => {
				this.messageService.success(
					'DATASET_EDIT_SUCCESS_TITLE',
					'DATASET_EDIT_SUCCESS'
				);
			});
	}
}
