(function() {
    'use strict';

    var filterState = {
        gridFilters: []
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
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters.push(filterInfo);
        }

        function updateGridFilter(oldFilter, newFilter) {
            var index = filterState.gridFilters.indexOf(oldFilter);
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters[index] = newFilter;
        }

        function removeGridFilter(filterInfo) {
            var index = filterState.gridFilters.indexOf(filterInfo);
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters.splice(index, 1);
        }

        function removeAllGridFilters() {
            filterState.gridFilters = [];
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