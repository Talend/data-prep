(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.dataset.service:DatasetRestService
     * @description Dataset service. This service provide the entry point to the backend dataset REST api.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.dataset.service:DatasetService DatasetService} must be the only entry point for datasets</b>
     */
    function DatasetRestService($rootScope, $upload, $http, RestURLs) {
        return {
            import: importRemoteDataset,
            create: create,
            update: update,
            delete: deleteDataset,
            clone: cloneDataset,

            updateColumn: updateColumn,

            getDatasets: getDatasets,
            updateMetadata: updateMetadata,
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
         * @returns {Promise} the $upload promise
         */
        function create(dataset) {
            return $upload.http({
                url: RestURLs.datasetUrl + '?name=' + encodeURIComponent(dataset.name),
                headers: {'Content-Type': 'text/plain'},
                data: dataset.file
            });
        }

        /**
         * @ngdoc method
         * @name importRemoteDataset
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Import the remote dataset
         * @param {parameters} the import parameters
         * @returns {Promise} the $post promise
         */
        function importRemoteDataset(parameters) {
            var req = {
                method: 'POST',
                url: RestURLs.datasetUrl + '?name=' + encodeURIComponent(parameters.name),
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
        function update(dataset) {
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
        function deleteDataset(dataset) {
            return $http.delete(RestURLs.datasetUrl + '/' + dataset.id);
        }

        /**
         * @ngdoc method
         * @name cloneDataset
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Clone the dataset
         * @param {dataset} dataset the dataset infos to delete
         * @param {string) the optional clone name
         * @returns {Promise} The GET promise
         */
        function cloneDataset(dataset, name){
            var url = RestURLs.datasetUrl + '/clone/' + dataset.id;
            if(name){
                url += '?name='+encodeURIComponent(name);
            }
            return $http.get(url);
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
        function getDatasets(sortType, sortOrder, deferredAbort) {
            var url = RestURLs.datasetUrl;

            if(sortType) {
                url += '?sort=' + sortType;
            }
            if(sortOrder) {
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
        function updateMetadata(metadata) {
            return $http.post(RestURLs.datasetUrl + '/' + metadata.id, metadata);
        }

        /**
         * @ngdoc method
         * @name processCertification
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Ask certification for a dataset
         * @param {string} datasetId The dataset id
         */
        function processCertification(datasetId) {
            return $http.put(RestURLs.datasetUrl + '/' + datasetId + '/processcertification');
        }


        /**
         * @ngdoc method
         * @name updateColumn
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Update the dataset column
         * @param {string} datasetId The dataset id
         * @param {string} columnId The column id
         * @param {object} params The parameters containing typeId and/or domainId
         * @returns {Promise} The POST promise
         */
        function updateColumn(datasetId, columnId, params) {
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
                .then(function(res) {
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
        function getSheetPreview(datasetId, sheetName) {
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
        //------------------------------------------------Toogle Favorite-------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toogleFavorite
         * @methodOf data-prep.services.dataset.service:DatasetRestService
         * @description Toogle the Favorite flag for a dataset for the current user
         * @param {dataset} dataset The dataset to be toggled
         * @returns {Promise} The PUT promise
         */
        function toggleFavorite(dataset) {
            return $http.post(RestURLs.datasetUrl + '/favorite/' + dataset.id + '?unset=' + dataset.favorite);
        }

    }

    angular.module('data-prep.services.dataset')
        .service('DatasetRestService', DatasetRestService);
})();