(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.preparation.service:PreparationListService
     * @description Preparation list service. This service holds the preparations list and adapt them for the application.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.preparation.service:PreparationService PreparationService} must be the only entry point for preparations</b>
     * @requires data-prep.services.preparation.service:PreparationRestService
     */
    function PreparationListService($q, PreparationRestService) {
        var preparationsPromise;

        var service = {
            /**
             * @ngdoc property
             * @name preparations
             * @propertyOf data-prep.services.preparation.service:PreparationListService
             * @description the preparations list
             */
            preparations: null,

            refreshPreparations: refreshPreparations,
            getPreparationsPromise: getPreparationsPromise,
            refreshMetadataInfos: refreshMetadataInfos,

            create: create,
            update: update,
            delete: deletePreparation
        };
        return service;


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
                        service.preparations = response.data;

                        return service.preparations;
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
            return service.preparations === null ? refreshPreparations() : $q.when(service.preparations);
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
            var createdResult;
            return PreparationRestService.create(datasetId, name)
                .then(function(response) {
                    createdResult = response;
                    return refreshPreparations();
                })
                .then(function() {
                    return createdResult;
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
            var updateResult;
            return PreparationRestService.update(preparationId, name)
                .then(function(result) {
                    updateResult = result;
                    return refreshPreparations();
                })
                .then(function() {
                    return updateResult;
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
                    var index = service.preparations.indexOf(preparation);
                    service.preparations.splice(index, 1);
                });
        }

        /**
         * @ngdoc method
         * @name refreshMetadataInfos
         * @methodOf data-prep.services.preparation.service:PreparationListService
         * @param {object[]} datasets The datasets to inject
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