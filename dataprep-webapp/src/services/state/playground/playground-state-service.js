(function() {
    'use strict';

    var playgroundState = {};

    function PlaygroundStateService(
        RecipeStateService, recipeState,
        GridStateService, gridState,
        FilterStateService, filterState,
        SuggestionsStateService, suggestionsState,
        LookupStateService, lookupState,
        StatisticsStateService, statisticsState) {

        playgroundState.recipe = recipeState;
        playgroundState.grid = gridState;
        playgroundState.lookup = lookupState;
        playgroundState.filter = filterState;
        playgroundState.suggestions = suggestionsState;
        playgroundState.statistics = statisticsState;

        return {
            //playground
            show: show,
            hide: hide,
            setDataset: setDataset,
            setPreparation: setPreparation,
            setNameEditionMode: setNameEditionMode,
            reset: reset,
            setData: setData,
            updateDatasetStatistics: updateDatasetStatistics,

            //recipe
            showRecipe: RecipeStateService.show,
            hideRecipe: RecipeStateService.hide,

            //datagrid
            setColumnFocus: GridStateService.setColumnFocus,
            setGridSelection: GridStateService.setGridSelection,

            //lookup
            setLookupActions: LookupStateService.setActions,
            setLookupAddMode: LookupStateService.setAddMode,
            setLookupSelectedColumn: LookupStateService.setSelectedColumn,
            setLookupUpdateMode: LookupStateService.setUpdateMode,
            setLookupVisibility: LookupStateService.setVisibility,
            updateLookupColumnsToAdd: LookupStateService.updateColumnsToAdd,

            //filters
            addGridFilter: addGridFilter,
            updateGridFilter: updateGridFilter,
            removeGridFilter: removeGridFilter,
            removeAllGridFilters: removeAllGridFilters,

            //suggestion
            setColumnTransformations: SuggestionsStateService.setColumnTransformations,
            setLineTransformations: SuggestionsStateService.setLineTransformations,
            setSuggestionsLoading: SuggestionsStateService.setLoading,
            setTransformationsForEmptyCells: SuggestionsStateService.setTransformationsForEmptyCells,
            setTransformationsForInvalidCells: SuggestionsStateService.setTransformationsForInvalidCells,
            updateFilteredTransformations: SuggestionsStateService.updateFilteredTransformations,

            //statistics
            setStatisticsHistogram: StatisticsStateService.setHistogram,
            setStatisticsFilteredHistogram: StatisticsStateService.setFilteredHistogram,
            setStatisticsHistogramActiveLimits: StatisticsStateService.setHistogramActiveLimits,
            setStatisticsPatterns: StatisticsStateService.setPatterns,
            setStatisticsFilteredPatterns: StatisticsStateService.setFilteredPatterns
        };

        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------PLAYGROUND--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        function setDataset(dataset) {
            playgroundState.dataset = dataset;
        }

        function setData(data) {
            playgroundState.data = data;
            GridStateService.setData(data);
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        }

        function setPreparation(preparation) {
            playgroundState.preparation = preparation;
        }

        function setNameEditionMode(editionMode) {
            playgroundState.nameEditionMode = editionMode;
        }

        function show() {
            playgroundState.visible = true;
        }

        function hide() {
            playgroundState.visible = false;
        }

        function updateDatasetStatistics(metadata) {
            _.forEach(playgroundState.data.metadata.columns, function(col) {
                var correspondingColumn = _.find(metadata.columns, {id: col.id});
                col.statistics = correspondingColumn.statistics;
            });
            playgroundState.dataset.records = metadata.records;
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------FILTERS----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        function addGridFilter(filter) {
            FilterStateService.addGridFilter(filter);
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        }

        function updateGridFilter(oldFilter, newFilter) {
            FilterStateService.updateGridFilter(oldFilter, newFilter);
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        }

        function removeGridFilter(filter) {
            FilterStateService.removeGridFilter(filter);
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        }

        function removeAllGridFilters() {
            FilterStateService.removeAllGridFilters();
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        }

        function reset() {
            playgroundState.data = null;
            playgroundState.dataset = null;
            playgroundState.preparation = null;
            playgroundState.nameEditionMode = false;
            playgroundState.lookupData = null;
            playgroundState.exportParameters = null;

            FilterStateService.reset();
            GridStateService.reset();
            LookupStateService.reset();
            SuggestionsStateService.reset();
            StatisticsStateService.reset();
        }
    }

    angular.module('data-prep.services.state')
        .service('PlaygroundStateService', PlaygroundStateService)
        .constant('playgroundState', playgroundState);
})();