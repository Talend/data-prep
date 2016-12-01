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

	constructor($rootScope, ImportRestService, StateService) {
		'ngInject';

		this.$rootScope = $rootScope;
		this.ImportRestService = ImportRestService;
		this.StateService = StateService;
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
				this.StateService.setImportTypes(response.data);
			});
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
}
