/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

const CONTAINS = 'contains';
const EXACT = 'exact';
const INVALID_RECORDS = 'invalid_records';
const EMPTY_RECORDS = 'empty_records';
const VALID_RECORDS = 'valid_records';
const INSIDE_RANGE = 'inside_range';
const MATCHES = 'matches';

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterAdapterService
 * @description Filter adapter service. This service provides filter constructor and adapters
 * @requires data-prep.services.state.constant:state
 */
export default function FilterAdapterService(state) {
    'ngInject';

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
     * @returns {Object} instance of the Filter
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
     * @returns {Object} The filter value
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
                return this.args.label;
            case MATCHES:
                return this.args.pattern;
        }
    }

    /**
     * @ngdoc method
     * @name getFilterTree
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @description Adapt filter to single tree. This function should be used with filter definition object binding
     * @returns {Object} The filter tree
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
                //on date we shift timestamp to fit UTC timezone
                var offset = 0;
                if (this.args.type === 'date') {
                    var minDate = new Date(this.args.interval[0]);
                    offset = minDate.getTimezoneOffset() * 60 * 1000;
                }
                return {
                    range: {
                        field: this.colId,
                        start: this.args.interval[0] - offset,
                        end: this.args.interval[1] - offset,
                        type: this.args.type,
                        label: this.args.label
                    }
                };
            case MATCHES:
                return {
                    matches: {
                        field: this.colId,
                        value: this.value
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
     * @returns {Object} The filters tree
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
     * @returns {Object} The combined filter/accumulator tree
     */
    function reduceFn(accu, filterItem) {
        var nextAccuFilter = filterItem.toTree();

        if (accu.filter) {
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
     * @returns {Array} The filters definition array
     */
    function fromTree(tree) {
        //no tree, no filter
        if (!tree) {
            return;
        }

        //it is a leaf
        if (!tree.and) {
            return [leafToFilter(tree)];
        }

        //it is an "and" node
        return _.reduce(tree.and, function (accu, nodeChild) {
            return accu.concat(fromTree(nodeChild));
        }, []);
    }

    /**
     * @ngdoc method
     * @name leafToFilter
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @param {object} leaf The leaf to convert
     * @description Adapt a leaf into a filter definition
     * @returns {Object} The resulting filter definition
     */
    function leafToFilter(leaf) {
        var type, args, condition;
        var editable = false;

        if ('contains' in leaf) {
            type = CONTAINS;
            condition = leaf.contains;
            args = {phrase: condition.value};
        }
        else if ('eq' in leaf) {
            type = EXACT;
            condition = leaf.eq;
            args = {phrase: condition.value};
        }
        else if ('range' in leaf) {
            type = INSIDE_RANGE;
            condition = leaf.range;

            //on date we shift timestamp to fit UTC timezone
            var offset = 0;
            if (condition.type === 'date') {
                var minDate = new Date(condition.start);
                offset = minDate.getTimezoneOffset() * 60 * 1000;
            }

            args = {
                interval: [condition.start + offset, condition.end + offset],
                label: condition.label,
                type: condition.type
            };
        }
        else if ('invalid' in leaf) {
            type = INVALID_RECORDS;
            condition = leaf.invalid;
        }
        else if ('empty' in leaf) {
            type = EMPTY_RECORDS;
            condition = leaf.empty;
        }
        else if ('valid' in leaf) {
            type = VALID_RECORDS;
            condition = leaf.valid;
        }
        else if ('matches' in leaf) {
            type = MATCHES;
            condition = leaf.matches;
            args = {pattern: condition.value};
        }

        var colId = condition.field;
        var filteredColumn = _.find(state.playground.data.metadata.columns, {id: colId});
        var colName = (filteredColumn && filteredColumn.name) || colId;
        return createFilter(type, colId, colName, editable, args, null, null);
    }
}