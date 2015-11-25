(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderService
     * @description Folder service. This service provide the entry point to the Folder service.
     * @requires data-prep.services.folder.service:FolderRestService
     * @requires data-prep.services.state.service:StateService
     */
    function FolderService(FolderRestService,StateService, DatasetListSortService, $translate) {

        return {
            // folder operations
            create: createFolder,
            renameFolder: renameFolder,
            getFolderContent: getFolderContent,

            // shared folder ui mngt
            buidStackFromId: buidStackFromId,
            populateMenuChilds: populateMenuChilds
        };

        //----------------------------------------------
        //   folders
        //----------------------------------------------

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
         * @name renameFolder
         * @methodOf data-prep.services.folder.service:FolderService
         * @description Rename a folder
         * @param {string} path the path to rename
         * @param {string} newPath the new path
         * @returns {Promise} The PUT promise
         */
        function renameFolder(path, newPath){
            return FolderRestService.renameFolder(path, newPath);
        }


        //----------------------------------------------
        //   shared ui management
        //----------------------------------------------

        /**
         * @ngdoc method
         * @name buidStackFromId
         * @methodOf data-prep.services.folder.service:FolderService
         * @description build the folder stack from the the given id
         * @param {string} the folder id
         */
        function buidStackFromId(folderId){

            var foldersStack = [];
            foldersStack.push({id:'', path:'', name: $translate.instant('HOME_FOLDER')});

            if(folderId) {
                var paths = folderId.split('/');
                for(var i = 1;i<=paths.length + 1;i++){
                    if(paths[i-1]) {
                        if ( i > 1 ) {
                            foldersStack.push({id: foldersStack[i - 1].id + '/' + paths[i-1], path: foldersStack[i - 1].id + '/' + paths[i-1], name: paths[i-1]});
                        } else {
                            foldersStack.push({id: paths[i-1], path: paths[i-1],name: paths[i-1]});
                        }
                    }
                }
            }

            StateService.setFoldersStack(foldersStack);
        }

        /**
         * @ngdoc method
         * @name populateMenuChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description build the child list of the part part given by the index parameter
         */
        function populateMenuChilds(folder){
            var promise = FolderRestService.getFolderContent(folder);

            promise.then(function(content){
                StateService.setMenuChilds(content.data.folders);
            });
            return promise;
        }

        /**
         * @ngdoc method
         * @name goToFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder to go
         */
        function getFolderContent(folder){

            var sort = DatasetListSortService.getSort();
            var order = DatasetListSortService.getOrder();
            var promise = FolderRestService.getFolderContent(folder, sort, order);

            promise.then(function(content){
                StateService.setCurrentFolder(folder? folder : {id:'', path:'', name: $translate.instant('HOME_FOLDER')});
                StateService.setCurrentFolderContent(content.data);
                buidStackFromId(folder? folder.id : '');
            });
            return promise;
        }

    }

    angular.module('data-prep.services.folder')
        .service('FolderService', FolderService);
})();