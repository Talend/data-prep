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
 * @name data-prep.transformation-form.controller:TransformRegexParamCtrl
 * @description Regex transformation parameter controller.
 */
export default function TransformRegexParamCtrl(TextFormatService) {
	'ngInject';

	const vm = this;
	vm.TextFormatService = TextFormatService;
    /**
     * @ngdoc method
     * @name initParamValues
     * @methodOf data-prep.transformation-form.controller:TransformRegexParamCtrl
     * @description [PRIVATE] Init param values to default
     */
	function initParamValues() {
		if (angular.isUndefined(vm.parameter.value) && angular.isDefined(vm.parameter.default)) {
			vm.parameter.value = vm.parameter.default;
		}
	}

	initParamValues();
}
