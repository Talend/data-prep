/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SEARCH_BAR_MODULE from './bar/search-bar-module';
import SEARCH_DOCUMENTATION_MODULE from './documentation/documentation-search-module';
import SEARCH_INVENTORY_MODULE from './inventory/inventory-search-module';

const MODULE_NAME = 'data-prep.search';

export default MODULE_NAME;

angular.module(MODULE_NAME,
	[
		SEARCH_BAR_MODULE,
		SEARCH_DOCUMENTATION_MODULE,
		SEARCH_INVENTORY_MODULE,
	]);
