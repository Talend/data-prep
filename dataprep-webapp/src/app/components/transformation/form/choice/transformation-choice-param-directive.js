/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './transformation-choice-param.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformChoiceParam
 * @description This directive display a transformation choice parameter form
 * @restrict E
 * @usage <transform-choice-param parameter="parameter"></transform-choice-params>
 * @param {object} parameter The transformation choices parameter
 */
export default function TransformChoiceParam($rootScope, $compile) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			parameter: '=',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'choiceParamCtrl',
		controller: 'TransformChoiceParamCtrl',
		link: (scope, iElement, iAttrs, ctrl) => {
			ctrl.parameter.configuration.values
				.filter((optionValue) => {
					return optionValue.parameters && optionValue.parameters.length;
				})
				.forEach((optionValue) => {
					const isolatedScope = $rootScope.$new(true);
					isolatedScope.parameter = ctrl.parameter;
					isolatedScope.optionValue = optionValue;
					isolatedScope.isReadonly = ctrl.isReadonly;

					const template =
						`<transform-params
							parameters="optionValue.parameters"
							ng-if="parameter.value === optionValue.value"
							is-readonly="isReadonly">
						</transform-params>`;
					$compile(template)(isolatedScope, (cloned) => {
						iElement.append(cloned);
					});

					scope.$on('$destroy', () => {
						isolatedScope.$destroy();
					});
				});
		},
	};
}
