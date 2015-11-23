(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.datagrid-header.controller:DatagridHeaderCtrl
     * @description Dataset Column Header controller.
     * @requires data-prep.services.transformation.service:TransformationCacheService
     * @requires data-prep.services.utils.service:ConverterService
     * @requires data-prep.services.playground.service:PlaygroundService
     */
    function DatagridHeaderCtrl(TransformationCacheService, ConverterService, PlaygroundService) {
        var ACTION_SCOPE = 'column_metadata';
        var RENAME_ACTION = 'rename_column';
        var originalName;

        var vm = this;
        vm.converterService = ConverterService;

        /**
         * @ngdoc property
         * @name newName
         * @propertyOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description the new column modified name
         * @type {string}
         */
        vm.newName = null;

        /**
         * @ngdoc property
         * @name isEditMode
         * @propertyOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description the flag to switch column name edition mode
         * @type {string}
         */
        vm.isEditMode = false;

        /**
         * @ngdoc property
         * @name rawTransformations
         * @propertyOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description a copy of the initial transformations without changed parameters
         * @type {Array}
         */
        vm.rawTransformations = [];

        /**
         * @ngdoc method
         * @name initTransformations
         * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description Get transformations from REST call
         */
        vm.initTransformations = function initTransformations() {
            if (!vm.transformations && !vm.initTransformationsInProgress) {
                vm.transformationsRetrieveError = false;
                vm.initTransformationsInProgress = true;

                TransformationCacheService.getTransformations(vm.column, true)
                    .then(function(menus) {
                        vm.transformations = _.filter(menus, function(menu) {
                            return (menu.actionScope.indexOf(ACTION_SCOPE) !== -1);
                        });
                    })
                    .catch(function() {
                        vm.transformationsRetrieveError = true;
                    })
                    .finally(function() {
                        vm.initTransformationsInProgress = false;
                    });
            }
        };

        /**
         * @ngdoc method
         * @name updateColumnName
         * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description update the new column name
         */
        vm.updateColumnName = function updateColumnName() {
            var params = {
                /*jshint camelcase: false */
                new_column_name: vm.newName,
                scope: 'column',
                column_id: vm.column.id,
                column_name: vm.column.name
            };

            PlaygroundService.appendStep(RENAME_ACTION, params)
                .then(function() {
                    vm.setEditMode(false);
                    originalName = vm.newName;
                });
        };

        /**
         * @ngdoc method
         * @name nameHasChanged
         * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description Check if the new name is correct for column name change
         */
        vm.nameHasChanged = function nameHasChanged() {
            return vm.newName && originalName !== vm.newName;
        };


        /**
         * @ngdoc method
         * @name setEditMode
         * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description Set isEditMode to provided value
         * @param {boolean} bool The new edit mode value
         */
        vm.setEditMode = function setEditMode(bool) {
            vm.isEditMode = bool;

            if (bool) {
                vm.newName = originalName = vm.column.name;
            }
        };


        /**
         * @ngdoc method
         * @name resetColumnName
         * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
         * @description Reset newName with the original name
         */
        vm.resetColumnName = function resetColumnName() {
            vm.newName = originalName;
        };


    }

    angular.module('data-prep.datagrid-header')
        .controller('DatagridHeaderCtrl', DatagridHeaderCtrl);
})();
