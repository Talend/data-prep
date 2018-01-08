/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.about.service:AboutService
 * @description About Service. This service provides the entry point to the backend builds details
 * @requires data-prep.services.utils.service:RestURLs
 * @requires data-prep.services.state.service:StateService
 */
export default class AboutService {
	constructor($http, RestURLs, StateService) {
		'ngInject';
		this.$http = $http;
		this.url = RestURLs.versionUrl;
		this.stateService = StateService;
	}

	/**
	 * @ngdoc method
	 * @name fetchBuildDetails
	 * @methodOf data-prep.services.about.service:AboutService
	 * @description Fetches the build id of each backend service
	 * @returns {Promise} The GET call promise
	 */
	fetchBuildDetails() {
		return this.$http.get(this.url).then(resp => resp.data);
	}

	/**
	 * @ngdoc method
	 * @name loadBuilds
	 * @methodOf data-prep.services.about.service:AboutService
	 * @description sets the fetched builds in the state
	 */
	loadBuilds() {
		this.fetchBuildDetails().then((details) => {
			this.stateService.setBuilds(details);
		});
	}
}
