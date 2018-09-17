/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/
import { CTRL_KEY_NAME } from '../../../services/filter/filter-service.js';
import {
	MATCHES,
	MATCHES_WORDS,
	QUALITY,
} from '../../../services/filter/adapter/tql-filter-adapter-service';

const PATTERNS_TYPE = {
	CHARACTER: 'character',
	WORD: 'word',
};

/**
 * @ngdoc controller
 * @name data-prep.stats-details.controller:StatsDetailsCtrl
 * @description Statistics details
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.filter-manager.service:FilterManagerService
 * @requires data-prep.services.statisticsService.service:StatisticsService
 * @requires data-prep.services.statisticsService.service:StatisticsTooltipService
 */
export default function StatsDetailsCtrl(state, $translate, FilterManagerService, StateService, StatisticsService, StatisticsTooltipService) {
	'ngInject';

	const vm = this;

	vm.state = state;
	vm.statisticsService = StatisticsService;
	vm.statisticsTooltipService = StatisticsTooltipService;
	vm.StateService = StateService;

	vm.addPatternFilter = addPatternFilter;
	vm.getCurrentPatterns = getCurrentPatterns;
	vm.onCharacterPatternSelect = onCharacterPatternSelect;
	vm.onWordPatternSelect = onWordPatternSelect;

	vm.tabs = [
		{
			key: 'stats-tab-chart',
			label: $translate.instant('STATS_TAB_CHART'),
			'data-feature': 'preparation.chart',
		},
		{
			key: 'stats-tab-value',
			label: $translate.instant('STATS_TAB_VALUE'),
			'data-feature': 'preparation.value',
		},
		{
			key: 'stats-tab-pattern',
			label: $translate.instant('STATS_TAB_PATTERN'),
			'data-feature': 'preparation.pattern',
		},
		{
			key: 'stats-tab-advanced',
			label: $translate.instant('STATS_TAB_ADVANCED'),
			'data-feature': 'preparation.advanced',
		},
	];
	vm.selectedTab = vm.tabs[0].key;

	vm.selectTab = function (event, item) {
		vm.selectedTab = item.key;
	};

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
			FilterManagerService.addFilterAndDigest(vm.patternType === PATTERNS_TYPE.WORD ? MATCHES_WORDS : MATCHES, column.id, column.name, args, null, keyName) :
			FilterManagerService.addFilterAndDigest(QUALITY, column.id, column.name, {
				empty: true,
				invalid: false,
			}, null, keyName);
	}

	/**
	 * @ngdoc method
	 * @name hasWordPatterns
	 * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
	 */
	function getCurrentPatterns() {
		const column = state.playground.grid.selectedColumns[0];
		if (vm.previousColumn === column) {
			// FIXME use state instead
			return vm.patternType === PATTERNS_TYPE.WORD
				? state.playground.statistics.wordPatterns
				: state.playground.statistics.patterns;
		}
		else {
			vm.previousColumn = column;
			delete vm.patternType;
		}
		if (!vm.patternType) {
			if (column.type === 'string' || column.type === 'boolean') {
				vm.patternType = PATTERNS_TYPE.CHARACTER;
				vm.onWordPatternSelect();
			}
			else {
				vm.patternType = PATTERNS_TYPE.W;
				vm.onCharacterPatternSelect();
			}
		}
		return vm.patternType === PATTERNS_TYPE.WORD
			? state.playground.statistics.wordPatterns
			: state.playground.statistics.patterns;
	}

	/**
	 * @ngdoc method
	 * @name onCharacterPatternSelect
	 * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
	 */
	function onCharacterPatternSelect() {
		vm.patternType = PATTERNS_TYPE.CHARACTER;
	}

	/**
	 * @ngdoc method
	 * @name onWordPatternSelect
	 * @methodOf data-prep.stats-details.controller:StatsDetailsCtrl
	 */
	function onWordPatternSelect() {
		vm.patternType = PATTERNS_TYPE.WORD;
	}
}
