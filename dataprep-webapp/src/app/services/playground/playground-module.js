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
import SERVICES_DATAGRID_MODULE from '../datagrid/datagrid-module';
import SERVICES_DATASET_MODULE from '../dataset/dataset-module';
import SERVICES_EXPORT_MODULE from '../export/export-module';
import SERVICES_FILTER_MODULE from '../filter/filter-module';
import SERVICES_HISTORY_MODULE from '../history/history-module';
import SERVICES_LOOKUP_MODULE from '../lookup/lookup-module';
import SERVICES_ONBOARDING_MODULE from '../onboarding/onboarding-module';
import SERVICES_PREPARATION_MODULE from '../preparation/preparation-module';
import SERVICES_PREVIEW_MODULE from '../preview/preview-module';
import SERVICES_RECIPE_MODULE from '../recipe/recipe-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_STATISTICS_MODULE from '../statistics/statistics-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';
import SERVICES_MESSAGE_MODULE from '../message/message-module';

import PlaygroundService from './playground-service';

const MODULE_NAME = 'data-prep.services.playground';

/**
 * @ngdoc object
 * @name data-prep.services.playground
 * @description This module contains the services to load the playground
 * @requires data-prep.services.datagrid
 * @requires data-prep.services.dataset
 * @requires data-prep.services.export
 * @requires data-prep.services.filter
 * @requires data-prep.services.history
 * @requires data-prep.services.onboarding
 * @requires data-prep.services.preparation
 * @requires data-prep.services.preview
 * @requires data-prep.services.recipe
 * @requires data-prep.services.state
 * @requires data-prep.services.statistics
 * @requires data-prep.services.utils
 * @requires data-prep.services.message
 */
angular.module(MODULE_NAME,
	[
		uiRouter,
		SERVICES_DATAGRID_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_EXPORT_MODULE,
		SERVICES_FILTER_MODULE,
		SERVICES_HISTORY_MODULE,
		SERVICES_LOOKUP_MODULE,
		SERVICES_ONBOARDING_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_RECIPE_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_STATISTICS_MODULE,
		SERVICES_UTILS_MODULE,
		SERVICES_MESSAGE_MODULE,
	])
    .service('PlaygroundService', PlaygroundService);

export default MODULE_NAME;
