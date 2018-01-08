/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/
import { CTRL_KEY_NAME } from '../../../services/filter/filter-service.js';

/**
 * @ngdoc controller
 * @name data-prep.stats-details.controller:StatsDetailsCtrl
 * @description Statistics details
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.filter-manager.service:FilterManagerService
 * @requires data-prep.services.statisticsService.service:StatisticsService
 * @requires data-prep.services.statisticsService.service:StatisticsTooltipService
 */
export default function StatsDetailsCtrl(state, FilterManagerService, StatisticsService, StatisticsTooltipService) {
	'ngInject';

	const vm = this;
	vm.state = state;
	vm.statisticsService = StatisticsService;
	vm.statisticsTooltipService = StatisticsTooltipService;
	vm.addPatternFilter = addPatternFilter;

    /**
     * @ngdoc method
     * @name addPatternFilter
     * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
     * @param {object} item Pattern object (ex : {'pattern':'aaa','occurrences':8})
     * @description Add a pattern filter from selected pattern item
     */
	function addPatternFilter(item, keyName = null) {
		const column = state.playground.grid.selectedColumns[0];
		const args = {
			patterns: [
				{
					value: item.pattern,
				},
			],
		};
		return item.pattern || keyName === CTRL_KEY_NAME ?
			FilterManagerService.addFilterAndDigest('matches', column.id, column.name, args, null, keyName) :
			FilterManagerService.addFilterAndDigest('empty_records', column.id, column.name, null, null, keyName);
	}
}
