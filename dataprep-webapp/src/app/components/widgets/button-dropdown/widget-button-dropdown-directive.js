/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './button-dropdown.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendButtonDropdown
 * @description This directive create a button dropdown element.
 * @restrict EA
 * @usage
 <talend-button-dropdown button-text="Click Me" button-action="buttonAction()">
 <ul>
 <li>Menu 1</li>
 <li>Menu 2</li>
 </ul>
 </talend-button-dropdown>
 * @param {string} buttonId The id of the main button
 * @param {string} buttonAdditionalClass The additional class of the main button
 * @param {string} buttonIcon The icon to display in the main button
 * @param {string} buttonText The text to display in the main button
 * @param {string} buttonDropdownTitle The text to display in the main button
 * @param {string} buttonTitle The tooltip to display in the main button
 * @param {string} appendToBody The dropdown is appended to the body
 * @param {function} buttonAction The callback to execute on main button click
 */
export default function TalendButtonDropdown($window, $timeout) {
	'ngInject';
	return {
		restrict: 'E',
		transclude: true,
		templateUrl: template,
		scope: {
			buttonId: '@id',
			buttonAdditionalClass: '@',
			buttonTitle: '@',
			buttonIcon: '@',
			buttonText: '@',
			buttonDropdownTitle: '@',
			buttonAction: '&',
			closeOnSelect: '<',
			appendToBody: '<',
			dropdownMenuDirection: '@',
		},
		bindToController: true,
		controller: () => {
		},
		controllerAs: 'buttonDropdownCtrl',
		link: {
			post: (scope, iElement, attrs) => {
				if (!attrs.buttonAction) {
					$timeout(function () {
						const action = iElement.find('.dropdown-action').eq(0);

						iElement.find('.button-dropdown-main')
							.on('click', function () {
								action.click();
							});
					});
				}
			},
		},
	};
}
