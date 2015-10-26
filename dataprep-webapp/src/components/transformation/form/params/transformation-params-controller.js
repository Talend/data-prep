(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformParamsCtrl
     * @description Transformation parameters controller.
     */
    function TransformParamsCtrl() {
        var vm = this;

        /**
         * @ngdoc method
         * @name getParameterType
         * @methodOf data-prep.transformation-form.controller:TransformParamsCtrl
         * @description Return the parameter type to display
         * @param {object} parameter The parameter
         */
        vm.getParameterType = function(parameter) {
            var type = parameter.type.toLowerCase();
            switch (type) {
                case 'select':
                case 'cluster':
                case 'date':
                case 'column':
                    return type;
                default:
                    return 'simple';
            }
        };

    }

    angular.module('data-prep.transformation-form')
        .controller('TransformParamsCtrl', TransformParamsCtrl);
})();