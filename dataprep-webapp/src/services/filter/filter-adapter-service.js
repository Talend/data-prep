(function () {
    'use strict';

    var CONTAINS = 'contains';
    var EXACT = 'exact';
    var INVALID_RECORDS = 'invalid_records';
    var EMPTY_RECORDS = 'empty_records';
    var VALID_RECORDS = 'valid_records';
    var INSIDE_RANGE = 'inside_range';

    /**
     * @ngdoc service
     * @name data-prep.services.filter.service:FilterAdapterService
     * @description Filter adapter service. This service provides filter constructor and adapters
     */
    function FilterAdapterService(state) {
        return {
            createFilter: createFilter,
            toTree: toTree,
            fromTree: fromTree
        };

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------------CREATION-------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createFilter
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @param {string} type The filter type
         * @param {string} colId The column id
         * @param {string} colName The column name
         * @param {boolean} editable True if the filter is editable
         * @param {object} args The filter arguments
         * @param {function} filterFn The filter function
         * @param {function} removeFilterFn The remove filter callback
         * @description creates a Filter definition instance
         * @returns {Object} instance of the Filter
         */
        function createFilter(type, colId, colName, editable, args, filterFn, removeFilterFn) {
            var filter = {
                type: type,
                colId: colId,
                colName: colName,
                editable: editable,
                args: args,
                filterFn: filterFn,
                removeFilterFn: removeFilterFn
            };

            filter.__defineGetter__('value', getFilterValue.bind(filter));
            filter.toTree = getFilterTree.bind(filter);
            return filter;
        }

        /**
         * @ngdoc method
         * @name getFilterValue
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @description Return the filter value depending on its type. This function should be used with filter definition object binding
         * @returns {Object} The filter value
         */
        function getFilterValue() {
            switch (this.type) {
                case CONTAINS:
                    return this.args.phrase;
                case EXACT:
                    return this.args.phrase;
                case INVALID_RECORDS:
                    return 'invalid records';
                case EMPTY_RECORDS:
                    return 'empty records';
                case VALID_RECORDS:
                    return 'valid records';
                case INSIDE_RANGE:
                    var min = d3.format(',')(this.args.interval[0]);
                    var max = d3.format(',')(this.args.interval[1]);
                    return min === max ? '[' + min + ']' : '[' + min + ' .. ' + max + '[';
            }
        }

        /**
         * @ngdoc method
         * @name getFilterTree
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @description Adapt filter to single tree. This function should be used with filter definition object binding
         * @returns {Object} The filter tree
         */
        function getFilterTree() {
            switch (this.type) {
                case CONTAINS:
                    return {
                        contains: {
                            field: this.colId,
                            value: this.value
                        }
                    };
                case EXACT:
                    return {
                        eq: {
                            field: this.colId,
                            value: this.value
                        }
                    };
                case INVALID_RECORDS:
                    return {
                        invalid: {
                            field: this.colId
                        }
                    };
                case EMPTY_RECORDS:
                    return {
                        empty: {
                            field: this.colId
                        }
                    };
                case VALID_RECORDS:
                    return {
                        valid: {
                            field: this.colId
                        }
                    };
                case INSIDE_RANGE:
                    //TODO JSO ask FHU/AMA why start and end are strings ?
                    return {
                        range: {
                            field: this.colId,
                            start: '' + this.args.interval[0],
                            end: '' + this.args.interval[1]
                        }
                    };
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------CONVERTION-------------------------------------------------
        //-------------------------------------------------FILTER ==> TREE----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toTree
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @param {array} filters The filters to adapt to tree
         * @description Reduce filters into a tree representation
         * @returns {Object} The filters tree
         */
        function toTree(filters) {
            return _.reduce(filters, reduceFn, {});
        }

        /**
         * @ngdoc method
         * @name reduceFn
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @param {Object} accu The filter tree accumulator
         * @param {Object} filterItem The filter definition
         * @description Reduce function for filters adaptation to tree
         * @returns {Object} The combined filter/accumulator tree
         */
        function reduceFn(accu, filterItem) {
            var nextAccuFilter = filterItem.toTree();

            if(accu.filter) {
                nextAccuFilter = {
                    and: [accu.filter, nextAccuFilter]
                };
            }

            return {
                filter: nextAccuFilter
            };
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------CONVERTION-------------------------------------------------
        //-------------------------------------------------TREE ==> FILTER----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name fromTree
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @param {object} tree The filters tree
         * @description Adapt tree to filters definition array
         * @returns {Array} The filters definition array
         */
        function fromTree(tree) {
            //no tree, no filter
            if(!tree) {
                return;
            }

            //it is a leaf
            if(!tree.and) {
                return [leafToFilter(tree)];
            }

            //it is an "and" node
            return _.reduce(tree.and, function(accu, nodeChild) {
                return accu.concat(fromTree(nodeChild));
            }, []);
        }

        /**
         * @ngdoc method
         * @name leafToFilter
         * @methodOf data-prep.services.filter.service:FilterAdapterService
         * @param {object} leaf The leaf to convert
         * @description Adapt a leaf into a filter definition
         * @returns {Object} The resulting filter definition
         */
        function leafToFilter(leaf) {
            var type, args, condition;
            var editable = false;

            if('contains' in leaf) {
                type = CONTAINS;
                condition = leaf.contains;
                args = {phrase: condition.value};
            }
            else if('eq' in leaf) {
                type = EXACT;
                condition = leaf.eq;
                args = {phrase: condition.value};
            }
            else if('range' in leaf) {
                type = INSIDE_RANGE;
                condition = leaf.range;
                args = {interval: [condition.start, condition.end]};
            }
            else if('invalid' in leaf) {
                type = INVALID_RECORDS;
                condition = leaf.invalid;
            }
            else if('empty' in leaf) {
                type = EMPTY_RECORDS;
                condition = leaf.empty;
            }
            else if('valid' in leaf) {
                type = VALID_RECORDS;
                condition = leaf.valid;
            }

            var colId = condition.field;
            var colName = _.find(state.playground.data.columns, {id: colId}).name;
            return createFilter(type, colId, colName, editable, args, null, null);
        }
    }

    angular.module('data-prep.services.filter')
        .service('FilterAdapterService', FilterAdapterService);
})();