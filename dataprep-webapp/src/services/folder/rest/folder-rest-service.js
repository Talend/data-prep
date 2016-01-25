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
            children: children,
            create: create,
            getContent: getContent,
            rename: rename,
            remove: remove,
            search: search
        };

        /**
         * @ngdoc method
         * @name children
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Get a folder's children
         * @param {string} folderPath The folder path
         * @returns {Promise} The GET promise
         */
        function children(folderPath) {
            var url = RestURLs.folderUrl;
            if (folderPath) {
                url += '?path=' + encodeURIComponent(folderPath);
            }
            return $http.get(url);
        }

        /**
         * @ngdoc method
         * @name create
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Create a folder
         * @param {string} folderPath The folder path to create
         * @returns {Promise} The PUT promise
         */
        function create(folderPath) {
            return $http.put(RestURLs.folderUrl + '?path=' + encodeURIComponent(folderPath));
        }

        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description List the children (folders and datasets) of a folder
         * @param {string} folderPath The folder path to list
         * @param {string} sortType Sort by specified type
         * @param {string} sortOrder Sort in specified order
         * @returns {Promise} The GET promise
         */
        function getContent(folderPath, sortType, sortOrder) {
            var url = RestURLs.folderUrl + '/datasets?folder=' + encodeURIComponent(folderPath || '/');

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
         * @name remove
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Remove a folder
         * @param {string} folderPath the path to remove
         * @returns {Promise} The DELETE promise
         */
        function remove(folderPath) {
            return $http.delete(RestURLs.folderUrl + '?path=' + encodeURIComponent(folderPath));
        }

        /**
         * @ngdoc method
         * @name rename
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Rename a folder
         * @param {string} folderPath The folder path to rename
         * @param {string} newPath The new path
         * @returns {Promise} The PUT promise
         */
        function rename(folderPath, newPath) {
            return $http.put(RestURLs.folderUrl + '/rename?path=' + encodeURIComponent(folderPath) + '&newPath=' + encodeURIComponent(newPath));
        }

        /**
         * @ngdoc method
         * @name search
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Search folders with a part of the name
         * @param {string} query The part of the name to search
         * @returns {Promise} The GET promise
         */
        function search(query) {
            var url = RestURLs.folderUrl + '/search';
            if (query) {
                url += '?pathName=' + encodeURIComponent(query);
            }
            return $http.get(url);
        }
    }

    angular.module('data-prep.services.folder')
        .service('FolderRestService', FolderRestService);
})();