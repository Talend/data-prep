/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';

const RANGE_SEPARATOR = ' .. ';
const INTERVAL_SEPARATOR = ',';
const VALUES_SEPARATOR = '|';
const SHIFT_KEY_NAME = 'shift';
const CTRL_KEY_NAME = 'ctrl';

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterService
 * @description Filter service. This service provide the entry point to datagrid filters
 * @requires data-prep.services.filter.service:FilterAdapterService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.statistics.service:StatisticsService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 * @requires data-prep.services.utils.service:DateService
 */
export default function FilterService($timeout, state, StateService, FilterAdapterService, StatisticsService, ConverterService, TextFormatService, DateService) {
    'ngInject';

    const service = {
        // constants
        VALUES_SEPARATOR,
        SHIFT_KEY_NAME,
        CTRL_KEY_NAME,
        EMPTY_RECORDS_LABEL: FilterAdapterService.EMPTY_RECORDS_LABEL,

        // utils
        getRangeLabelFor,
        getSplittedRangeLabelFor,

        // life
        addFilter,
        addFilterAndDigest,
        updateFilter,
        removeAllFilters,
        removeFilter,
        toggleFilters,
    };
    return service;

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------UTILS------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name getRangeLabelFor
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Define an interval label
     * @param {Object} interval
     * @param {boolean} isDateRange
     * @return {string} interval label
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

    /**
     * @name getSplittedRangeLabelFor
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Split range label into an array with its min and its max
     * @param label Range label to split
     * @returns {Array} Splitted range values as string
     */
    function getSplittedRangeLabelFor(label) {
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
    //        var column = _.find(data.metadata.columns, {id: '0001'});
    //        var invalidValues = column.quality.invalidValues;
    //        return function (item) {                                                  // second level : returns true if the item is not in the invalid values list
    //            return item['0001'] && invalidValues.indexOf(item['0001']) === -1;
    //        };
    //    };
    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name createContainFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @param {string} phrase The phrase that the item must contain
     * @description [PRIVATE] Create a 'contains' filter function
     * @returns {function} The predicate function
     */
    function createContainFilterFn(colId, phrase) {
        const regexps = phrase
            .map(phraseValue => {
                const lowerCasePhrase = phraseValue.value.toLowerCase();
                return new RegExp(TextFormatService.escapeRegexpExceptStar(lowerCasePhrase));
            });
        const fns = (item) => regexps
            .map(regexp => item.toLowerCase().match(regexp))
            .reduce((newResult, oldResult) => newResult || oldResult);
        return function () {
            return function (item) {
                // col could be removed by a step
                const currentItem = item[colId];
                if (currentItem) {
                    return fns(currentItem);
                }

                return false;
            };
        };
    }

    /**
     * @ngdoc method
     * @name createExactFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @param {string} phrase The phrase that the item must be exactly equal to
     * @param {boolean} caseSensitive Determine if the filter is case sensitive
     * @description [PRIVATE] Create a filter function that test exact equality
     * @returns {function} The predicate function
     */
    function createExactFilterFn(colId, filterValues, caseSensitive) {
        const hasEmptyRecords = filterValues
            .filter(filterValue => filterValue.isEmpty)
            .length;
        const flattenFiltersValues = filterValues
            .filter(filterValue => !filterValue.isEmpty)
            .map(filterValue => TextFormatService.escapeRegexpExceptStar(filterValue.value))
            .join(VALUES_SEPARATOR);
        const regExpPatternToMatch = `^(${flattenFiltersValues})$`;
        const regExpToMatch = caseSensitive ? new RegExp(regExpPatternToMatch) : new RegExp(regExpPatternToMatch, 'i');
        return function () {
            return function (item) {
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
        };
    }

    /**
     * @ngdoc method
     * @name createInvalidFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @description Create a filter function that test if the value is one of the invalid values
     * @returns {function} The predicate function
     */
    function createInvalidFilterFn(colId) {
        return function (data) {
            const column = _.find(data.metadata.columns, { id: colId });
            const invalidValues = column.quality.invalidValues;
            return function (item) {
                return invalidValues.indexOf(item[colId]) > -1;
            };
        };
    }

    /**
     * @ngdoc method
     * @name createValidFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @description Create a 'valid' filter function
     * @returns {function} The predicate function
     */
    function createValidFilterFn(colId) {
        return function (data) {
            const column = _.find(data.metadata.columns, { id: colId });
            const invalidValues = column.quality.invalidValues;
            return function (item) {
                return item[colId] && invalidValues.indexOf(item[colId]) === -1;
            };
        };
    }

    /**
     * @ngdoc method
     * @name createEmptyFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @description Create an 'empty' filter function
     * @returns {function} The predicate function
     */
    function createEmptyFilterFn(colId) {
        return function () {
            return function (item) {
                return !item[colId];
            };
        };
    }

    /**
     * @ngdoc method
     * @name createRangeFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @param {Array} values The filter interval
     * @description Create a 'range' filter function
     * @returns {function} The predicate function
     */
    function createRangeFilterFn(colId, intervals) {
        return function () {
            return function (item) {
                if (!ConverterService.isNumber(item[colId])) {
                    return false;
                }

                const numberValue = ConverterService.adaptValue('numeric', item[colId]);
                return intervals
                    .map(interval => {
                        const values = interval.value;
                        const pairMin = values[0];
                        const pairMax = values[1];
                        return interval.isMaxReached ?
                        (numberValue === pairMin) || (numberValue > pairMin && numberValue <= pairMax) :
                        (numberValue === pairMin) || (numberValue > pairMin && numberValue < pairMax);
                    })
                    .reduce((oldResult, newResult) => oldResult || newResult);
            };
        };
    }


    /**
     * @ngdoc method
     * @name createDateRangeFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @param {Array} values The filter interval
     * @description Create a 'range' filter function
     * @returns {function} The predicate function
     */
    function createDateRangeFilterFn(colId, values) {
        const patterns = _.chain(state.playground.grid.selectedColumns[0].statistics.patternFrequencyTable)
            .map('pattern')
            .map(TextFormatService.convertJavaDateFormatToMomentDateFormat)
            .value();
        const getValueInDateLimitsFn = value => {
            const minTimestamp = value[0];
            const maxTimestamp = value[1];
            return DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns);
        };

        const valueInDateLimitsFn = (item) => values
            .map(dateRange => getValueInDateLimitsFn(dateRange.value)(item))
            .reduce((oldResult, newResult) => oldResult || newResult);
        return function () {
            return function (item) {
                return valueInDateLimitsFn(item[colId]);
            };
        };
    }

    /**
     * @ngdoc method
     * @name createMatchFilterFn
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} colId The column id
     * @param {Array} patterns The filter patterns
     * @description Create a 'match' filter function
     * @returns {function} The predicate function
     */
    function createMatchFilterFn(colId, patterns) {
        const hasEmptyRecords = patterns
            .filter(patternValue => patternValue.isEmpty)
            .length;
        const patternValues = patterns
            .map(patternValue => patternValue.value);
        const valueMatchPatternFns = (item) => patternValues
            .map(pattern => TextFormatService.valueMatchPatternFn(pattern)(item))
            .reduce((oldResult, newResult) => oldResult || newResult);
        return function () {
            return function (item) {
                return hasEmptyRecords ? (!item[colId] || valueMatchPatternFns(item[colId])) : valueMatchPatternFns(item[colId]);
            };
        };
    }

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------FILTER LIFE------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name getValuesToDisplay
     * @param {Array} filterValues The filter values to convert
     * @description Replace new line character
     */
    function getValuesToDisplay(filterValues) {
        const regexp = new RegExp('\n', 'g');  // eslint-disable-line no-control-regex
        return filterValues
            .map(filterValue => {
                if (!filterValue.isEmpty) {
                    filterValue.label = filterValue.value.replace(regexp, '\\n');
                }

                return filterValue;
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
     * @description Add a filter and update datagrid filters
     */
    function addFilter(type, colId, colName, args, removeFilterFn, keyName) {
        const sameColAndTypeFilter = _.find(state.playground.filter.gridFilters, { colId, type });
        let filterFn;
        let createFilter;
        let getFilterValue;
        let filterExists;
        let argsToDisplay;
        let sameColEmptyFilter;
        let hasEmptyRecordsExactFilter;
        let hasEmptyRecordsMatchFilter;

        const emptyFilterValue = {
            label: FilterAdapterService.EMPTY_RECORDS_LABEL,
            value: '',
            isEmpty: true,
        };

        switch (type) {
        case 'contains':
                // If we want to select records and a empty filter is already applied to that column
                // Then we need remove it before
            sameColEmptyFilter = _.find(state.playground.filter.gridFilters, {
                colId,
                type: 'empty_records',
            });
            if (sameColEmptyFilter) {
                removeFilter(sameColEmptyFilter);
                if (keyName === CTRL_KEY_NAME) {
                    args.phrase = [emptyFilterValue].concat(args.phrase);
                }
            }

            if (args.phrase.length === 1 && args.phrase[0].value === '') {
                args.phrase = [emptyFilterValue];
            }

            argsToDisplay = {
                phrase: getValuesToDisplay(args.phrase),
                caseSensitive: args.caseSensitive,
            };

            createFilter = function createFilter() {
                filterFn = createContainFilterFn(colId, args.phrase);
                return FilterAdapterService.createFilter(type, colId, colName, true, argsToDisplay, filterFn, removeFilterFn);
            };

            getFilterValue = function getFilterValue() {
                return argsToDisplay.phrase;
            };

            filterExists = function filterExists() {
                if (sameColAndTypeFilter &&
                        sameColAndTypeFilter.args &&
                        sameColAndTypeFilter.args.phrase) {
                    return _.isEqual(
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
        case 'exact':
                // If we want to select records and a empty filter is already applied to that column
                // Then we need remove it before
            sameColEmptyFilter = _.find(state.playground.filter.gridFilters, {
                colId,
                type: 'empty_records',
            });
            if (sameColEmptyFilter) {
                removeFilter(sameColEmptyFilter);
                if (keyName === CTRL_KEY_NAME) {
                    args.phrase = [emptyFilterValue].concat(args.phrase);
                }
            }

            if (args.phrase.length === 1 && args.phrase[0].value === '') {
                args.phrase = [emptyFilterValue];
            }

            argsToDisplay = {
                phrase: getValuesToDisplay(args.phrase),
                caseSensitive: args.caseSensitive,
            };

            createFilter = function createFilter() {
                filterFn = createExactFilterFn(colId, args.phrase, args.caseSensitive);
                return FilterAdapterService.createFilter(type, colId, colName, true, argsToDisplay, filterFn, removeFilterFn);
            };

            getFilterValue = function getFilterValue() {
                return argsToDisplay.phrase;
            };

            filterExists = function filterExists() {
                if (sameColAndTypeFilter &&
                        sameColAndTypeFilter.args &&
                        sameColAndTypeFilter.args.phrase) {
                    return _.isEqual(
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
        case 'invalid_records':
            createFilter = function createFilter() {
                filterFn = createInvalidFilterFn(colId);
                return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
            };

            filterExists = function filterExists() {
                return sameColAndTypeFilter;
            };

            break;
        case 'empty_records':
            {
                // If we want to select empty records and another filter is already applied to that column
                // Then we need remove it before
                const sameColExactFilter = _.find(state.playground.filter.gridFilters, { colId, type: 'exact' });
                const sameColMatchFilter = _.find(state.playground.filter.gridFilters, {
                    colId,
                    type: 'matches',
                });
                if (sameColExactFilter) {
                    hasEmptyRecordsExactFilter = (
                        sameColExactFilter.args
                        && sameColExactFilter.args.phrase.length === 1
                        && sameColExactFilter.args.phrase[0].value === ''
                    );
                    removeFilter(sameColExactFilter);
                }
                else if (sameColMatchFilter) {
                    hasEmptyRecordsMatchFilter = (
                        sameColMatchFilter.args &&
                        sameColMatchFilter.args.patterns.length === 1 &&
                        sameColMatchFilter.args.patterns[0].value === ''
                    );
                    removeFilter(sameColMatchFilter);
                }

                createFilter = function createFilter() {
                    filterFn = createEmptyFilterFn(colId);
                    return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                };

                filterExists = function filterExists() {
                    return sameColAndTypeFilter || hasEmptyRecordsExactFilter || hasEmptyRecordsMatchFilter;
                };

                break;
            }

        case 'valid_records':
            createFilter = function createFilter() {
                filterFn = createValidFilterFn(colId);
                return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
            };

            filterExists = function filterExists() {
                return sameColAndTypeFilter;
            };

            break;
        case 'inside_range':
            createFilter = function createFilter() {
                filterFn = args.type === 'date' ?
                        createDateRangeFilterFn(colId, args.intervals) :
                        createRangeFilterFn(colId, args.intervals, args.isMaxReached);
                return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
            };

            getFilterValue = function getFilterValue() {
                return args.intervals;
            };

            filterExists = function filterExists() {
                if (sameColAndTypeFilter &&
                        sameColAndTypeFilter.args &&
                        sameColAndTypeFilter.args.intervals) {
                    return _.isEqual(
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
        case 'matches':
                // If we want to select records and a empty filter is already applied to that column
                // Then we need remove it before
            sameColEmptyFilter = _.find(state.playground.filter.gridFilters, {
                colId,
                type: 'empty_records',
            });
            if (sameColEmptyFilter) {
                removeFilter(sameColEmptyFilter);
                if (keyName === CTRL_KEY_NAME) {
                    args.patterns = [emptyFilterValue].concat(args.patterns);
                }
            }

            if (args.patterns.length === 1 && args.patterns[0].value === '') {
                args.patterns = [emptyFilterValue];
            }

            createFilter = function createFilter() {
                filterFn = createMatchFilterFn(colId, args.patterns);
                return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
            };

            getFilterValue = function getFilterValue() {
                return args.patterns;
            };

            filterExists = function filterExists() {
                if (sameColAndTypeFilter &&
                        sameColAndTypeFilter.args &&
                        sameColAndTypeFilter.args.patterns) {
                    return _.isEqual(
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

        if (!sameColAndTypeFilter && !hasEmptyRecordsExactFilter && !hasEmptyRecordsMatchFilter) {
            const filterInfo = createFilter();
            pushFilter(filterInfo);
        }
        else if (filterExists()) {
            removeFilter(sameColAndTypeFilter);
        }
        else {
            const filterValue = getFilterValue();
            updateFilter(sameColAndTypeFilter, filterValue, keyName);
        }
    }

    /**
     * @ngdoc method
     * @name addFilterAndDigest
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} type The filter type (ex : contains)
     * @param {string} colId The column id
     * @param {string} colName The column name
     * @param {string} args The filter arguments (ex for 'contains' type : {phrase: 'toto'})
     * @param {function} removeFilterFn An optional remove callback
     * @description Wrapper on addFilter method that trigger a digest at the end (use of $timeout)
     */
    function addFilterAndDigest(type, colId, colName, args, removeFilterFn, keyName) {
        $timeout(addFilter.bind(service, type, colId, colName, args, removeFilterFn, keyName));
    }

    /**
     * @ngdoc method
     * @name updateFilter
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {object} oldFilter The filter to update
     * @param {object} newValue The filter update parameters
     * @description Update an existing filter and update datagrid filters
     */
    function updateFilter(oldFilter, newValue, keyName) {
        let newFilterFn;
        let newArgs;
        let editableFilter;

        let newComputedValue;

        const addOrCriteria = keyName === CTRL_KEY_NAME;
        const addFromToCriteria = keyName === SHIFT_KEY_NAME;

        switch (oldFilter.type) {
        case 'contains':
            if (addOrCriteria) {
                newComputedValue = computeOr(oldFilter.args.phrase, newValue);
            }
            else {
                newComputedValue = newValue;
            }

            newArgs = {
                phrase: newComputedValue,
            };
            newFilterFn = createContainFilterFn(oldFilter.colId, newValue);
            editableFilter = true;
            break;
        case 'exact':
            if (addOrCriteria) {
                newComputedValue = computeOr(oldFilter.args.phrase, newValue);
            }
            else {
                newComputedValue = newValue;
            }

            newArgs = {
                phrase: newComputedValue,
                caseSensitive: oldFilter.args.caseSensitive,
            };
            newFilterFn = createExactFilterFn(oldFilter.colId, newComputedValue, oldFilter.args.caseSensitive);
            editableFilter = true;
            break;
        case 'inside_range':
            {
                let newComputedArgs;
                let newComputedRange;
                if (addFromToCriteria) {
                    // Need to pass complete old filter there in order to stock its direction
                    newComputedArgs = computeFromToRange(oldFilter, newValue);
                    newComputedRange = newComputedArgs.intervals;
                }
                else if (addOrCriteria) {
                    newComputedRange = computeOr(oldFilter.args.intervals, newValue);
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
                    createDateRangeFilterFn(oldFilter.colId, newComputedRange) :
                    createRangeFilterFn(oldFilter.colId, newComputedRange);
                break;
            }

        case 'matches':
            {
                let newComputedPattern;
                if (addOrCriteria) {
                    newComputedPattern = computeOr(oldFilter.args.patterns, newValue);
                }
                else {
                    newComputedPattern = newValue;
                }

                newArgs = {
                    patterns: newComputedPattern,
                };
                newFilterFn = createMatchFilterFn(oldFilter.colId, newComputedPattern);
                editableFilter = false;
                break;
            }
        }
        const newFilter = FilterAdapterService.createFilter(oldFilter.type, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, newFilterFn, oldFilter.removeFilterFn);

        StateService.updateGridFilter(oldFilter, newFilter);
        StatisticsService.updateFilteredStatistics();
    }

    /**
     * @name computeOr
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Create filter values with Or criteria
     * @param oldFilter Previous filter to update
     * @param criteria New filter value
     * @returns {string} Filter values with Or criteria
     */
    function computeOr(oldCriteria, newCriteria) {
        let mergedCriteria = [];
        newCriteria.forEach(criterion => {
            if (_.some(oldCriteria, criterion)) {
                _.remove(oldCriteria, criterion);
            }
            else {
                oldCriteria.push(criterion);
            }

            mergedCriteria = oldCriteria;
        });
        return mergedCriteria;
    }

    /**
     *
     * @param intervals
     * @returns {*}
     * @private
     */
    function findMinInterval(intervals) {
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
    function findMaxInterval(intervals) {
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

    /**
     * @name computeFromToRange
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Create filter values with From To criteria
     * @param oldFilter Previous filter to update
     * @param newValue New filter value
     * @returns {Object} Filter values with From To criteria
     */
    function computeFromToRange(oldFilter, newValue) {
        const oldFilterArgs = oldFilter.args;
        const oldIntervals = oldFilterArgs.intervals;
        const oldDirection = oldFilterArgs.direction || 1;
        newValue.forEach(newInterval => {
            // Identify min and max old interval
            const oldMinInterval = findMinInterval(oldIntervals);
            const oldMaxInterval = findMaxInterval(oldIntervals);

            // Identify min and max from previous intervals
            const oldMin = oldMinInterval.value[0];
            const oldMax = oldMaxInterval.value[1] || oldMaxInterval.value[0];
            const oldMinLabel = getSplittedRangeLabelFor(oldMinInterval.label);
            const oldMaxLabel = getSplittedRangeLabelFor(oldMaxInterval.label);

            // Identify min and max from new interval
            const newMin = newInterval.value[0];
            const newMax = newInterval.value[1] || newMin;
            const newLabel = getSplittedRangeLabelFor(newInterval.label);

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
     *
     * @ngdoc method
     * @name removeAllFilters
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Remove all the filters and update datagrid filters
     */
    function removeAllFilters() {
        const filters = state.playground.filter.gridFilters;
        StateService.removeAllGridFilters();

        _.chain(filters)
            .filter(function (filter) {
                return filter.removeFilterFn;
            })
            .forEach(function (filter) {
                filter.removeFilterFn(filter);
            })
            .value();
        StatisticsService.updateFilteredStatistics();
    }

    /**
     * @ngdoc method
     * @name removeFilter
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {object} filter The filter to delete
     * @description Remove a filter and update datagrid filters
     */
    function removeFilter(filter) {
        StateService.removeGridFilter(filter);
        if (filter.removeFilterFn) {
            filter.removeFilterFn(filter);
        }

        StatisticsService.updateFilteredStatistics();
    }

    /**
     * @ngdoc method
     * @name pushFilter
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {object} filter The filter to push
     * @description Push a filter in the filter list
     */
    function pushFilter(filter) {
        StateService.addGridFilter(filter);
        StatisticsService.updateFilteredStatistics();
    }

    /**
     * @ngdoc method
     * @name toggleFilters
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Push a filter in the filter list
     */
    function toggleFilters() {
        if (state.playground.filter.enabled) {
            StateService.disableFilters();
        }
        else {
            StateService.enableFilters();
        }

        StatisticsService.updateFilteredStatistics();
    }
}
