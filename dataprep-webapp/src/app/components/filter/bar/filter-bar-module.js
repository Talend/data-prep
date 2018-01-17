/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import FILTER_ITEM from '../item/filter-item-module';
import FILTER_LIST from '../list/filter-list-module';
import FILTER_MANAGER_SERVICE from '../../../services/filter/manager/filter-manager-module';
import FILTER_MONITOR from '../monitor/filter-monitor-module';
import FILTER_SEARCH_MODULE from '../search/filter-search-module';

import FilterBar from './filter-bar-directive';

const MODULE_NAME = 'data-prep.filter-bar';

/**
 * @ngdoc object
 * @name data-prep.filter-bar
 * @description This module aggregate the filters components into a bar
 */
angular.module(MODULE_NAME,
	[
		FILTER_ITEM,
		FILTER_LIST,
		FILTER_MANAGER_SERVICE,
		FILTER_MONITOR,
		FILTER_SEARCH_MODULE,
	])
    .directive('filterBar', FilterBar);

export default MODULE_NAME;
