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

const EMPTY_RECORDS_LABEL = 'empty records';
const INVALID_RECORDS_LABEL = 'invalid records';
const VALID_RECORDS_LABEL = 'valid records';

const INVALID_RECORDS_VALUES = [{
    label: INVALID_RECORDS_LABEL
}];

const EMPTY_RECORDS_VALUES = [{
    label: EMPTY_RECORDS_LABEL,
    isEmpty: true
}];

const VALID_RECORDS_VALUES = [{
    label: VALID_RECORDS_LABEL
}];

/**
 * @ngdoc service
 * @name data-prep.services.filter.service:FilterAdapterService
 * @description Filter adapter service. This service provides filter constructor and adapters
 * @requires data-prep.services.state.constant:state
 */
export default function FilterAdapterService(state) {
    'ngInject';

    return {
        EMPTY_RECORDS_LABEL: EMPTY_RECORDS_LABEL,
        INVALID_RECORDS_LABEL: INVALID_RECORDS_LABEL,
        VALID_RECORDS_LABEL: VALID_RECORDS_LABEL,

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

        filter.__defineGetter__('value', getFilterValueGetter.bind(filter));
        filter.__defineSetter__('value', (value) => getFilterValueSetter.call(filter, value));
        filter.toTree = getFilterTree.bind(filter);
        return filter;
    }

    /**
     * @ngdoc method
     * @name getFilterValueGetter
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @description Return the filter value depending on its type. This function should be used with filter definition object binding
     * @returns {Object} The filter value
     */
    function getFilterValueGetter() {
        switch (this.type) {
            case CONTAINS:
                return this.args.phrase;
            case EXACT:
                return this.args.phrase;
            case INVALID_RECORDS:
                return INVALID_RECORDS_VALUES;
            case EMPTY_RECORDS:
                return EMPTY_RECORDS_VALUES;
            case VALID_RECORDS:
                return VALID_RECORDS_VALUES;
            case INSIDE_RANGE:
                return this.args.intervals;
            case MATCHES:
                return this.args.patterns;
        }
    }
    
    /**
     * @ngdoc method
     * @name getFilterValueSetter
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @description Set the filter value depending on its type. This function should be used with filter definition object binding
     * @returns {Object} The filter value
     */
    function getFilterValueSetter(newValue) {
        switch (this.type) {
            case CONTAINS:
                this.args.phrase = newValue;
                break;
            case EXACT:
                this.args.phrase = newValue;
                break;
            case INSIDE_RANGE:
                this.args.intervals = newValue;
                break;
            case MATCHES:
                this.args.patterns = newValue;
                break;
        }
    }

    /**
     * @ngdoc method
     * @name reduceOrFn
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @param {Object} accu The filter tree accumulator
     * @param {Object} filterItem The filter definition
     * @description Reduce function for filters adaptation to tree
     * @returns {Object} The combined filter/accumulator tree
     */
    function reduceOrFn(oldFilter, newFilter) {
        if (oldFilter) {
            newFilter = {
                or: [oldFilter, newFilter]
            };
        }
        return newFilter;
    }

    /**
     * @ngdoc method
     * @name getFilterTree
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @description Adapt filter to single tree. This function should be used with filter definition object binding
     * @returns {Object} The filter tree
     */
    function getFilterTree() {
        const args = this.args;
        const colId = this.colId;
        const value = this.value;
        switch (this.type) {
            case CONTAINS:
                return value
                    .map(filterValue => {
                        return {
                            contains: {
                                field: colId,
                                value: filterValue.value
                            }
                        }
                    })
                    .reduce(reduceOrFn);
            case EXACT:
                return value
                    .map(filterValue => {
                        return {
                            eq: {
                                field: colId,
                                value: filterValue.value
                            }
                        }
                    })
                    .reduce(reduceOrFn);
            case INVALID_RECORDS:
                return {
                    invalid: {
                        field: colId
                    }
                };
            case EMPTY_RECORDS:
                return {
                    empty: {
                        field: colId
                    }
                };
            case VALID_RECORDS:
                return {
                    valid: {
                        field: colId
                    }
                };
            case INSIDE_RANGE:
                const argsType = args.type;
                return value
                    .map(filterValue => {
                        const min = filterValue.value[0];
                        const max = filterValue.value[1];
                        //on date we shift timestamp to fit UTC timezone
                        let offset = 0;
                        if (argsType === 'date') {
                            const minDate = new Date(min);
                            offset = minDate.getTimezoneOffset() * 60 * 1000;
                        }
                        return {
                            range: {
                                field: colId,
                                start: min - offset,
                                end: max - offset,
                                type: argsType,
                                label: filterValue.label
                            }
                        }
                    })
                    .reduce(reduceOrFn);
            case MATCHES:
                return value
                    .map(filterValue => {
                        return {
                            matches: {
                                field: colId,
                                value: filterValue.value
                            }
                        }
                    })
                    .reduce(reduceOrFn);
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
        return _.reduce(filters, reduceAndFn, {});
    }

    /**
     * @ngdoc method
     * @name reduceAndFn
     * @methodOf data-prep.services.filter.service:FilterAdapterService
     * @param {Object} accu The filter tree accumulator
     * @param {Object} filterItem The filter definition
     * @description Reduce function for filters adaptation to tree
     * @returns {Object} The combined filter/accumulator tree
     */
    function reduceAndFn(accu, filterItem) {
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
        if (!tree.and && !tree.or) {
            return [leafToFilter(tree)];
        }

        //it is an "or" node
        if (tree.or) {
            const reducedOrTree = _.reduce(tree.or, (accu, nodeChild) => {
                const existingFilter = accu;
                const newFilter = fromTree(nodeChild);
                const existingFilterFirstValue = existingFilter[0];
                const newFilterFirstValue = newFilter[0];
                if (existingFilterFirstValue && existingFilterFirstValue.args) {
                    const getExistingArgs = getFilterValueGetter.bind(existingFilterFirstValue);
                    const newValues = getExistingArgs().concat(newFilterFirstValue.value);
                    switch (existingFilterFirstValue.type) {
                        case CONTAINS:
                        case EXACT:
                            existingFilterFirstValue.args.phrase = newValues;
                            break;
                        case INSIDE_RANGE:
                            existingFilterFirstValue.args.intervals = newValues;
                            break;
                        case MATCHES:
                            existingFilterFirstValue.args.patterns = newValues;
                            break;
                    }
                    return existingFilter;
                }
                return accu.concat(newFilter);
            }, []);
            return reducedOrTree;
        }

        //it is an "and" node
        return _.reduce(tree.and, (accu, nodeChild) => accu.concat(fromTree(nodeChild)), []);
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
            args = {
                phrase: [
                    {
                        value: condition.value
                    }
                ]
            };
        }
        else if ('eq' in leaf) {
            type = EXACT;
            condition = leaf.eq;
            args = {
                phrase: [
                    {
                        value: condition.value
                    }
                ]
            };
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
                intervals: [{
                    label: condition.label,
                    value: [condition.start + offset, condition.end + offset]
                }],
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
            args = {
                patterns: [
                    {
                        value: condition.value
                    }
                ]
            };
        }

        var colId = condition.field;
        var filteredColumn = _.find(state.playground.data.metadata.columns, {id: colId});
        var colName = (filteredColumn && filteredColumn.name) || colId;
        return createFilter(type, colId, colName, editable, args, null, null);
    }
}