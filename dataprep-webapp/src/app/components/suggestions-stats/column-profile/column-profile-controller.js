/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
 * @description Column profile controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.statistics.service:StatisticsService
 * @requires data-prep.statistics.service:StatisticsTooltipService
 * @requires data-prep.services.filter.service:FilterService
 */
export default function ColumnProfileCtrl($translate, $timeout, state, StatisticsService, StatisticsTooltipService, FilterService) {
    'ngInject';

    const vm = this;
    vm.chartConfig = {};
    vm.state = state;
    vm.statisticsService = StatisticsService;
    vm.statisticsTooltipService = StatisticsTooltipService;
    vm.addBarchartFilter = addBarchartFilter;
    vm.addRangeFilter = addRangeFilter;
    vm.changeAggregation = changeAggregation;

    //------------------------------------------------------------------------------------------------------
    //------------------------------------------------FILTER------------------------------------------------
    //------------------------------------------------------------------------------------------------------
    function addExactFilter(value, keyName = null) {
        const column = state.playground.grid.selectedColumn;
        const args = {
            phrase: [
                {
                    value: value
                }
            ],
            caseSensitive: true
        };
        return value.length || keyName === FilterService.CTRL_KEY_NAME ?
            FilterService.addFilterAndDigest('exact', column.id, column.name, args, null, keyName) :
            FilterService.addFilterAndDigest('empty_records', column.id, column.name, null, null, keyName);
    }

    /**
     * @ngdoc property
     * @name addBarchartFilter
     * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description Add an "exact" case sensitive filter if the value is not empty, an "empty_records" filter otherwise
     * @type {array}
     */
    function addBarchartFilter(item, keyName) {
        return addExactFilter(item.data, keyName);
    }

    /**
     * @ngdoc method
     * @name addRangeFilter
     * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description Add an "range" filter
     * @param {object} interval The interval [min, max] to filter
     */
    function addRangeFilter(interval, keyName = null) {
        const selectedColumn = state.playground.grid.selectedColumn;
        const min = interval.min;
        const max = interval.max;
        const isDateRange = selectedColumn.type === 'date';
        const removeFilterFn = StatisticsService.getRangeFilterRemoveFn();
        const args = {
            intervals: [
                {
                    label: interval.label || FilterService.getRangeLabelFor(interval, isDateRange),
                    value: [min, max],
                    isMaxReached: interval.isMaxReached
                }
            ],
            type: selectedColumn.type
        };
        FilterService.addFilterAndDigest('inside_range', selectedColumn.id, selectedColumn.name, args, removeFilterFn, keyName);
    }

    function changeAggregation(column, aggregation) {
        if (aggregation) {
            StatisticsService.processAggregation(column, aggregation);
        }
        else {
            StatisticsService.processClassicChart();
        }
    }
}
