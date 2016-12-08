/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import AboutRestService from './rest/about-rest-service';
import AboutService from './about-service';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

const MODULE_NAME = 'data-prep.services.about';

/**
 * @ngdoc object
 * @name data-prep.services.about
 * @description This module contains the services for build details
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_UTILS_MODULE,
	])
	.service('AboutRestService', AboutRestService)
	.service('AboutService', AboutService);

export default MODULE_NAME;
