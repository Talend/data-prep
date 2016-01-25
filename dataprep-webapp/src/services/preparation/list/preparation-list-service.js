(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.preparation.service:PreparationListService
     * @description Preparation list service. This service holds the preparations list and adapt them for the application.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.preparation.service:PreparationService PreparationService} must be the only entry point for preparations</b>
     * @requires data-prep.services.preparation.service:PreparationRestService
     * @requires data-prep.services.state.service:StateService
     */
    function PreparationListService($q, state, PreparationRestService, StateService) {
        var preparationsPromise;

        return {
            refreshPreparations: refreshPreparations,
            getPreparationsPromise: getPreparationsPromise,
            refreshMetadataInfos: refreshMetadataInfos,

            create: create,
            clone: clone,
            update: update,
            delete: deletePreparation
        };


        /**
         * @ngdoc method
         * @name refreshPreparations
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @description Refresh the preparations list
         * @returns {promise} The process promise
         */
        function refreshPreparations() {
            if(!preparationsPromise) {
                preparationsPromise = PreparationRestService.getPreparations()
                    .then(function (response) {
                        preparationsPromise = null;
                        StateService.setPreparations(response.data);
                        return response.data;
                    });
            }
            return preparationsPromise;
        }

        /**
         * @ngdoc method
         * @name getPreparationsPromise
         * @methodOf data-prep.services.preparation.service:PreparationService
         * @description Return preparation promise that resolve current preparation list if not empty, or call GET service
         * @returns {promise} The process promise
         */
        function getPreparationsPromise() {
            return state.inventory.preparations === null ? refreshPreparations() : $q.when(state.inventory.preparations);
        }

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @param {string} datasetId The dataset id
         * @param {string} name The preparation name
         * @description Create a new preparation
         * @returns {promise} The POST promise
         */
        function create(datasetId, name) {
            var createdPreparationId;
            return PreparationRestService.create(datasetId, name)
                .then(function(response) {
                    createdPreparationId = response.data;
                    return refreshPreparations();
                })
                .then(function(preparations) {
                    return _.find(preparations, {id: createdPreparationId});
                });
        }

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @param {string} preparationId The preparation id
         * @description Clone the preparation
         * @returns {promise} The GET promise
         */
        function clone(preparationId) {
            return PreparationRestService.clone(preparationId)
                .then(function(){
                    return refreshPreparations();
                });
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
            var updatedPreparationId;
            return PreparationRestService.update(preparationId, name)
                .then(function(result) {
                    updatedPreparationId = result.data;
                    return refreshPreparations();
                })
                .then(function(preparations) {
                    return _.find(preparations, {id: updatedPreparationId});
                });
        }

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @param {object} preparation The preparation to delete
         * @description Delete a preparation from backend and from its internal list
         * @returns {promise} The DELETE promise
         */
        function deletePreparation(preparation) {
            return PreparationRestService.delete(preparation.id)
                .then(function() {
                    StateService.removePreparation(preparation);
                });
        }

        /**
         * @ngdoc method
         * @name refreshMetadataInfos
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @param {array} datasets The datasets to inject
         * @description [PRIVATE] Inject the corresponding dataset to every preparation
         * @returns {promise} The process promise
         */
        function refreshMetadataInfos(datasets) {
            return getPreparationsPromise()
                .then(function(preparations) {
                    _.forEach(preparations, function(prep) {
                        prep.dataset = _.find(datasets, function(dataset) {
                            return dataset.id === prep.dataSetId;
                        });
                    });

                    return preparations;
                });
        }
    }

    angular.module('data-prep.services.preparation')
        .service('PreparationListService', PreparationListService);
})();