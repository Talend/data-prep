(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.stats-details.controller:StatsDetailsCtrl
     * @description Statistics details
     * @requires data-prep.services.statisticsService.service:StatisticsService
     */
    function StatsDetailsCtrl(StatisticsService, state, FilterService) {
        var vm = this;
        vm.state = state;
        vm.statisticsService = StatisticsService;
        
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

    Object.defineProperty(StatsDetailsCtrl.prototype,
        'statistics', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.statisticsService.statistics;
            }
        });

    Object.defineProperty(StatsDetailsCtrl.prototype,
        'boxPlot', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.statisticsService.boxPlot;
            }
        });

    angular.module('data-prep.stats-details')
        .controller('StatsDetailsCtrl', StatsDetailsCtrl);
})();