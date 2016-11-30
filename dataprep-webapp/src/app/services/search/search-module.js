/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import SERVICES_INVENTORY_MODULE from '../inventory/inventory-module';
import SERVICES_DOCUMENTATION_MODULE from '../documentation/documentation-module';
import SERVICES_EASTER_EGGS_MODULE from '../easter-eggs/easter-eggs-module';

import SearchService from './search-service';

const MODULE_NAME = 'data-prep.services.search';

/**
 * @ngdoc object
 * @name data-prep.services.search
 * @description This module contains the services to search
 */
angular.module(MODULE_NAME,
	[
		SERVICES_DOCUMENTATION_MODULE,
		SERVICES_EASTER_EGGS_MODULE,
		SERVICES_INVENTORY_MODULE,
	])
	.service('SearchService', SearchService);

export default MODULE_NAME;
