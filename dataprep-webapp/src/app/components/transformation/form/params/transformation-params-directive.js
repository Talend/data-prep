/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-params.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformParams
 * @description This directive display a transformation parameters form
 * @restrict E
 * @usage <transform-params parameters="parameters"></transform-params>
 * @param {array} parameters The parameters to render
 */
export default function TransformParams() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			parameters: '=',
			isReadonly: '<',
		},
		bindToController: true,
		controllerAs: 'paramsCtrl',
		controller: 'TransformParamsCtrl',
	};
}
