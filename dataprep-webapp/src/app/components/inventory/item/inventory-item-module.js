/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngSanitize from 'angular-sanitize';
import ngTranslate from 'angular-translate';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import InventoryItemCtrl from './inventory-item-controller';
import InventoryItemComponent from './inventory-item-component';

const MODULE_NAME = 'data-prep.inventory-item';

/**
 * @ngdoc object
 * @name data-prep.data-prep.inventory-item
 * @description This module contains the controller and directives to manage an inventory item
 * @requires talend.widget
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		ngSanitize,
		ngTranslate,
		TALEND_WIDGET_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('InventoryItemCtrl', InventoryItemCtrl)
    .component('inventoryItem', InventoryItemComponent);

export default MODULE_NAME;
