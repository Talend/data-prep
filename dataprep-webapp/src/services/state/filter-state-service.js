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
            var isFirstFilter = ! filterState.gridFilters.length;
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters.push(filterInfo);

            if(isFirstFilter){
                filterState.applyTransformationOnFilters = true;
            }
        }

        function updateGridFilter(oldFilter, newFilter) {
            var index = filterState.gridFilters.indexOf(oldFilter);
            filterState.gridFilters = filterState.gridFilters.slice(0);
            filterState.gridFilters[index] = newFilter;
        }

        function removeGridFilter(filterInfo) {
            filterState.gridFilters = _.filter(filterState.gridFilters, function(nextFilter) {
                return nextFilter !== filterInfo;
            });

            if(filterState.gridFilters.length === 0){
                filterState.applyTransformationOnFilters = false;
            }
        }

        function removeAllGridFilters() {
            filterState.gridFilters = [];
            filterState.applyTransformationOnFilters = false;
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