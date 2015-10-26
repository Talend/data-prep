(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformFormCtrl
     * @description Transformation parameters controller.
     */
    function TransformFormCtrl() {
        var vm = this;

        /**
         * @ngdoc method
         * @name getParamIteration
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description [PRIVATE] Inner function for recursively gather params
         * @param {object} paramsAccu The parameters values accumulator
         * @param {array} parameters The parameters array
         * @returns {object} The parameters
         */
        function getParamIteration(paramsAccu, parameters) {
            if (parameters) {
                _.forEach(parameters, function (paramItem) {
                    paramsAccu[paramItem.name] = typeof (paramItem.value) !== 'undefined'? paramItem.value : paramItem.default;

                    // deal with select inline parameters
                    if (paramItem.type === 'select') {
                        var selectedValue = _.find(paramItem.configuration.values, {value: paramItem.value});
                        getParamIteration(paramsAccu, selectedValue.parameters);
                    }
                });
            }
            return paramsAccu;
        }

        /**
         * @ngdoc method
         * @name getParams
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description [PRIVATE] Get item parameters into one object for REST call
         * @returns {object} - the parameters
         */
        var getParams = function () {
            return getParamIteration({}, vm.transformation.parameters);
        };

        /**
         * @ngdoc method
         * @name getClusterParams
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description [PRIVATE] Get cluster choice and choice parameters into one object for REST call
         * @returns {object} - the parameters
         */
        var getClusterParams = function() {
            var params = {};
            if (vm.transformation.cluster) {
                _.chain(vm.transformation.cluster.clusters)
                    .filter('active')
                    .forEach(function (cluster) {
                        var replaceValue = cluster.replace.value;
                        _.forEach(cluster.parameters, function(param) {
                            if(param.value) {
                                params[param.name] = replaceValue;
                            }
                        });
                    })
                    .value();
            }

            return params;
        };

        /**
         * @ngdoc method
         * @name gatherParams
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description [PRIVATE] Gather params into one unique object
         * @returns {object} The entire parameter values
         */
        var gatherParams = function() {
            var params = getParams();
            var clusterParams = getClusterParams();
            return _.merge(params, clusterParams);
        };

        /**
         * @ngdoc method
         * @name transformWithParam
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description Gather params and perform a transformation on the column if the form is valid
         */
        vm.transformWithParam = function () {
            var transformationParams = gatherParams();
            vm.onSubmit({params: transformationParams});
        };

        /**
         * @ngdoc method
         * @name submitHoverOn
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description Gather params and perform the submit mouseenter action
         */
        vm.submitHoverOn = function() {
            var params = gatherParams();
            vm.onSubmitHoverOn({params: params});
        };

        /**
         * @ngdoc method
         * @name submitHoverOff
         * @methodOf data-prep.transformation-form.controller:TransformFormCtrl
         * @description Gather params and perform the submit mouseleave action
         */
        vm.submitHoverOff = function() {
            var params = gatherParams();
            vm.onSubmitHoverOff({params: params});
        };
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformFormCtrl', TransformFormCtrl);
})();