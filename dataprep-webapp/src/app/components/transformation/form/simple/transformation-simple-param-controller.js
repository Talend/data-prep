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
 * @name data-prep.transformation-form.controller:TransformSimpleParamCtrl
 * @description Simple transformation parameter controller.
 * @requires data-prep.services.utils.service:ConverterService
 */
export default function TransformSimpleParamCtrl(ConverterService, TextFormatService) {
	'ngInject';

	const vm = this;
	vm.TextFormatService = TextFormatService;

    /**
     * @ngdoc method
     * @name initParamValues
     * @methodOf data-prep.transformation-form.controller:TransformSimpleParamCtrl
     * @description [PRIVATE] Init simple param values to default
     */
	const initParamValues = function () {
		if (typeof vm.parameter.initialValue !== 'undefined' && vm.parameter.initialValue !== null) {
			vm.parameter.initialValue = ConverterService.adaptValue(vm.parameter.type, vm.parameter.initialValue);
		}

		if (typeof vm.parameter.value !== 'undefined' && vm.parameter.value !== null) {
			vm.parameter.value = ConverterService.adaptValue(vm.parameter.type, vm.parameter.value);
		}
		else if (typeof vm.parameter.default !== 'undefined' && vm.parameter.default !== null) {
			vm.parameter.default = ConverterService.adaptValue(vm.parameter.type, vm.parameter.default);
			vm.parameter.value = vm.parameter.default;
		}
	};

    /**
     * @ngdoc method
     * @name initInputTypes
     * @methodOf data-prep.transformation-form.controller:TransformSimpleParamsCtrl
     * @description [PRIVATE] Init params input type, depending on param type
     */
	const initInputTypes = function () {
		vm.parameter.inputType = ConverterService.toInputType(vm.parameter.type);
	};


	/**
	 * @ngdoc method
	 * @name isBooleanType
	 * @methodOf data-prep.transformation-form.controller:TransformSimpleParamsCtrl
	 * @description check if it is a boolean input type
	 */
	vm.isBooleanType = () => {
		return vm.parameter.type === 'boolean';
	};

	initParamValues();
	initInputTypes();
}
