(function() {
    'use strict';

    function DatasetColumnHeader() {
        return {
            restrict: 'E',
            templateUrl: 'components/dataset/grid/header/dataset-column-header-directive.html',
            scope:{
                metadata: '=',
                column : '=',
                columnIndex: '='
            },
            bindToController: true,
            controllerAs: 'datasetHeaderCtrl',
            controller: 'DatasetColumnHeaderCtrl',
            link: {
                post: function(scope, iElement, iAttrs, ctrl) {
                    ctrl.refreshQualityBar();
                }
            }
        };
    }

    angular.module('data-prep-dataset')
        .directive('datasetColumnHeader', DatasetColumnHeader);
})();
