(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.preparation.service:PreparationRestService
     * @description Preparation service. This service provides the entry point to preparation REST api. It holds the loaded preparation.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.preparation.service:PreparationService PreparationService} must be the only entry point for preparations</b>
     */
    function PreparationRestService($http, RestURLs) {
        return {
            //lifecycle
            create: create,
            clone: clone,
            update: update,
            delete: deletePreparation,

            //step
            appendStep: appendStep,
            updateStep: updateStep,
            removeStep: removeStep,
            setHead: setHead,

            //getter : list, content, details
            getPreparations: getPreparations,
            getContent: getContent,
            getDetails: getDetails,

            //preview
            getPreviewDiff: getPreviewDiff,
            getPreviewUpdate: getPreviewUpdate,
            getPreviewAdd: getPreviewAdd
        };

        //---------------------------------------------------------------------------------
        //----------------------------------------GETTERS----------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getPreparations
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @description Get All the user's preparations
         * @returns {promise} - the GET promise
         */
        function getPreparations() {
            return $http.get(RestURLs.preparationUrl);
        }

        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preparation id to load
         * @param {string} stepId The step id to load
         * @description Get preparation records at the specific step
         * @returns {promise} The GET promise
         */
        function getContent(preparationId, stepId) {
            var url = RestURLs.preparationUrl + '/' + preparationId + '/content?version=' + stepId;
            return $http.get(url)
                .then(function(res) {
                    return res.data;
                });
        }

        /**
         * @ngdoc method
         * @name getDetails
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preparation id to load
         * @description Get current preparation details
         * @returns {promise} The GET promise
         */
        function getDetails(preparationId) {
            return $http.get(RestURLs.preparationUrl + '/' + preparationId + '/details');
        }

        //---------------------------------------------------------------------------------
        //---------------------------------------LIFECYCLE---------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} datasetId The dataset id
         * @param {string} name The preparation name
         * @description Create a new preparation
         * @returns {promise} The POST promise
         */
        function create(datasetId, name) {
            var request = {
                method: 'POST',
                url: RestURLs.preparationUrl,
                data: {
                    name: name,
                    dataSetId: datasetId
                }
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name clone
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preparation id
         * @description Clone the preparation
         * @returns {promise} The PUT promise
         */
        function clone(preparationId) {
            var request = {
                method: 'PUT',
                url: RestURLs.preparationUrl + '/clone/' + preparationId
            };
            return $http(request);
        }

        /**
         * @ngdoc method
         * @name update
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preparation id
         * @param {string} name The new preparation name
         * @description Update the current preparation name
         * @returns {promise} The PUT promise
         */
        function update(preparationId, name) {
            var request = {
                method: 'PUT',
                url: RestURLs.preparationUrl + '/' + preparationId,
                headers: {
                    'Content-Type': 'application/json'
                },
                data: {name: name}
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object} preparationId The preparation id to delete
         * @description Delete a preparation
         * @returns {promise} The DELETE promise
         */
        function deletePreparation(preparationId) {
            return $http.delete(RestURLs.preparationUrl + '/' + preparationId);
        }

        //---------------------------------------------------------------------------------
        //-----------------------------------------STEPS-----------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name adaptTransformAction
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
         * @param {string} insertionStepId The insertion point step id. (Head = 'head' | falsy | head_step_id)
         * @description Adapt transformation action to api
         * @returns {object} - the adapted action
         */
        function adaptTransformAction (actionParams, insertionStepId) {
            return {
                insertionStepId: insertionStepId,
                actions: actionParams instanceof Array ? actionParams : [actionParams]
            };
        }

        /**
         * @ngdoc method
         * @name appendStep
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object} preparationId The preparation id
         * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
         * @param {string} insertionStepId The insertion point step id. (Head = 'head' | falsy | head_step_id)
         * @description Append a new transformation in the current preparation.
         * @returns {promise} - the POST promise
         */
        function appendStep(preparationId, actionParams, insertionStepId) {
            var actionParam = adaptTransformAction(actionParams, insertionStepId);
            var request = {
                method: 'POST',
                url: RestURLs.preparationUrl + '/' + preparationId + '/actions',
                headers: {
                    'Content-Type': 'application/json'
                },
                data: actionParam
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name updateStep
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preaparation id to update
         * @param {string} stepId The step to update
         * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
         * @description Update a step with new parameters
         * @returns {promise} The PUT promise
         */
        function updateStep(preparationId, stepId, actionParams) {
            var request = {
                method: 'PUT',
                url: RestURLs.preparationUrl + '/' + preparationId + '/actions/' + stepId,
                headers: {
                    'Content-Type': 'application/json'
                },
                data: {actions: [actionParams]}
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name removeStep
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preaparation id to update
         * @param {string} stepId The step to delete
         * @description Delete a step
         * @returns {promise} The DELETE promise
         */
        function removeStep(preparationId, stepId) {
            var url = RestURLs.preparationUrl + '/' + preparationId + '/actions/' + stepId;
            return $http.delete(url);
        }

        /**
         * @ngdoc method
         * @name setHead
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {string} preparationId The preparation id
         * @param {string} stepId The head step id
         * @description Move the preparation head to the specified step
         * @returns {promise} The PUT promise
         */
        function setHead(preparationId, stepId) {
            var url = RestURLs.preparationUrl + '/' + preparationId + '/head/' + stepId;
            return $http.put(url);
        }

        //---------------------------------------------------------------------------------
        //----------------------------------------PREVIEW----------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getPreviewDiff
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object} params The preview parameters
         * @param {string} canceler The canceler promise
         * @description POST Preview diff between 2 unchanged steps of a recipe
         * @returns {promise} The POST promise
         */
        function getPreviewDiff(params, canceler) {
            var request = {
                method: 'POST',
                url: RestURLs.previewUrl + '/diff',
                headers: {
                    'Content-Type': 'application/json'
                },
                data: params,
                timeout: canceler.promise
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name getPreviewUpdate
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object} params The preview parameters
         * @param {string} canceler The canceler promise
         * @description POST preview diff between 2 same actions but with 1 updated step
         * @returns {promise} The POST promise
         */
        function getPreviewUpdate(params, canceler) {
            var request = {
                method: 'POST',
                url: RestURLs.previewUrl + '/update',
                headers: {
                    'Content-Type': 'application/json'
                },
                data: params,
                timeout: canceler.promise
            };

            return $http(request);
        }

        /**
         * @ngdoc method
         * @name getPreviewAdd
         * @methodOf data-prep.services.preparation.service:PreparationRestService
         * @param {object} params The preview parameters
         * @param {string} canceler The canceler promise
         * @description POST preview diff between the preparation head and a new added transformation
         * @returns {promise} The POST promise
         */
        function getPreviewAdd(params, canceler) {
            var request = {
                method: 'POST',
                url: RestURLs.previewUrl + '/add',
                headers: {
                    'Content-Type': 'application/json'
                },
                data: params,
                timeout: canceler.promise
            };

            return $http(request);
        }
    }

    angular.module('data-prep.services.preparation')
        .service('PreparationRestService', PreparationRestService);
})();