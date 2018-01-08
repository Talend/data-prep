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
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import InventoryHeaderComponent from './inventory-header-component';

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
		TALEND_WIDGET_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .component('inventoryHeader', InventoryHeaderComponent);

export default MODULE_NAME;
