/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import sunchoke from 'sunchoke';
import asSortable from 'ng-sortable';

import RECIPE_KNOT_MODULE from '../recipe/knot/recipe-knot-module';
import RECIPE_STEP_DESCRIPTION from '../recipe/step-description/step-description-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import TRANSFORMATION_FORM_MODULE from '../transformation/form/transformation-form-module';
import SERVICES_FILTER_MODULE from '../../services/filter/filter-module';
import SERVICES_PARAMETERS_MODULE from '../../services/parameters/parameters-module';
import SERVICES_PLAYGROUND_MODULE from '../../services/playground/playground-module';
import SERVICES_PREVIEW_MODULE from '../../services/preview/preview-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';
import SERVICES_RECIPE_MODULE from '../../services/recipe/recipe-module';

import Recipe from './recipe-component';

const MODULE_NAME = 'data-prep.recipe';

/**
 * @ngdoc object
 * @name data-prep.recipe
 * @description This module contains the controller and component to manage the recipe
 * @requires talend.widget
 * @requires data-prep.recipe-knot
 * @requires data-prep.services.parameters
 * @requires data-prep.services.playground
 * @requires data-prep.services.preview
 * @requires data-prep.services.preparation
 * @requires data-prep.services.state
 * @requires data-prep.step-description
 * @requires data-prep.transformation-form
 * @requires data-prep.services.recipe.service:RecipeKnotService
 * @requires data-prep.services.playground.service:PlaygroundService
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		sunchoke.all,
		asSortable,
		RECIPE_KNOT_MODULE,
		RECIPE_STEP_DESCRIPTION,
		TALEND_WIDGET_MODULE,
		TRANSFORMATION_FORM_MODULE,
		SERVICES_FILTER_MODULE,
		SERVICES_PARAMETERS_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_RECIPE_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .component('recipe', Recipe);

export default MODULE_NAME;
