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
}
