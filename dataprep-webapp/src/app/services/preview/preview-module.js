/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_DATAGRID_MODULE from '../datagrid/datagrid-module';
import SERVICES_PREPARATION_MODULE from '../preparation/preparation-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import PreviewService from './preview-service';

const MODULE_NAME = 'data-prep.services.preview';

/**
 * @ngdoc object
 * @name data-prep.services.preview
 * @description This module contains the preview services
 * @requires data-prep.services.playground
 * @requires data-prep.services.preparation
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_DATAGRID_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('PreviewService', PreviewService);

export default MODULE_NAME;
