/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_ADAPTER_MODULE from './adapter/filter-adapter-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import FilterService from './filter-service';

const MODULE_NAME = 'data-prep.services.filter-service';

/**
 * @ngdoc object
 * @name data-prep.services.filter-service
 * @description This module contains the services to manage filters in the monitor
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_ADAPTER_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
	.service('FilterService', FilterService);

export default MODULE_NAME;
