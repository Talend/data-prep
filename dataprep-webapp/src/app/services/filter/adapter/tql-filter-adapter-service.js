/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';


const isString = v => typeof v === 'string';
const wrap = (v) => {
	return isString(v) ? `'${v}'` : v;
};

export const CONTAINS = 'contains';
export const EXACT = 'exact';
export const INVALID_RECORDS = 'invalid_records';
export const VALID_RECORDS = 'valid_records';
export const EMPTY_RECORDS = 'empty_records';
export const INSIDE_RANGE = 'inside_range';
export const MATCHES = 'matches';
export const QUALITY = 'quality';
export const EMPTY = 'empty';

const OPERATORS = {
	EQUAL: {
		value: '=',
	},
	CONTAINS: {
		value: 'contains',
	},
	COMPLIES_TO: {
		value: 'complies to',
	},
	GREATER_THAN: {
		value: '>=',
	},
	LESS_THAN: {
		value: '<=',
	},
	IS_VALID: {
		value: 'is valid',
		hasOperand: false,
	},
	IS_INVALID: {
		value: 'is invalid',
		hasOperand: false,
	},
	IS_EMPTY: {
		value: 'is empty',
		hasOperand: false,
	},
};

export default function TqlFilterAdapterService($translate) {
	'ngInject';

	const CONVERTERS = {
		[CONTAINS]: (field, value) => buildQuery(field, OPERATORS.CONTAINS, value),
		[EXACT]: (field, value) => buildQuery(field, OPERATORS.EQUAL, value),
		[INVALID_RECORDS]: field => buildQuery(field, OPERATORS.IS_INVALID),
		[VALID_RECORDS]: field => buildQuery(field, OPERATORS.IS_VALID),
		[MATCHES]: (field, value) => buildQuery(field, OPERATORS.COMPLIES_TO, value),
		[EMPTY]: field => buildQuery(field, OPERATORS.IS_EMPTY),
		[INSIDE_RANGE]: convertRangeFilterToTQL,
	};


	const INVALID_EMPTY_RECORDS_VALUES = [{
		label: $translate.instant('INVALID_EMPTY_RECORDS_LABEL'),
	}];

	const INVALID_RECORDS_VALUES = [{
		label: $translate.instant('INVALID_RECORDS_LABEL'),
	}];

	const VALID_RECORDS_VALUES = [{
		label: $translate.instant('VALID_RECORDS_LABEL'),
	}];

	const EMPTY_RECORDS_VALUES = [{
		label: $translate.instant('EMPTY_RECORDS_LABEL'),
		isEmpty: true,
	}];

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
		case INSIDE_RANGE:
			return this.args.intervals;
		case MATCHES:
			return this.args.patterns;
		case INVALID_RECORDS:
			return INVALID_RECORDS_VALUES;
		case VALID_RECORDS:
			return VALID_RECORDS_VALUES;
		case EMPTY_RECORDS:
			return EMPTY_RECORDS_VALUES;
		case QUALITY: // TODO: refacto QUALITY filter
			if (this.args.invalid && this.args.empty) {
				return INVALID_EMPTY_RECORDS_VALUES;
			}
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
		case INSIDE_RANGE:
			this.args.intervals = newValue;
			break;
		case MATCHES:
			this.args.patterns = newValue;
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

	function buildQuery(fieldId, operator, value) {
		if (operator.hasOperand !== false && value !== '') {
			return `(${fieldId} ${operator.value} ${wrap(value)})`;
		}
		else if (operator.hasOperand === false) {
			return `(${fieldId} ${operator.value})`;
		}

		return `(${fieldId} ${OPERATORS.IS_EMPTY.value})`;
	}

	function convertRangeFilterToTQL(fieldId, values) {
		// FIXME [NC]:
		if (!Array.isArray(values)) {
			values = Array(2).fill(values);
		}

		return [OPERATORS.GREATER_THAN, OPERATORS.LESS_THAN]
			.map((operator, i) => buildQuery(fieldId, operator, values[i]))
			.reduce(reduceAndFn);
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

	function reduceAndFn(accu, item) {
		if (!isString(item)) {
			item = item.toTQL();
		}

		return (accu && item) ? `${accu} and ${item}` : item;
	}
}
