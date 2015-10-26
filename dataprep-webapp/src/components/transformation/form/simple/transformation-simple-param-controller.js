(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformSimpleParamCtrl
     * @description Simple transformation parameter controller.
     * @requires data-prep.services.utils.service:ConverterService
     */
    function TransformSimpleParamCtrl(ConverterService) {

        var vm = this;

        /**
         * @ngdoc method
         * @name initParamValues
         * @methodOf data-prep.transformation-form.controller:TransformSimpleParamCtrl
         * @description [PRIVATE] Init simple param values to default
         */
        var initParamValues = function () {
            if(typeof vm.parameter.initialValue !== 'undefined' && vm.parameter.initialValue !== null) {
                vm.parameter.initialValue = ConverterService.adaptValue(vm.parameter.type, vm.parameter.initialValue);
            }

            if (typeof vm.parameter.value !== 'undefined' && vm.parameter.value !== null) {
                vm.parameter.value = ConverterService.adaptValue(vm.parameter.type, vm.parameter.value);
            }
            else if (typeof vm.parameter.default !== 'undefined' && vm.parameter.default !== null) {
                vm.parameter.default = ConverterService.adaptValue(vm.parameter.type, vm.parameter.default);
                vm.parameter.value = vm.parameter.default;
            }
        };

        /**
         * @ngdoc method
         * @name initInputTypes
         * @methodOf data-prep.transformation-form.controller:TransformSimpleParamsCtrl
         * @description [PRIVATE] Init params input type, depending on param type
         */
        var initInputTypes = function() {
            vm.parameter.inputType = ConverterService.toInputType(vm.parameter.type);
        };

        initParamValues();
        initInputTypes();
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformSimpleParamCtrl', TransformSimpleParamCtrl);
})();