(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.stats-details.controller:StatsDetailsCtrl
     * @description Statistics details
     * @requires data-prep.services.filter.service:FilterService
     * @requires data-prep.services.statisticsService.service:StatisticsService
     * @requires data-prep.services.statisticsService.service:StatisticsTooltipService
     */
    function StatsDetailsCtrl(state, FilterService, StatisticsService, StatisticsTooltipService) {
        var vm = this;
        vm.state = state;
        vm.statisticsService = StatisticsService;
        vm.statisticsTooltipService = StatisticsTooltipService;

        /**
         * @ngdoc method
         * @name addPatternFilter
         * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
         * @param {object} item Pattern object (ex : {'pattern':'aaa','occurrences':8})
         * @description Add a pattern filter from selected pattern item
         */
        vm.addPatternFilter = function addPatternFilter(item) {
            var column = state.playground.grid.selectedColumn;
            return item.pattern ?
                FilterService.addFilterAndDigest('matches', column.id, column.name, {
                    pattern: item.pattern
                }) :
                FilterService.addFilterAndDigest('empty_records', column.id, column.name);
        };
    }

    angular.module('data-prep.stats-details')
        .controller('StatsDetailsCtrl', StatsDetailsCtrl);
})();