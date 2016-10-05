/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_ADAPTER_MODULE from '../filter/adapter/filter-adapter-module';
import SERVICES_STATISTICS_MODULE from '../statistics/statistics-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import FilterService from './filter-service';

const MODULE_NAME = 'data-prep.services.filter';

/**
 * @ngdoc object
 * @name data-prep.services.filter
 * @description This module contains the services to manage filters in the datagrid.
 * It is responsible for the filter update within the SlickGrid grid
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_ADAPTER_MODULE,
		SERVICES_STATISTICS_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('FilterService', FilterService);

export default MODULE_NAME;
