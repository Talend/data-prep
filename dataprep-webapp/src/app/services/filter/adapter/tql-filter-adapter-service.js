/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';

const CONTAINS = 'contains';
const EXACT = 'exact';
const INVALID_RECORDS = 'invalid_records';
const EMPTY_RECORDS = 'empty_records';
const VALID_RECORDS = 'valid_records';
const INSIDE_RANGE = 'inside_range';
const MATCHES = 'matches';
const QUALITY = 'quality';

const OPERATORS = {
	EQUAL: {
		value: '=',
		hasOperand: true,
	},
	CONTAINS: {
		value: 'contains',
		hasOperand: true,
	},
	IS_VALID: {
		value: 'is valid',
		hasOperand: false,
	},
	IS_INVALID: {
		value: 'is invalid',
		hasOperand: false,
	},
	COMPLIES_TO: {
		value: 'complies to',
		hasOperand: true,
	},
	GREATER_THAN: {
		value: '>=',
		hasOperand: true,
	},
	LESS_THAN: {
		value: '<=',
		hasOperand: true,
	},
};

export default function TqlFilterAdapterService() {
	const CONVERTERS = {
		[CONTAINS]: convertContainsFilterToTQL,
		[EXACT]: convertExactFilterToTQL,
		[VALID_RECORDS]: convertValidFilterToTQL,
		[INVALID_RECORDS]: convertInvalidFilterToTQL,
		[MATCHES]: convertPatternFilterToTQL,
		[INSIDE_RANGE]: convertRangeFilterToTQL,
	};


	return {
		createFilter,
		toTQL,
	};

	//--------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------CREATION-------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function createFilter(
		type,
		colId,
		colName,
		editable,
		args,
		filterFn,
		removeFilterFn
	) {
		const filter = {
			type,
			colId,
			colName,
			editable,
			args,
			filterFn,
			removeFilterFn,
		};

		filter.__defineGetter__('badgeClass', getBadgeClass.bind(filter)); // eslint-disable-line no-underscore-dangle
		filter.__defineGetter__('value', getFilterValueGetter.bind(filter)); // eslint-disable-line no-underscore-dangle
		filter.__defineSetter__('value', value => getFilterValueSetter.call(filter, value)); // eslint-disable-line no-underscore-dangle
		filter.toTQL = getFilterTQL.bind(filter);
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
		case EXACT:
			return this.args.phrase;
		}
	}

	/**
	 * @ngdoc method
	 * @name getBadgeClass
	 * @methodOf data-prep.services.filter.service:FilterAdapterService
	 * @description Return a usable class name for the filter
	 * @returns {Object} The class name
	 */
	function getBadgeClass() {
		if (this.type === QUALITY) {
			const classes = {
				[VALID_RECORDS]: !!this.args.valid,
				[EMPTY_RECORDS]: !!this.args.empty,
				[INVALID_RECORDS]: !!this.args.invalid,
			};

			return Object.keys(classes).filter(n => classes[n]).join(' ');
		}
		return this.type;
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
		case EXACT:
			this.args.phrase = newValue;
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
			newFilter = `(${oldFilter} or ${newFilter})`;
		}
		return newFilter;
	}

	/**
	 * @ngdoc method
	 * @name getFilterTQL
	 * @methodOf data-prep.services.filter.service:FilterAdapterService
	 * @description Adapt filter to single TQL string.
	 * @returns {String} The filter TQL
	 */
	function getFilterTQL() {
		const converter = CONVERTERS[this.type];

		if (converter) {
			return this.value
				.map(filterValue => converter(this.colId, filterValue.value))
				.reduce(reduceOrFn);
		}
	}

	function convertContainsFilterToTQL(fieldId, value) {
		return buildQuery(fieldId, OPERATORS.CONTAINS, value);
	}

	function convertExactFilterToTQL(fieldId, value) {
		return buildQuery(fieldId, OPERATORS.EQUAL, value);
	}

	function convertValidFilterToTQL(fieldId) {
		return buildQuery(fieldId, OPERATORS.IS_VALID);
	}

	function convertInvalidFilterToTQL(fieldId) {
		return buildQuery(fieldId, OPERATORS.IS_INVALID);
	}

	function convertPatternFilterToTQL(fieldId, value) {
		return buildQuery(fieldId, OPERATORS.COMPLIES_TO, value);
	}

	function convertRangeFilterToTQL(fieldId, values) {
		// FIXME [NC]:
		if (!Array.isArray(values)) {
			values = Array(2).fill(values);
		}

		return [
			buildQuery(fieldId, OPERATORS.GREATER_THAN, values[0]),
			buildQuery(fieldId, OPERATORS.LESS_THAN, values[1]),
		].reduce(reduceAndFn);
	}

	function buildQuery(fieldId, operator, value) {
		function wrap(value) {
			return typeof value === 'string' ? `'${value}'` : value;
		}

		if (operator.hasOperand && value && value.length) {
			return `(${fieldId} ${operator.value} ${wrap(value)})`;
		}
		else if (!operator.hasOperand) {
			return `(${fieldId} ${operator.value})`;
		}

		return `(${fieldId} is empty)`;
	}


	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------CONVERTION-------------------------------------------------
	// -------------------------------------------------FILTER ==> TQL----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function toTQL(filters) {
		if (filters.length === 1) {
			return filters[0].toTQL();
		}
		return _.reduce(filters, reduceAndFn, '');
	}

	function reduceAndFn(accu, filterItem) {
		let nextAccuFilter = filterItem.toTQL();

		if (accu && nextAccuFilter) {
			nextAccuFilter = `${accu} and ${nextAccuFilter}`;
		}

		return nextAccuFilter;
	}
}
