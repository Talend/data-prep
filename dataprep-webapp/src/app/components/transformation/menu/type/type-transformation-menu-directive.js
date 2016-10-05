/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './type-transformation-menu.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-menu.directive:TypeTransformMenu
 * @description This directive display the type transformation menu
 * @restrict E
 * @usage <type-transform-menu column="column"></type-transform-menu>
 * @param {object} column The target column of this transformation menu item
 */
export default function TypeTransformMenu() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			column: '=',
		},
		bindToController: true,
		controllerAs: 'typeMenuCtrl',
		controller: 'TypeTransformMenuCtrl',
	};
}
