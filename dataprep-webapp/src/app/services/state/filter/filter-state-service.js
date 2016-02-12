/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const filterState = {
    gridFilters: [],
    applyTransformationOnFilters: false
};

export function FilterStateService() {
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
        var isFirstFilter = !filterState.gridFilters.length;
        filterState.gridFilters = filterState.gridFilters.slice(0);
        filterState.gridFilters.push(filterInfo);

        if (isFirstFilter) {
            filterState.applyTransformationOnFilters = true;
        }
    }

    function updateGridFilter(oldFilter, newFilter) {
        var index = filterState.gridFilters.indexOf(oldFilter);
        filterState.gridFilters = filterState.gridFilters.slice(0);
        filterState.gridFilters[index] = newFilter;
    }

    function removeGridFilter(filterInfo) {
        filterState.gridFilters = _.filter(filterState.gridFilters, function (nextFilter) {
            return nextFilter !== filterInfo;
        });

        if (filterState.gridFilters.length === 0) {
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