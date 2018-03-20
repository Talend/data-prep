/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './file-selector.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendFileSelector
 * @description This directive create an icon that hide a file selector
 * @restrict E
 * @usage <talend-file-selector button-data-icon="icon"
 *                              button-title="title"
 *                              file-model="model"
 *                              on-file-change="change()"></talend-file-selector>
 * @param {string} buttonDataIcon The icon font item to display
 * @param {string} buttonTitle The icon tooltip
 * @param {object} fileModel The ng-model ref
 * @param {function} onFileChange The file selection change callback
 */
export default function TalendFileSelector() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			buttonDataIcon: '@',
			buttonTitle: '@',
			fileModel: '=',
			onFileChange: '&',
		},
		bindToController: true,
		controllerAs: 'talendFileSelectorCtrl',
		controller: () => {
		},

		link: (scope, element) => {
			element.find('span').bind('click', () => {
				element.find('input').click();
			});
		},
	};
}
