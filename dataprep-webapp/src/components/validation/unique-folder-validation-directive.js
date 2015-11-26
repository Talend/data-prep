(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.validation.directive:UniqueFolderValidation
     * @description This directive perform checks if a folder name is unique
     * @restrict E
     * @usage
     <input
     ...
     unique-folder="folders" />
     * @param {string} folders List of existing folders
     */
    function UniqueFolderValidation() {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                uniqueFolder: '='
            },
            link: function(scope, elm, attrs, ctrl) {
                ctrl.$validators.uniqueFolderValidation = function(name) {
                    if(name === '') {
                        return false;
                    }
                   return !_.find(scope.uniqueFolder, function(folder){
                        return folder.name.toLowerCase() === name.toLowerCase();
                    });
                };
            }
        };
    }

    angular.module('data-prep.validation')
        .directive('uniqueFolder', UniqueFolderValidation);
})();