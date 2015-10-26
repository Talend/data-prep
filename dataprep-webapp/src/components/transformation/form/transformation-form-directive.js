(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.transformation-form.directive:TransformForm
     * @description This directive display a transformation parameters form
     * @restrict E
     * @usage
     <transform-form
             transformation="transformation"
             on-submit="callback()"
             on-submit-hover-on="callbackOn()"
             on-submit-hover-off="callbackOff()">
     </transform-form>
     * @param {object} transformation The transformation containing parameters
     * @param {function} onSubmit The callback executed on form submit
     * @param {function} onSubmitHoverOn The callback executed on mouseenter on form submit
     * @param {function} onSubmitHoverOff The callback executed on mouseleave on form submit
     */
    function TransformForm() {
        return {
            restrict: 'E',
            templateUrl: 'components/transformation/form/transformation-form.html',
            scope: {
                transformation: '=',
                onSubmit: '&',
                onSubmitHoverOn: '&',
                onSubmitHoverOff: '&'
            },
            bindToController: true,
            controllerAs: 'formCtrl',
            controller: 'TransformFormCtrl'
        };
    }

    angular.module('data-prep.transformation-form')
        .directive('transformForm', TransformForm);
})();