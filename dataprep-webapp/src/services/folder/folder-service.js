(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderService
     * @description Folder service. This service provide the entry point to the Folder service.
     * @requires data-prep.services.folder.service:FolderRestService
     */
    function FolderService(FolderRestService,StateService,state,DatasetService) {

        return {
            // folder operations
            create: createFolder,
            delete: deleteFolder,
            folders: listFolderChilds,

            // folder entry operations
            createFolderEntry: createFolderEntry,
            deleteFolderEntry: deleteFolderEntry,
            listFolderEntries: listFolderEntries,

            // shared folder ui mngt
            buidStackFromId: buidStackFromId,
            loadFolders: loadFolders,
            cleanupPathFolderArray: cleanupPathFolderArray,
            populateChilds: populateChilds,
            goToFolder: goToFolder,
            populateMenuChilds: populateMenuChilds
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


        //----------------------------------------------
        //   shared ui management
        //----------------------------------------------

        function buidStackFromId(folderId){

            // folder.id can be:
            // foo/bar
            // foo
            // foo/
            // so parse that to generate the stack

            // TODO root folder as a constant
            var foldersStack = [];
            foldersStack.push({id:'',path:'All files'});

            if(folderId) {
                var paths = folderId.split('/');
                for(var i = 0;i<=paths.length;i++){
                    if(paths[i]) {
                        if ( i > 0 ) {
                            foldersStack.push( {id: foldersStack[i - 1].path + '/' + paths[i], path: paths[i]} );
                        } else {
                            foldersStack.push( {id: paths[i], path: paths[i]} );
                        }
                    }
                }
            }

            StateService.setFoldersStack(foldersStack);
        }

        function loadFolders(folder,loadController){
            FolderRestService.folders(state.folder.currentFolder.id)
                .then(function(folders){
                    // do not configure childs if it's the first loading
                    if(!folder) {
                        StateService.setCurrentChilds( cleanupPathFolderArray( folders.data, '' ));
                    }
                    // special case for root and first time loading
                    if(!state.folder.currentFolder.id && loadController){
                        var foldersStack = [];
                        foldersStack.push(state.folder.currentFolder);
                        StateService.setFoldersStack(foldersStack);
                    }
                });
        }

        function cleanupPathFolderArray(folders,path){
            _.forEach(folders,function(folder){
                if (folder.path){
                    folder.path = cleanupPath(folder.path.substring(path.length,folder.path.length));
                }

            });
            return folders;
        }

        /**
         * @ngdoc method
         * @name cleanupPath
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {string} str - the path to clean
         * @description remove / character
         */
        function cleanupPath(str){
            return str.split('/').join('');
        }

        /**
         * @ngdoc method
         * @name populateChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description build the child list of the part part given by the index parameter
         */
        function populateChilds(folder){
            // special for root folder
            console.log('populateChilds:'+folder.id);
            var currentPath = folder.id?folder.path:'';
            var promise = FolderRestService.folders(folder.id);

            promise.then(function(response){
                    var foundChilds = cleanupPathFolderArray(response.data,currentPath);
                    StateService.setCurrentChilds(foundChilds);
                });

            return promise;
        }

        /**
         * @ngdoc method
         * @name populateMenuChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description build the child list of the part part given by the index parameter
         */
        function populateMenuChilds(folder){
            // special for root folder
            console.log('initMenuChilds:'+folder.id);
            var currentPath = folder.id?folder.path:'';
            var promise = FolderRestService.folders(folder.id);

            promise.then(function(response){
                var foundChilds = cleanupPathFolderArray(response.data,currentPath);
                StateService.setMenuChilds(foundChilds);
            });

            return promise;
        }

        /**
         * @ngdoc method
         * @name goToFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder to go
         */
        function goToFolder(folder){
            buidStackFromId(folder.id);
            StateService.setCurrentFolder(folder);
            loadFolders(folder);
            // loading folder entries
            if (folder.id){
                listFolderEntries( 'dataset', folder.id )
                    .then(function(response){
                        DatasetService.filterDatasets(response.data);
                    })
                    .then(populateChilds(folder,true));
            } else {
                DatasetService.filterDatasets();
                populateChilds(folder);
            }

        }

    }

    angular.module('data-prep.services.folder')
        .service('FolderService', FolderService);
})();