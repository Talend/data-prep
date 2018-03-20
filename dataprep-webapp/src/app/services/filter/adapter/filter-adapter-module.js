/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import TqlFilterAdapterService from './tql-filter-adapter-service';
import SERVICES_UTILS_MODULE from '../../utils/utils-module';

const MODULE_NAME = 'data-prep.services.filter-adapter';

/**
 * @ngdoc object
 * @name data-prep.services.filter
 * @description This module contains the services to manage filters in the datagrid.
 * It is responsible for the filter update within the SlickGrid grid
 */
angular.module(MODULE_NAME, [ SERVICES_UTILS_MODULE ])
	.service('TqlFilterAdapterService', TqlFilterAdapterService);

export default MODULE_NAME;
