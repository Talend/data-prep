/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './transformation-simple-param.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-form.directive:TransformSimpleParam
 * @description This directive display a simple input parameter form
 * @restrict E
 * @usage
 <transform-params
     transformation="transformation"
     on-submit="callback()">

     <div ng-repeat="parameter in paramsCtrl.transformation.parameters track by $index"
          ng-switch="parameter.type">
         <transform-simple-param ng-switch-when="simple"
                                 parameter="parameter">
         </transform-simple-param>
     </div>

 </transform-params>
 * @param {object} parameter The simple parameter
 * @param {object} label Do NOT display label if 'false'. Display it otherwise (by default).
 * @param {boolean} editableSelect If this parameter is an editable-select
 */
export default function TransformSimpleParam() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			editableSelect: '=',
			parameter: '=',
			label: '@',
			isReadonly: '<',
		},
		require: '^?form',
		bindToController: true,
		controllerAs: 'simpleParamCtrl',
		controller: 'TransformSimpleParamCtrl',
		link: (scope, element, attrs, form) => {
			scope.parentForm = form;
		},
	};
}
