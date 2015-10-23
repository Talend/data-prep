(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderRestService
     * @description Folder service. This service provide the entry point to the backend folder REST api.<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly.
     * {@link data-prep.services.folder.service:FolderService FolderService} must be the only entry point for folder</b>
     */
    function FolderRestService($http, RestURLs) {
        return {
            create: createFolder,
            delete: deleteFolder,

            childs: listFolderChilds
        };

        /**
         * @ngdoc method
         * @name deleteFolder
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Delete the folder
         * @param {string} path the path to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteFolder(path) {
            return $http.delete(RestURLs.datasetUrl + '/folders?path=' + encodeURIComponent(path));
        }

        /**
         * @ngdoc method
         * @name createFolder
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Create a folder
         * @param {string} path the path to create
         * @returns {Promise} The GET promise
         */
        function createFolder(path){
            return $http.get(RestURLs.datasetUrl + '/folders/add?path=' + encodeURIComponent(path));
        }

        /**
         * @ngdoc method
         * @name listFolderChilds
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description List the childs of a folder (or child of root folder)
         * @param {string} path optional path to list childs
         * @returns {Promise} The GET promise
         */
        function listFolderChilds(path){
            var url = RestURLs.datasetUrl + '/folders';
            if (path) {
                url += '?path=' + encodeURIComponent(path);
            }
            return $http.get(url);
        }

    }

    angular.module('data-prep.services.folder')
        .service('FolderRestService', FolderRestService);
})();