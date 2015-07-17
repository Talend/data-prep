(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetService
     * @description Dataset general service. This service manage the operations that touches the datasets
     * @requires data-prep.services.dataset.service:DatasetListService
     * @requires data-prep.services.dataset.service:DatasetRestService
     * @requires data-prep.services.preparation.service:PreparationListService
     */
    function DatasetService($q, DatasetListService, DatasetRestService, PreparationListService) {
        var self = this;

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Dataset----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The dataset to delete
         * @description Delete a dataset. It just call {@link data-prep.services.dataset.service:DatasetListService DatasetListService} delete function
         * @returns {promise} The pending DELETE promise
         */
        self.delete = function(dataset) {
            return DatasetListService.delete(dataset)
                .then(consolidatePreparationsAndDatasets);
        };

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The dataset to create
         * @description Create a dataset. It just call {@link data-prep.services.dataset.service:DatasetListService DatasetListService} create function
         * @returns {promise} The pending CREATE promise
         */
        self.create = function(dataset) {
            var promise = DatasetListService.create(dataset);
            promise.then(consolidatePreparationsAndDatasets);
            return promise;
        };

        /**
         * @ngdoc method
         * @name import
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} parameters The import parameters (type, url, username...)
         * @description Import call the backend to import the remote. It just call {@link data-prep.services.dataset.service:DatasetListService DatasetListService} import function
         * @returns {promise} The pending IMPORT promise
         */
        self.import = function(parameters) {
            var promise = DatasetListService.importRemoteDataset(parameters);
            promise.then(consolidatePreparationsAndDatasets);
            return promise;
        };

        /**
         * @ngdoc method
         * @name update
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The dataset to update
         * @description Update a dataset. It just call {@link data-prep.services.dataset.service:DatasetListService DatasetListService} update function
         * @returns {promise} The pending PUT promise
         */
        self.update = function(dataset) {
            var promise = DatasetListService.update(dataset);
            promise.then(consolidatePreparationsAndDatasets);
            return promise;
        };

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Metadata---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name datasetsList
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @description Return the datasets list. See {@link data-prep.services.dataset.service:DatasetListService DatasetListService}.datasets
         * @returns {object[]} The datasets list
         */
        self.datasetsList = function() {
            return DatasetListService.datasets;
        };

        /**
         * @ngdoc method
         * @name consolidatePreparationsAndDatasets
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @description [PRIVATE] Refresh the metadata within the preparations
         */
        var consolidatePreparationsAndDatasets = function(response) {
            PreparationListService.refreshMetadataInfos(self.datasetsList())
                .then(DatasetListService.refreshDefaultPreparation);
            return response;
        };

        /**
         * @ngdoc method
         * @name getDatasets
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @description Return a promise that resolves the datasets list.
         * @returns {promise} The pending GET or resolved promise
         */
        self.getDatasets = function() {
            return self.datasetsList() ?
                $q.when(self.datasetsList()) :
                DatasetListService.refreshDatasets().then(consolidatePreparationsAndDatasets);
        };

        /**
         * @ngdoc method
         * @name refreshDatasets
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @description Refresh the dataset list
         * @returns {promise} The process promise
         */
        self.refreshDatasets = function() {
            return DatasetListService.refreshDatasets()
                .then(consolidatePreparationsAndDatasets);
        };

        /**
         * @ngdoc method
         * @name processCertification
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The target dataset for certification
         * @description Ask certification for a dataset
         * @returns {promise} The pending PUT promise
         */
        self.processCertification = function(dataset) {
            return DatasetListService.processCertification(dataset)
                .then(consolidatePreparationsAndDatasets);
        };

        /**
         * @ngdoc method
         * @name toggleFavorite
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} dataset The target dataset to set or unset favorite
         * @description Set or Unset the dataset as favorite
         * @returns {promise} The pending POST promise
         */
        self.toggleFavorite = function(dataset) {
            return DatasetRestService.toggleFavorite(dataset).then(function(){
                dataset.favorite = !dataset.favorite;
            });
        };


        /**
         * @ngdoc method
         * @name getDatasetByName
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {string} name The dataset name
         * @description Get the dataset that has the wanted name
         * @returns {object} The dataset
         */
        self.getDatasetByName = function(name) {
            return _.find(self.datasetsList(), function(dataset) {
                return dataset.name === name;
            });
        };

        /**
         * @ngdoc method
         * @name getDatasetById
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {string} datasetId The dataset id
         * @description Get the dataset that has the wanted id
         * @returns {promise} The dataset
         */
        self.getDatasetById = function(datasetId) {
            return DatasetListService.getDatasetsPromise().then( function(datasetList) {
                return _.find(datasetList, function(dataset) {
                    return dataset.id === datasetId;
                });
            });
        };

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Content----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {string} datasetId The dataset id
         * @param {boolean} metadata If false, the metadata will not be returned
         * @description Get a dataset content. It just call {@link data-prep.services.dataset.service:DatasetRestService DatasetRestService} getContent function
         * @returns {promise} The pending GET promise
         */
        self.getContent = DatasetRestService.getContent;

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Utils-----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createDatasetInfo
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @description create the dataset info from the given parameters.
         * @param {file} file - the file tu upload in case of
         * @param {string} name - the dataset name
         * @param {string} id - the dataset id (used to update existing dataset)
         * @returns {Object} - the adapted dataset infos {name: string, progress: number, file: *, error: boolean}
         */
        self.createDatasetInfo = function(file, name, id) {
            var info = {
                name: name,
                progress: 0,
                file: file,
                error: false,
                id: id,
                type: file === null ? 'remote' : 'file'
            };

            return info;
        };

        /**
         * @ngdoc method
         * @name getUniqueName
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {string} name - the base name
         * @description Get a unique name from a base name. The existence check is done on the local dataset list. It transform the base name, adding "(number)"
         * @returns {string} - the unique name
         */
        self.getUniqueName = function(name) {
            var cleanedName = name.replace(/\([0-9]+\)$/, '').trim();
            var result = cleanedName;

            var index = 1;
            while(self.getDatasetByName(result)) {
                result = cleanedName + ' (' + index + ')';
                index ++;
            }

            return result;
        };

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------Sheet Preview-------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getSheetPreview
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {object} metadata The dataset metadata
         * @param {string} sheetName The sheet name
         * @description Get a dataset sheet preview
         * @returns {object} The preview data
         */
        self.getSheetPreview = function(metadata, sheetName) {
            return DatasetRestService.getSheetPreview(metadata.id, sheetName);
        };

        /**
         * @ngdoc method
         * @name setDatasetSheet
         * @methodOf data-prep.services.dataset.service:DatasetService
         * @param {string} sheetName The sheet name
         * @description Set the selected sheet to the dataset
         * @returns {Promise} The process Promise
         */
        self.setDatasetSheet = function(metadata, sheetName) {
            metadata.sheetName = sheetName;
            return DatasetRestService.updateMetadata(metadata);
        };
    }

    angular.module('data-prep.services.dataset')
        .service('DatasetService', DatasetService);
})();