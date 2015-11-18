(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.folder.controller:FolderCtrl
     * @description Export controller.
     * @requires data-prep.services.folder.service:FolderService
     */
    function FolderCtrl(FolderService,StateService,DatasetService,state) {
        var vm = this;
        vm.contentType='';

        vm.currentPathParts=[];
        vm.folders=[];
        vm.loadingChilds=true;
        vm.state=state;

        vm.foldersStack=[];
        vm.childsList=[];


        /**
         * @ngdoc method
         * @name goToFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder to go
         */
        vm.goToFolder = function(folder){
            console.log('goToFolder:\''+folder.id+'\'');

            buidStackFromId(folder.id);

            console.log('goToFolder vm.foldersStack.length:\''+vm.foldersStack.length+'\'');

            StateService.setCurrentFolder(folder);


            loadFolders(folder);
            // loading folder entries
            if (folder.id){
                FolderService.listFolderEntries( 'dataset', folder.id )
                    .then(function(response){
                        DatasetService.filterDatasets(response.data);
                    })
                    .then(vm.initChilds(folder,true));
            } else {
                DatasetService.filterDatasets();
            }

        };

        /**
         * @ngdoc method
         * @name initChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description build the child list of the part part given by the index parameter
         */
        vm.initChilds = function(folder,setCurrentChilds){
            // 0 is the root folder
            console.log('initChilds, folder.id\''+folder.id+'\'');

            // special for root folder
            var currentPath = folder.id?folder.path:'';

            vm.loadingChilds=true;
            FolderService.folders(folder.id)
                 .then(function(response){
                     var foundChilds = cleanupPathFolderArray(response.data,currentPath);
                     console.log('foundChilds:'+foundChilds.length);
                     vm.childsList = foundChilds;
                     if(setCurrentChilds){
                         StateService.setCurrentChilds(vm.childsList);
                     }
                 })
                 .then(vm.loadingChilds=false);
        };

        var buidStackFromId = function(folderId){

            // folder.id can be:
            // foo/bar
            // foo
            // foo/
            // so parse that to generate the stack
            vm.foldersStack = [];
            // TODO root folder as a constant
            vm.foldersStack.push({id:'',path:'All files'});

            if(folderId) {
                var paths = folderId.split('/');
                console.log('buidStackFromId:'+folderId+',paths'+paths.length);

                for(var i = 0;i<=paths.length;i++){
                    if(paths[i]) {
                        if ( i > 0 ) {
                            vm.foldersStack.push( {id: vm.foldersStack[i - 1].path + '/' + paths[i], path: paths[i]} );
                        } else {
                            vm.foldersStack.push( {id: paths[i], path: paths[i]} );
                        }
                    }
                }
            }
        };

        var loadFolders = function(folder){
            FolderService.folders(state.folder.currentFolder.id)
                .then(function(folders){
                    // do not configure childs if it's the first loading
                    if(!folder) {
                        StateService.setCurrentChilds( cleanupPathFolderArray( folders.data, '' ));
                    }
                        // special case for root
                    if(!state.folder.currentFolder.id){
                        vm.foldersStack.push(state.folder.currentFolder);
                    }
                });
        };

        var cleanupPathFolderArray = function(folders,path){
            var cleaned = _.filter(folders,function(folder){
                console.log('before cleanupPathFolderArray:\''+folder.path+'\' with path:\''+path+'\'');
                if (folder.path){
                    folder.path = cleanupPath(folder.path.substring(path.length,folder.path.length));
                }
                console.log('after cleanupPathFolderArray:\''+folder.path+'\' with path:\''+path+'\'');
                return true;
            });
            return cleaned;
        };

        /**
         * @ngdoc method
         * @name cleanupPath
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {string} str - the path to clean
         * @description remove / character
         */
        var cleanupPath = function(str){
            return str.split('/').join('');
        };

        /**
         * Load folders on start
         */
        loadFolders();

    }

    angular.module('data-prep.folder')
        .controller('FolderCtrl', FolderCtrl);
})();