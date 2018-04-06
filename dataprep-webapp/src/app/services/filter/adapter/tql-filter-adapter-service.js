/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { find } from 'lodash';

import { Parser } from '@talend/daikon-tql-client';
import { parse } from '@talend/tql/index';

export const CONTAINS = 'contains';
export const EXACT = 'exact';
export const INVALID_RECORDS = 'invalid_records';
export const INVALID_EMPTY_RECORDS = 'invalid_empty_records';
export const VALID_RECORDS = 'valid_records';
export const EMPTY_RECORDS = 'empty_records';
export const INSIDE_RANGE = 'inside_range';
export const MATCHES = 'matches';
export const QUALITY = 'quality';
export const WILDCARD = '*';

export default function TqlFilterAdapterService($translate, FilterUtilsService) {
	let EMPTY_RECORDS_VALUES;
	let INVALID_EMPTY_RECORDS_VALUES;
	let INVALID_RECORDS_VALUES;
	let VALID_RECORDS_VALUES;


	return {
		createFilter,
		toTQL,
		fromTQL,
		EMPTY_RECORDS_VALUES,
		INVALID_EMPTY_RECORDS_VALUES,
		INVALID_RECORDS_VALUES,
		VALID_RECORDS_VALUES,

	};

	//--------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------CREATION-------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function createFilter(type, colId, colName, editable, args, removeFilterFn) {
		const filter = {
			type,
			colId,
			colName,
			editable,
			args,
			removeFilterFn,
		};

		EMPTY_RECORDS_VALUES = [
			{
				label: $translate.instant('EMPTY_RECORDS_LABEL'),
				isEmpty: true,
				value: '',
			},
		];

		INVALID_EMPTY_RECORDS_VALUES = [
			{
				label: $translate.instant('INVALID_EMPTY_RECORDS_LABEL'),
			},
		];

		INVALID_RECORDS_VALUES = [
			{
				label: $translate.instant('INVALID_RECORDS_LABEL'),
			},
		];

		VALID_RECORDS_VALUES = [
			{
				label: $translate.instant('VALID_RECORDS_LABEL'),
			},
		];

		filter.__defineGetter__('badgeClass', getBadgeClass.bind(filter)); // eslint-disable-line no-underscore-dangle
		filter.__defineGetter__('value', getFilterValueGetter.bind(filter)); // eslint-disable-line no-underscore-dangle
		filter.__defineSetter__('value', value =>
			getFilterValueSetter.call(filter, value)
		); // eslint-disable-line no-underscore-dangle
		return filter;
	}

	/**
	 * @ngdoc method
	 * @name getFilterValueGetter
	 * @methodOf data-prep.services.filter.service:TqlFilterAdapterService
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
		case QUALITY:
			if (this.args.invalid && this.args.empty) {
				return INVALID_EMPTY_RECORDS_VALUES;
			}
			else if (this.args.invalid && !this.args.empty) {
				return INVALID_RECORDS_VALUES;
			}
			else if (!this.args.invalid && this.args.empty) {
				return EMPTY_RECORDS_VALUES;
			}
			else {
				return VALID_RECORDS_VALUES;
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name getBadgeClass
	 * @methodOf data-prep.services.filter.service:TqlFilterAdapterService
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

			return Object.keys(classes)
				.filter(n => classes[n])
				.join(' ');
		}

		return this.type;
	}

	/**
	 * @ngdoc method
	 * @name getFilterValueSetter
	 * @methodOf data-prep.services.filter.service:TqlFilterAdapterService
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

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------CONVERTION-------------------------------------------------
	// -------------------------------------------------FILTER ==> TQL----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function toTQL(filters) {
		return Parser.parse(filters).serialize();
	}

	//--------------------------------------------------------------------------------------------------------------
	// ---------------------------------------------------CONVERTION-------------------------------------------------
	// -------------------------------------------------TQL ==> FILTER----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	function fromTQL(tql, columns) {
		let type;
		let args;
		let field;
		const editable = false;
		let filters = [];

		const createFilterFromTQL = (type, colId, editable, args, columns) => {
			const filteredColumn = find(columns, { id: colId });
			const colName = (filteredColumn && filteredColumn.name) || colId;

			const existingColEmptyFilter = find(filters, {
				colId,
				type: QUALITY,
				args: { empty: true, invalid: false },
			});

			const InvalidFilterWithWildcard = find(filters, {
				colId: WILDCARD,
				type: QUALITY,
				args: { empty: false, invalid: true },
			});

			if (colId === WILDCARD && type === QUALITY) {
				// if there is already a quality filter => merge it with the new quality filter
				if (InvalidFilterWithWildcard || existingColEmptyFilter) {
					const existingQualityFilter = filters.find(filter => filter.colId === WILDCARD && filter.type === QUALITY);
					existingQualityFilter.args.empty = existingQualityFilter.args.empty || args.empty;
					existingQualityFilter.args.invalid = existingQualityFilter.args.empty || args.invalid;
				}
				// Otherwise, add the new quality filter
				else {
					filters.push(
						createFilter(type, colId, colName, editable, args, null)
					);
				}
			}
			else {
				const sameColAndTypeFilter = find(filters, {
					colId,
					type,
				});
				if (sameColAndTypeFilter) {
					switch (type) {
					case CONTAINS:
					case EXACT:
						sameColAndTypeFilter.args.phrase = sameColAndTypeFilter.args.phrase.concat(args.phrase);
						break;
					case INSIDE_RANGE:
						sameColAndTypeFilter.args.intervals = sameColAndTypeFilter.args.intervals.concat(args.intervals);
						break;
					case MATCHES:
						sameColAndTypeFilter.args.patterns = sameColAndTypeFilter.args.patterns.concat(args.patterns);
						break;
					}
				}
				else {
					filters.push(
						createFilter(type, colId, colName, editable, args, null)
					);
				}
			}
		};

		// initialize filter listeners
		const onExactFilter = (ctx) => {
			type = EXACT;
			field = ctx.children[0].getText();
			args = {
				phrase: [
					{
						value: ctx.children[2].getText().replace(/'/g, ''),
					},
				],
			};
			return createFilterFromTQL(type, field, editable, args, columns);
		};

		const onContainsFilter = (ctx) => {
			type = CONTAINS;
			field = ctx.children[0].getText();
			args = {
				phrase: [
					{
						value: ctx.children[2].getText().replace(/'/g, ''),
					},
				],
			};
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onCompliesFilter = (ctx) => {
			type = MATCHES;
			field = ctx.children[0].getText();
			args = {
				patterns: [
					{
						value: ctx.children[2].getText().replace(/'/g, ''),
					},
				],
			};
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onBetweenFilter = (ctx) => {
			type = INSIDE_RANGE;
			field = ctx.children[0].getText();

			const min = parseInt(ctx.children[3].getText(), 10);
			const max = parseInt(ctx.children[5].getText(), 10);
			const filteredColumn = find(columns, { id: field });
			const isDateRange = filteredColumn && (filteredColumn.type === 'date');
			// on date we shift timestamp to fit UTC timezone
			let offset = 0;
			if (isDateRange) {
				const minDate = new Date(min);
				offset = minDate.getTimezoneOffset() * 60 * 1000;
			}
			const label = isDateRange ?
				FilterUtilsService.getDateLabel(
					filteredColumn.statistics.histogram.pace,
					new Date(min),
					new Date(max)
				) : FilterUtilsService.getRangeLabelFor({ min, max }, isDateRange);

			args = {
				intervals: [{
					label,
					value: [parseInt(min, 10) + offset, parseInt(max, 10) + offset],
				}],
				type: filteredColumn.type,
			};
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onEmptyFilter = (ctx) => {
			type = QUALITY;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
			args = { empty: true, invalid: false };
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onValidFilter = (ctx) => {
			type = QUALITY;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
			args = { valid: true };
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onInvalidFilter = (ctx) => {
			type = QUALITY;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
			args = { empty: false, invalid: true };
			return createFilterFromTQL(type, field, editable, args, columns);
		};

		if (tql) {
			parse(
				tql,
				onExactFilter,
				onContainsFilter,
				onCompliesFilter,
				onBetweenFilter,
				onEmptyFilter,
				onValidFilter,
				onInvalidFilter,
			);
		}

		return filters;
	}
}
