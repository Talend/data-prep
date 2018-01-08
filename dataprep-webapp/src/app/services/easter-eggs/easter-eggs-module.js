/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import EasterEggsService from './easter-eggs-service';

const MODULE_NAME = 'data-prep.services.easter-eggs';

/**
 * @ngdoc object
 * @name data-prep.services.easter-eggs
 * @description This module contains the services for easter eggs
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('EasterEggsService', EasterEggsService);

export default MODULE_NAME;
