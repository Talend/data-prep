(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:TransformationApplicationService
     * @description Manage parameters and apply transformations
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.state.constant:state
     * @requires data-prep.services.filters.service:FilterAdapterService
     */
    function TransformationApplicationService(PlaygroundService, state, FilterAdapterService) {
        return {
            append: append,
            appendClosure: appendClosure
        };

        /**
         * @ngdoc method
         * @name appendClosure
         * @methodOf data-prep.services.transformation.service:TransformationApplicationService
         * @description Transformation application closure. It take the transformation to build the closure.
         * The closure then takes the parameters and append the new step in the current preparation
         */
        function append(action, scope, params) {
            return appendClosure(action, scope)(params);
        }

        /**
         * @ngdoc method
         * @name appendClosure
         * @methodOf data-prep.services.transformation.service:TransformationApplicationService
         * @description Transformation application closure. It take the transformation to build the closure.
         * The closure then takes the parameters and append the new step in the current preparation
         */
        function appendClosure(action, scope) {
            /*jshint camelcase: false */
            return function (params) {
                var column = state.playground.grid.selectedColumn;

                params = params || {};
                params.scope = scope;
                params.column_id = column.id;
                params.column_name = column.name;

                if(state.playground.filter.applyTransformationOnFilters){
                    var stepFilters = FilterAdapterService.toTree(state.playground.filter.gridFilters);
                    _.extend(params, stepFilters);
                }

                return PlaygroundService.appendStep(action.name, params);
            };
        }

    }

    angular.module('data-prep.services.transformation')
        .service('TransformationApplicationService', TransformationApplicationService);
})();