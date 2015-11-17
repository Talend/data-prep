(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.filter.service:FilterService
     * @description Filter service. This service provide the entry point to datagrid filters
     * @requires data-prep.services.playground.service:DatagridService
     */
    function FilterService($timeout, state, StateService, FilterAdapterService, DatagridService, ConverterService) {
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
         * Escape all regexp characters except * wildcard, and adapt * wildcard to regexp (* --> .*)
         * @param {string} str The string to escape
         * @returns {*}
         */
        function escapeRegExpExceptStar(str) {
            return str.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, '\\$&').replace(/\*/g, '.*');
        }

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
                    if (!ConverterService.isNumber(item[colId])) {
                        return false;
                    }

                    var numberValue = ConverterService.adaptValue('numeric', item[colId]);
                    return numberValue >= values[0] && numberValue <= values[1];
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

            var existingColTypeFilter = _.find(state.playground.filter.gridFilters, function (filter) {
                return filter.colId === colId && filter.type === type;
            });

            var createFilter, updateFilter, doesFilterExist;

            switch (type) {
                case 'contains':
                    createFilter = function createFilter() {
                        filterFn = createContainFilterFn(colId, args.phrase);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, true, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {
                        service.updateFilter(existingColTypeFilter, args.phrase);
                    };

                    doesFilterExist = function compareFilter() {
                        return existingColTypeFilter.args.phrase === args.phrase;
                    };
                    break;
                case 'exact':
                    createFilter = function createFilter() {
                        filterFn = createExactFilterFn(colId, args.phrase, args.caseSensitive);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, true, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {
                        service.updateFilter(existingColTypeFilter, args.phrase);
                    };

                    doesFilterExist = function compareFilter() {
                        return existingColTypeFilter.args.phrase === args.phrase;
                    };
                    break;
                case 'invalid_records':
                    createFilter = function createFilter() {
                        filterFn = createInvalidFilterFn(colId);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {};

                    doesFilterExist = function compareFilter() {
                        return true;
                    };
                    break;
                case 'empty_records':
                    createFilter = function createFilter() {
                        filterFn = createEmptyFilterFn(colId);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {
                    };

                    doesFilterExist = function compareFilter() {
                        return true;
                    };
                    break;
                case 'valid_records':
                    createFilter = function createFilter() {
                        filterFn = createValidFilterFn(colId);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {
                    };

                    doesFilterExist = function compareFilter() {
                        return true;
                    };
                    break;
                case 'inside_range':
                    createFilter = function createFilter() {
                        filterFn = createRangeFilterFn(colId, args.interval);
                        filterInfo = FilterAdapterService.createFilter(type, colId, colName, false, args, filterFn, removeFilterFn);
                        StateService.addGridFilter(filterInfo);
                    };

                    updateFilter = function updateFilter() {
                        service.updateFilter(existingColTypeFilter, args.interval);
                    };

                    doesFilterExist = function compareFilter() {
                        return _.isEqual(existingColTypeFilter.args.interval, args.interval);
                    };
                    break;
            }

            if(!existingColTypeFilter) {
                createFilter();
            }
            else if(doesFilterExist()) {
                service.removeFilter(existingColTypeFilter);
            }
            else {
                updateFilter();
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
            newFilter = FilterAdapterService.createFilter(oldFilter.type, oldFilter.colId, oldFilter.colName, editableFilter, newArgs, newFilterFn, oldFilter.removeFilterFn);

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
                .filter(function (filter) {
                    return filter.removeFilterFn;
                })
                .forEach(function (filter) {
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