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
import uiRouter from 'angular-ui-router';
import DATAGRID_MODULE from '../datagrid/datagrid-module';
import DATASET_PARAMETERS_MODULE from '../dataset/parameters/dataset-parameters-module';
import EXPORT_MODULE from '../export/export-module';
import FILTER_BAR from '../filter/bar/filter-bar-module';
import HISTORY_CONTROL from '../history-control/history-control-module';
import LOOKUP_MODULE from '../lookup/lookup-module';
import PREPARATION_PICKER_MODULE from '../preparation/picker/preparation-picker-module';
import RECIPE_MODULE from '../recipe/recipe-module';
import SUGGESTION_STATS_MODULE from '../suggestions-stats/suggestions-stats-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import TALEND_WIDGET_CONTAINERS_MODULE from '../widgets-containers/widgets-containers-module';

import SERVICES_ONBOARDING_MODULE from '../../services/onboarding/onboarding-module';
import SERVICES_PLAYGROUND_MODULE from '../../services/playground/playground-module';
import SERVICES_PREVIEW_MODULE from '../../services/preview/preview-module';
import SERVICES_PREPARATION_MODULE from '../../services/preparation/preparation-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';

import PlaygroundCtrl from './playground-controller';
import Playground from './playground-directive';
import PlaygroundHeader from './header/playground-header-component';

const MODULE_NAME = 'data-prep.playground';

/**
 * @ngdoc object
 * @name data-prep.playground
 * @description This module contains the controller and directives to manage the playground
 * @requires pascalprecht.translate
 * @requires talend.widget
 * @requires ui.router
 * @requires data-prep.datagrid
 * @requires data-prep.dataset-parameters
 * @requires data-prep.export
 * @requires data-prep.filter-bar
 * @requires data-prep.history-control
 * @requires data-prep.lookup
 * @requires data-prep.preparation-picker
 * @requires data-prep.recipe
 * @requires data-prep.services.onboarding
 * @requires data-prep.services.playground
 * @requires data-prep.services.preparation
 * @requires data-prep.services.state
 * @requires data-prep.suggestions-stats
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		uiRouter,
		DATAGRID_MODULE,
		DATASET_PARAMETERS_MODULE,
		EXPORT_MODULE,
		FILTER_BAR,
		HISTORY_CONTROL,
		LOOKUP_MODULE,
		PREPARATION_PICKER_MODULE,
		RECIPE_MODULE,
		SUGGESTION_STATS_MODULE,
		TALEND_WIDGET_MODULE,
		TALEND_WIDGET_CONTAINERS_MODULE,
		SERVICES_ONBOARDING_MODULE,
		SERVICES_PLAYGROUND_MODULE,
		SERVICES_PREVIEW_MODULE,
		SERVICES_PREPARATION_MODULE,
		SERVICES_STATE_MODULE,
	])
    .controller('PlaygroundCtrl', PlaygroundCtrl)
    .directive('playground', Playground)
    .component('playgroundHeader', PlaygroundHeader);

export default MODULE_NAME;
