(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformRegexParamCtrl
     * @description Regex transformation parameter controller.
     */
    function TransformRegexParamCtrl() {

        var vm = this;

        /**
         * @ngdoc method
         * @name initParamValues
         * @methodOf data-prep.transformation-form.controller:TransformRegexParamCtrl
         * @description [PRIVATE] Init param values to default
         */
        var initParamValues = function () {
            if(! angular.isDefined(vm.parameter.value) && angular.isDefined(vm.parameter.default)) {
                vm.parameter.value = vm.parameter.default;
            }
        };

        initParamValues();
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformRegexParamCtrl', TransformRegexParamCtrl);
})();