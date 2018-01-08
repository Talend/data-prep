/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-regex-param.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformRegexParam
 * @description This directive display a regex parameters form
 * @restrict E
 * @usage <transform-regex-param parameters="parameters"></transform-regex-param>
 * @param {object} parameter The parameter to render
 */
export default function TransformRegexParam() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			parameter: '=',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'regexParamCtrl',
		controller: 'TransformRegexParamCtrl',
	};
}
