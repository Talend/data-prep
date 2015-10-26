(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.transformation-form.directive:TransformColumnParam
     * @description This directive display a select parameter form to select a column
     * @restrict E
     * @usage
     <transform-params
             transformation="transformation"
             on-submit="callback()">

        <div ng-repeat="parameter in paramsCtrl.transformation.parameters track by $index" ng-switch="parameter.type">
            <transform-column-param
                ng-switch-when="column"
                parameter="parameter">
            </transform-column-param>
        </div>

     </transform-params>
     * @param {object} parameter The column parameter
     */
    function TransformColumnParam() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'components/transformation/form/column/transformation-column-param.html',
            scope: {
                parameter: '='
            },
            bindToController: true,
            controllerAs: 'columnParamCtrl',
            controller: 'TransformColumnParamCtrl'
        };
    }

    angular.module('data-prep.transformation-form')
        .directive('transformColumnParam', TransformColumnParam);
})();