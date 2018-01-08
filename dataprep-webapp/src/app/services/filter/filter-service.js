/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { chain, find, isEqual, remove, some } from 'lodash';

import { EMPTY_RECORDS_LABEL } from './adapter/tql-filter-adapter-service';

export const RANGE_SEPARATOR = ' .. ';
export const INTERVAL_SEPARATOR = ',';
export const VALUES_SEPARATOR = '|';
export const SHIFT_KEY_NAME = 'shift';
export const CTRL_KEY_NAME = 'ctrl';

const emptyFilterValue = {
	label: EMPTY_RECORDS_LABEL,
	value: '',
	isEmpty: true,
};

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterService
 * @description Filter service. This service provide the entry point to datagrid filters
 * @requires data-prep.services.filter.service:FilterAdapterService
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 * @requires data-prep.services.utils.service:DateService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class FilterService {

	constructor(state, StateService, FilterAdapterService, TqlFilterAdapterService, ConverterService, TextFormatService, DateService, StorageService) {
		'ngInject';

		this.state = state;
		this.StateService = StateService;
		this.FilterAdapterService = FilterAdapterService;
		this.TqlFilterAdapterService = TqlFilterAdapterService;
		this.ConverterService = ConverterService;
		this.TextFormatService = TextFormatService;
		this.DateService = DateService;
		this.StorageService = StorageService;

		this.TQL_ENABLED = false;
		const { playground } = state;
		if (playground) {
			const { filter } = playground;
			this.TQL_ENABLED = filter && filter.isTQL;
		}
	}

	//----------------------------------------------------------------------------------------------
	// ---------------------------------------------------FILTER LIFE-------------------------------
	//----------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name initFilters
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} dataset The dataset
	 * @param {object} preparation The preparation
	 * @description Init filter in the playground
	 */
	initFilters(dataset, preparation) {
		const filters = this.StorageService.getFilter(preparation ? preparation.id : dataset.id);
		filters.forEach((filter) => {
			this.addFilter(filter.type, filter.colId, filter.colName, filter.args, null, '');
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
	addFilter(type, colId, colName, args, removeFilterFn, keyName) {
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
		case 'contains': {
			// If we want to select records and a empty filter is already applied to that column
			// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);
			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.phrase = [emptyFilterValue].concat(args.phrase);
				}
			}

			if (args.phrase.length === 1 && args.phrase[0].value === '') {
				args.phrase = [emptyFilterValue];
			}

			argsToDisplay = {
				phrase: this._getValuesToDisplay(args.phrase),
				caseSensitive: args.caseSensitive,
			};

			createFilter = () => {
				filterFn = this._createContainFilterFn(colId, args.phrase);
				return this.FilterAdapterService.createFilter(type, colId, colName, true, argsToDisplay, filterFn, removeFilterFn);
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
							.reduce((oldValue, newValue) => oldValue.concat(newValue))
					);
				}

				return false;
			};

			break;
		}
		case 'exact': {
			// If we want to select records and a empty filter is already applied to that column
			// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);

			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.phrase = [emptyFilterValue].concat(args.phrase);
				}
			}

			if (args.phrase.length === 1 && args.phrase[0].value === '') {
				args.phrase = [emptyFilterValue];
			}

			argsToDisplay = {
				phrase: this._getValuesToDisplay(args.phrase),
				caseSensitive: args.caseSensitive,
			};

			createFilter = () => {
				let filterAdaptaterService;
				if (this.TQL_ENABLED) {
					filterFn = () => {};
					filterAdaptaterService = this.TqlFilterAdapterService;
				}
				else {
					filterFn = this._createExactFilterFn(colId, args.phrase, args.caseSensitive);
					filterAdaptaterService = this.FilterAdapterService;
				}
				return filterAdaptaterService.createFilter(type, colId, colName, true, argsToDisplay, filterFn, removeFilterFn);
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
							.reduce((oldValue, newValue) => oldValue.concat(newValue))
					);
				}

				return false;
			};

			break;
		}
		case 'quality': {
			createFilter = () => {
				const qualityFilters = this._getQualityFilters(colId);
				if (qualityFilters.length) {
					this._removeQualityFilters(qualityFilters);
				}
				filterFn = this._createQualityFilterFn(colId, args);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
			};

			filterExists = () => {
				return sameColAndTypeFilter;
			};

			break;
		}
		case 'invalid_records': {
			createFilter = () => {
				const qualityFilters = this._getQualityFilters(colId);
				if (qualityFilters.length) {
					this._removeQualityFilters(qualityFilters);
				}
				filterFn = this._createInvalidFilterFn(colId);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
			};

			filterExists = () => {
				return sameColAndTypeFilter;
			};

			break;
		}
		case 'empty_records': {
			// If we want to select empty records and another filter is already applied to that column
			// Then we need remove it before
			const sameColExactFilter = find(this.state.playground.filter.gridFilters, {
				colId,
				type: 'exact',
			});
			const sameColMatchFilter = find(this.state.playground.filter.gridFilters, {
				colId,
				type: 'matches',
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

			createFilter = () => {
				const qualityFilters = this._getQualityFilters(colId);
				if (qualityFilters.length) {
					this._removeQualityFilters(qualityFilters);
				}

				filterFn = this._createEmptyFilterFn(colId);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
			};

			filterExists = () => {
				return sameColAndTypeFilter || hasEmptyRecordsExactFilter || hasEmptyRecordsMatchFilter;
			};

			break;
		}
		case 'valid_records': {
			createFilter = () => {
				const qualityFilters = this._getQualityFilters(colId);
				if (qualityFilters.length) {
					this._removeQualityFilters(qualityFilters);
				}

				filterFn = this._createValidFilterFn(colId);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
			};

			filterExists = () => {
				return sameColAndTypeFilter;
			};

			break;
		}
		case 'inside_range': {
			createFilter = () => {
				filterFn = args.type === 'date' ?
					this._createDateRangeFilterFn(colId, args.intervals) :
					this._createRangeFilterFn(colId, args.intervals);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
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
							.reduce((oldValue, newValue) => oldValue.concat(newValue))
					);
				}

				return false;
			};

			break;
		}
		case 'matches': {
			// If we want to select records and a empty filter is already applied to that column
			// Then we need remove it before
			const sameColEmptyFilter = this._getEmptyFilter(colId);
			if (sameColEmptyFilter) {
				this.removeFilter(sameColEmptyFilter);
				if (keyName === CTRL_KEY_NAME) {
					args.patterns = [emptyFilterValue].concat(args.patterns);
				}
			}

			if (args.patterns.length === 1 && args.patterns[0].value === '') {
				args.patterns = [emptyFilterValue];
			}

			createFilter = () => {
				filterFn = this.createMatchFilterFn(colId, args.patterns);
				return this.FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
			};

			getFilterValue = () => {
				return args.patterns;
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
							.reduce((oldValue, newValue) => oldValue.concat(newValue))
					);
				}
			};

			break;
		}
		}

		if (!sameColAndTypeFilter && !hasEmptyRecordsExactFilter && !hasEmptyRecordsMatchFilter) {
			const filterInfo = createFilter();
			this.pushFilter(filterInfo);
		}
		else if (filterExists()) {
			this.removeFilter(sameColAndTypeFilter);
		}
		else {
			const filterValue = getFilterValue();
			this.updateFilter(sameColAndTypeFilter, filterValue, keyName);
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
	 * @name updateFilter
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {object} oldFilter The filter to update
	 * @param {object} newValue The filter update parameters
	 * @param {string} keyName keyboard key
	 * @description Updates an existing filter
	 */
	updateFilter(oldFilter, newValue, keyName) {
		let newFilterFn;
		let newArgs;
		let editableFilter;

		let newComputedValue;

		const addOrCriteria = keyName === CTRL_KEY_NAME;
		const addFromToCriteria = keyName === SHIFT_KEY_NAME;

		switch (oldFilter.type) {
		case 'contains': {
			if (addOrCriteria) {
				newComputedValue = this._computeOr(oldFilter.args.phrase, newValue);
			}
			else {
				newComputedValue = newValue;
			}

			newArgs = {
				phrase: newComputedValue,
			};
			newFilterFn = this._createContainFilterFn(oldFilter.colId, newValue);
			editableFilter = true;
			break;
		}
		case 'exact': {
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

			if (this.TQL_ENABLED) {
				newFilterFn = () => {};
			}
			else {
				newFilterFn = this._createExactFilterFn(oldFilter.colId, newComputedValue, oldFilter.args.caseSensitive);
			}
			editableFilter = true;
			break;
		}
		case 'inside_range': {
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
			newFilterFn = newArgs.type === 'date' ?
				this._createDateRangeFilterFn(oldFilter.colId, newComputedRange) :
				this._createRangeFilterFn(oldFilter.colId, newComputedRange);
			break;
		}
		case 'matches': {
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
			newFilterFn = this.createMatchFilterFn(oldFilter.colId, newComputedPattern);
			editableFilter = false;
			break;
		}
		}

		let filterAdapterService;
		if (this.TQL_ENABLED) {
			filterAdapterService = this.TqlFilterAdapterService;
		}
		else {
			filterAdapterService = this.FilterAdapterService;
		}

		const newFilter = filterAdapterService.createFilter(oldFilter.type, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, newFilterFn, oldFilter.removeFilterFn);

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

			let mergedInterval;
			let newDirection = oldFilter.direction;

			const updateMinInterval = () => {
				newDirection = 1;
				mergedInterval = oldMinInterval;
				mergedInterval.value[1] = newMax;
				mergedInterval.label = `[${oldMinLabel[0]}${RANGE_SEPARATOR}${newLabel[1] || newLabel[0]}[`;
			};

			const updateMaxInterval = () => {
				newDirection = -1;
				mergedInterval = oldMaxInterval;
				mergedInterval.value[0] = newMin;
				mergedInterval.label = `[${newLabel[0]}${RANGE_SEPARATOR}${oldMaxLabel[1] || oldMaxLabel[0]}[`;
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

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------FILTER FNs-------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	// To add a new filter function, you must follow this steps.
	// A filter function has 2 levels of functions : (data) => (item) => {predicate}
	// * the first level is the initialization level. It takes the data {columns: [], records: []} as parameter. The goal is to initialize the values for the closure it returns.
	// * the second level is the predicate that is applied on every record item. It returns 'true' if it matches the predicate, 'false' otherwise.
	//
	// Example :
	//    return function(data) {                                                       // first level: it init the list of invalid values, based on the current data. It returns the predicate that use this list.
	//        var column = find(data.metadata.columns, {id: '0001'});
	//        var invalidValues = column.quality.invalidValues;
	//        return function (item) {                                                  // second level : returns true if the item is not in the invalid values list
	//            return item['0001'] && invalidValues.indexOf(item['0001']) === -1;
	//        };
	//    };
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name _createRangeFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {Array} intervals The filter interval
	 * @description Create a 'range' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	_createRangeFilterFn(colId, intervals) {
		return () => (item) => {
			if (!this.ConverterService.isNumber(item[colId])) {
				return false;
			}

			const numberValue = this.ConverterService.adaptValue('numeric', item[colId]);
			return intervals
				.map((interval) => {
					const values = interval.value;
					const pairMin = values[0];
					const pairMax = values[1];
					return interval.isMaxReached ?
						(numberValue === pairMin) || (numberValue > pairMin && numberValue <= pairMax) :
						(numberValue === pairMin) || (numberValue > pairMin && numberValue < pairMax);
				})
				.reduce((oldResult, newResult) => oldResult || newResult);
		};
	}

	/**
	 * @ngdoc method
	 * @name _createDateRangeFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {Array} values The filter interval
	 * @description Create a 'range' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	_createDateRangeFilterFn(colId, values) {
		const column = this.state.playground.grid.columns.find(col => col.id === colId);
		const patterns = chain(column.statistics.patternFrequencyTable)
			.map('pattern')
			.map(this.TextFormatService.convertJavaDateFormatToMomentDateFormat)
			.value();
		const getValueInDateLimitsFn = (value) => {
			const minTimestamp = value[0];
			const maxTimestamp = value[1];
			return this.DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns);
		};

		const valueInDateLimitsFn = item => values
			.map(dateRange => getValueInDateLimitsFn(dateRange.value)(item))
			.reduce((oldResult, newResult) => oldResult || newResult);
		return () => item => valueInDateLimitsFn(item[colId]);
	}

	/**
	 * @ngdoc method
	 * @name createMatchFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {Array} patterns The filter patterns
	 * @description Create a 'match' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	createMatchFilterFn(colId, patterns) {
		const hasEmptyRecords = patterns
			.filter(patternValue => patternValue.isEmpty)
			.length;
		const patternValues = patterns
			.map(patternValue => patternValue.value);
		const valueMatchPatternFns = item => patternValues
			.map(pattern => this.TextFormatService.valueMatchPatternFn(pattern)(item))
			.reduce((oldResult, newResult) => oldResult || newResult);
		return () => (item) => {
			return hasEmptyRecords ? (!item[colId] || valueMatchPatternFns(item[colId])) : valueMatchPatternFns(item[colId]);
		};
	}

	/**
	 * @ngdoc method
	 * @name _createExactFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {string} filterValues The phrase that the item must be exactly equal to
	 * @param {boolean} caseSensitive Determine if the filter is case sensitive
	 * @description [PRIVATE] Create a filter function that test exact equality
	 * @returns {function} The predicate function
	 * @private
	 */
	_createExactFilterFn(colId, filterValues, caseSensitive) {
		const hasEmptyRecords = filterValues
			.filter(filterValue => filterValue.isEmpty)
			.length;
		const flattenFiltersValues = filterValues
			.filter(filterValue => !filterValue.isEmpty)
			.map(filterValue => this.TextFormatService.escapeRegexpExceptStar(filterValue.value))
			.join(VALUES_SEPARATOR);
		const regExpPatternToMatch = `^(${flattenFiltersValues})$`;
		const regExpToMatch = caseSensitive ? new RegExp(regExpPatternToMatch) : new RegExp(regExpPatternToMatch, 'i');
		return () => (item) => {
			// col could be removed by a step
			const currentItem = item[colId];
			if (hasEmptyRecords) {
				return !currentItem || currentItem.match(regExpToMatch);
			}

			if (currentItem) {
				return currentItem.match(regExpToMatch) !== null;
			}
			return false;
		};
	}

	/**
	 * @ngdoc method
	 * @name _createContainFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {string} phrase The phrase that the item must contain
	 * @description [PRIVATE] Create a 'contains' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	_createContainFilterFn(colId, phrase) {
		const regexps = phrase
			.map((phraseValue) => {
				const lowerCasePhrase = phraseValue.value.toLowerCase();
				return new RegExp(this.TextFormatService.escapeRegexpExceptStar(lowerCasePhrase));
			});
		const fns = item => regexps
			.map(regexp => item.toLowerCase().match(regexp))
			.reduce((newResult, oldResult) => newResult || oldResult);
		return () => (item) => {
			// col could be removed by a step
			const currentItem = item[colId];
			if (currentItem) {
				return fns(currentItem);
			}
			return false;
		};
	}

	/**
	 * @ngdoc method
	 * @name _createInvalidFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @description Create a filter function that test if the value is one of the invalid values
	 * @returns {function} The predicate function
	 * @private
	 */
	_createInvalidFilterFn(colId) {
		return () => {
			return colId ?
				item => item.__tdpInvalid && item.__tdpInvalid.indexOf(colId) > -1 :
				item => item.__tdpInvalid && item.__tdpInvalid.length;
		};
	}

	/**
	 * @ngdoc method
	 * @name _createValidFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @description Create a 'valid' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	_createValidFilterFn(colId) {
		const invalidPredicate = this._createInvalidFilterFn(colId);
		const emptyPredicate = this._createEmptyFilterFn(colId);

		return (data) => {
			const invalidConfiguredPredicate = invalidPredicate(data);
			const emptyConfiguredPredicate = emptyPredicate(data);
			return item => !emptyConfiguredPredicate(item) && !invalidConfiguredPredicate(item);
		};
	}

	/**
	 * @ngdoc method
	 * @name _createEmptyFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @description Create an 'empty' filter function
	 * @returns {function} The predicate function
	 * @private
	 */
	_createEmptyFilterFn(colId) {
		return (data) => {
			return colId ?
				item => !item[colId] :
				item => find(data.metadata.columns, col => !item[col.id]);
		};
	}

	/**
	 * @ngdoc method
	 * @name _createQualityFilterFn
	 * @methodOf data-prep.services.filter.service:FilterService
	 * @param {string} colId The column id
	 * @param {object} args The filter arguments { invalid: boolean, empty: boolean, valid: boolean }
	 * @description Create a filter function that test if the value is one of the invalid/empty/valid values
	 * @returns {function} The predicate function
	 * @private
	 */
	_createQualityFilterFn(colId, args) {
		const noOpPredicate = () => () => false;
		const invalidPredicate = args.invalid ? this._createInvalidFilterFn(colId) : noOpPredicate;
		const emptyPredicate = args.empty ? this._createEmptyFilterFn(colId) : noOpPredicate;
		const validPredicate = args.valid ? this._createValidFilterFn(colId) : noOpPredicate;

		return (data) => {
			const invalidConfiguredPredicate = invalidPredicate(data);
			const emptyConfiguredPredicate = emptyPredicate(data);
			const validConfiguredPredicate = validPredicate(data);
			return item => invalidConfiguredPredicate(item) || emptyConfiguredPredicate(item) || validConfiguredPredicate(item);
		};
	}

	/**
	 * @ngdoc method
	 * @name _getValuesToDisplay
	 * @param {Array} filterValues The filter values to convert
	 * @description Replace new line character
	 * @private
	 */
	_getValuesToDisplay(filterValues) {
		const regexp = new RegExp('\n', 'g');  // eslint-disable-line no-control-regex
		return filterValues
			.map((filterValue) => {
				if (!filterValue.isEmpty) {
					filterValue.label = filterValue.value.replace(regexp, '\\n');
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
		return find(this.state.playground.filter.gridFilters, { colId, type: 'empty_records' });
	}

	/**
	 * Get quality filters on the provided column
	 * @param colId The column id
	 * @private
	 */
	_getQualityFilters(colId) {
		const quality = find(this.state.playground.filter.gridFilters, { colId, type: 'quality' });
		const empty = this._getEmptyFilter(colId);
		const valid = find(this.state.playground.filter.gridFilters, {
			colId,
			type: 'valid_records',
		});
		const invalid = find(this.state.playground.filter.gridFilters, {
			colId,
			type: 'invalid_records',
		});

		const filters = [];
		if (quality) {
			filters.push(quality);
		}
		if (empty) {
			filters.push(empty);
		}
		if (valid) {
			filters.push(valid);
		}
		if (invalid) {
			filters.push(invalid);
		}
		return filters;
	}

	/**
	 * Remove the filters
	 * @param qualityFilters
	 * @private
	 */
	_removeQualityFilters(qualityFilters) {
		qualityFilters.forEach(filter => this.removeFilter(filter));
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
		if (this.TQL_ENABLED) {
			return this.TqlFilterAdapterService.toTQL(filters);
		}
		return this.FilterAdapterService.toTree(filters);
	}

	parse(str) {
		if (this.TQL_ENABLED) {
			return this.TqlFilterAdapterService.fromTQL(str);
		}
		return this.FilterAdapterService.fromTree(str);
	}
}
