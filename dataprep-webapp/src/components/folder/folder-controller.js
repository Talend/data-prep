(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.folder.controller:FolderCtrl
     * @description Export controller.
     * @requires data-prep.services.folder.service:FolderService
     */
    function FolderCtrl(FolderService,state) {
        var vm = this;
        vm.state = state;
        vm.contentType='';

        vm.loadingChilds=true;
        vm.state=state;


        /**
         * @ngdoc method
         * @name goToFolder
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder to go
         */
        vm.goToFolder = function(folder){
            FolderService.goToFolder(folder);
        };

        /**
         * @ngdoc method
         * @name initMenuChilds
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder - the folder
         * @description build the child list of the folder menu entry as parameter
         */
        vm.initMenuChilds = function(folder){
            vm.loadingChilds=true;
            FolderService.populateMenuChilds(folder)
                .then(vm.loadingChilds=false);
        };

        /**
         * Load folders on start
         */
        FolderService.loadFolders();



        Object.defineProperty(FolderCtrl.prototype,
            'foldersStack', {
                enumerable: true,
                configurable: false,
                get: function () {
                    return this.state.folder.foldersStack;
                }
            });

        Object.defineProperty(FolderCtrl.prototype,
            'menuChilds', {
                enumerable: true,
                configurable: false,
                get: function () {
                    return this.state.folder.menuChilds;
                }
            });

    }

    angular.module('data-prep.folder')
        .controller('FolderCtrl', FolderCtrl);
})();