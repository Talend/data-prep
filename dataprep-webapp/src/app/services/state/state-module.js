/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import { datasetState, DatasetStateService } from './dataset/dataset-state-service';
import { easterEggsState, EasterEggsStateService } from './easter-eggs/easter-eggs-state-service';
import { feedbackState, FeedbackStateService } from './feedback/feedback-state-service';
import { filterState, FilterStateService } from './filter/filter-state-service';
import { folderState, FolderStateService } from './folder/folder-state-service';
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
import { state, StateService } from './state-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.state
     * @description This module contains the service that hold the application state
     */
    angular.module('data-prep.services.state', [])
        .service('DatasetStateService', DatasetStateService)
        .constant('datasetState', datasetState)

        .service('EasterEggsStateService', EasterEggsStateService)
        .constant('easterEggsState', easterEggsState)

        .service('FeedbackStateService', FeedbackStateService)
        .constant('feedbackState', feedbackState)

        .service('FilterStateService', FilterStateService)
        .constant('filterState', filterState)

        .service('FolderStateService', FolderStateService)
        .constant('folderState', folderState)

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

        .service('StateService', StateService)
        .constant('state', state);
})();
