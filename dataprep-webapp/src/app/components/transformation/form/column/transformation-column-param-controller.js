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
 * @name data-prep.transformation-form.controller:TransformColumnParamCtrl
 * @description Column parameter controller.
 * @requires data-prep.services.state.service:StateService
 */
export default function TransformColumnParamCtrl(state) {
    'ngInject';

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
        vm.columns = _.filter(state.playground.data.metadata.columns, function (column) {
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
        if (!vm.parameter.value) {
            if (vm.columns.length) {
                vm.parameter.value = vm.columns[0].id;
            }
        }
    };

    initColumns();
    initDefaultValue();
}