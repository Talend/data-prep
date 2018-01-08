/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_MODULE from '../../../services/filter/filter-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';

import FilterMonitor from './filter-monitor-directive';

const MODULE_NAME = 'data-prep.filter-monitor';

/**
 * @ngdoc object
 * @name data-prep.filter-monitor
 * @description This module contains the controller and directives to manage the filter list
 * @requires data-prep.services.filter
 * @requires data-prep.services.state
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_MODULE,
		SERVICES_STATE_MODULE,
	])
    .directive('filterMonitor', FilterMonitor);

export default MODULE_NAME;
