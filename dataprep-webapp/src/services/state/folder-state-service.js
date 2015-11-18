(function() {
    'use strict';

    // currentFolder is initalized with root value
    var folderState = {
                        currentFolder:{id:'', path: 'All files'},
                        currentFolderChilds: []
                      };

    /**
     * @ngdoc service
     * @name data-prep.services.state.service:FolderStateService
     * @description Folder state service. Manage the folder state
     */
    function FolderStateService() {
        return {
            setCurrentFolder: setCurrentFolder,
            setCurrentChilds: setCurrentChilds
        };

        /**
         * @ngdoc method
         * @name setCurrentFolder
         * @methodOf data-prep.services.state.service:FolderStateService
         * @param {object} the current folder
         * @description Update the current folder
         */
        function setCurrentFolder(folder) {
            folderState.currentFolder = folder;
        }

        function setCurrentChilds(childs){
            folderState.currentFolderChilds = childs;
        }

    }

    angular.module('data-prep.services.state')
        .service('FolderStateService', FolderStateService)
        .constant('folderState', folderState);
})();