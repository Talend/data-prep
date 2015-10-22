(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetListService
     * @description Dataset grid service. This service holds the dataset list like a cache and consume DatasetRestService to access to the REST api<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
     * @requires data-prep.services.dataset.service:DatasetRestService
     */
    function DatasetListService($q, DatasetRestService, DatasetListSortService) {

        var deferredCancel;
        var datasetsPromise;

        var service = {
            refreshDatasets : refreshDatasets,
            create : create,
            clone: clone,
            importRemoteDataset : importRemoteDataset,
            update : update,
            processCertification : processCertification,
            delete : deleteDataset,
            refreshDefaultPreparation : refreshDefaultPreparation,
            getDatasetsPromise : getDatasetsPromise,
            hasDatasetsPromise: hasDatasetsPromise,
            datasets: null
        };

        return service;

        /**
         * @ngdoc method
         * @name cancelPendingGetRequest
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Cancel the pending datasets list GET request
         */
        function cancelPendingGetRequest() {
            if(datasetsPromise){
                deferredCancel.resolve('user cancel');
                datasetsPromise = null;
            }
        }

        /**
         * @ngdoc method
         * @name refreshDatasets
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Refresh datasets list
         * @returns {promise} The pending GET promise
         */
        function refreshDatasets() {
            cancelPendingGetRequest();
            var sort = DatasetListSortService.getSort();
            var order = DatasetListSortService.getOrder();

            deferredCancel = $q.defer();
            datasetsPromise = DatasetRestService.getDatasets(sort, order, deferredCancel)
                .then(function(res) {
                    service.datasets = res.data;
                    return service.datasets;
                });
            return datasetsPromise;
        }

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to create
         * @description Create a dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        function create(dataset) {
            var promise = DatasetRestService.create(dataset);

            //The appended promise is not returned because DatasetRestService.create return a $upload object with progress function
            //which is used by the caller
            promise.then(function (){
                  refreshDatasets();
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name clone
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to clone
         * @param {string} the optional cloned dataset name
         * @description Clone a dataset from backend and refresh its internal list
         * @returns {promise} The pending GET promise
         */
        function clone(dataset,name) {
            var promise = DatasetRestService.clone(dataset,name);

            promise.then(function (){
                refreshDatasets();
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name importRemoteDataset
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} parameters The import parameters to import
         * @description Import a remote dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        function importRemoteDataset(parameters) {
            var promise = DatasetRestService.import(parameters);

            //The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
            //which is used by the caller
            promise.then(function (){
                refreshDatasets();
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name update
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to delete
         * @description Update a dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        function update(dataset) {
            var promise = DatasetRestService.update(dataset);

            //The appended promise is not returned because DatasetRestService.import return a $upload object with progress function
            //which is used by the caller
            promise.then(function (){
                refreshDatasets();
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name processCertification
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The target dataset for certification
         * @description Ask certification for a dataset and refresh its internal list
         * @returns {promise} The pending PUT promise
         */
        function processCertification(dataset) {
            return DatasetRestService.processCertification(dataset.id)
                .then(function (){
                    refreshDatasets();
                });
        }

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to delete
         * @description Delete a dataset from backend and from its internal list
         * @returns {promise} The pending DELETE promise
         */
        function deleteDataset(dataset) {
            return DatasetRestService.delete(dataset)
                .then(function() {
                    var index = service.datasets.indexOf(dataset);
                    service.datasets.splice(index, 1);
                });
        }

        /**
         * @ngdoc method
         * @name refreshDefaultPreparation
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object[]} preparations The preparations to use
         * @description [PRIVATE] Set the default preparation to each dataset
         * @returns {promise} The process promise
         */
        function refreshDefaultPreparation(preparations) {
            return getDatasetsPromise()
                .then(function(datasets) {
                        // group preparation per dataset
                        var datasetPreps = _.groupBy(preparations, function(preparation){
                            return preparation.dataSetId;
                        });

                        // reset default preparation for all datasets
                        _.forEach(datasets, function(dataset){
                            var preparations = datasetPreps[dataset.id];
                            dataset.defaultPreparation = preparations && preparations.length === 1 ?  preparations[0] : null;
                        });

                        return datasets;
                });
        }

        /**
         * @ngdoc method
         * @name getDatasetsPromise
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Return resolved or unresolved promise that returns the most updated datasetsList
         * @returns {promise} Promise that resolves datasetsList
         */
        function getDatasetsPromise() {
            return datasetsPromise ? datasetsPromise : refreshDatasets();
        }

        /**
         * @ngdoc method
         * @name hasDatasetsPromise
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @description Check if datasetsPromise is true or not
         * @returns {promise} datasetsPromise
         */
        function hasDatasetsPromise() {
            return datasetsPromise;
        }

    }

    angular.module('data-prep.services.dataset')
        .service('DatasetListService', DatasetListService);
})();