/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_DATASET_MODULE from '../dataset/dataset-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_TRANSFORMATION_MODULE from '../transformation/transformation-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import LookupService from './lookup-service';

const MODULE_NAME = 'data-prep.services.lookup';

/**
 * @ngdoc object
 * @name data-prep.services.lookup
 * @description This module contains the services to load dataset lookup
 * @requires data-prep.services.dataset
 * @requires data-prep.services.transformation
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_DATASET_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('LookupService', LookupService);

export default MODULE_NAME;
