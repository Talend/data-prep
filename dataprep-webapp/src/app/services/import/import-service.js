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

	constructor($document, $rootScope, $translate, appSettings, ImportRestService, StateService) {
		'ngInject';

		this.$rootScope = $rootScope;
		this.appSettings = appSettings;

		this.ImportRestService = ImportRestService;
		this.StateService = StateService;


		//TODO
		this.currentInputType = null;
		this.datastoreFormActions = null;
		this.datasetFormActions = null;
		this.dataStoreId = null;
		this.$document = $document;
		this.$translate = $translate;
	}

	manageLoader(method, args) {
		this.$rootScope.$emit('talend.loading.start');
		return method(...args)
			.finally(() => this.$rootScope.$emit('talend.loading.stop'));
	}

	/**
	 * @ngdoc method
	 * @name initImport
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Initialize the import types list
	 */
	initImport() {
		return this.ImportRestService.importTypes()
			.then((response) => {
				const adaptImportTypes = this.adaptImportTypes(response.data);
				this.StateService.setImportTypes(adaptImportTypes);
				return adaptImportTypes;
			});
	}


	/**
	 * @ngdoc method
	 * @name adaptImportTypes
	 * @methodOf data-prep.services.import.service:ImportService
	 * @description Adapt import types for UI components
	 * @param {object[]} importTypes The import types
	 * @returns {object[]} The adapted import types
	 */
	adaptImportTypes(importTypes) {
		return importTypes.map(type => ({
			defaultImport: type.defaultImport,
			label: type.label,
			model: type,
		}));
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
				this.StateService.setShowImportModal(true);
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
		this.StateService.setShowImportModal(false);
		this.datastoreForm = null;
		this.dataStoreId = null;
		this.datasetForm = null;
	}
}
