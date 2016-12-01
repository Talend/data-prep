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
 * @name data-prep.services.import.service:ImportRestService
 * @description Import service. This service provide the entry point to the backend import REST api.<br/>
 */
export default function ImportRestService($http, RestURLs) {
	'ngInject';

	return {
		importTypes,
		importParameters,
		refreshParameters,
		testConnection,
		getDatasetForm,
		refreshDatasetForm,
		createDataset,
	};
	/**
	 * @ngdoc method
	 * @name importTypes
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the available import types
	 * @returns {Promise}  The GET call promise
	 */
	function importTypes() {
		return $http.get(RestURLs.datasetUrl + '/imports');
	}

	/**
	 * @ngdoc method
	 * @name importParameters
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the available import parameters
	 * @returns {Promise}  The GET call promise
	 */
	function importParameters(locationType) {
		return $http.get(RestURLs.datasetUrl + '/imports/' + locationType + '/parameters');
	}

	/**
	 * @ngdoc method
	 * @name refreshParameters
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Refresh the available import parameters
	 * @returns {Promise}  The POST call promise
	 */
	function refreshParameters(formId, propertyName, formData) {
		return $http.post(`${RestURLs.tcompUrl}/properties/${formId}/after/${propertyName}`, formData);
	}

	/**
	 * @ngdoc method
	 * @name testConnection
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Test connection to a datastore
	 * @returns {Promise} The POST call promise
	 */
	function testConnection(formId, formData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/${formId}`, formData);
	}

	/**
	 * @ngdoc method
	 * @name getDatasetForm
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Get dataset form properties
	 * @returns {Promise} The GET call promise
	 */
	function getDatasetForm(datastoreId) {
		return $http.get(`${RestURLs.tcompUrl}/datastores/${datastoreId}/dataset/properties`);
	}

	/**
	 * @ngdoc method
	 * @name refreshDatasetForm
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Refresh the available dataset form parameters
	 * @returns {Promise}  The POST call promise
	 */
	function refreshDatasetForm(datastoreId, propertyName, formData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/${datastoreId}/after/${propertyName}`, formData);
	}

	/**
	 * @ngdoc method
	 * @name createDataset
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Create dataset for a datastore
	 * @returns {Promise} The POST call promise
	 */
	function createDataset(datastoreId, formData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/${datastoreId}/dataset`, formData);
	}
}
