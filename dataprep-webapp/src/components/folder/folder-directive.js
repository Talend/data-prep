(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.datagrid.directive:Folder
     * @description This directive create the folder
     * @param type the type of folder entries to display (dataset or preparation)
     * @restrict E
     */
    function Folder() {
        return {
            templateUrl: 'components/folder/folder.html',
            restrict: 'E',
            bindToController: true,
            controllerAs: 'folderCtrl',
            controller: 'FolderCtrl',
            link: function(scope, iElement, iAttrs, folderCtrl) {
                folderCtrl.contentType=iAttrs.type;
            }
        };
    }

    angular.module('data-prep.folder')
        .directive('folder', Folder);
})();