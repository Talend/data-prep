/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SERVICES_FILTER_ADAPTER_MODULE from '../filter/adapter/filter-adapter-module';
import SERVICES_FILTER_MODULE from '../filter/filter-module';
import SERVICES_PARAMETERS_MODULE from '../parameters/parameters-module';
import SERVICES_PREPARATION_MODULE from '../preparation/preparation-module';
import SERVICES_PREVIEW_MODULE from '../../services/preview/preview-module';
import SERVICES_STATE_MODULE from '../state/state-module';
import SERVICES_TRANSFORMATION_MODULE from '../transformation/transformation-module';
import SERVICES_UTILS_MODULE from '../utils/utils-module';

import RecipeKnotService from './knot/recipe-knot-service';
import RecipeService from './recipe-service';

const MODULE_NAME = 'data-prep.services.recipe';

/**
 * @ngdoc object
 * @name data-prep.services.recipe
 * @description This module contains the services to manipulate the recipe
 * @requires data-prep.services.preparation
 * @requires data-prep.services.transformation
 * @requires data-prep.services.playground
 * @requires data-prep.services.preview
 * @requires data-prep.services.state
 * @requires data-prep.services.utils
 */
angular.module(MODULE_NAME,
	[
		SERVICES_FILTER_ADAPTER_MODULE,
		SERVICES_FILTER_MODULE,
		SERVICES_PARAMETERS_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_TRANSFORMATION_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .service('RecipeKnotService', RecipeKnotService)
    .service('RecipeService', RecipeService);

export default MODULE_NAME;
