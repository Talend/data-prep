/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const state = {};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:StateService
 * @description Entry level for State services
 */
export function StateService(RouteStateService, routeState, //
                             PlaygroundStateService, playgroundState, //
                             DatasetStateService, datasetState, //
                             EasterEggsStateService, easterEggsState, //
                             InventoryStateService, inventoryState,
                             FeedbackStateService, feedbackState,
                             ImportStateService, importState) {
    'ngInject';

    state.route = routeState;
    state.playground = playgroundState;
    state.dataset = datasetState;
    state.easterEggsState = easterEggsState;
    state.inventory = inventoryState;
    state.feedback = feedbackState;
    state.import = importState;

    return {
        //route
        setPreviousRoute: RouteStateService.setPrevious,
        setNextRoute: RouteStateService.setNext,
        resetPreviousRoute: RouteStateService.resetPrevious,
        resetNextRoute: RouteStateService.resetNext,
        resetRoute: RouteStateService.reset.bind(RouteStateService),

        //playground
        resetPlayground: PlaygroundStateService.reset,
        setCurrentDataset: PlaygroundStateService.setDataset,
        setCurrentData: PlaygroundStateService.setData,
        setCurrentPreparation: PlaygroundStateService.setPreparation,
        setIsFetchingStats: PlaygroundStateService.setIsFetchingStats,
        setNameEditionMode: PlaygroundStateService.setNameEditionMode,
        setPreparationName: PlaygroundStateService.setPreparationName,
        updateDatasetStatistics: PlaygroundStateService.updateDatasetStatistics,
        updateDatasetRecord: PlaygroundStateService.updateDatasetRecord,

        //playground - dataset parameters
        toggleDatasetParameters: PlaygroundStateService.toggleDatasetParameters,
        hideDatasetParameters: PlaygroundStateService.hideDatasetParameters,
        setIsSendingDatasetParameters: PlaygroundStateService.setIsSendingDatasetParameters,
        setDatasetEncodings: PlaygroundStateService.setDatasetEncodings,

        //playground - recipe
        hideRecipe: PlaygroundStateService.hideRecipe,
        showRecipe: PlaygroundStateService.showRecipe,

        //playground - grid
        setColumnFocus: PlaygroundStateService.setColumnFocus,
        setGridSelection: PlaygroundStateService.setGridSelection,

        //playground - lookup
        setLookupActions: PlaygroundStateService.setLookupActions,
        setLookupAddedActions: PlaygroundStateService.setLookupAddedActions,
        setLookupDatasets: PlaygroundStateService.setLookupDatasets,
        setLookupAddMode: PlaygroundStateService.setLookupAddMode,
        setLookupSelectedColumn: PlaygroundStateService.setLookupSelectedColumn,
        setLookupUpdateMode: PlaygroundStateService.setLookupUpdateMode,
        setLookupVisibility: PlaygroundStateService.setLookupVisibility,
        updateLookupColumnsToAdd: PlaygroundStateService.updateLookupColumnsToAdd,
        setLookupDatasetsSort: PlaygroundStateService.setLookupDatasetsSort,
        setLookupDatasetsOrder: PlaygroundStateService.setLookupDatasetsOrder,

        //playground - filters
        addGridFilter: PlaygroundStateService.addGridFilter,
        removeGridFilter: PlaygroundStateService.removeGridFilter,
        removeAllGridFilters: PlaygroundStateService.removeAllGridFilters,
        updateGridFilter: PlaygroundStateService.updateGridFilter,
        enableFilters: PlaygroundStateService.enableFilters,
        disableFilters: PlaygroundStateService.disableFilters,

        //playground - Suggestions
        setColumnTransformations: PlaygroundStateService.setColumnTransformations,
        setLineTransformations: PlaygroundStateService.setLineTransformations,
        setSuggestionsLoading: PlaygroundStateService.setSuggestionsLoading,
        setTransformationsForEmptyCells: PlaygroundStateService.setTransformationsForEmptyCells,
        setTransformationsForInvalidCells: PlaygroundStateService.setTransformationsForInvalidCells,
        updateFilteredTransformations: PlaygroundStateService.updateFilteredTransformations,

        //playground - Statistics
        setStatisticsBoxPlot: PlaygroundStateService.setStatisticsBoxPlot,
        setStatisticsDetails: PlaygroundStateService.setStatisticsDetails,
        setStatisticsRangeLimits: PlaygroundStateService.setStatisticsRangeLimits,
        setStatisticsHistogram: PlaygroundStateService.setStatisticsHistogram,
        setStatisticsFilteredHistogram: PlaygroundStateService.setStatisticsFilteredHistogram,
        setStatisticsHistogramActiveLimits: PlaygroundStateService.setStatisticsHistogramActiveLimits,
        setStatisticsPatterns: PlaygroundStateService.setStatisticsPatterns,
        setStatisticsFilteredPatterns: PlaygroundStateService.setStatisticsFilteredPatterns,

        //dataset
        startUploadingDataset: DatasetStateService.startUploadingDataset,
        finishUploadingDataset: DatasetStateService.finishUploadingDataset,

        //easter eggs
        enableEasterEgg: EasterEggsStateService.enableEasterEgg,
        disableEasterEgg: EasterEggsStateService.disableEasterEgg,

        //inventory
        setDatasetName: InventoryStateService.setDatasetName,
        setPreparations: InventoryStateService.setPreparations,
        removePreparation: InventoryStateService.removePreparation,
        setDatasets: InventoryStateService.setDatasets,
        removeDataset: InventoryStateService.removeDataset,
        setDatasetsSort: InventoryStateService.setDatasetsSort,
        setDatasetsOrder: InventoryStateService.setDatasetsOrder,
        setPreparationsSort: InventoryStateService.setPreparationsSort,
        setPreparationsOrder: InventoryStateService.setPreparationsOrder,

        setFolder: InventoryStateService.setFolder,
        setMenuChildren: InventoryStateService.setMenuChildren,

        //feedback
        showFeedback: FeedbackStateService.show,
        hideFeedback: FeedbackStateService.hide,

        //import
        setImportTypes: ImportStateService.setImportTypes,
        setCurrentImportType: ImportStateService.setCurrentImportType
    };
}