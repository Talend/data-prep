/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './filter-list.html';

/**
 * @ngdoc directive
 * @name data-prep.filter-list.directive:FilterList
 * @description This directive display the filter list as badges. It consumes the filter list from {@link data-prep.services.filter.service:FilterService FilterService}
 * @restrict E
 * @usage
 * <filter-list filters="filters"
 *              on-filter-change="change(filter, value)"
 *              on-filter-remove="remove(filter)"></filter-list>
 * @param {array} filters The filters to display
 * @param {function} onFilterChange The change filter callback
 * @param {function} onFilterRemove The remove filter callback
 * @param {boolean} enabled Filters are enabled or disabled
 */
export default function FilterList() {
	return {
		restrict: 'E',
		templateUrl: template,
		scope: {
			filters: '=',
			onFilterChange: '&',
			onFilterRemove: '&',
			enabled: '<',
		},
		bindToController: true,
		controllerAs: 'filterCtrl',
		controller: 'FilterListCtrl',
	};
}
