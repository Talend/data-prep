/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './transformation-menu.html';

/**
 * @ngdoc directive
 * @name data-prep.transformation-menu.directive:TransformMenu
 * @description This directive display a transformation menu item, and create the parameters form if needed
 * @restrict E
 * @usage <transform-menu column="column" menu="menu"> </transform-menu>
 * @param {object} column The target column of this transformation menu item
 * @param {object} menu The menu item to display
 */
export default function TransformMenu() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			column: '=',
			menuItems: '=',
		},
		bindToController: true,
		controllerAs: 'menuCtrl',
		controller: 'TransformMenuCtrl',
	};
}
