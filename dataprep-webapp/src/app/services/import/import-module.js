/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import ImportRestService from './import-rest-service';
import ImportService from './import-service';

const MODULE_NAME = 'data-prep.services.import';

/**
 * @ngdoc object
 * @name data-prep.services.import
 * @description This module contains the services for import
 */
angular.module(MODULE_NAME,
	[
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('ImportRestService', ImportRestService)
    .service('ImportService', ImportService);

export default MODULE_NAME;
