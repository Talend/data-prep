(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.folder.controller:FolderCtrl
     * @description Export controller.
     * @requires data-prep.services.folder.service:FolderService
     */
    function FolderCtrl(FolderService,StateService,DatasetService) {
        var vm = this;
        vm.showAddModal = false;
        vm.contentType='';
        vm.currentFolder={id:''};
        vm.currentPathParts=[];
        vm.folders=[];
        vm.folderName='';
        vm.currentChilds=[];
        vm.loadingChilds=true;

        vm.addFolder = function(){
            FolderService.create(vm.currentFolder.id + '/' + vm.folderName)
                .then(vm.folderName='')
                .then(loadFolders);
        };

        vm.goToPath = function(index){
            // -1 is root
            var path = '';
            for(var i = 0; i<=index;i++){
                path = path + vm.currentPathParts[i] + '/';
            }
            var folder = {id:path,path: path};

            vm.goToFolder(folder);
        };

        vm.goToFolder = function(folder){
            vm.currentFolder=folder;
            loadFolders();
            // loading folder entries
            if (folder.path){
                FolderService.listFolderEntries( 'dataset', folder.id )
                    .then(function(response){
                        DatasetService.filterDatasets(response.data);
                    });
            }else{
                DatasetService.filterDatasets();
            }
            StateService.setCurrentFolder(folder.id);

        };

        // -1 is the root folder
        vm.initChilds = function(index){
            var path='';
            for(var i = 0; i<=index;i++){
                path = path + '/' + vm.currentPathParts[i];
            }
            vm.loadingChilds=true;
            FolderService.folders(path)
                 .then(function(response){
                   vm.currentChilds=_.forEach(response.data,function(folder){
                       if (folder.path){
                           folder.path = cleanupPath(folder.path.substring(path.length-1,folder.path.length));
                       }
                   });
                 })
                 .then(function(){
                     vm.loadingChilds=false;
                 });
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