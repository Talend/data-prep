/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import FOLDER_SELECTION from '../../folder/selection/folder-selection-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import TALEND_WIDGET_CONTAINERS_MODULE from '../../widgets-containers/widgets-containers-module';

import InventoryCopyMoveComponent from './inventory-copy-move-component';

const MODULE_NAME = 'data-prep.inventory-copy-move';

/**
 * @ngdoc object
 * @name data-prep.inventory-copy-move
 * @description This module contains the entities to manage the inventory copy/move wiazerd
 * @requires data-prep.folder-selection
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		FOLDER_SELECTION,
		TALEND_WIDGET_MODULE,
		TALEND_WIDGET_CONTAINERS_MODULE,
	])
    .component('inventoryCopyMove', InventoryCopyMoveComponent);

export default MODULE_NAME;
