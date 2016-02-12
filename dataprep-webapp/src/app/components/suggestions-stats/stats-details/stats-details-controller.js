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
 * @name data-prep.stats-details.controller:StatsDetailsCtrl
 * @description Statistics details
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.filter.service:FilterService
 * @requires data-prep.services.statisticsService.service:StatisticsService
 * @requires data-prep.services.statisticsService.service:StatisticsTooltipService
 */
export default function StatsDetailsCtrl(state, FilterService, StatisticsService, StatisticsTooltipService) {
    'ngInject';

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
            FilterService.addFilterAndDigest('matches', column.id, column.name, {pattern: item.pattern}) :
            FilterService.addFilterAndDigest('empty_records', column.id, column.name);
    };
}