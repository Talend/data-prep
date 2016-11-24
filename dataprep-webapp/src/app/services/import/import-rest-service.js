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
export default class ImportRestService {

	constructor($http, RestURLs) {
		'ngInject';

		this.$http = $http;
		this.RestURLs = RestURLs;
	}

	/**
	 * @ngdoc method
	 * @name importTypes
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the available import types
	 * @returns {Promise}  The GET call promise
	 */
	importTypes() {
		return this.$http.get(`${this.RestURLs.datasetUrl}/imports`);
	}

	/**
	 * @ngdoc method
	 * @name importParameters
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Fetch the available import parameters
	 * @returns {Promise}  The GET call promise
	 */
	importParameters(locationType) {
		return this.$http.get(`${this.RestURLs.datasetUrl}/imports/${locationType}/parameters`);
	}

	/**
	 * @ngdoc method
	 * @name refreshParameters
	 * @methodOf data-prep.services.import.service:ImportRestService
	 * @description Refresh the available import parameters
	 * @returns {Promise}  The POST call promise
	 */
	refreshParameters(formId, propertyName, formData) {
		if (formId) {
			return this.$http.post(`${this.RestURLs.tcompUrl}/properties/${formId}/after/${propertyName}`, formData);
		}
	}
}
