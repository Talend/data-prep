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
            getFolderContent: getFolderContent,
            renameFolder: renameFolder
        };

        //----------------------------------------------
        //   folders
        //----------------------------------------------

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
         * @name getFolderContent
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description List the childs (folders or datasets) of a folder (or child of root folder)
         * @param {object} folder - the current folder
         * @param {string} sortType Sort by specified type
         * @param {string} sortOrder Sort in specified order
         * @returns {Promise} The GET promise
         */
        function getFolderContent(folder, sortType, sortOrder){
            var url = RestURLs.folderUrl + '/datasets';
            if (folder && folder.id) {
                url += '?folder=' + encodeURIComponent(folder.id);
            } else {
                url += '?folder='+encodeURIComponent('/');
            }

            if (sortType) {
                url += '&sort=' + sortType;
            }
            if (sortOrder) {
                url += '&order=' + sortOrder;
            }

            return $http.get(url);
        }

        /**
         * @ngdoc method
         * @name renameFolder
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Rename a folder
         * @param {string} path the path to rename
         * @param {string} newPath the new path
         * @returns {Promise} The PUT promise
         */
        function renameFolder(path, newPath){
            return $http.put(RestURLs.folderUrl + '/rename?path=' + encodeURIComponent(path) + '&newPath=' + encodeURIComponent(newPath) );
        }
    }

    angular.module('data-prep.services.folder')
        .service('FolderRestService', FolderRestService);
})();