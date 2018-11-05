/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterService
 * @description Filter service. This service provide the entry point to datagrid filters
 * @requires data-prep.services.statistics.service:StatisticsService
 * @requires data-prep.services.state.constant:state
 */
export default function FilterManagerService($timeout, state, PlaygroundService, StatisticsService, StorageService, FilterService) {
	'ngInject';

	const service = {
		addFilter,
		addFilterAndDigest,
		updateFilter,
		removeAllFilters,
		removeFilter,
		toggleFilters,
	};
	return service;

	/**
	 * @ngdoc method
	 * @name addFilter
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description Adds a filter and updates datagrid filters
	 */
	function addFilter(type, colId, colName, args, removeFilterFn, keyName) {
		FilterService.addFilter(type, colId, colName, args, removeFilterFn, keyName);
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
		_saveFilters();
	}

	/**
	 * @ngdoc method
	 * @name addFilterAndDigest
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @param {string} type The filter type (ex : contains)
	 * @param {string} colId The column id
	 * @param {string} colName The column name
	 * @param {string} args The filter arguments (ex for 'contains' type : {phrase: 'toto'})
	 * @param {function} removeFilterFn An optional remove callback
	 * @param {string} keyName keyboard key
	 * @description Wrapper on addFilter method that triggers a digest at the end (use of $timeout)
	 */
	function addFilterAndDigest(type, colId, colName, args, removeFilterFn, keyName) {
		$timeout(() => this.addFilter(type, colId, colName, args, removeFilterFn, keyName));
	}

	/**
	 * @ngdoc method
	 * @name removeAllFilters
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description Removes all the filters and updates datagrid filters
	 */
	function removeAllFilters() {
		FilterService.removeAllFilters();
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
		StorageService.removeFilter(state.playground.preparation ?
			state.playground.preparation.id : state.playground.dataset.id);
	}

	/**
	 * @ngdoc method
	 * @name removeFilter
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @param {object} filter The filter to delete
	 * @description Removes a filter and updates datagrid filters
	 */
	function removeFilter(filter) {
		FilterService.removeFilter(filter);
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
		_saveFilters();
	}

	/**
	 * @ngdoc method
	 * @name toggleFilters
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description Pushes a filter in the filter list and updates datagrid filters
	 */
	function toggleFilters() {
		FilterService.toggleFilters();
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
	}

	/**
	 * @ngdoc method
	 * @name _saveFilters
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description Saves filter in the localStorage
	 * @private
	 */
	function _saveFilters() {
		StorageService.saveFilter(
			state.playground.preparation ? state.playground.preparation.id : state.playground.dataset.id,
			state.playground.filter.gridFilters
		);
	}

	/**
	 * @ngdoc method
	 * @name updateFilter
	 * @param {Object} oldFilter Previous filter to update
	 * @param {string} newType The filter type to update
	 * @param {object} newValue The filter update parameters
	 * @param {string} keyName keyboard key
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description updates an existing filter and updates datagrid filters
	 */
	function updateFilter(oldFilter, newType, newValue, keyName) {
		FilterService.updateFilter(oldFilter, newType, newValue, keyName);
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
		_saveFilters();
	}
}
