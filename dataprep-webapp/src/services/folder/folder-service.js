(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderService
     * @description Folder service. This service provide the entry point to the Folder service.
     * @requires data-prep.services.folder.service:FolderRestService
     */
    function FolderService(FolderRestService) {
        return {
            // folder operations
            create: createFolder,
            delete: deleteFolder,
            folders: listFolderChilds,

            // folder entry operations
            createFolderEntry: createFolderEntry,
            deleteFolderEntry: deleteFolderEntry,
            listFolderEntries: listFolderEntries,

        };

        //----------------------------------------------
        //   folders
        //----------------------------------------------
        /**
         * @ngdoc method
         * @name deleteFolder
         * @methodOf data-prep.services.folder.service:FolderService
         * @description Delete the folder
         * @param {string} path the path to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteFolder(path) {
            return FolderRestService.delete(path);
        }

        /**
         * @ngdoc method
         * @name createFolder
         * @methodOf data-prep.services.folder.service:FolderService
         * @description Create a folder
         * @param {string} path the path to create
         * @returns {Promise} The PUT promise
         */
        function createFolder(path){
            return FolderRestService.create(path);
        }

        /**
         * @ngdoc method
         * @name listFolderChilds
         * @methodOf data-prep.services.folder.service:FolderService
         * @description List the childs of a folder (or child of root folder)
         * @param {string} path optional path to list childs
         * @returns {Promise} The GET promise
         */
        function listFolderChilds(path){
            return FolderRestService.folders(path);
        }

        //----------------------------------------------
        //   folder entries
        //----------------------------------------------
        /**
         * @ngdoc method
         * @name deleteFolderEntry
         * @methodOf data-prep.services.folder.service:FolderService
         * @description Delete the folder entry
         * @param {string} contentType the content type
         * @param {string} contentId the content id
         * @param {string} path the path to delete
         * @returns {Promise} The DELETE promise
         */
        function deleteFolderEntry(contentType, contentId, path) {
            return FolderRestService.deleteFolderEntry(contentType, contentId, path);
        }

        /**
         * @ngdoc method
         * @name createFolderEntry
         * @methodOf data-prep.services.folder.service:FolderRestService
         * @description Create a folder
         * @param {string} path the path to create
         * @returns {Promise} The PUT promise
         */
        function createFolderEntry(contentType, contentId, path){
            return FolderRestService.createFolderEntry(contentType, contentId, path);
        }

        /**
         * @ngdoc method
         * @name listFolderChilds
         * @methodOf data-prep.services.folder.service:FolderService
         * @description List the childs of a folder (or child of root folder)
         * @param {string} path optional path to list childs
         * @returns {Promise} The GET promise
         */
        function listFolderEntries(contentType,path){
            return FolderRestService.listFolderEntries(contentType, path);
        }


    }

    angular.module('data-prep.services.folder')
        .service('FolderService', FolderService);
})();