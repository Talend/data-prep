/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc component
 * @name data-prep.theme-filters:themeFilters
 * @description This component renders filters as list with selection management
 * @usage
 *      <theme-filters
 *          filters="filters"
 *          selected-filter="selectedFilter"
 *          on-select="onSelect(filter)"
 *          disable-selection="false">
 *       </theme-filters>
 * @param {Array} filters Filters definition
 * @param {boolean} selectedFilter The selected filter
 * @param {function} onSelect Selection callback
 * @param {Boolean} disableSelection Disable selection
 * */

export default {
	bindings: {
		filters: '<',
		selectedFilter: '<',
		onSelect: '&',
		disableSelection: '<',
	},
	template: `
        <div class="theme-filter"
             ng-repeat="filter in $ctrl.filters track by filter.id"
             ng-class="{'selected': $ctrl.selectedFilter === filter}"
             ng-click="!$ctrl.disableSelection && $ctrl.onSelect({ filter })">
            <div class="theme-filter-icon">
                <div ng-if="::filter.imageUrl"><img ng-src="{{::filter.imageUrl}}"/></div>
                <div ng-if="::filter.icon" data-icon="{{filter.icon}}"></div>
            </div>
    
            <div class="theme-filter-text">
                <span class="theme-filter-title" translate-once="{{filter.title}}"></span>
                <div class="theme-filter-description" translate-once="{{filter.description}}"></div>
            </div>
        </div>
    `,
};
