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

        /**
         * @ngdoc method
         * @name getTransformations
         * @methodOf data-prep.services.transformation.service:TransformationRestService
         * @param {string} stringifiedColumn The column metadata
         * @param {boolean} showAll show all transformation or some of them
         * @description Fetch the transformations on a column of the dataset
         * @returns {HttpPromise} The POST promise
         */
        this.getTransformations = function(stringifiedColumn, showAll) {
            if(showAll) {
                //Fetch the all transformations on a column of the dataset
                return $http.post(RestURLs.transformUrl + '/actions/column', stringifiedColumn);
            }
            //Fetch some suggested transformations on a column of the dataset (5 by default)
            //To change the number of suggested transformations, add "?limit=10" to the end of URL
            return $http.post(RestURLs.transformUrl + '/suggest/column', stringifiedColumn);
         };


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
         * @returns {HttpPromise} The GET promise
         */
        this.getDynamicParameters = function(action, columnId, datasetId, preparationId, stepId) {
            var queryParams = preparationId ? '?preparationId=' + encodeURIComponent(preparationId) : '?datasetId=' + encodeURIComponent(datasetId);
            queryParams += stepId ? '&stepId=' + encodeURIComponent(stepId) : '';
            queryParams+= '&columnId=' + encodeURIComponent(columnId);

            return $http.get(RestURLs.transformUrl + '/suggest/' + action + '/params' + queryParams);
        };
    }

    angular.module('data-prep.services.transformation')
        .service('TransformationRestService', TransformationRestService);
})();