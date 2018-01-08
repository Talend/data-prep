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
 * @name data-prep.transformation-form.controller:TransformChoiceParamCtrl
 * @description Transformation choices controller.
 */
export default function TransformChoiceParamCtrl() {
	const vm = this;

    /**
     * @ngdoc method
     * @name initParamValues
     * @methodOf data-prep.transformation-form.controller:TransformChoiceParamCtrl
     * @description [PRIVATE] Init choice element default value (either the parameter default value or the first value in the select)
     */
	const initParamValues = () => {
		if (!vm.parameter.value && (vm.parameter.configuration.values.length > 0)) {
            // init with the default value
			if (vm.parameter.default) {
				const defaultValue = vm.parameter.configuration.values.filter((item) => {
					return item.value === vm.parameter.default;
				})[0];
				vm.parameter.value = defaultValue.value;
			}
            // or with the first value in the list
			else {
				vm.parameter.value = vm.parameter.configuration.values[0].value;
			}
		}
	};

	/**
	 * @ngdoc method
	 * @name getLabelByValue
	 * @methodOf data-prep.transformation-form.controller:TransformChoiceParamCtrl
	 * @description get label to display by value
	 */
	vm.getLabelByValue = (value) => {
		return vm.parameter.configuration.values.filter((item) => {
			return item.value === value;
		})[0].label;
	};

	initParamValues();
}
