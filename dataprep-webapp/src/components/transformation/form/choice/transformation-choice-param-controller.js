(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformChoiceParamCtrl
     * @description Transformation choices controller.
     */
    function TransformChoiceParamCtrl() {
        var vm = this;

        /**
         * @ngdoc method
         * @name initParamValues
         * @methodOf data-prep.transformation-form.controller:TransformChoiceParamCtrl
         * @description [PRIVATE] Init choice element default value (either the parameter default value or the first value in the select)
         */
        var initParamValues = function () {

            if (!vm.parameter.value) {
                // init with the default value
                if (vm.parameter.default) {
                    var defaultValue = _.find(vm.parameter.configuration.values, {value: vm.parameter.default});
                    vm.parameter.value = defaultValue.value;
                }
                // or with the first value in the list
                else {
                    vm.parameter.value = vm.parameter.configuration.values[0].value;
                }
            }

        };

        initParamValues();
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformChoiceParamCtrl', TransformChoiceParamCtrl);
})();