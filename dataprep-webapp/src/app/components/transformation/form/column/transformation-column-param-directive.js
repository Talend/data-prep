/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-column-param.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformColumnParam
 * @description This directive display a select parameter form to select a column
 * @restrict E
 * @usage <transform-column-param parameter="parameter"></transform-column-param>
 * @param {object} parameter The column parameter
 */
export default function TransformColumnParam() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			parameter: '=',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'columnParamCtrl',
		controller: 'TransformColumnParamCtrl',
	};
}
