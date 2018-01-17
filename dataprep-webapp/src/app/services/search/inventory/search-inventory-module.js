/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../../state/state-module';
import SERVICES_UTILS_MODULE from '../../utils/utils-module';

import SearchInventoryService from './search-inventory-service';
import SearchInventoryRestService from './rest/search-inventory-rest-service';

const MODULE_NAME = 'data-prep.services.search.inventory';

/**
 * @ngdoc object
 * @name data-prep.services.search.inventory
 * @description This module contains the services to manage the inventory
 */
angular.module(MODULE_NAME,
	[
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('SearchInventoryService', SearchInventoryService)
    .service('SearchInventoryRestService', SearchInventoryRestService);

export default MODULE_NAME;
