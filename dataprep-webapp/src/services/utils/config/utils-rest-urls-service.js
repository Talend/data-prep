(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.utils.service:RestURLs
     * @description The REST api services url
     */
    function RestURLs() {
        var service = {
            setServerUrl: setServerUrl
        };
        setServerUrl('');
        
        return service;

        /**
         * @ngdoc method
         * @name setServerUrl
         * @propertyOf data-prep.services.utils.service:RestURLs
         * @description Init the api urls with a provided server url
         * @param {string} serverUrl The server url
         */
        function setServerUrl(serverUrl) {
            service.datasetUrl = serverUrl + '/api/datasets';
            service.transformUrl = serverUrl + '/api/transform';
            service.preparationUrl = serverUrl + '/api/preparations';
            service.previewUrl = serverUrl + '/api/preparations/preview';
            service.exportUrl = serverUrl + '/api/export';
            service.aggregationUrl = serverUrl + '/api/aggregate';
            service.typesUrl = serverUrl + '/api/types';
        }
    }

    angular.module('data-prep.services.utils')
        .service('RestURLs', RestURLs);
})();