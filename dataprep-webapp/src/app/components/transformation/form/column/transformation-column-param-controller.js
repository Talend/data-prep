/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

	const vm = this;
	vm.columns = state.playground.data.metadata.columns;

    /**
     * @ngdoc method
     * @name initDefaultValue
     * @methodOf data-prep.transformation-form.controller:TransformColumnParamCtrl
     * @description [PRIVATE] Init select default value
     */
	function initDefaultValue() {
		if (!vm.parameter.value) {
			if (vm.columns.length) {
				vm.parameter.value = vm.columns[0].id;
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name getLabelById
	 * @methodOf data-prep.transformation-form.controller:TransformColumnParamCtrl
	 * @description get Label by id
	 */
	vm.getLabelById = (id) => {
		return _.find(vm.columns, { id }).name;
	};

	initDefaultValue();
}
