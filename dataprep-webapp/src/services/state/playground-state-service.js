(function() {
    'use strict';

    var playgroundState = {};

    function PlaygroundStateService(
        RecipeStateService, recipeState,
        GridStateService, gridState,
        FilterStateService, filterState) {

        playgroundState.recipe = recipeState;
        playgroundState.grid = gridState;
        playgroundState.filter = filterState;

        return {
            //playground
            show: show,
            hide: hide,
            setDataset: setDataset,
            setPreparation: setPreparation,
            setNameEditionMode: setNameEditionMode,
            reset: reset,
            setData: setData,
            setLookupVisibility: setLookupVisibility,

            //recipe
            showRecipe: RecipeStateService.show,
            hideRecipe: RecipeStateService.hide,

            //datagrid
            setColumnFocus: GridStateService.setColumnFocus,
            setGridSelection: GridStateService.setGridSelection,

            //filters
            addGridFilter: addGridFilter,
            updateGridFilter: updateGridFilter,
            removeGridFilter: removeGridFilter,
            removeAllGridFilters: removeAllGridFilters
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

        function setLookupVisibility(visibility) {
            playgroundState.lookupVisibility = visibility;
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
            playgroundState.lookupVisibility = false;

            GridStateService.reset();
            FilterStateService.reset();
        }
    }

    angular.module('data-prep.services.state')
        .service('PlaygroundStateService', PlaygroundStateService)
        .constant('playgroundState', playgroundState);
})();