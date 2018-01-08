/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './editable-text.html';

import TalendEditableTextCtrl from './widget-editable-text-controller';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendEditableText
 * @description This directive create a text that user can edit
 * @restrict E
 * @usage <talend-editable-text placeholder="placeholder"
 *                              text="text"
 *                              text-title="textTitle"
 *                              text-class="textClass"
 *                              edition-mode="editionMode"
 *                              on-text-click="onTextClick()"
 *                              on-validate="onValidate()"
 *                              on-cancel="onCancel()"
 *                              validate-only-on-change></talend-editable-text>
 * @param {string} placeholder The placeholder to insert in the edition input
 * @param {string} text The text to display
 * @param {string} textTitle The text tooltip
 * @param {string} textClass The text div class
 * @param {boolean} editionMode The flag that switch between text and input
 * @param {function} onTextClick The action triggered by a text click
 * @param {function} onValidate The action triggered by an edition validation
 * @param {function} onCancel The action triggered by an edition cancelation
 * @param {any} validateOnlyOnChange If this attribute is present, the onValidate callback is triggered only when value has changed
 */
export default function TalendEditableText() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			placeholder: '@',
			text: '<',
			textTitle: '@',
			textClass: '@',
			editionMode: '=?',
			link: '@',
			onTextClick: '&',
			onValidate: '&',
			onCancel: '&',
			validateOnlyOnChange: '@',
		},
		bindToController: true,
		controller: TalendEditableTextCtrl,
		controllerAs: 'editableTextCtrl',
		link(scope, iElement, iAttrs, ctrl) {
			const inputElement = iElement.find('.edition-text-input').eq(0);
			inputElement.keydown((e) => {
				if (e.keyCode === 27) {
					ctrl.cancel();
					scope.$digest();
				}
			});

			const editBtn = iElement.find('.edit-btn').eq(0);
			editBtn.click(() => inputElement.select());
		},
	};
}
