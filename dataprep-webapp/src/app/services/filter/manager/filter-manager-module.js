/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_MODULE from './../filter-module';
import SERVICES_PLAYGROUND_MODULE from './../../playground/playground-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_STATISTICS_MODULE from '../../statistics/statistics-module';

import FilterManagerService from './filter-manager-service';

const MODULE_NAME = 'data-prep.services.filter-manager-service';

/**
 * @ngdoc object
 * @name data-prep.services.filter
 * @description This module contains the services to manage filters in the datagrid.
 * It is responsible for the filter update within the SlickGrid grid
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_STATISTICS_MODULE,
	])
	.service('FilterManagerService', FilterManagerService);

export default MODULE_NAME;
