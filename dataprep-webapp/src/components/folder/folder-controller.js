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
        vm.currentPath='';
        vm.folders=[];
        vm.folderName='';

        vm.onTextClick = function() {
          console.log('onTextClick, contentType:'+vm.contentType);
        };

        vm.addFolder = function(){
            console.log('addFolder:'+vm.folderName);
            FolderService.create(vm.folderName).then(vm.folder.name='');
        };

        vm.displayRootChilds = function(){
            console.log('displayRootChilds'+vm.folders.length);
        };

        vm.goToChild = function(folder){
            console.log('goToChild:'+folder.path);
        };

        var loadFolders = function(folders){
            vm.folders=folders.data;
        };

        /**
         * Load folders
         */
        FolderService.folders(vm.currentPath).then(loadFolders);

    }

    angular.module('data-prep.folder')
        .controller('FolderCtrl', FolderCtrl);
})();