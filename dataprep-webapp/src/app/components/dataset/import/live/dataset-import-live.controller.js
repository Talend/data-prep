/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const LIVE_LOCATION_TYPE = 'job';

const DATASTORE_PROPERTIES_MOCK = {
	jsonSchema: {
		title: 'Live Dataset connection',
		type: 'object',
		properties: {
			username: {
				title: 'Username',
				type: 'string',
			},
			password: {
				title: 'Password',
				type: 'string',
			},
			tdp_name: {
				title: 'Dataset name',
				type: 'string',
			},
		},
		required: [
			'username',
			'password',
			'tdp_name',
		],
	},
	properties: {},
	uiSchema: {
		password: {
			'ui:widget': 'password',
		},
		'ui:order': [
			'tdp_name',
			'username',
			'password',
		],
		tdp_name: {
			'ui:autofocus': true,
		},
	},
};

const DATASET_PROPERTIES_MOCK = {
	jsonSchema: {
		title: 'Live dataset',
		type: 'object',
		properties: {
			executable: {
				title: 'Executable',
				type: 'string',
				enumNames: [
					'default/LiveDataSet',
					'production/LiveDataSet',
					'production/LiveDataSet2',
				],
				enum: [
					'5954cde9e4b0f1431b964c9d',
					'594ce425e4b0818c91face84',
				],
			},
		},
		required: [
			'executable',
		],
	},
	properties: {},
	uiSchema: {},
};

/**
 * @ngdoc controller
 * @name data-prep.dataset-import-live:DatasetImportLiveCtrl
 * @description Live Dataset Import controller
 */
export default class DatasetImportLiveCtrl {
	constructor($document, $timeout, $translate, DatasetService, MessageService, ImportService, UploadWorkflowService) {
		'ngInject';

		this.$document = $document;
		this.$timeout = $timeout;
		this.$translate = $translate;

		this.locationType = LIVE_LOCATION_TYPE;

		this.datasetService = DatasetService;
		this.importService = ImportService;
		this.messageService = MessageService;
		this.uploadWorkflowService = UploadWorkflowService;

		this.onDatastoreFormSubmit = this.onDatastoreFormSubmit.bind(this);
		this._getDatastoreFormActions = this._getDatastoreFormActions.bind(this);

		this.onDatasetFormSubmit = this.onDatasetFormSubmit.bind(this);
		this._getDatasetFormActions = this._getDatasetFormActions.bind(this);

		this._create = this._create.bind(this);
		this._edit = this._edit.bind(this);
		this._reset = this._reset.bind(this);
	}

	$onChanges(changes) {
		const item = changes.item && changes.item.currentValue;
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
		else {
			this.importService
				.importParameters(this.locationType)
				.then(({ data }) => {
					const { properties } = data;
					this.datastoreForm = null;
					this._getDatastoreFormActions(properties);
					this.$timeout(() => {
						this.datastoreForm = data;
					});
					return properties;
				})
				.catch(this._reset);
		}
	}

	/**
	 * @ngdoc method
	 * @name _initDatasetForm
	 * @methodOf data-prep.dataset-import-tcomp:DatasetImportTcompCtrl
	 * @description Initialize dataset form from datastore form data
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
	 * @methodOf data-prep.dataset-import-tcomp:DatasetImportTcompCtrl
	 * @description Populates datastore form actions if they don't exist
	 */
	_getDatastoreFormActions() {
		if (!this.datastoreFormActions) {
			this.datastoreFormActions = [{
				style: 'info',
				type: 'submit',
				label: this.$translate.instant('DATASTORE_TEST_CONNECTION'),
			}];
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
	 * @name onDatastoreFormSubmit
	 * @methodOf data-prep.import.controller:ImportCtrl
	 * @description Datastore form change handler
	 * @param uiSpecs All data as form properties
	 * @param definitionName ID attached to the form
	 */
	onDatastoreFormSubmit(uiSpecs, definitionName = this.locationType) {
		const { formData } = uiSpecs;
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
		const formsData = {
			dataStoreProperties: formData,
			dataSetProperties: this.datasetFormData,
		};
		const action = this.item ? this._edit : this._create;
		action(formsData)
			.then(this.uploadWorkflowService.openDataset)
			.then(this._reset)
			.finally(() => {
				this.currentPropertyName = null;
			});
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
			.then(({ data }) => {
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
