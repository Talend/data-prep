/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';
import {
	INVALID_RECORDS,
	EMPTY_RECORDS,
	INVALID_EMPTY_RECORDS,
	VALID_RECORDS,
	QUALITY,
} from '../../../services/filter/adapter/filter-adapter-service';

const RANGE_SEPARATOR = ' .. ';

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
		// utils
		getRangeLabelFor,

		// life
		addFilter,
		addFilterAndDigest,
		updateFilter,
		removeAllFilters,
		removeFilter,
		toggleFilters,
		createQualityFilter,
	};
	return service;

	//----------------------------------------------------------------------------------------------
	// ---------------------------------------------------UTILS-------------------------------------
	//----------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name getRangeLabelFor
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description Define an interval label
	 * @param {Object} interval
	 * @param {boolean} isDateRange
	 * @returns {string} interval label
	 */
	function getRangeLabelFor(interval, isDateRange) {
		let label;
		const formatDate = d3.time.format('%Y-%m-%d');
		const formatNumber = d3.format(',');
		let min;
		let max;
		if (isDateRange) {
			min = formatDate(new Date(interval.min));
			max = formatDate(new Date(interval.max));
		}
		else if (angular.isNumber(interval.min)) {
			min = formatNumber(interval.min);
			max = formatNumber(interval.max);
		}
		else {
			min = interval.min;
			max = interval.max;
		}

		if (min === max) {
			label = '[' + min + ']';
		}
		else {
			label = '[' + min + RANGE_SEPARATOR + max + (interval.isMaxReached ? ']' : '[');
		}

		return label;
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------FILTER LIFE------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------

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
	 * @param {object} newValue The filter update parameters
	 * @param {string} keyName keyboard key
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description updates an existing filter and updates datagrid filters
	 */
	function updateFilter(oldFilter, newValue, keyName) {
		FilterService.updateFilter(oldFilter, newValue, keyName);
		StatisticsService.updateFilteredStatistics();
		PlaygroundService.updateDatagrid();
		_saveFilters();
	}

	/**
	 * @ngdoc method
	 * @name createQualityFilter
	 * @param {string} type filter type
	 * @methodOf data-prep.services.filter-manager.service:FilterManagerService
	 * @description create a quality filter (valid, invalid, empty...)
	 */
	function createQualityFilter(type, column) {
		switch (type) {
		case VALID_RECORDS:
		case INVALID_RECORDS:
		case EMPTY_RECORDS:
			this.addFilterAndDigest(type, column && column.id, column && column.name);
			break;
		case INVALID_EMPTY_RECORDS:
			this.addFilterAndDigest(QUALITY, column && column.id, column && column.name, { invalid: true, empty: true });
			break;
		}
	}
}
