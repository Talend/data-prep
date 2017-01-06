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
		importParameters,
		refreshForm,
		testConnection,
		getDatasetForm,
		createDataset,
		// TODO
		// getDatastoreFormByDatasetId,
		// getDatasetFormByDatasetId,
		// editDataset,
	};

	/**
	 * @ngdoc method
	 * @name importParameters
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the available import parameters
	 * @returns {Promise}  The GET call promise
	 */
	function importParameters(locationType) {
		return $http.get(`${RestURLs.datasetUrl}/imports/${locationType}/parameters`);
	}

	/**
	 * @ngdoc method
	 * @name getDatasetForm
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the dataset form
	 * @returns {Promise}  The POST call promise
	 */
	function getDatasetForm(datastoreFormData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/dataset/properties`, datastoreFormData);
	}

	/**
	 * @ngdoc method
	 * @name refreshForm
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Refresh the form
	 * @returns {Promise}  The POST call promise
	 */
	function refreshForm(definitionName, propertyName, formData) {
		return $http.post(`${RestURLs.tcompUrl}/properties/${definitionName}/after/${propertyName}`, formData);
	}

	/**
	 * @ngdoc method
	 * @name testConnection
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Test connection to a datastore
	 * @returns {Promise} The POST call promise
	 */
	function testConnection(definitionName, formsData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/${definitionName}/test`, formsData);
	}

	/**
	 * @ngdoc method
	 * @name createDataset
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Create dataset for a datastore
	 * @returns {Promise} The POST call promise
	 */
	function createDataset(definitionName, formsData) {
		return $http.post(`${RestURLs.tcompUrl}/datastores/${definitionName}/dataset`, formsData);
	}

	// TODO
	// /**
	//  * @ngdoc method
	//  * @name getDatastoreFormByDatasetId
	//  * @methodOf data-prep.services.import.service:ImportRestService
	//  * @description Get filled datastore form by dataset id
	//  * @returns {Promise} The GET call promise
	//  */
	// function getDatastoreFormByDatasetId(datasetId) {
	// 	return $http.get(`${RestURLs.tcompUrl}/datasets/${datasetId}/datastore/properties`);
	// }

	// TODO
	// /**
	//  * @ngdoc method
	//  * @name getDatasetFormByDatasetId
	//  * @methodOf data-prep.services.import.service:ImportRestService
	//  * @description Get filled dataset form by dataset id
	//  * @returns {Promise} The GET call promise
	//  */
	// function getDatasetFormByDatasetId(datasetId) {
	// 	return $http.get(`${RestURLs.tcompUrl}/datasets/${datasetId}/properties`);
	// }

	// TODO
	// /**
	//  * @ngdoc method
	//  * @name editDataset
	//  * @methodOf data-prep.services.import.service:ImportRestService
	//  * @description Create dataset for a datastore
	//  * @returns {Promise} The POST call promise
	//  */
	// function editDataset(datasetId, formsData) {
	// 	return $http.post(`${RestURLs.tcompUrl}/datasets/${datasetId}`, formsData);
	// }
}
