(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.preparation.service:PreparationService
     * @description Preparation list service. his service manage the operations that touches the preparations
     * @requires data-prep.services.preparation.service:PreparationListService
     * @requires data-prep.services.preparation.service:PreparationRestService
     * @requires data-prep.services.dataset.service:DatasetListService
     */
    function PreparationService($q, PreparationListService, PreparationRestService, DatasetListService) {
        var service = {
            /**
             * @ngdoc property
             * @name currentPreparationId
             * @propertyOf data-prep.services.preparation.service:PreparationService
             * @description The currently loaded preparation
             */
            currentPreparationId: null,

            //get, refresh preparations
            preparationsList: preparationsList,
            refreshPreparations: refreshPreparations,
            getPreparations: getPreparations,

            //details, content
            getContent: getContent,
            getDetails: getDetails,

            //preparation lifecycle
            create: create,
            delete: deletePreparation,
            deleteCurrentPreparation: deleteCurrentPreparation,

            //update preparation
            setName: setName,
            updateStep: updateStep,
            insertColumnInfo: insertColumnInfo,
            appendStep: appendStep,
            removeStep: removeStep,
            paramsHasChanged: paramsHasChanged,

            //preview
            getPreviewDiff: getPreviewDiff,
            getPreviewUpdate: getPreviewUpdate
        };
        return service;

        //---------------------------------------------------------------------------------
        //------------------------------------GET/REFRESH----------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name preparationsList
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Return the preparations list. See {@link data-prep.services.preparation.service:PreparationListService PreparationListService}.preparations
         * @returns {object[]} The preparations list
         */
        function preparationsList() {
            return PreparationListService.preparations;
        }

        /**
         * @ngdoc method
         * @name refreshPreparationsMetadata
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description [PRIVATE] Refresh the default preparation within each dataset
         */
        function consolidatePreparationsAndDatasets(response) {
            DatasetListService.refreshDefaultPreparation(preparationsList())
                .then(PreparationListService.refreshMetadataInfos);
            return response;
        }

        /**
         * @ngdoc method
         * @name refreshPreparations
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Refresh the preparations list
         * @returns {promise} The process promise
         */
        function refreshPreparations() {
            return PreparationListService.refreshPreparations()
                .then(consolidatePreparationsAndDatasets);
        }

        /**
         * @ngdoc method
         * @name getPreparations
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Return preparation promise that resolve current preparation list if not empty, or call GET service
         * @returns {promise} The process promise
         */
        function getPreparations() {
            return preparationsList() !== null ?
                $q.when(preparationsList()) :
                refreshPreparations();
        }

        //---------------------------------------------------------------------------------
        //----------------------------------DETAILS/CONTENT--------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} version The version (step id) to load
         * @description Get preparation records at the specific 'version' step
         * @returns {promise} The GET promise
         */
        function getContent(version) {
            return PreparationRestService.getContent(service.currentPreparationId, version);
        }

        /**
         * @ngdoc method
         * @name getDetails
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Get current preparation details
         * @returns {promise} The GET promise
         */
        function getDetails() {
            return PreparationRestService.getDetails(service.currentPreparationId);
        }

        //---------------------------------------------------------------------------------
        //-----------------------------------------LIFE------------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} metadata The dataset metadata
         * @param {string} name The preparation name
         * @description Create a new preparation, and keep the current preparation id
         * @returns {promise} The POST promise
         */
        function create(metadata, name) {
            return PreparationListService.create(metadata.id, name)
                .then(consolidatePreparationsAndDatasets)
                .then(function(resp) {
                    service.currentPreparationId = resp.data;
                });
        }

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} preparation The preparation to delete
         * @description Delete a preparation
         * @returns {promise} The DELETE promise
         */
        function deletePreparation(preparation) {
            return PreparationListService.delete(preparation)
                .then(consolidatePreparationsAndDatasets);
        }

        /**
         * @ngdoc method
         * @name deleteCurrentPreparation
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Delete the current preparation
         * @returns {promise} The DELETE promise
         */
        function deleteCurrentPreparation() {
            var preparationToDelete = _.find(PreparationListService.preparations, function(preparation) {
                return preparation.id === service.currentPreparationId;
            });
            return deletePreparation(preparationToDelete);
        }

        //---------------------------------------------------------------------------------
        //----------------------------------------UPDATE-----------------------------------
        //---------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name setName
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} metadata The dataset metadata
         * @param {string} name The preparation name
         * @description Create a new preparation if no preparation is loaded, update the name otherwise
         * @returns {promise} The POST promise
         */
        function setName(metadata, name) {
            if(service.currentPreparationId) {
                return PreparationListService.update(service.currentPreparationId, name)
                    .then(function(result) {
                        service.currentPreparationId = result.data;
                    })
                    .then(consolidatePreparationsAndDatasets);
            }
            else {
                return create(metadata, name);
            }
        }

        /**
         * @ngdoc method
         * @name updateStep
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} step The step to update
         * @param {object} parameters The new action parameters
         * @description Update a step with new parameters
         * @returns {promise} The PUT promise
         */
        function updateStep(step, parameters) {
            parameters = parameters || {};
            insertColumnInfo(parameters, step.column);
            return PreparationRestService.updateStep(service.currentPreparationId, step.transformation.stepId, step.transformation.name, parameters);
        }


        /**
         * @ngdoc method
         * @name insertColumnInfo
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} parameters The parameters to update the column from
         * @param {object} column The update source.
         */
        function insertColumnInfo(parameters, column) {
            /*jshint camelcase: false */
            parameters.column_id = column.id;
            parameters.column_name = column.name;
        }

        /**
         * @ngdoc method
         * @name appendStep
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} metadata The target metadata
         * @param {object} action The action name
         * @param {object} column The target column
         * @param {object} parameters The new action parameters
         * @description Append a step. If the preparation does not exists, it is created
         * @returns {promise} The PUT promise
         */
        function appendStep(metadata, action, column, parameters) {
            parameters = parameters || {};
            insertColumnInfo(parameters, column);
            var promise = service.currentPreparationId ? $q.when(service.currentPreparationId) : create(metadata, 'Preparation draft');

            return promise.then(function() {
                return PreparationRestService.appendStep(service.currentPreparationId, action, parameters);
            });
        }

        /**
         * @ngdoc method
         * @name removeStep
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} stepId The step to delete
         * @description Delete a step.
         * @returns {promise} The DELETE promise
         */
        function removeStep(stepId) {
            return PreparationRestService.removeStep(service.currentPreparationId, stepId);
        }

        /**
         * @ngdoc method
         * @name paramsHasChanged
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} step The step to update
         * @param {object} newParams The new action parameters
         * @description Check if the parameters has changed
         * @returns {boolean} true if parameters has changed, false otherwise
         */
        function paramsHasChanged(step, newParams) {
            newParams = newParams || {};
            insertColumnInfo(newParams, step.column);
            return JSON.stringify(newParams) !== JSON.stringify(step.actionParameters.parameters);
        }

        //---------------------------------------------------------------------------------
        //----------------------------------------PREVIEW----------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getPreviewDiff
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} currentStep The current loaded step
         * @param {string} previewStep The target preview step
         * @param {string} recordsTdpId The records TDP ids to preview
         * @param {string} canceler The canceler promise
         * @description POST Preview diff between 2 unchanged steps of a recipe
         * @returns {promise} The POST promise
         */
        function getPreviewDiff(currentStep, previewStep, recordsTdpId, canceler) {
            return PreparationRestService.getPreviewDiff(service.currentPreparationId, currentStep, previewStep, recordsTdpId, canceler);
        }

        /**
         * @ngdoc method
         * @name getPreviewUpdate
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} currentStep The current loaded step
         * @param {string} updateStep The target step to update
         * @param {string} newParams The new parameters
         * @param {string} recordsTdpId The records TDP ids to preview
         * @param {string} canceler The canceler promise
         * @description POST preview diff between 2 same actions but with 1 updated step
         * @returns {promise} The POST promise
         */
        function getPreviewUpdate(currentStep, updateStep, newParams, recordsTdpId, canceler) {
            return PreparationRestService.getPreviewUpdate(service.currentPreparationId, currentStep, updateStep, newParams, recordsTdpId, canceler);
        }
    }

    angular.module('data-prep.services.preparation')
        .service('PreparationService', PreparationService);
})();