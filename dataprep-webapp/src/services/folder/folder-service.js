(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderService
     * @description Folder service. This service provide the entry point to the Folder service.
     * @requires data-prep.services.folder.service:FolderRestService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.dataset.service:DatasetService
     */
    function FolderService(FolderRestService,StateService,DatasetService,state) {

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
         * @param contentType the entry content type
         * @param contentId the id of the entry content type
         * @param {object} Folder the folder to create the entry
         * @returns {Promise} The PUT promise
         */
        function createFolderEntry(contentType, contentId, folder){
            return FolderRestService.createFolderEntry(contentType, contentId, folder.id);
        }

        /**
         * @ngdoc method
         * @name listFolderChilds
         * @methodOf data-prep.services.folder.service:FolderService
         * @description List the childs of a folder (or child of root folder)
         * @param {object} Folder the folder to list entries
         * @returns {Promise} The GET promise
         */
        function listFolderEntries(contentType,folder){
            return FolderRestService.listFolderEntries(contentType, folder.id);
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

            // folder.id can be:
            // foo/bar
            // foo
            // foo/
            // so parse that to generate the stack

            // TODO root folder as a constant
            var foldersStack = [];
            foldersStack.push({id:'',path:'Home'});

            if(folderId) {
                var paths = folderId.split('/');
                for(var i = 1;i<=paths.length + 1;i++){
                    if(paths[i-1]) {
                        if ( i > 1 ) {
                            foldersStack.push( {id: foldersStack[i - 1].id + '/' + paths[i-1], path: paths[i-1]} );
                        } else {
                            foldersStack.push( {id: paths[i-1], path: paths[i-1]} );
                        }
                    }
                }
            }

            StateService.setFoldersStack(foldersStack);
        }

        /**
         * @ngdoc method
         * @name loadFolders
         * @methodOf data-prep.services.folder.service:FolderService
         * @description build childs for root folder
         */
        function loadFolders(){

            FolderRestService.folders('')
                .then(function(folders){
                    StateService.setCurrentChilds( cleanupPathFolderArray( folders.data, '' ));
                    // build for root
                    buidStackFromId();
                    console.log('loadFolders:'+state.folder.foldersStack.length);
                });
        }

        /**
         * @ngdoc method
         * @name loadFolders
         * @methodOf data-prep.services.folder.service:FolderService
         * @param {array} array of Folder
         * @param {string} path - the origin path
         * @description cleanup the path for all folder in the array
         */
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
            //loadFolders(folder);
            // loading folder entries
            if (folder.id){
                listFolderEntries( 'dataset', folder )
                    .then(function(response){
                        DatasetService.filterDatasets(response.data);
                    })
                    .then(populateChilds(folder));
            } else {
                DatasetService.filterDatasets();
                populateChilds(folder);
            }

        }

    }

    angular.module('data-prep.services.folder')
        .service('FolderService', FolderService);
})();