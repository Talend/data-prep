/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.datagrid-header.controller:DatagridHeaderCtrl
 * @description Dataset Column Header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.transformation.service:TransformationCacheService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.filter.service:FilterService
 * @requires data-prep.services.transformation.service:TransformationApplicationService
 * @requires data-prep.services.transformation.service:ColumnSuggestionService
 */
export default function DatagridHeaderCtrl(state, TransformationCacheService, ConverterService, PlaygroundService,
                            FilterService, TransformationApplicationService, ColumnSuggestionService) {
    'ngInject';

    var ACTION_SCOPE = 'column_metadata';
    var RENAME_ACTION = 'rename_column';
    var originalName;

    var vm = this;
    vm.converterService = ConverterService;
    vm.filterService = FilterService;
    vm.transformationApplicationService = TransformationApplicationService;
    vm.columnSuggestionService = ColumnSuggestionService;
    vm.state = state;

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
     * @ngdoc method
     * @name initTransformations
     * @methodOf data-prep.datagrid-header.controller:DatagridHeaderCtrl
     * @description Get transformations from REST call
     */
    vm.initTransformations = function initTransformations() {
        if (!vm.transformations && !vm.initTransformationsInProgress) {
            vm.transformationsRetrieveError = false;
            vm.initTransformationsInProgress = true;

            TransformationCacheService.getColumnTransformations(vm.column, true)
                .then(function(columnTransformations) {
                    vm.transformations = _.filter(columnTransformations.allTransformations, function(menu) {
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