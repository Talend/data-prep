/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './editable-regex.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:EditableRegex
 * @description This directive create an regex input
 * @restrict E
 * @usage <editable-regex ng-model="value"></editable-regex>
 * @param {object} ngModel The model to bind
 */
export default function TalendEditableRegex() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			value: '=ngModel',
			isReadonly: '<',
			transformText: '=',
		},
		bindToController: true,
		controller: 'TalendEditableRegexCtrl',
		controllerAs: 'editableRegexCtrl',
	};
}
