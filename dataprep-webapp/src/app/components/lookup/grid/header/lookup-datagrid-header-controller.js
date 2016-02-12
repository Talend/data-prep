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
 * @name data-prep.lookup-datagrid-header.controller:LookupDatagridHeaderCtrl
 * @description Lookup Column Header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:ConverterService
 */
export default function LookupDatagridHeaderCtrl(ConverterService, state, StateService) {
    'ngInject';

    var vm = this;
    vm.converterService = ConverterService;
    vm.state = state;

    /**
     * @ngdoc method
     * @name showCheckbox
     * @methodOf data-prep.lookup-datagrid-header.controller:LookupDatagridHeaderCtrl
     * @description show/hide the checkbox responsible for adding the columns to the lookup action
     */
    vm.showCheckbox = function showCheckbox() {
        if (vm.state.playground.lookup.selectedColumn) {
            return vm.column.id !== vm.state.playground.lookup.selectedColumn.id;
        }
        else {
            return false;
        }
    };

    /**
     * @ngdoc method
     * @name updateColsToAdd
     * @methodOf data-prep.lookup-datagrid-header.controller:LookupDatagridHeaderCtrl
     * @description updates the array of the selected columns to be added to the lookup
     */
    vm.updateColsToAdd = function updateColsToAdd(e) {
        //stop event propagation in order not to select the column
        e.stopPropagation();
        StateService.updateLookupColumnsToAdd();
    };
}