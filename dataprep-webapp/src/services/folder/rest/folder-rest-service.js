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
            // folder operations
            create: createFolder,
            delete: deleteFolder,
            folders: listFolderChilds,

            // folder entry operations
            createFolderEntry: createFolderEntry,
            deleteFolderEntry: deleteFolderEntry,
            listFolderEntries: listFolderEntries
        };

        //----------------------------------------------
        //   folders
        //----------------------------------------------
        /**
         * @ngdoc method
         * @name deleteFolder
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Delete the folder
         * @param {string} path the path to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteFolder(path) {
            return $http.delete(RestURLs.folderUrl + '?path=' + encodeURIComponent(path));
        }

        /**
         * @ngdoc method
         * @name createFolder
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Create a folder
         * @param {string} path the path to create
         * @returns {Promise} The PUT promise
         */
        function createFolder(path){
            return $http.put(RestURLs.folderUrl + '?path=' + encodeURIComponent(path));
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
            var url = RestURLs.folderUrl;
            if (path) {
                url += '?path=' + encodeURIComponent(path);
            }
            return $http.get(url);
        }

        //----------------------------------------------
        //   folder entries
        //----------------------------------------------
        /**
         * @ngdoc method
         * @name deleteFolderEntry
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Delete the folder entry
         * @param {string} contentType the content type to delete
         * @param {string} contentId the content id to delete
         * @param {string} path the path to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteFolderEntry(contentType, contentId, path) {
            var url = RestURLs.folderUrl + '/entries';
            url += '/'+contentType;
            url += '/'+contentId;
            if (path) {
                url += '?path=' + encodeURIComponent(path);
            }
            return $http.delete(url);
        }

        /**
         * @ngdoc method
         * @name createFolderEntry
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Create a folder entry
         * @param {string} contentType the content type to create
         * @param {string} contentId the content id to create
         * @param {string} path the path to create
         * @returns {Promise} The PUT promise
         */
        function createFolderEntry(contentType, contentId, path){
            var request = {
                method: 'PUT',
                url: RestURLs.folderUrl+ '/entries',
                data: {
                    contentType: contentType,
                    contentId: contentId,
                    path: path
                }
            };
            return $http(request);
        }

        /**
         * @ngdoc method
         * @name listFolderChilds
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description List the childs of a folder (or child of root folder)
         * @param {string} contentType the content type to create
         * @param {string} path optional path to list childs
         * @returns {Promise} The GET promise
         */
        function listFolderEntries(contentType,path){
            var url = RestURLs.folderUrl + '/entries';
            if (path) {
                url += '?path=' + encodeURIComponent(path);
            }
            if (contentType) {
                if (path) {
                    url += '&contentType=' + encodeURIComponent( contentType );
                } else {
                    url += '?contentType=' + encodeURIComponent( contentType );
                }
            }

            return $http.get(url);
        }


    }

    angular.module('data-prep.services.folder')
        .service('FolderRestService', FolderRestService);
})();