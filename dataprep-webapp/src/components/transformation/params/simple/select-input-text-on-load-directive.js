(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.transformation-params.directive:SelectInputTextOnLoad
     * @description This directive set focus and select the old text in the input
     * @restrict A
     * @usage
     <select-input-text-on-load>
     */
    function SelectInputTextOnLoad($timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                $timeout(function () {
                    element[0].focus();
                    element[0].select();
                },0);
            }
        }
    }

    angular.module('data-prep.transformation-params')
        .directive('selectInputTextOnLoad', SelectInputTextOnLoad);
})();