(function () {
    'use strict';

    /**
     * Escape all regexp characters except * wildcard, and adapt * wildcard to regexp (* --> .*)
     * @param {string} str The string to escape
     * @returns {*}
     */
    function escapeRegExpExceptStar(str) {
        return str.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, '\\$&').replace(/\*/g, '.*');
    }

    /**
     * Filter info object
     * @param {string} type The filter type
     * @param {string} colId The column id
     * @param {string} colName The column name
     * @param {boolean} editable True if the filter is editable
     * @param {object} args The filter arguments
     * @param {function} filterFn The filter function
     * @param {function} removeFilterFn The remove filter callback
     * @constructor
     */
    function Filter(type, colId, colName, editable, args, filterFn, removeFilterFn) {
        var self = this;

        self.type = type;
        self.colId = colId;
        self.colName = colName;
        self.editable = editable;
        self.args = args;
        self.filterFn = filterFn;
        self.removeFilterFn = removeFilterFn;
        self.__defineGetter__('value', function () {
            switch (self.type) {
                case 'contains':
                    return self.args.phrase;
                case 'exact':
                    return self.args.phrase;
                case 'invalid_records':
                    return 'invalid records';
                case 'empty_records':
                    return 'empty records';
                case 'valid_records':
                    return 'valid records';
                case 'inside_range':
                    return '[' + d3.format(',')(args.interval[0]) + ' .. ' + d3.format(',')(args.interval[1]) + ']';
            }
        });
    }

    /**
     * @ngdoc service
     * @name data-prep.services.filter.service:FilterService
     * @description Filter service. This service holds the filters list and provide the entry point to datagrid filters
     * @requires data-prep.services.playground.service:DatagridService
     */
    function FilterService($timeout, state, StateService, DatagridService, NumbersValidityService) {
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

            var regexp = new RegExp(escapeRegExpExceptStar(phrase));
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
        //        var column = _.find(data.columns, {id: '0001'});
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
            var regexp = new RegExp(escapeRegExpExceptStar(lowerCasePhrase));

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
                var column = _.find(data.columns, {id: colId});
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
                var column = _.find(data.columns, {id: colId});
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
                    return NumbersValidityService.toNumber(item[colId]) >= values[0] && NumbersValidityService.toNumber(item[colId]) <= values[1];
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
            var filterInfo;
            switch (type) {
                case 'contains':
                    filterFn = createContainFilterFn(colId, args.phrase);
                    filterInfo = new Filter(type, colId, colName, true, args, filterFn, removeFilterFn);
                    break;
                case 'exact':
                    filterFn = createExactFilterFn(colId, args.phrase, args.caseSensitive);
                    filterInfo = new Filter(type, colId, colName, true, args, filterFn, removeFilterFn);
                    break;
                case 'invalid_records':
                    filterFn = createInvalidFilterFn(colId);
                    filterInfo = new Filter(type, colId, colName, false, args, filterFn, removeFilterFn);
                    break;
                case 'empty_records':
                    filterFn = createEmptyFilterFn(colId);
                    filterInfo = new Filter(type, colId, colName, false, args, filterFn, removeFilterFn);
                    break;
                case 'valid_records':
                    filterFn = createValidFilterFn(colId);
                    filterInfo = new Filter(type, colId, colName, false, args, filterFn, removeFilterFn);
                    break;
                case 'inside_range':
                    var existingNumColFilter = _.find(state.playground.filter.gridFilters, function (filter) {
                        return filter.colId === colId && filter.type === 'inside_range';
                    });

                    if (existingNumColFilter) {
                        service.updateFilter(existingNumColFilter, args.interval);
                        return;
                    }
                    else {
                        filterFn = createRangeFilterFn(colId, args.interval);
                        filterInfo = new Filter(type, colId, colName, false, args, filterFn, removeFilterFn);
                    }
                    break;
            }
            StateService.addGridFilter(filterInfo);
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
            var newArgs = {};
            var editableFilter;
            switch (oldFilter.type) {
                case 'contains':
                    newArgs.phrase = newValue;
                    newFilterFn = createContainFilterFn(oldFilter.colId, newValue);
                    editableFilter = true;
                    break;
                case 'exact':
                    newArgs.phrase = newValue;
                    newFilterFn = createExactFilterFn(oldFilter.colId, newValue);
                    editableFilter = true;
                    break;
                case 'inside_range':
                    newArgs.interval = newValue;
                    newFilterFn = createRangeFilterFn(oldFilter.colId, newValue);
                    editableFilter = false;
                    break;
            }
            newFilter = new Filter(oldFilter.type, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, newFilterFn, oldFilter.removeFilterFn);

            StateService.updateGridFilter(oldFilter, newFilter);
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
                .filter(function(filter) {
                    return filter.removeFilterFn;
                })
                .forEach(function(filter) {
                    filter.removeFilterFn(filter);
                })
                .value();
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
        }
    }

    angular.module('data-prep.services.filter')
        .service('FilterService', FilterService);
})();