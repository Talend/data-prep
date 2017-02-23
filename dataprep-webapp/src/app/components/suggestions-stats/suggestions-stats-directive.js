/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/
const SuggestionsStats = {
	template: `
    <div class="suggestions-stats-details">
		<div class="suggestions-title" ng-switch="suggestionsStatsCtrl.state.playground.grid.selectedColumns.length > 1">
			<span class="title"
				  title="{{suggestionsStatsCtrl.state.playground.grid.selectedColumns[0].name}}"
				  ng-switch-when="false"
				  ng-if="suggestionsStatsCtrl.state.playground.grid.selectedColumns[0].name">
				{{suggestionsStatsCtrl.state.playground.grid.selectedColumns[0].name}}
			</span>
			<span class="title"
				  ng-switch-when="true"
				  translate="MULTI_COLUMNS_SELECTED"
				  translate-values="{nb: suggestionsStatsCtrl.state.playground.grid.selectedColumns.length}">
			</span>
		</div>
		<ng-transclude></ng-transclude>
    </div>`,
	transclude: true,
	controllerAs: 'suggestionsStatsCtrl',
	controller(state) {
		'ngInject';

		this.state = state;
	},
};

export default SuggestionsStats;
