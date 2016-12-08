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
 * @name data-prep.services.about.service:AboutRestService
 * @description About service. This service provides the list of the build ids of each backend service
 * @requires data-prep.services.utils.service:RestURLs
 */
export default class AboutRestService {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.url = RestURLs.buildsUrl;
	}

	/**
	 * @ngdoc method
	 * @name getBuildsIds
	 * @methodOf data-prep.services.about.service:AboutRestService
	 * @description Fetch the build id of each backend service
	 * @returns {Promise}  The GET call promise
	 */
	buildDetails() {
		return this.$http.get(this.url).then(resp => resp.data);
	}
}
