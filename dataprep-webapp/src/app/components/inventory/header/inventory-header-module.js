/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import PREPARATION_CREATOR from '../../preparation/creator/preparation-creator-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import InventoryHeaderCtrl from './inventory-header-controller';
import InventoryHeaderDirective from './inventory-header-directive';

const MODULE_NAME = 'data-prep.inventory-header';

/**
 * @ngdoc object
 * @name data-prep.inventory-header
 * @description This module contains the entities to manage the inventory list header
 * @requires talend.widget
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		PREPARATION_CREATOR,
		TALEND_WIDGET_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('InventoryHeaderCtrl', InventoryHeaderCtrl)
    .directive('inventoryHeader', InventoryHeaderDirective);

export default MODULE_NAME;
