(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.preparation.service:PreparationService
     * @description Preparation list service. this service manage the operations that touches the preparations
     * @requires data-prep.services.utils.service:StorageService
     * @requires data-prep.services.preparation.service:PreparationListService
     * @requires data-prep.services.preparation.service:PreparationRestService
     * @requires data-prep.services.dataset.service:DatasetListService
     * @requires data-prep.services.folder.service:FolderService
     */
    function PreparationService(state, $q, StorageService, PreparationListService, PreparationRestService, DatasetListService, FolderService) {
        return {
            //get, refresh preparations
            refreshPreparations: refreshPreparations,
            getPreparations: getPreparations,

            //details, content
            getContent: PreparationRestService.getContent,
            getDetails: PreparationRestService.getDetails,

            //preparation lifecycle
            create: create,
            clone: clone,
            delete: deletePreparation,
            setName: setName,

            //preparation steps lifecycle
            copyImplicitParameters: copyImplicitParameters,
            paramsHasChanged: paramsHasChanged,
            appendStep: PreparationRestService.appendStep,
            updateStep: updateStep,
            removeStep: PreparationRestService.removeStep,
            setHead: PreparationRestService.setHead,

            //preview
            getPreviewDiff: PreparationRestService.getPreviewDiff,
            getPreviewUpdate: PreparationRestService.getPreviewUpdate,
            getPreviewAdd: PreparationRestService.getPreviewAdd
        };

        //---------------------------------------------------------------------------------
        //------------------------------------GET/REFRESH----------------------------------
        //---------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name refreshPreparationsMetadata
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description [PRIVATE] Refresh the default preparation within each dataset
         */
        function consolidatePreparationsAndDatasets(response) {
            DatasetListService.refreshDefaultPreparation(state.inventory.preparations)
                .then(PreparationListService.refreshMetadataInfos)
                .then(FolderService.refreshDefaultPreparation);
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
            return state.inventory.preparations !== null ?
                $q.when(state.inventory.preparations) :
                refreshPreparations();
        }

        //---------------------------------------------------------------------------------
        //-----------------------------------------LIFE------------------------------------
        //---------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} datasetId The dataset id
         * @param {string} name The preparation name
         * @description Create a new preparation
         * @returns {promise} The POST promise
         */
        function create(datasetId, name) {
            return PreparationListService.create(datasetId, name)
                .then(consolidatePreparationsAndDatasets)
                .then(function(preparation) {
                    //get all dataset aggregations per columns from localStorage and save them for the new preparation
                    StorageService.savePreparationAggregationsFromDataset(datasetId, preparation.id);
                    return preparation;
                });
        }

        /**
         * @ngdoc method
         * @name clone
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} preparationId The preparation id
         * @description Create a new preparation, and keep the current preparation id
         * @returns {promise} The POST promise
         */
        function clone(preparationId) {
            return PreparationListService.clone(preparationId)
                .then(consolidatePreparationsAndDatasets);
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
                .then(consolidatePreparationsAndDatasets)
                .then(function(response) {
                    //get remove all preparation aggregations per columns in localStorage
                    StorageService.removeAllAggregations(preparation.dataSetId, preparation.id);
                    return response;
                });
        }

        //---------------------------------------------------------------------------------
        //----------------------------------------UPDATE-----------------------------------
        //---------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name setName
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} preparationId The preparation id
         * @param {string} name The preparation name
         * @description Create a new preparation if no preparation is loaded, update the name otherwise
         * @returns {promise} The POST promise
         */
        function setName(preparationId, name) {
            return PreparationListService.update(preparationId, name)
                .then(consolidatePreparationsAndDatasets)
                .then(function(preparation) {
                    StorageService.moveAggregations(preparation.dataSetId, preparationId, preparation.id);
                    return preparation;
                });
        }

        /**
         * @ngdoc method
         * @name copyImplicitParameters
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {object} parameters The parameters to copy into
         * @param {object} originalParameters The original parameters containing the implicit parameters values
         * @description Copy the original implicit parameters values into new parameters
         */
        function copyImplicitParameters(parameters, originalParameters) {
            /*jshint camelcase: false */
            parameters.scope = originalParameters.scope;

            if('column_id' in originalParameters) {
                parameters.column_id = originalParameters.column_id;
            }

            if('column_name' in originalParameters) {
                parameters.column_name = originalParameters.column_name;
            }

            if('row_id' in originalParameters) {
                parameters.row_id = originalParameters.row_id;
            }
        }

        /**
         * @ngdoc method
         * @name updateStep
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @param {string} preparationId The preparation id
         * @param {object} step The step to update
         * @param {object} parameters The new action parameters
         * @description Update a step with new parameters
         * @returns {promise} The PUT promise
         */
        function updateStep(preparationId, step, parameters) {
            return PreparationRestService.updateStep(
                preparationId,
                step.transformation.stepId,
                {action: step.transformation.name, parameters: parameters}
            );
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
            return JSON.stringify(newParams) !== JSON.stringify(step.actionParameters.parameters);
        }
    }

    angular.module('data-prep.services.preparation')
        .service('PreparationService', PreparationService);
})();