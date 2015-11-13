(function() {
    'use strict';

    var folderState = {currentFolder:''};

    /**
     * @ngdoc service
     * @name data-prep.services.state.service:FolderStateService
     * @description Folder state service. Manage the folder state
     */
    function FolderStateService() {
        return {
            setCurrentFolder: setCurrentFolder
        };

        /**
         * @ngdoc method
         * @name setCurrentFolder
         * @methodOf data-prep.services.state.service:FolderStateService
         * @param {string} the current folder
         * @description Update the current folder
         */
        function setCurrentFolder(folder) {
            folderState.currentFolder = folder;
        }

    }

    angular.module('data-prep.services.state')
        .service('FolderStateService', FolderStateService)
        .constant('folderState', folderState);
})();