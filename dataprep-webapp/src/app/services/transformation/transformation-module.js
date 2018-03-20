/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_PARAMETERS_SERVICE from '../parameters/parameters-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import TransformationService from './transformation-service';
import TransformationUtilsService from './transformation-utils-service';
import TransformationCacheService from './transformation-cache-service';
import TransformationRestService from './transformation-rest-service';

const MODULE_NAME = 'data-prep.services.transformation';

/**
 * @ngdoc object
 * @name data-prep.services.transformation
 * @description This module contains the services to manipulate transformations
 * @requires data-prep.services.filter
 * @requires data-prep.services.playground
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_PARAMETERS_SERVICE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('TransformationService', TransformationService)
    .service('TransformationUtilsService', TransformationUtilsService)
    .service('TransformationRestService', TransformationRestService)
    .service('TransformationCacheService', TransformationCacheService);

export default MODULE_NAME;
