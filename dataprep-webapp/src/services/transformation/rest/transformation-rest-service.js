(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:TransformationRestService
     * @description Transformation REST service. This service provide the entry point to transformation REST api
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.transformation.service:TransformationService TransformationService} must be the only entry point for transformation</b>
     */
    function TransformationRestService($http, RestURLs) {
        return {
            getDynamicParameters: getDynamicParameters,
            getSuggestions: getSuggestions,
            getTransformations: getTransformations
        };

        /**
         * @ngdoc method
         * @name getTransformations
         * @methodOf data-prep.services.transformation.service:TransformationRestService
         * @param {string} column The column metadata
         * @description Fetch the transformations on a column
         * @returns {Promise} The POST promise
         */
        function getTransformations(column) {
            return $http.post(RestURLs.transformUrl + '/actions/column', column);
         }

        /**
         * @ngdoc method
         * @name getSuggestions
         * @methodOf data-prep.services.transformation.service:TransformationRestService
         * @param {string} column The column metadata
         * @description Fetch the suggestions on a column
         * @returns {Promise} The POST promise
         */
        function getSuggestions(column) {
            return $http.post(RestURLs.transformUrl + '/suggest/column', column);
         }


        /**
         * @ngdoc method
         * @name getDynamicParameters
         * @methodOf data-prep.services.transformation.service:TransformationRestService
         * @param {string} action The action name
         * @param {string} columnId The column Id
         * @param {string} datasetId The datasetId
         * @param {string} preparationId The preparation Id
         * @param {string} stepId The step Id
         * @description Fetch the transformations dynamic params
         * @returns {Promise} The GET promise
         */
        function getDynamicParameters(action, columnId, datasetId, preparationId, stepId) {
            var queryParams = preparationId ? '?preparationId=' + encodeURIComponent(preparationId) : '?datasetId=' + encodeURIComponent(datasetId);
            queryParams += stepId ? '&stepId=' + encodeURIComponent(stepId) : '';
            queryParams+= '&columnId=' + encodeURIComponent(columnId);

            return $http.get(RestURLs.transformUrl + '/suggest/' + action + '/params' + queryParams);
        }
    }

    angular.module('data-prep.services.transformation')
        .service('TransformationRestService', TransformationRestService);
})();