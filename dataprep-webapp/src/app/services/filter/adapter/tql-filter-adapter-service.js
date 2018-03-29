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
		removeFilterFn
	) {
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
	function fromTQL(tql, columns) { // TODO
		let type;
		let args;
		let field;
		const editable = false;
		let filters = [];

		const createFilterFromTQL = (type, colId, editable, args, columns) => {
			const filteredColumn = find(columns, { id: colId });
			const colName = (filteredColumn && filteredColumn.name) || colId;

			const sameColEmptyFilter = find(filters, {
				colId,
				type: EMPTY_RECORDS,
			});

			const InvalidFilterWithWildcard = find(filters, {
				WILDCARD,
				type: INVALID_RECORDS,
			});

			const EmptyFilterWithWildcard = find(filters, {
				WILDCARD,
				type: EMPTY_RECORDS,
			});

			if (colId === WILDCARD) {
				if (InvalidFilterWithWildcard && type === EMPTY_RECORDS) {
					filters = filters.filter(filter => filter.colId !== WILDCARD || filter.type !== EMPTY_RECORDS);
					filters.push(
						createFilter(QUALITY, colId, colName, editable, {
							invalid: true,
							empty: true,
						}, null)
					);
				}
				else if (EmptyFilterWithWildcard && type === INVALID_RECORDS) {
					filters = filters.filter(filter => filter.colId !== WILDCARD || filter.type !== INVALID_RECORDS);
					filters.push(
						createFilter(QUALITY, colId, colName, editable, {
							invalid: true,
							empty: true,
						}, null)
					);
				}
				else {
					filters.push(
						createFilter(type, colId, colName, editable, args, null)
					);
				}
			}
			else {
				// EMPTY_RECORDS case: if the filter is EMPTY_RECORDS, merge it into EXACT or MATCHES filters
				if (type === EMPTY_RECORDS) {
					const sameColExactFilter = find(filters, {
						colId,
						type: EXACT,
					});
					const sameColMatchFilter = find(filters, {
						colId,
						type: MATCHES,
					});

					if (sameColExactFilter) {
						sameColExactFilter.args.phrase = sameColExactFilter.args.phrase.concat(this.EMPTY_RECORDS_VALUES);
					}
					else if (sameColMatchFilter) {
						sameColExactFilter.args.patterns = sameColExactFilter.args.patterns.concat(this.EMPTY_RECORDS_VALUES);
					}
					else {
						filters.push(
							createFilter(type, colId, colName, editable, args, null)
						);
					}
				}
				// EMPTY_RECORDS case: if the filter is EMPTY_RECORDS, merge it into EXACT or MATCHES filters
				else if (sameColEmptyFilter) { // if EMPTY_RECORDS filter is already added,  merge it into new EXACT or MATCHES filters
					filters = filters.filter(filter => filter.colId !== colId || filter.type !== EMPTY_RECORDS);
					const filterArgs = {};
					switch (type) {
					case EXACT:
						filterArgs.phrase = this.EMPTY_RECORDS_VALUES.concat(args.phrase);
						break;
					case MATCHES:
						filterArgs.patterns = this.EMPTY_RECORDS_VALUES.concat(args.patterns);
						break;
					}
					filters.push(createFilter(type, colId, colName, editable, filterArgs, null));
				}
				// others case: if the filter is EMPTY_RECORDS, merge it into EXACT or MATCHES filters
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

			const min = ctx.children[3].getText();
			const max = ctx.children[5].getText();
			const filteredColumn = find(columns, { id: field });
			const isDateRange = filteredColumn && (filteredColumn.type === 'date');
			// on date we shift timestamp to fit UTC timezone
			let offset = 0;
			if (isDateRange) {
				const minDate = new Date(min);
				offset = minDate.getTimezoneOffset() * 60 * 1000;
			}

			const label = isDateRange ?
				FilterUtilsService.getDateLabel(filteredColumn.statistics.histogram.pace, min, max) :
				FilterUtilsService.getRangeLabelFor({ min, max }, isDateRange);

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
			type = EMPTY_RECORDS;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText(); // TODO
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onValidFilter = (ctx) => {
			type = VALID_RECORDS;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText(); // TODO
			return createFilterFromTQL(type, field, editable, args, columns);
		};
		const onInvalidFilter = (ctx) => {
			type = INVALID_RECORDS;
			field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText(); // TODO
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
				onInvalidFilter
			);
		}

		return filters;
	}
}
