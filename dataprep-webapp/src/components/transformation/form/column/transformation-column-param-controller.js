(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformColumnParamCtrl
     * @description Column parameter controller.
     * @requires data-prep.services.state.service:StateService
     */
    function TransformColumnParamCtrl(state) {

        var vm = this;

        vm.columns = [];

        /**
         * @ngdoc method
         * @name initColumns
         * @methodOf data-prep.transformation-form.controller:TransformColumnParamCtrl
         * @description [PRIVATE] Init column param values
         */
        var initColumns = function () {
            var currentColumn = state.playground.grid.selectedColumn;
            vm.columns = _.filter(state.playground.data.columns, function(column) {
                return currentColumn !== column;
            });
        };

        /**
         * @ngdoc method
         * @name initDefaultValue
         * @methodOf data-prep.transformation-form.controller:TransformColumnParamCtrl
         * @description [PRIVATE] Init select default value
         */
        var initDefaultValue = function () {
            if (vm.columns.length) {
                vm.parameter.value = vm.columns[0].id;
            }
        };

        initColumns();
        initDefaultValue();
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformColumnParamCtrl', TransformColumnParamCtrl);
})();