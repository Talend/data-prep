/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_PARAMETERS_MODULE from '../parameters/parameters-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';

import ExportRestService from './export-rest-service';
import ExportService from './export-service';

const MODULE_NAME = 'data-prep.services.export';

/**
 * @ngdoc object
 * @name data-prep.services.export
 * @description This module contains the services for export
 * @requires data-prep.services.transformation
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_PARAMETERS_MODULE,
		SERVICES_UTILS_MODULE,
		SERVICES_STATE_MODULE,
	])
    .service('ExportRestService', ExportRestService)
    .service('ExportService', ExportService);

export default MODULE_NAME;
