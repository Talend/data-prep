/*  ============================================================================

 Copyright (C) 2006-2017 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import {
	DEFAULTS,
	TYPES,
	STATES,
} from './progress-service-constants';

import ProgressService from './progress-service';


const MODULE_NAME = 'data-prep.services.progress';

/**
 * @ngdoc object
 * @name data-prep.services.progress
 * @description This module contains the services in charge of progression
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME, [])
	.value('PROGRESSION_STATES', STATES)
	.value('PROGRESSION_TYPES', TYPES)
	.value('PROGRESSION_DEFAULTS', DEFAULTS)
	.service('ProgressService', ProgressService);

export default MODULE_NAME;
