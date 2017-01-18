/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import RestErrorMessageHandler from './rest-error-message-interceptor-factory';

const MODULE_NAME = 'data-prep.services.rest';

/**
 * @ngdoc object
 * @name data-prep.services.rest
 * @description This module contains the REST interceptor
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME, [SERVICES_UTILS_MODULE])
	.factory('RestErrorMessageHandler', RestErrorMessageHandler)
	.config(($httpProvider) => {
		'ngInject';
		$httpProvider.interceptors.push('RestErrorMessageHandler');
	});

export default MODULE_NAME;
