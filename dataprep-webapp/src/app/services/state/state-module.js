/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import { datasetState, DatasetStateService } from './dataset/dataset-state-service';
import { easterEggsState, EasterEggsStateService } from './easter-eggs/easter-eggs-state-service';
import { feedbackState, FeedbackStateService } from './feedback/feedback-state-service';
import { filterState, FilterStateService } from './filter/filter-state-service';
import { gridState, GridStateService } from './grid/grid-state-service';
import { inventoryState, InventoryStateService } from './inventory/inventory-state-service';
import { lookupState, LookupStateService } from './lookup/lookup-state-service';
import { parametersState, ParametersStateService } from './parameters/parameters-state-service';
import { playgroundState, PlaygroundStateService } from './playground/playground-state-service';
import { recipeState, RecipeStateService } from './recipe/recipe-state-service';
import { statisticsState, StatisticsStateService } from './statistics/statistics-state-service';
import { suggestionsState, SuggestionsStateService } from './suggestions/suggestions-state-service';
import { routeState, RouteStateService } from './route/route-state-service';
import { importState, ImportStateService } from './import/import-state-service';
import { exportState, ExportStateService } from './export/export-state-service';
import { homeState, HomeStateService } from './home/home-state-service';
import { progressState, ProgressStateService } from './progress/progress-state-service';
import { state, StateService } from './state-service';
import { searchState, SearchStateService } from './search/search-state-service';
import { messageState, MessageStateService } from './message/message-state-service';

import SERVICES_UTILS_MODULE from '../utils/utils-module';

const MODULE_NAME = 'data-prep.services.state';

/**
 * @ngdoc object
 * @name data-prep.services.state
 * @description This module contains the service that hold the application state
 */
angular.module(MODULE_NAME, [SERVICES_UTILS_MODULE])
	.service('DatasetStateService', DatasetStateService)
	.constant('datasetState', datasetState)

	.service('EasterEggsStateService', EasterEggsStateService)
	.constant('easterEggsState', easterEggsState)

	.service('FeedbackStateService', FeedbackStateService)
	.constant('feedbackState', feedbackState)

	.service('FilterStateService', FilterStateService)
	.constant('filterState', filterState)

	.service('GridStateService', GridStateService)
	.constant('gridState', gridState)

	.service('InventoryStateService', InventoryStateService)
	.constant('inventoryState', inventoryState)

	.service('LookupStateService', LookupStateService)
	.constant('lookupState', lookupState)

	.service('ParametersStateService', ParametersStateService)
	.constant('parametersState', parametersState)

	.service('PlaygroundStateService', PlaygroundStateService)
	.constant('playgroundState', playgroundState)

	.service('RecipeStateService', RecipeStateService)
	.constant('recipeState', recipeState)

	.service('StatisticsStateService', StatisticsStateService)
	.constant('statisticsState', statisticsState)

	.service('SuggestionsStateService', SuggestionsStateService)
	.constant('suggestionsState', suggestionsState)

	.service('RouteStateService', RouteStateService)
	.constant('routeState', routeState)

	.service('ImportStateService', ImportStateService)
	.constant('importState', importState)

	.service('ExportStateService', ExportStateService)
	.constant('exportState', exportState)

	.service('HomeStateService', HomeStateService)
	.constant('homeState', homeState)

	.service('ProgressStateService', ProgressStateService)
	.constant('progressState', progressState)

	.service('StateService', StateService)
	.constant('state', state)

	.service('SearchStateService', SearchStateService)
	.constant('searchState', searchState)

	.service('MessageStateService', MessageStateService)
	.constant('messageState', messageState);

export default MODULE_NAME;
