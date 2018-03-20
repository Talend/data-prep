/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './type-transformation-menu.html';
import TypeTransformMenuCtrl from './type-transformation-menu-controller';

/**
 * @ngdoc component
 * @name data-prep.transformation-menu.component:TypeTransformMenu
 * @description This directive displays the type transformation menu
 * @usage <type-transform-menu column="column"></type-transform-menu>
 * @param {object} column The target column of this transformation menu item
 */
const TypeTransformMenu = {
	templateUrl: template,
	controller: TypeTransformMenuCtrl,
	bindings: {
		column: '<',
		domains: '<',
		types: '<',
	},
};

export default TypeTransformMenu;
