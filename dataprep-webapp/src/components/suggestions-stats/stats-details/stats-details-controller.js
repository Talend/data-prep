(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.stats-details.controller:StatsDetailsCtrl
     * @description Statistics details
     * @requires data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.statisticsService.service:StatisticsService
     */
    function StatsDetailsCtrl(SuggestionService, StatisticsService, $translate) {
        var vm = this;
        vm.suggestionService = SuggestionService;
        vm.statisticsService = StatisticsService;
        
        /**
         * @ngdoc method
         * @name addPatternFilter
         * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
         * @param {object} item Pattern object (ex : {'pattern':'aaa','occurrences':8})
         * @description Add a pattern filter from selected pattern item
         */
        vm.addPatternFilter = function addPatternFilter(item) {
            alert($translate.instant('SELECT_PATTERN_IS') + item.pattern + '.' + $translate.instant('FILTERING_COMING_SOON'));
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

    Object.defineProperty(StatsDetailsCtrl.prototype,
        'rangeLimits', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.statisticsService.rangeLimits;
            }
        });

    Object.defineProperty(StatsDetailsCtrl.prototype,
        'patternFrequencyTable', {
            enumerable: true,
            configurable: false,
            get: function () {
                var column = this.suggestionService.currentColumn;
                return column ? column.statistics.patternFrequencyTable : null;
            }
        });

    angular.module('data-prep.stats-details')
        .controller('StatsDetailsCtrl', StatsDetailsCtrl);
})();