(function () {
    'use strict';

    function DatasetTransformMenu() {
        return {
            restrict: 'E',
            templateUrl: 'components/dataset/grid/transform/dataset-transform-menu-directive.html',
            replace: true,
            scope: {
                metadata: '=',
                column: '=',
                menu: '=',
                columnIndex: '='
            },
            bindToController: true,
            controllerAs: 'menuCtrl',
            controller: 'DatasetTransformMenuCtrl'
        };
    }

    angular.module('data-prep-dataset')
        .directive('datasetTransformMenu', DatasetTransformMenu);
})();