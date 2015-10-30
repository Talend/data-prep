(function() {
    'use strict';

    var filterState = {
        gridFilters: [],
        applyTransformationOnFilters: false
    };

    function FilterStateService() {
        return {
            //common
            reset: reset,

            //grid
            addGridFilter: addGridFilter,
            updateGridFilter: updateGridFilter,
            removeGridFilter: removeGridFilter,
            removeAllGridFilters: removeAllGridFilters
        };

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------------GRID-----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        function addGridFilter(filterInfo) {
            var _aux = filterState.gridFilters;
            filterState.gridFilters = _aux.slice(0);
            filterState.gridFilters.push(filterInfo);

            if(filterState.gridFilters.length && checkRelevantChanges(filterState.gridFilters, _aux)){
                filterState.applyTransformationOnFilters = true;
            }

        }

        function updateGridFilter(oldFilter, newFilter) {
            var index = filterState.gridFilters.indexOf(oldFilter);
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters[index] = newFilter;
        }

        function removeGridFilter(filterInfo) {
            var index = filterState.gridFilters.indexOf(filterInfo);
            var _aux = filterState.gridFilters;
            filterState.gridFilters = _aux.slice(0);
            filterState.gridFilters.splice(index, 1);

            if(filterState.gridFilters.length === 0  && checkRelevantChanges(filterState.gridFilters, _aux)){
                //in order to keep All the lines radio selected
                filterState.applyTransformationOnFilters = false;
            }
        }

        function removeAllGridFilters() {
            filterState.gridFilters = [];
            filterState.applyTransformationOnFilters = false;
        }

        function checkRelevantChanges(newArr, ancientArr){
            return !(newArr.length && ancientArr.length);//the real condition is : !!(newArr.length) !== !!(ancientArr.length);
        }

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------------COMMON-----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        function reset() {
            removeAllGridFilters();
        }
    }

    angular.module('data-prep.services.state')
        .service('FilterStateService', FilterStateService)
        .constant('filterState', filterState);
})();