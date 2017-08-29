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

export default function TqlFilterAdapterService() {
	return {
		createFilter,
		toTQL,
	};

	//--------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------CREATION-------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function createFilter(type, colId, colName, editable, args, filterFn, removeFilterFn) {
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
			return this.args.phrase;
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
			let className = '';

			if (this.args.valid) {
				className += ` ${VALID_RECORDS}`;
			}

			if (this.args.empty) {
				className += ` ${EMPTY_RECORDS}`;
			}

			if (this.args.invalid) {
				className += ` ${INVALID_RECORDS}`;
			}

			return className;
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
			this.args.phrase = newValue;
			break;
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
			newFilter = '(' + oldFilter + ' or ' + newFilter + ')';
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
		//const args = this.args;
		const colId = this.colId;
		const value = this.value;
		switch (this.type) {
		case CONTAINS:
			return value
				.map((filterValue) => {
					return convertContainsFilterToTQL(colId, filterValue);
				})
				.reduce(reduceOrFn);
		case EXACT:
			return value
				.map((filterValue) => {
					return convertExactFilterToTQL(colId, filterValue);
				})
				.reduce(reduceOrFn);
		}
	}

	function convertContainsFilterToTQL(fieldId, value) {
		let dsl = '';
		if (typeof (value) === 'string') {  // eslint-disable-line angular/typecheck-string
			dsl = value !== '' ?
			'(' + fieldId + "contains" + value + "')" :
			'(' + fieldId + ' is empty)';
		} else {
			dsl = value !== '' ?
			'(' + fieldId + 'contains' + value + ')' :
			'(' + fieldId + ' is empty)';
		}
		return dsl;
	}

	function convertExactFilterToTQL(fieldId, value) {
		let dsl = '';
		if (typeof (value) === 'string') {  // eslint-disable-line angular/typecheck-string
			dsl = value !== '' ?
			'(' + fieldId + "='" + value + "')" :
			'(' + fieldId + ' is empty)';
		} else {
			dsl = value !== '' ?
			'(' + fieldId + '=' + value + ')' :
			'(' + fieldId + ' is empty)';
		}
		return dsl;
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------CONVERTION-------------------------------------------------
	// -------------------------------------------------FILTER ==> TQL----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function toTQL(filters) {
		return _.reduce(filters, reduceAndFn, '');
	}

	function reduceAndFn(accu, filterItem) {
		let nextAccuFilter = filterItem.toTree();

		if (nextAccuFilter) {
			nextAccuFilter = accu + ' and ' + nextAccuFilter;
		}

		return nextAccuFilter;
	}
}
