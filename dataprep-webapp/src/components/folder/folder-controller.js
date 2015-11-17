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
        vm.showAddModal = false;
        vm.contentType='';
        vm.currentFolder={id:''};
        vm.currentPathParts=[];
        vm.folders=[];
        vm.folderName='';
        vm.loadingChilds=true;
        vm.state=state;

        /**
         * @ngdoc method
         * @name addFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description Create a new folder
         */
        vm.addFolder = function(){
            FolderService.create(vm.currentFolder.id + '/' + vm.folderName)
                .then(vm.folderName='')
                .then(loadFolders);
        };

        /**
         * @ngdoc method
         * @name goToPath
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {number} index - the index of the part of the current path parts to go to
         * @description go to path of the given index
         */
        vm.goToPath = function(index){
            // -1 is root
            var path = '';
            for(var i = 0; i<=index;i++){
                path = path + vm.currentPathParts[i] + '/';
            }
            var folder = {id:path,path: path};

            vm.goToFolder(folder,index);
        };

        /**
         * @ngdoc method
         * @name goToFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder to go
         */
        vm.goToFolder = function(folder,index){
            vm.currentFolder=folder;
            loadFolders();
            // loading folder entries
            if (folder.path){
                FolderService.listFolderEntries( 'dataset', folder.id )
                    .then(function(response){
                        DatasetService.filterDatasets(response.data);
                    })
                    .then(vm.initChilds(index));
            }else{
                DatasetService.filterDatasets();
            }
            StateService.setCurrentFolder(folder.id);

        };

        /**
         * @ngdoc method
         * @name initChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {number} index - the index of the part of the current path parts to get childs
         * @description build the child list of the part part given by the index parameter
         */
        vm.initChilds = function(index){
            // -1 is the root folder
            var path='';
            for(var i = 0; i<=index;i++){
                path = path + '/' + vm.currentPathParts[i];
            }
            vm.loadingChilds=true;
            FolderService.folders(path)
                 .then(function(response){
                     StateService.setCurrentChilds (_.forEach(response.data,function(folder){
                       if (folder.path){
                           folder.path = cleanupPath(folder.path.substring(path.length-1,folder.path.length));
                       }
                   }));
                 })
                 .then(vm.loadingChilds=false);
        };

        var loadFolders = function(){
            FolderService.folders(vm.currentFolder.id)
                .then(function(folders){
                    vm.folders=_.forEach(folders.data,function(folder){
                        if (folder.path){
                            // we remove the current path from the path to display only subfolder path
                            folder.path = cleanupPath(folder.path.substring(vm.currentFolder.id.length-1,folder.path.length));
                        }
                    });
                    vm.currentPathParts = _.filter(_.trim(vm.currentFolder.id).split('/'),function(path){
                        return path.length>0;
                    });
                });
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

    /**
     * @ngdoc property
     * @name currentChilds
     * @propertyOf data-prep.folder.controller:FolderCtrl
     * @description The childs list.
     * This list is bound to {@link data-prep.services.state.service:FolderStateService}.folderState.currentChilds
     */
    Object.defineProperty(FolderCtrl.prototype,
        'currentChilds', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.state.folder.currentChilds;
                }
        });

    angular.module('data-prep.folder')
        .controller('FolderCtrl', FolderCtrl);
})();