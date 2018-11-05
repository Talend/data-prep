/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { chain, find, isEqual, remove, some } from 'lodash';
import {
	CONTAINS,
	EXACT,
	INSIDE_RANGE,
	MATCHES,
	MATCHES_WORDS,
	QUALITY,
} from './adapter/tql-filter-adapter-service';

export const RANGE_SEPARATOR = ' .. ';
export const INTERVAL_SEPARATOR = ',';
export const SHIFT_KEY_NAME = 'shift';
export const CTRL_KEY_NAME = 'ctrl';

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterService
 * @description Filter service. This service provide the entry point to datagrid filters
 * @requires data-prep.services.filter.service:TqlFilterAdapterService
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 * @requires data-prep.services.utils.service:DateService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class FilterService {

	constructor(state, StateService, TqlFilterAdapterService, ConverterService, TextFormatService, DateService, StorageService) {
		'ngInject';

		this.state = state;
		this.StateService = StateService;
		this.TqlFilterAdapterService = TqlFilterAdapterService;
		this.ConverterService = ConverterService;
		this.TextFormatService = TextFormatService;
		this.DateService = DateService;
		this.StorageService = StorageService;
	}

	//----------------------------------------------------------------------------------------------
	// ---------------------------------------------------FILTER LIFE-------------------------------
	//----------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name initFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} entityId The preparation (or the dataset) id
	 * @description Init filter in the playground
	 */
	initFilters(entityId) {
		const filters = this.StorageService.getFilter(entityId);
		filters.forEach((filter) => {
			this.addFilter(filter.type, filter.colId, filter.colName, filter.args, null, '', false);
		});
	}

	/**
	 * @ngdoc method
	 * @name addFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} type The filter type (ex : contains)
	 * @param {string} colId The column id
	 * @param {string} colName The column name
	 * @param {object} args The filter arguments (ex for 'contains' type : {phrase: 'toto'})
	 * @param {function} removeFilterFn An optional remove callback
	 * @param {string} keyName keyboard key
	 * @description Adds a filter
	 */
	addFilter(type, colId, colName, args, removeFilterFn, keyName, shouldEscape) {
		const sameColAndTypeFilter = find(this.state.playground.filter.gridFilters, {
			colId,
			type,
		});

		let filterFn;
		let createFilter;
		let getFilterValue;
		let filterExists;
		let argsToDisplay;
		let hasEmptyRecordsExactFilter;
		let hasEmptyRecordsMatchFilter;

		switch (type) {
		case CONTAINS: {
				// If we want to select records and a empty filter is already applied to that column
				// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);
			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.phrase = this.TqlFilterAdapterService.getEmptyRecordsValues()
							.concat(args.phrase);
				}
			}

			if (args.phrase.length === 1 && args.phrase[0].value === '') {
				args.phrase = this.TqlFilterAdapterService.getEmptyRecordsValues();
			}

			argsToDisplay = {
				phrase: this._getValuesToDisplay(args.phrase, shouldEscape),
				caseSensitive: args.caseSensitive,
			};

			createFilter = () => {
				return this.TqlFilterAdapterService.createFilter(type, colId, colName, true, argsToDisplay, removeFilterFn);
			};

			getFilterValue = () => {
				return argsToDisplay.phrase;
			};

			filterExists = () => {
				if (sameColAndTypeFilter &&
						sameColAndTypeFilter.args &&
						sameColAndTypeFilter.args.phrase) {
					return isEqual(
							sameColAndTypeFilter.args.phrase
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
							argsToDisplay.phrase
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
						);
				}

				return false;
			};

			break;
		}
		case EXACT: {
				// If we want to select records and a empty filter is already applied to that column
				// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);

			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.phrase = this.TqlFilterAdapterService.getEmptyRecordsValues()
							.concat(args.phrase);
				}
			}

			if (args.phrase.length === 1 && args.phrase[0].value === '') {
				args.phrase = this.TqlFilterAdapterService.getEmptyRecordsValues();
			}

			argsToDisplay = {
				phrase: this._getValuesToDisplay(args.phrase, shouldEscape),
				caseSensitive: args.caseSensitive,
			};

			createFilter = () => {
				return this.TqlFilterAdapterService.createFilter(type, colId, colName, true, argsToDisplay, filterFn, removeFilterFn);
			};

			getFilterValue = () => {
				return argsToDisplay.phrase;
			};

			filterExists = () => {
				if (sameColAndTypeFilter &&
						sameColAndTypeFilter.args &&
						sameColAndTypeFilter.args.phrase) {
					return isEqual(
							sameColAndTypeFilter.args.phrase
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
							args.phrase
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
						);
				}

				return false;
			};

			break;
		}
		case QUALITY: {
			if (args && args.empty && !args.invalid) {
					// If we want to select empty records and another filter is already applied to that column
					// Then we need remove it before
				const sameColExactFilter = find(this.state.playground.filter.gridFilters, {
					colId,
					type: EXACT,
				});
				const sameColMatchFilter = find(this.state.playground.filter.gridFilters, {
					colId,
					type: MATCHES,
				}) || find(this.state.playground.filter.gridFilters, {
					colId,
					type: MATCHES_WORDS,
				});
				if (sameColExactFilter) {
					hasEmptyRecordsExactFilter = (
							sameColExactFilter.args
							&& sameColExactFilter.args.phrase.length === 1
							&& sameColExactFilter.args.phrase[0].value === ''
						);
					this.removeFilter(sameColExactFilter);
				}
				else if (sameColMatchFilter) {
					hasEmptyRecordsMatchFilter = (
							sameColMatchFilter.args &&
							sameColMatchFilter.args.patterns.length === 1 &&
							sameColMatchFilter.args.patterns[0].value === ''
						);
					this.removeFilter(sameColMatchFilter);
				}
			}

			createFilter = () => {
				const qualityFilter = this._getQualityFilters(colId);
				if (qualityFilter) {
					this.removeFilter(qualityFilter);
				}
				return this.TqlFilterAdapterService.createFilter(type, colId, colName, false, args, removeFilterFn);
			};

			filterExists = () => {
				return sameColAndTypeFilter;
			};

			break;
		}
		case INSIDE_RANGE: {
			createFilter = () => {
				return this.TqlFilterAdapterService.createFilter(type, colId, colName, false, args, removeFilterFn);
			};

			getFilterValue = () => args.intervals;

			filterExists = () => {
				if (sameColAndTypeFilter &&
						sameColAndTypeFilter.args &&
						sameColAndTypeFilter.args.intervals) {
					return isEqual(
							sameColAndTypeFilter.args.intervals
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
							args.intervals
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
						);
				}

				return false;
			};

			break;
		}
		case MATCHES:
		case MATCHES_WORDS: {
				// If we want to select records and a empty filter is already applied to that column
				// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);
			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.patterns = this.TqlFilterAdapterService.getEmptyRecordsValues()
							.concat(args.patterns);
				}
			}

			const sameColAndOtherTypeFilter = find(this.state.playground.filter.gridFilters, {
				colId,
				type: type === MATCHES ? MATCHES_WORDS : MATCHES,
			});
			if (sameColAndOtherTypeFilter) {
				this.removeFilter(sameColAndOtherTypeFilter);
			}

			if (args.patterns.length === 1 && args.patterns[0].value === '') {
				args.patterns = this.TqlFilterAdapterService.getEmptyRecordsValues();
			}

			argsToDisplay = {
				patterns: this._getValuesToDisplay(args.patterns, shouldEscape),
				caseSensitive: args.caseSensitive,
			};

			createFilter = () => {
				return this.TqlFilterAdapterService.createFilter(type, colId, colName, false, argsToDisplay, removeFilterFn);
			};

			getFilterValue = () => {
				return argsToDisplay.patterns;
			};

			filterExists = () => {
				if (sameColAndTypeFilter &&
						sameColAndTypeFilter.args &&
						sameColAndTypeFilter.args.patterns) {
					return isEqual(
							sameColAndTypeFilter.args.patterns
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
							args.patterns
								.map(criterion => (criterion.label || criterion.value))
								.reduce((oldValue, newValue) => oldValue.concat(newValue)),
						);
				}
			};

			break;
		}
		}

		if ((!sameColAndTypeFilter &&
			!hasEmptyRecordsExactFilter &&
			!hasEmptyRecordsMatchFilter) || type === QUALITY) {
			if (createFilter) {
				this.pushFilter(createFilter());
			}
		}
		else if (filterExists()) {
			this.removeFilter(sameColAndTypeFilter);
		}
		else {
			const filterValue = getFilterValue();
			this.updateFilter(sameColAndTypeFilter, type, filterValue, keyName);
		}
	}

	/**
	 * @ngdoc method
	 * @name removeFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} filter The filter to delete
	 * @description Removes a filter
	 */
	removeFilter(filter) {
		this.StateService.removeGridFilter(filter);
		if (filter.removeFilterFn) {
			filter.removeFilterFn(filter);
		}
	}

	/**
	 * @ngdoc method
	 * @name updateColumnNameInFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {array} columns The columns
	 * @description Updates the columns name in the filter's label
	 */
	updateColumnNameInFilters(columns) {
		this.StateService.updateColumnNameInFilters(columns);
	}

	/**
	 * @ngdoc method
	 * @name updateFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} oldFilter The filter to update
	 * @param {string} newType The new filter if different
	 * @param {object} newValue The filter update parameters
	 * @param {string} keyName keyboard key
	 * @description Updates an existing filter
	 */
	updateFilter(oldFilter, newType, newValue, keyName) {
		let newArgs;
		let editableFilter;

		let newComputedValue;

		const addOrCriteria = keyName === CTRL_KEY_NAME;
		const addFromToCriteria = keyName === SHIFT_KEY_NAME;

		switch (oldFilter.type) {
		case CONTAINS: {
			if (addOrCriteria) {
				newComputedValue = this._computeOr(oldFilter.args.phrase, newValue);
			}
			else {
				newComputedValue = newValue;
			}

			newArgs = {
				phrase: newComputedValue,
			};
			editableFilter = true;
			break;
		}
		case EXACT: {
			if (addOrCriteria) {
				newComputedValue = this._computeOr(oldFilter.args.phrase, newValue);
			}
			else {
				newComputedValue = newValue;
			}

			newArgs = {
				phrase: newComputedValue,
				caseSensitive: oldFilter.args.caseSensitive,
			};
			editableFilter = true;
			break;
		}
		case INSIDE_RANGE: {
			let newComputedArgs;
			let newComputedRange;
			if (addFromToCriteria) {
					// Need to pass complete old filter there in order to stock its direction
				newComputedArgs = this._computeFromToRange(oldFilter, newValue);
				newComputedRange = newComputedArgs.intervals;
			}
			else if (addOrCriteria) {
				newComputedRange = this._computeOr(oldFilter.args.intervals, newValue);
			}
			else {
				newComputedRange = newValue;
			}

			if (newComputedArgs) {
				newArgs = newComputedArgs;
			}
			else {
				newArgs = {
					intervals: newComputedRange,
					type: oldFilter.args.type,
				};
			}

			editableFilter = false;
			break;
		}
		case MATCHES:
		case MATCHES_WORDS: {
			let newComputedPattern;
			if (addOrCriteria) {
				newComputedPattern = this._computeOr(oldFilter.args.patterns, newValue);
			}
			else {
				newComputedPattern = newValue;
			}

			newArgs = {
				patterns: newComputedPattern,
			};
			editableFilter = false;
			break;
		}
		}

		const newFilter = this.TqlFilterAdapterService.createFilter(newType, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, oldFilter.removeFilterFn);
		this.StateService.updateGridFilter(oldFilter, newFilter);
	}

	/**
	 * @name _computeOr
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Create filter values with Or criteria
	 * @param oldCriteria Previous filter to update
	 * @param newCriteria New filter value
	 * @returns {Array} Filter values with Or criteria
	 * @private
	 */
	_computeOr(oldCriteria, newCriteria) {
		let mergedCriteria = [];
		newCriteria.forEach((criterion) => {
			if (some(oldCriteria, criterion)) {
				remove(oldCriteria, criterion);
			}
			else {
				oldCriteria.push(criterion);
			}

			mergedCriteria = oldCriteria;
		});
		return mergedCriteria;
	}

	/**
	 * @name _computeFromToRange
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Creates filter values with From To criteria
	 * @param oldFilter Previous filter to update
	 * @param newValue New filter value
	 * @returns {Object} Filter values with From To criteria
	 * @private
	 */
	_computeFromToRange(oldFilter, newValue) {
		const oldFilterArgs = oldFilter.args;
		const oldIntervals = oldFilterArgs.intervals;
		const oldDirection = oldFilterArgs.direction || 1;

		newValue.forEach((newInterval) => {
			// Identify min and max old interval
			const oldMinInterval = this._findMinInterval(oldIntervals);
			const oldMaxInterval = this._findMaxInterval(oldIntervals);

			// Identify min and max from previous intervals
			const oldMin = oldMinInterval.value[0];
			const oldMax = oldMaxInterval.value[1] || oldMaxInterval.value[0];
			const oldMinLabel = this._getSplittedRangeLabelFor(oldMinInterval.label);
			const oldMaxLabel = this._getSplittedRangeLabelFor(oldMaxInterval.label);

			// Identify min and max from new interval
			const newMin = newInterval.value[0];
			const newMax = newInterval.value[1] || newMin;
			const newLabel = this._getSplittedRangeLabelFor(newInterval.label);

			// Identify the appropriated closing bound
			const closing = newInterval.excludeMax ? '[' : ']';

			let mergedInterval;
			let newDirection = oldFilter.direction;
			const updateMinInterval = () => {
				newDirection = 1;
				mergedInterval = oldMinInterval;
				mergedInterval.value[1] = newMax;
				mergedInterval.label = `[${oldMinLabel[0]}${RANGE_SEPARATOR}${newLabel[1] || newLabel[0]}${closing}`;
				mergedInterval.excludeMax = newInterval.excludeMax;
			};

			const updateMaxInterval = () => {
				newDirection = -1;
				mergedInterval = oldMaxInterval;
				mergedInterval.value[0] = newMin;
				mergedInterval.label = `[${newLabel[0]}${RANGE_SEPARATOR}${oldMaxLabel[1] || oldMaxLabel[0]}${closing}`;
				mergedInterval.excludeMax = newInterval.excludeMax;
			};

			// Compare old and new interval values
			if (newMin >= oldMin) {
				if (oldDirection < 0) {
					if (newMax >= oldMax) {
						// after current maximum and direction is <-
						updateMinInterval();
					}
					else {
						// between current min and current maximum and direction is <-
						updateMaxInterval();
					}
				}
				else {
					// between current min and current maximum and direction is ->
					updateMinInterval();
				}
			}
			else {
				// before current minimum and direction is ->
				updateMaxInterval();
			}

			// Store direction
			oldFilterArgs.direction = newDirection;
		});

		return oldFilterArgs;
	}

	/**
	 * @ngdoc method
	 * @name pushFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} filter The filter to push
	 * @description Pushes a filter in the filter list
	 */
	pushFilter(filter) {
		this.StateService.addGridFilter(filter);
	}

	/**
	 * @ngdoc method
	 * @name removeAllFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Removes all the filters
	 */
	removeAllFilters() {
		const filters = this.state.playground.filter.gridFilters;
		this.StateService.removeAllGridFilters();

		chain(filters)
			.filter(filter => filter.removeFilterFn)
			.forEach(filter => filter.removeFilterFn(filter))
			.value();
	}

	/**
	 * @ngdoc method
	 * @name toggleFilters
	 * @methodOf data-prep.services.filter.service:FilterMonitorService
	 * @description enables/disables filters
	 */
	toggleFilters() {
		if (this.state.playground.filter.enabled) {
			this.StateService.disableFilters();
		}
		else {
			this.StateService.enableFilters();
		}
	}

	/**
	 * @ngdoc method
	 * @name _getValuesToDisplay
	 * @param {Array} filterValues The filter values to convert
	 * @param {boolean} shouldEscape if value should be escaped
	 * @description Replace new line character
	 * @private
	 */
	_getValuesToDisplay(filterValues, shouldEscape = true) {
		const regexp = new RegExp('\n', 'g');  // eslint-disable-line no-control-regex
		const regexpQuote = new RegExp('\'', 'g');  // eslint-disable-line no-control-regex
		return filterValues
			.map((filterValue) => {
				if (!filterValue.isEmpty && shouldEscape) {
					filterValue.label = filterValue.value.replace(regexp, '\\n');
					filterValue.value = filterValue.value.replace(regexpQuote, '\\\'');
				}

				return filterValue;
			});
	}

	/**
	 * Get empty filter on the provided column
	 * @param colId The column id
	 * @private
	 */
	_getEmptyFilter(colId) {
		return find(this.state.playground.filter.gridFilters, { colId, type: QUALITY, args: { empty: true, invalid: false } });
	}

	/**
	 * Get quality filters on the provided column
	 * @param colId The column id
	 * @private
	 */
	_getQualityFilters(colId) {
		return find(this.state.playground.filter.gridFilters, { colId, type: QUALITY });
	}

	/**
	 *
	 * @param intervals
	 * @returns {*}
	 * @private
	 */
	_findMinInterval(intervals) {
		return intervals
			.map(interval => interval)
			.reduce((oldV, newV) => {
				if (oldV.value[0] > newV.value[0]) {
					return newV;
				}
				else {
					return oldV;
				}
			});
	}

	/**
	 *
	 * @param intervals
	 * @returns {*}
	 * @private
	 */
	_findMaxInterval(intervals) {
		return intervals
			.map(interval => interval)
			.reduce((oldInterval, newInterval) => {
				if (oldInterval.value[1] < newInterval.value[1]) {
					return newInterval;
				}
				else {
					return oldInterval;
				}
			});
	}

	//----------------------------------------------------------------------------------------------
	// ---------------------------------------------------UTILS-------------------------------------
	//----------------------------------------------------------------------------------------------

	/**
	 * @name _getSplittedRangeLabelFor
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @description Splits range label into an array with its min and its max
	 * @param label Range label to split
	 * @returns {Array} Splitted range values as string
	 * @private
	 */
	_getSplittedRangeLabelFor(label) {
		let splittedLabel = [];
		label = label.replace(new RegExp(/(\[|])/g), ''); // eslint-disable-line no-control-regex
		if (label.indexOf(RANGE_SEPARATOR) > -1) {
			splittedLabel = label.split(RANGE_SEPARATOR);
		}
		else if (label.indexOf(INTERVAL_SEPARATOR) > -1) {
			splittedLabel = label.split(INTERVAL_SEPARATOR);
		}
		else {
			splittedLabel.push(label);
		}

		return splittedLabel;
	}

	stringify(filters) {
		return this.TqlFilterAdapterService.toTQL(filters);
	}
}
