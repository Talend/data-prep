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
import SERVICES_DATASET_MODULE from '../dataset/dataset-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';
import SERVICES_ONBOARDING_MODULE from '../../services/onboarding/onboarding-module';

import FolderService from './folder-service';
import FolderRestService from './rest/folder-rest-service';

const MODULE_NAME = 'data-prep.services.folder';

/**
 * @ngdoc object
 * @name data-prep.services.folder
 * @description This module contains the services to manipulate folders
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_DATASET_MODULE,
		SERVICES_ONBOARDING_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('FolderRestService', FolderRestService)
    .service('FolderService', FolderService);

export default MODULE_NAME;
