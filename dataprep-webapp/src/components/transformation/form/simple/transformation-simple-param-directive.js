(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.transformation-form.directive:TransformSimpleParam
     * @description This directive display a simple input parameter form
     * @restrict E
     * @usage
     <transform-params
             transformation="transformation"
             on-submit="callback()">

        <div ng-repeat="parameter in paramsCtrl.transformation.parameters track by $index" ng-switch="parameter.type">
            <transform-simple-param
                ng-switch-when="simple"
                parameter="parameter">
            </transform-simple-param>
        </div>

     </transform-params>
     * @param {object} parameter The simple parameter
     * @param {object} label Do NOT display label if 'false'. Display it otherwise (by default).
     * @param {boolean} editableSelect If this parameter is an editable-select
     */
    function TransformSimpleParam() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'components/transformation/form/simple/transformation-simple-param.html',
            scope: {
                editableSelect: '=',
                parameter: '=',
                label: '@'
            },
            bindToController: true,
            controllerAs: 'simpleParamCtrl',
            controller: 'TransformSimpleParamCtrl'
        };
    }

    angular.module('data-prep.transformation-form')
        .directive('transformSimpleParam', TransformSimpleParam);
})();