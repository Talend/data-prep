(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetListService
     * @description Dataset grid service. This service holds the dataset list like a cache and consume DatasetRestService to access to the REST api<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
     * @requires data-prep.services.dataset.service:DatasetRestService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.utils.service:StorageService
     */
    function DatasetListService(state, $q, DatasetRestService, StateService) {

        var deferredCancel;
        var datasetsPromise;

        var service = {
            refreshDatasets : refreshDatasets,
            create : create,
            clone: clone,
            move: move,
            importRemoteDataset : importRemoteDataset,
            update : update,
            processCertification : processCertification,
            delete : deleteDataset,
            refreshPreparations : refreshPreparations,
            getDatasetsPromise : getDatasetsPromise,
            hasDatasetsPromise: hasDatasetsPromise
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
            var sort = state.inventory.sort.id;
            var order = state.inventory.order.id;

            deferredCancel = $q.defer();
            datasetsPromise = DatasetRestService.getDatasets(sort, order, deferredCancel)
                .then(function(res) {
                    StateService.setDatasets(res.data);
                    return res.data;
                });
            return datasetsPromise;
        }

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object} dataset The dataset to create
         * @param {object} folder - the dataset folder
         * @description Create a dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        function create(dataset, folder) {
            var promise = DatasetRestService.create(dataset, folder);

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
         * @param {object} folder to clone the dataset
         * @param {string} cloneName the name for the cloned dataset
         * @description Clone a dataset from backend and refresh its internal list
         * @returns {promise} The pending GET promise
         */
        function clone(dataset, folder, cloneName) {
            var promise = DatasetRestService.clone(dataset, folder, cloneName);

            promise.then(function (){
                refreshDatasets();
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name move
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {Object} dataset the dataset infos to move
         * @param {Object} folder the original folder of the dataset
         * @param {Object} newFolder the folder to move the dataset
         * @param {String} newName the name for the moved dataset (optional)
         * @description Move a dataset from backend and refresh its internal list
         * @returns {promise} The pending PUT promise
         */
        function move(dataset, folder, newFolder, newName) {
            var promise = DatasetRestService.move(dataset, folder, newFolder, newName);

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
         * @param {object} folder - the dataset folder
         * @description Import a remote dataset from backend and refresh its internal list
         * @returns {promise} The pending POST promise
         */
        function importRemoteDataset(parameters, folder) {
            var promise = DatasetRestService.import(parameters, folder);

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
                    StateService.removeDataset(dataset);
                });
        }

        /**
         * @ngdoc method
         * @name refreshPreparations
         * @methodOf data-prep.services.dataset.service:DatasetListService
         * @param {object[]} preparations The preparations to use
         * @description [PRIVATE] Set the default preparation to each dataset
         * @returns {promise} The process promise
         */
        function refreshPreparations(preparations) {
            return getDatasetsPromise()
                .then(function(datasets) {
                        // group preparation per dataset
                        var datasetPreps = _.groupBy(preparations, function(preparation){
                            return preparation.dataSetId;
                        });

                        // reset default preparation for all datasets
                        _.forEach(datasets, function(dataset){
                            var preparations = datasetPreps[dataset.id];
                            dataset.preparations = preparations || [];
                            dataset.preparations = _.sortByOrder(dataset.preparations, 'lastModificationDate', false);
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