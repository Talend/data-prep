/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_ADAPTER_MODULE from '../filter/adapter/filter-adapter-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import StatisticsTooltipService from './statistics-tooltip-service';
import StatisticsRestService from './rest/statistics-rest-service';
import StatisticsService from './statistics-service';

const MODULE_NAME = 'data-prep.services.statistics';

/**
 * @ngdoc object
 * @name data-prep.services.statistics
 * @description This module contains the statistics service
 * @requires data-prep.services.playground
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_ADAPTER_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('StatisticsTooltipService', StatisticsTooltipService)
    .service('StatisticsRestService', StatisticsRestService)
    .service('StatisticsService', StatisticsService);

export default MODULE_NAME;
