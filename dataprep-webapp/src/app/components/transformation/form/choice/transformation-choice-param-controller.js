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
	const initParamValues = function () {
		if (!vm.parameter.value && (vm.parameter.configuration.values.length > 0)) {
            // init with the default value
			if (vm.parameter.default) {
				const defaultValue = _.find(vm.parameter.configuration.values, { value: vm.parameter.default });
				vm.parameter.value = defaultValue.value;
			}
            // or with the first value in the list
			else {
				vm.parameter.value = vm.parameter.configuration.values[0].value;
			}
		}
	};

	initParamValues();
}
