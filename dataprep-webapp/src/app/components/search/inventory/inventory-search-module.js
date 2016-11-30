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
import SEARCH_BAR_MODULE from '../bar/search-bar-module';
import SERVICES_SEARCH_MODULE from '../../../services/search/search-module';
import SERVICES_UTILS_MODULE from '../../../services/utils/utils-module';

import InventorySearch from './inventory-search-component';

const MODULE_NAME = 'data-prep.inventory-search';

/**
 * @ngdoc object
 * @name data-prep.data-prep.inventory-search
 * @description This module contains the component to manage an inventory search
 * @requires data-prep.services.documentation
 * @requires data-prep.services.easter-eggs
 * @requires data-prep.services.inventory
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		SEARCH_BAR_MODULE,
		SERVICES_SEARCH_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .component('inventorySearch', InventorySearch);

export default MODULE_NAME;
