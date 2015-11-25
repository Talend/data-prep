(function () {
	'use strict';

	/**
	 * @ngdoc service
	 * @name data-prep.services.lookup:LookupRestService
	 * @description .
	 * {@link data-prep.services.lookup.service:LookupService LookupService} must be the only entry point for lookup</b>
	 */
	function LookupRestService($http, RestURLs) {
		return {
			getLookupActions: getLookupActions,
			getLookupContent: getLookupContent
		};

		/**
		 * @ngdoc method
		 * @name getLookupActions
		 * @methodOf data-prep.services.lookup.service:LookupRestService
		 * @description Import the list of possible actions
		 * @param {string} datasetId the dataset id
		 * @returns {Promise} the $get promise
		 */
		function getLookupActions (datasetId){
			var url = RestURLs.datasetActionsUrl+ '/' + datasetId + '/actions';
			return $http.get(url);
		}

		/**
		 * @ngdoc method
		 * @name getLookupContent
		 * @methodOf data-prep.services.lookup.service:LookupRestService
		 * @description Import the remote dataset content
		 * @param {string} lookupDsUrl dataset url
		 * @returns {Promise} the $get promise
		 */
		function getLookupContent(lookupDsUrl){
			return $http.get(lookupDsUrl);
		}
	}

	angular.module('data-prep.services.lookup')
		.service('LookupRestService', LookupRestService);
})();