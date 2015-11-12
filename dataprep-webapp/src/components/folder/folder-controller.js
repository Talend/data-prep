(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.folder.controller:FolderCtrl
     * @description Export controller.
     * @requires data-prep.services.folder.service:FolderService
     */
    function FolderCtrl(FolderService) {
        var vm = this;
        vm.showAddModal = false;
        vm.contentType='';
        vm.currentFolder={id:''};
        vm.currentPathParts=[];
        vm.folders=[];
        vm.folderName='';
        vm.currentChilds=[];

        vm.onTextClick = function() {
          console.log('onTextClick, contentType:'+vm.contentType);
        };

        vm.addFolder = function(){
            console.log('addFolder:'+vm.folderName);
            FolderService.create(vm.currentFolder.id + '/' + vm.folderName)
                .then(vm.folderName='')
                .then(loadFolders);
        };

        vm.goToChild = function(folder){
            console.log('goToChild:'+folder.path);
            vm.currentFolder=folder;
            loadFolders();
        };


        // -1 is the root folder
        vm.initChilds = function(index){

            var path='';
            for(var i = 0; i<=index;i++){
                path = path + '/' + vm.currentPathParts[i];
            }

            console.log('initChilds:'+index+',path:'+path);

            FolderService.folders(path)
                 .then(function(response){
                   vm.currentChilds=_.forEach(response.data,function(folder){
                       if (folder.path){
                           folder.path = folder.path.substring(path.length-1,folder.path.length);
                       }
                 })});
        };

        var loadFolders = function(){
            FolderService.folders(vm.currentFolder.id)
                .then(function(folders){
                    vm.folders=_.forEach(folders.data,function(folder){
                        if (folder.path){
                            // we remove the current path from the path to display only subfolder path
                            folder.path = folder.path.substring(vm.currentFolder.id.length-1,folder.path.length);
                        }
                    });
                    vm.currentPathParts = vm.currentFolder.id.split( '/' );
                    console.log('vm.currentPathParts.length:'+vm.currentPathParts.length)
                });
        };

        /**
         * Load folders
         */
        loadFolders();

    }

    angular.module('data-prep.folder')
        .controller('FolderCtrl', FolderCtrl);
})();