(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.transformation-form.controller:TransformClusterParamsCtrl
     * @description Transformation cluster parameters controller.
     */
    function TransformClusterParamsCtrl() {
        var vm = this;

        /**
         * @ngdoc property
         * @name allCheckboxState
         * @propertyOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description The global checkbox state
         */
        vm.allCheckboxState = true;

        /**
         * @ngdoc method
         * @name refreshClusterState
         * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description Refresh all cluster "active" flag to the state of the global activation checkbox
         */
        vm.refreshClusterState = function () {
            _.forEach(vm.details.clusters, function (cluster) {
                cluster.active = vm.allCheckboxState;
            });
        };

        /**
         * @ngdoc method
         * @name initClusterState
         * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description Initialize all cluster "active" flag
         */
        vm.initClusterState = function () {
            _.forEach(vm.details.clusters, function (cluster) {
                cluster.active = cluster.initialActive;
            });
        };

        /**
         * @ngdoc method
         * @name getParams
         * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description Refresh the global activation checkbox
         */
        vm.refreshToggleCheckbox = function () {
            var inactiveCluster = _.find(vm.details.clusters, function (cluster) {
                return !cluster.active;
            });
            vm.allCheckboxState = !inactiveCluster;
        };

        /**
         * @ngdoc method
         * @name initParamsValues
         * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description Initialize parameters values and checkbox state if needed
         */
        var initParamsValues = function initParamsValues() {
            _.forEach(vm.details.clusters, function (cluster) {
                _.forEach(cluster.parameters, function (param) {
                    param.default = true;
                });
            });
        };

        /**
         * @ngdoc method
         * @name initActivationFlags
         * @methodOf data-prep.transformation-form.controller:TransformClusterParamsCtrl
         * @description Initialize clusters activation flag
         */
        var initActivationFlags = function initActivationFlags() {
            var hasInitialActive = _.find(vm.details.clusters, function (cluster) {
                return typeof cluster.initialActive !== 'undefined';
            });

            if (!hasInitialActive) {
                vm.refreshClusterState();
            }
            else {
                vm.initClusterState();
            }
        };

        /**
         * @ngdoc method
         * @name initInputTypes
         * @methodOf data-prep.transformation-form.controller:TransformSimpleParamsCtrl
         * @description [PRIVATE] Init params input type, depending on param type
         */
        var initReplaceList = function () {
            _.forEach(vm.details.clusters, function (cluster) {
                cluster.replace.list = _.map(cluster.parameters, 'name');
            });
        };

        initParamsValues();
        initReplaceList();
        initActivationFlags();
    }

    angular.module('data-prep.transformation-form')
        .controller('TransformClusterParamsCtrl', TransformClusterParamsCtrl);
})();