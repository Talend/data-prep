/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-date-param.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformDateParam
 * @description This directive display a transformation date parameter form
 * @restrict E
 * @usage <transform-date-param label="label" parameter="parameter">  </transform-date-param>
 * @param {object} parameters The transformation date parameter
 * @param {object} label Do NOT display label if 'false'. Display it otherwise (by default).
 */
export default function TransformDateParam() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			parameter: '=',
			label: '@',
			isReadonly: '<',
		},
		require: '^?form',
		bindToController: true,
		controllerAs: 'dateParamCtrl',
		controller: 'TransformDateParamCtrl',
		link: (scope, element, attrs, form) => {
			scope.parentForm = form;
		},
	};
}
