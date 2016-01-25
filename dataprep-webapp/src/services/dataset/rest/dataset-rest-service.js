(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetRestService
     * @description Dataset service. This service provide the entry point to the backend dataset REST api.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for
     *     datasets</b>
     */
    function DatasetRestService ($rootScope, $upload, $http, RestURLs) {
        return {
            import: importRemoteDataset,
            create: create,
            update: update,
            delete: deleteDataset,
            clone: cloneDataset,
            move: moveDataset,

            updateColumn: updateColumn,

            getDatasets: getDatasets,
            updateMetadata: updateMetadata,
            getMetadata: getMetadata,
            getContent: getContent,
            getSheetPreview: getSheetPreview,

            processCertification: processCertification,
            toggleFavorite: toggleFavorite
        };

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Dataset----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Create the dataset
         * @param {dataset} dataset - the dataset infos to create
         * @param {object} folder - the dataset folder
         * @returns {Promise} the $upload promise
         */
        function create (dataset, folder) {
            var folderPath = folder && folder.id ? folder.id : '/';
            return $upload.http({
                url: RestURLs.datasetUrl + '?name=' + encodeURIComponent(dataset.name) + '&folderPath=' + encodeURIComponent(folderPath),
                headers: {'Content-Type': 'text/plain'},
                data: dataset.file
            });
        }

        /**
         * @ngdoc method
         * @name importRemoteDataset
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Import the remote dataset
         * @param {parameters} parameters The import parameters
         * @param {object} folder The dataset folder
         * @returns {Promise} The POST promise
         */
        function importRemoteDataset (parameters, folder) {
            var folderPath = folder && folder.id ? folder.id : '/';
            var req = {
                method: 'POST',
                url: RestURLs.datasetUrl + '?name=' + encodeURIComponent(parameters.name) + '&folderPath=' + encodeURIComponent(folderPath),
                headers: {
                    'Content-Type': 'application/vnd.remote-ds.' + parameters.type
                },
                data: parameters
            };
            return $http(req);
        }

        /**
         * @ngdoc method
         * @name update
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Update the dataset
         * @param {dataset} dataset - the dataset infos to update
         * @returns {Promise} the $upload promise
         */
        function update (dataset) {
            return $upload.http({
                url: RestURLs.datasetUrl + '/' + dataset.id + '?name=' + encodeURIComponent(dataset.name),
                method: 'PUT',
                headers: {'Content-Type': 'text/plain'},
                data: dataset.file
            });
        }

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Delete the dataset
         * @param {dataset} dataset the dataset infos to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteDataset (dataset) {
            return $http.delete(RestURLs.datasetUrl + '/' + dataset.id);
        }

        /**
         * @ngdoc method
         * @name cloneDataset
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Clone the dataset
         * @param {Object} dataset the dataset infos to clone
         * @param {Object} folder the folder to clone the dataset
         * @param {string} cloneName the name for the cloned dataset
         * @returns {HttpPromise} The GET promise
         */
        function cloneDataset (dataset, folder, cloneName) {
            var url = RestURLs.datasetUrl + '/clone/' + dataset.id;
            if (folder) {
                url += '?folderPath=' + encodeURIComponent(folder.id);
                if (cloneName) {
                    url += '&cloneName=' + encodeURIComponent(cloneName);
                }
            } else {
                if (cloneName) {
                    url += '?cloneName=' + encodeURIComponent(cloneName);
                }
            }
            return $http.put(url);
        }


        /**
         * @ngdoc method
         * @name moveDataset
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Move the dataset to an other Folder
         * @param {Object} dataset the dataset infos to move
         * @param {Object} folder the original folder of the dataset
         * @param {Object} newFolder the folder to move the dataset
         * @param {String} newName the name for the moved dataset (optional)
         * @returns {HttpPromise} The PUT promise
         */
        function moveDataset (dataset, folder, newFolder, newName) {
            var url = RestURLs.datasetUrl + '/move/' + dataset.id;

            return $http.put(url,
                {
                    folderPath: folder.id,
                    newFolderPath: newFolder.id,
                    newName: newName
                }
            );
        }


        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Metadata---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getDatasets
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @param {string} sortType Sort by specified type
         * @param {string} sortOrder Sort in specified order
         * @param {Promise} deferredAbort abort request when resolved
         * @description Get the dataset list
         * @returns {Promise} The GET call promise
         */
        function getDatasets (sortType, sortOrder, deferredAbort) {
            var url = RestURLs.datasetUrl;

            if (sortType) {
                url += '?sort=' + sortType;
            }
            if (sortOrder) {
                url += (sortType ? '&' : '?') + 'order=' + sortOrder;
            }

            return $http({
                url: url,
                method: 'GET',
                timeout: deferredAbort.promise
            });
        }

        /**
         * @ngdoc method
         * @name updateMetadata
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Update the dataset metadata
         * @param {dataset} metadata The dataset infos to update
         * @returns {Promise} The POST promise
         */
        function updateMetadata (metadata) {
            return $http.post(RestURLs.datasetUrl + '/' + metadata.id, metadata);
        }

        /**
         * @ngdoc method
         * @name processCertification
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Ask certification for a dataset
         * @param {string} datasetId The dataset id
         */
        function processCertification (datasetId) {
            return $http.put(RestURLs.datasetUrl + '/' + datasetId + '/processcertification');
        }


        /**
         * @ngdoc method
         * @name updateColumn
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Update the dataset column
         * @param {string} datasetId The dataset id
         * @param {string} columnId The column id
         * @param {object} params The parameters containing typeId and/or domainId
         * @returns {Promise} The POST promise
         */
        function updateColumn (datasetId, columnId, params) {
            var url = RestURLs.datasetUrl + '/' + datasetId + '/column/' + columnId;
            return $http.post(url, params);
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------Content----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Get the dataset content
         * @param {string} datasetId The dataset id
         * @param {boolean} metadata If false, the metadata will not be returned
         * @returns {Promise} The GET promise
         */
        function getContent (datasetId, metadata) {
            var url = RestURLs.datasetUrl + '/' + datasetId + '?metadata=' + metadata;
            return $http.get(url)
                .then(function (res) {
                    return res.data;
                });
        }

        /**
         * @ngdoc method
         * @name getMetadata
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Get the dataset metadata
         * @param {string} datasetId The dataset id
         * @returns {Promise} The GET promise
         */
        function getMetadata (datasetId) {
            var url = RestURLs.datasetUrl + '/' + datasetId + '/metadata';
            return $http.get(url)
                .then(function (res) {
                    return res.data;
                });
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------Sheet Preview-------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getSheetPreview
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Get the dataset content
         * @param {string} datasetId The dataset id
         * @param {string} sheetName The sheet to preview
         * @returns {Promise} The GET promise
         */
        function getSheetPreview (datasetId, sheetName) {
            $rootScope.$emit('talend.loading.start');
            return $http.get(RestURLs.datasetUrl + '/preview/' + datasetId + '?metadata=true' + (sheetName ? '&sheetName=' + encodeURIComponent(sheetName) : ''))
                .then(function (res) {
                    return res.data;
                })
                .finally(function () {
                    $rootScope.$emit('talend.loading.stop');
                });
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------Toggle Favorite-----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toggleFavorite
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Toggle the Favorite flag for a dataset for the current user
         * @param {dataset} dataset The dataset to be toggled
         * @returns {Promise} The PUT promise
         */
        function toggleFavorite (dataset) {
            return $http.post(RestURLs.datasetUrl + '/favorite/' + dataset.id + '?unset=' + dataset.favorite);
        }
    }

    angular.module('data-prep.services.dataset')
        .service('DatasetRestService', DatasetRestService);
})();