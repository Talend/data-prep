/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
 * @requires data-prep.services.filter.service:FilterAdapterService
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.statistics.service:StatisticsService
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 * @requires data-prep.services.utils.service:DateService
 */
export default function FilterService($timeout, state, StateService, FilterAdapterService, DatagridService, StatisticsService, ConverterService, TextFormatService, DateService) {
    'ngInject';

    var service = {
        //utils
        getColumnsContaining: getColumnsContaining,

        //life
        addFilter: addFilter,
        addFilterAndDigest: addFilterAndDigest,
        updateFilter: updateFilter,
        removeAllFilters: removeAllFilters,
        removeFilter: removeFilter
    };
    return service;

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------UTILS------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name getColumnsContaining
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} phrase To match. Wildcard (*) accepted
     * @description Return the column with a cell that can match the phrase. It take into account a possible wildcard (*)
     * @returns {Object[]} The columns id that contains a matching value (col.id & col.name)
     */
    function getColumnsContaining(phrase) {
        if (!phrase) {
            return [];
        }

        var regexp = new RegExp(TextFormatService.escapeRegexpExceptStar(phrase));
        var canBeNumeric = !isNaN(phrase.replace(/\*/g, ''));
        var canBeBoolean = 'true'.match(regexp) || 'false'.match(regexp);

        return DatagridService.getColumnsContaining(regexp, canBeNumeric, canBeBoolean);
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------FILTER FNs-------------------------------------------------
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
        var lowerCasePhrase = phrase.toLowerCase();
        var regexp = new RegExp(TextFormatService.escapeRegexpExceptStar(lowerCasePhrase));

        return function () {
            return function (item) {
                // col could be removed by a step
                if (item[colId]) {
                    return item[colId].toLowerCase().match(regexp);
                }
                else {
                    return false;
                }
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
    function createExactFilterFn(colId, phrase, caseSensitive) {
        return function () {
            return function (item) {
                // col could be removed by a step
                if (item[colId]) {
                    return caseSensitive ?
                    item[colId] === phrase :
                    (item[colId]).toUpperCase() === phrase.toUpperCase();
                }
                else {
                    return false;
                }
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
            var column = _.find(data.metadata.columns, {id: colId});
            var invalidValues = column.quality.invalidValues;
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
            var column = _.find(data.metadata.columns, {id: colId});
            var invalidValues = column.quality.invalidValues;
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
    function createRangeFilterFn(colId, values) {
        return function () {
            return function (item) {
                if (!ConverterService.isNumber(item[colId])) {
                    return false;
                }

                var numberValue = ConverterService.adaptValue('numeric', item[colId]);
                var min = values[0];
                var max = values[1];
                return (numberValue === min) || (numberValue > min && numberValue < max);
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
        var minTimestamp = values[0];
        var maxTimestamp = values[1];
        var patterns = _.chain(state.playground.grid.selectedColumn.statistics.patternFrequencyTable)
            .pluck('pattern')
            .map(TextFormatService.convertJavaDateFormatToMomentDateFormat)
            .value();

        var valueInDateLimitsFn = DateService.isInDateLimits(minTimestamp, maxTimestamp, patterns);
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
     * @param {string} pattern The filter pattern
     * @description Create a 'match' filter function
     * @returns {function} The predicate function
     */
    function createMatchFilterFn(colId, pattern) {
        var valueMatchPatternFn = StatisticsService.valueMatchPatternFn(pattern);
        return function () {
            return function (item) {
                return valueMatchPatternFn(item[colId]);
            };
        };
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------FILTER LIFE------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name addFilter
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {string} type The filter type (ex : contains)
     * @param {string} colId The column id
     * @param {string} colName The column name
     * @param {string} args The filter arguments (ex for 'contains' type : {phrase: 'toto'})
     * @param {function} removeFilterFn An optional remove callback
     * @description Add a filter and update datagrid filters
     */
    function addFilter(type, colId, colName, args, removeFilterFn) {
        var filterFn;
        var sameColAndTypeFilter = _.find(state.playground.filter.gridFilters, {colId: colId, type: type});
        var createFilter, getFilterValue, filterExists;

        switch (type) {
            case 'contains':
                createFilter = function createFilter() {
                    filterFn = createContainFilterFn(colId, args.phrase);
                    return FilterAdapterService.createFilter(type, colId, colName, true, args, filterFn, removeFilterFn);
                };

                getFilterValue = function getFilterValue() {
                    return args.phrase;
                };

                filterExists = function filterExists() {
                    return sameColAndTypeFilter.args.phrase === args.phrase;
                };
                break;
            case 'exact':
                createFilter = function createFilter() {
                    filterFn = createExactFilterFn(colId, args.phrase, args.caseSensitive);
                    return FilterAdapterService.createFilter(type, colId, colName, true, args, filterFn, removeFilterFn);
                };

                getFilterValue = function getFilterValue() {
                    return args.phrase;
                };

                filterExists = function filterExists() {
                    return sameColAndTypeFilter.args.phrase === args.phrase;
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
                createFilter = function createFilter() {
                    filterFn = createEmptyFilterFn(colId);
                    return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                };

                filterExists = function filterExists() {
                    return sameColAndTypeFilter;
                };
                break;
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
                        createDateRangeFilterFn(colId, args.interval) :
                        createRangeFilterFn(colId, args.interval);
                    return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                };

                getFilterValue = function getFilterValue() {
                    return args;
                };

                filterExists = function filterExists() {
                    return _.isEqual(sameColAndTypeFilter.args.interval, args.interval);
                };
                break;
            case 'matches':
                createFilter = function createFilter() {
                    filterFn = createMatchFilterFn(colId, args.pattern);
                    return FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                };

                getFilterValue = function getFilterValue() {
                    return args.pattern;
                };

                filterExists = function filterExists() {
                    return sameColAndTypeFilter.args.pattern === args.pattern;
                };
                break;
        }

        if (!sameColAndTypeFilter) {
            var filterInfo = createFilter();
            pushFilter(filterInfo);
        }
        else if (filterExists()) {
            removeFilter(sameColAndTypeFilter);
        }
        else {
            var filterValue = getFilterValue();
            updateFilter(sameColAndTypeFilter, filterValue);
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
    function addFilterAndDigest(type, colId, colName, args, removeFilterFn) {
        $timeout(addFilter.bind(service, type, colId, colName, args, removeFilterFn));
    }

    /**
     * @ngdoc method
     * @name updateFilter
     * @methodOf data-prep.services.filter.service:FilterService
     * @param {object} oldFilter The filter to update
     * @param {object} newValue The filter update parameters
     * @description Update an existing filter and update datagrid filters
     */
    function updateFilter(oldFilter, newValue) {
        var newFilterFn;
        var newFilter;
        var newArgs;
        var editableFilter;
        switch (oldFilter.type) {
            case 'contains':
                newArgs = {phrase: newValue};
                newFilterFn = createContainFilterFn(oldFilter.colId, newValue);
                editableFilter = true;
                break;
            case 'exact':
                newArgs = {phrase: newValue};
                newFilterFn = createExactFilterFn(oldFilter.colId, newValue, oldFilter.args.caseSensitive);
                editableFilter = true;
                break;
            case 'inside_range':
                newArgs = newValue;
                editableFilter = false;
                newFilterFn = newValue.type === 'date' ?
                    createDateRangeFilterFn(oldFilter.colId, newValue.interval) :
                    createRangeFilterFn(oldFilter.colId, newValue.interval);
                break;
            case 'matches':
                newArgs = {pattern: newValue};
                newFilterFn = createMatchFilterFn(oldFilter.colId, newValue);
                editableFilter = false;
                break;
        }
        newFilter = FilterAdapterService.createFilter(oldFilter.type, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, newFilterFn, oldFilter.removeFilterFn);

        StateService.updateGridFilter(oldFilter, newFilter);
        StatisticsService.updateFilteredStatistics();
    }

    /**
     * @ngdoc method
     * @name removeAllFilters
     * @methodOf data-prep.services.filter.service:FilterService
     * @description Remove all the filters and update datagrid filters
     */
    function removeAllFilters() {
        var filters = state.playground.filter.gridFilters;
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
}