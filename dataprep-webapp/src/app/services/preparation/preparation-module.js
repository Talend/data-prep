/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';
import SERVICES_FOLDER_MODULE from '../folder/folder-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import PreparationRestService from './rest/preparation-rest-service';
import PreparationService from './preparation-service';

const MODULE_NAME = 'data-prep.services.preparation';

/**
 * @ngdoc object
 * @name data-prep.services.preparation
 * @description This module contains the services to manipulate preparations
 * @requires data-prep.services.preparation
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_FOLDER_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('PreparationRestService', PreparationRestService)
    .service('PreparationService', PreparationService);

export default MODULE_NAME;
