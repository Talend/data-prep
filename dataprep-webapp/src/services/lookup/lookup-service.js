(function () {
	'use strict';

	/**
	 * @ngdoc service
	 * @name data-prep.services.lookup.service:LookupService
	 * @description Lookup service. This service provide the entry point to load lookup content
	 * @requires data-prep.services.state.service:StateService
	 * @requires data-prep.services.utils.service:RestURLs
	 */
	function LookupService(LookupRestService, StateService) {
		var service = {
			loadLookupContent: loadLookupContent,
			getLookupPossibleActions: getLookupPossibleActions
		};

		return service;

		/**
		 * @ngdoc method
		 * @name loadLookupContent
		 * @methodOf data-prep.services.lookup.service:LookupService
		 * @param {string} lookupDsUrl dataset lookup url
		 * @description given a dataset Lookup url, it loads its content
		 */
		function loadLookupContent(lookupDsUrl){
			LookupRestService.getLookupContent(lookupDsUrl)
				.then(function(lookupDsContent){
					StateService.setCurrentLookupData(lookupDsContent.data);
					StateService.setLookupDataset(lookupDsContent.data.metadata);
				});
		}

		/**
		 * @ngdoc method
		 * @name getLookupPossibleActions
		 * @methodOf data-prep.services.lookup.service:LookupService
		 * @param {string} datasetId dataset id
		 * @description given a dataset id, it loads its possible actions
		 */
		function getLookupPossibleActions(datasetId){
			return LookupRestService.getLookupActions(datasetId);
		}
	}

	angular.module('data-prep.services.lookup')
		.service('LookupService', LookupService);
})();