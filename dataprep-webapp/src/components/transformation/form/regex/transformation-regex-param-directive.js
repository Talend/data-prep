(function() {
    'use strict';

    function TransformRegexParam() {
        return {
            restrict: 'E',
            templateUrl: 'components/transformation/form/regex/transformation-regex-param.html',
            scope: {
                parameter: '='
            },
            bindToController: true,
            controllerAs: 'regexParamCtrl',
            controller: 'TransformRegexParamCtrl'
        };
    }

    angular.module('data-prep.transformation-form')
        .directive('transformRegexParam', TransformRegexParam);
})();