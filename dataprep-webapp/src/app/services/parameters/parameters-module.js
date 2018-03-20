/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import ParametersService from './parameters-service';

const MODULE_NAME = 'data-prep.services.parameters';

/**
 * @ngdoc object
 * @name data-prep.services.parameters
 * @description This module contains the services to manage dynamic parameters
 */
angular.module(MODULE_NAME, [SERVICES_UTILS_MODULE])
    .service('ParametersService', ParametersService);

export default MODULE_NAME;
