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
export const MATCHES_WORDS = 'word_matches';
export const QUALITY = 'quality';
export const WILDCARD = '*';

export default function TqlFilterAdapterService($translate, FilterUtilsService) {
	'ngInject';

	let EMPTY_RECORDS_VALUES;
	let INVALID_EMPTY_RECORDS_VALUES;
	let INVALID_RECORDS_VALUES;
	let VALID_RECORDS_VALUES;
	let filters = [];
	let columns = [];
	return {
		createFilter,
		toTQL,
		fromTQL,
		getEmptyRecordsValues,
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

	function getEmptyRecordsValues() {
		return EMPTY_RECORDS_VALUES;
	}

	function getInvalidEmptyRecordsValues() {
		return INVALID_EMPTY_RECORDS_VALUES;
	}

	function getInvalidRecordsValues() {
		return INVALID_RECORDS_VALUES;
	}

	function getValidRecordsValues() {
		return VALID_RECORDS_VALUES;
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
		case MATCHES_WORDS:
			return this.args.patterns;
		case QUALITY:
			if (this.args.invalid && this.args.empty) {
				return getInvalidEmptyRecordsValues();
			}
			else if (this.args.invalid && !this.args.empty) {
				return getInvalidRecordsValues();
			}
			else if (!this.args.invalid && this.args.empty) {
				return getEmptyRecordsValues();
			}
			else {
				return getValidRecordsValues();
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
		case MATCHES_WORDS:
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
	function prettify(text) {
		return text.substr(1, text.length - 2).replace(/\\\'/g, '\'');
	}
	// Initialize filter listeners which convert TQL to filter models
	function onExactFilter(ctx) {
		const type = EXACT;
		const field = ctx.children[0].getText();
		const args = {
			phrase: [
				{
					value: prettify(ctx.children[2].getText()),
				},
			],
		};
		createFilterFromTQL(type, field, false, args);
	}
	function onContainsFilter(ctx) {
		const type = CONTAINS;
		const field = ctx.children[0].getText();
		const args = {
			phrase: [
				{
					value: prettify(ctx.children[2].getText()),
				},
			],
		};
		createFilterFromTQL(type, field, false, args);
	}
	function onCompliesFilter(ctx) {
		const type = MATCHES;
		const field = ctx.children[0].getText();
		const args = {
			patterns: [
				{
					value: prettify(ctx.children[2].getText()),
				},
			],
		};
		createFilterFromTQL(type, field, false, args);
	}
	function onWordCompliesFilter(ctx) {
		const type = MATCHES_WORDS;
		const field = ctx.children[0].getText();
		const args = {
			patterns: [
				{
					value: prettify(ctx.children[2].getText()),
				},
			],
		};
		createFilterFromTQL(type, field, false, args);
	}
	function onBetweenFilter(ctx) {
		const type = INSIDE_RANGE;
		const field = ctx.children[0].getText();

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
		const excludeMax = ctx.children[6].getText() === '[';

		const label = isDateRange && filteredColumn.statistics.histogram ?
			FilterUtilsService.getDateLabel(
				filteredColumn.statistics.histogram.pace,
				new Date(min),
				new Date(max),
				excludeMax
			) : FilterUtilsService.getRangeLabelFor({
				min,
				max,
				excludeMax,
			}, isDateRange);
		const args = {
			intervals: [{
				label,
				value: [parseInt(min, 10) + offset, parseInt(max, 10) + offset],
			}],
			type: filteredColumn.type,
		};
		createFilterFromTQL(type, field, false, args);
	}
	function onEmptyFilter(ctx) {
		const type = QUALITY;
		const field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
		const args = { empty: true, invalid: false };
		createFilterFromTQL(type, field, false, args);
	}
	function onValidFilter(ctx) {
		const type = QUALITY;
		const field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
		const args = { valid: true };
		createFilterFromTQL(type, field, false, args);
	}
	function onInvalidFilter(ctx) {
		const type = QUALITY;
		const field = ctx.children[0].getText() !== '(' ? ctx.children[0].getText() : ctx.children[1].getText();
		const args = { empty: false, invalid: true };
		createFilterFromTQL(type, field, false, args);
	}
	function createFilterFromTQL(type, colId, editable, args) {
		const filteredColumn = find(columns, { id: colId });
		const colName = (filteredColumn && filteredColumn.name) || colId;

		const existingEmptyFilter = find(filters, {
			colId,
			type: QUALITY,
			args: { empty: true, invalid: false },
		});

		const existingInvalidFilterWithWildcard = find(filters, {
			colId,
			type: QUALITY,
			args: { empty: false, invalid: true },
		});

		if (type === QUALITY) {
			// if there is already a quality filter => merge it with the new quality filter
			if (existingInvalidFilterWithWildcard || existingEmptyFilter) {
				const existingQualityFilter = filters.find(filter => filter.colId === colId && filter.type === QUALITY);
				existingQualityFilter.args.empty = existingQualityFilter.args.empty || args.empty;
				existingQualityFilter.args.invalid = existingQualityFilter.args.empty || args.invalid;
			}
			// For a column, if the new filter is an empty filter and there are already EXACT or MATCHES filters => Merge them into a filter with multi values (same filter badge)
			else if (colId !== WILDCARD && args.empty && !args.invalid) {
				const existingExactFilter = find(filters, {
					colId,
					type: EXACT,
				});
				const existingMatchFilter = find(filters, {
					colId,
					type: MATCHES,
				}) || find(filters, {
					colId,
					type: MATCHES_WORDS,
				});

				if (existingExactFilter) {
					existingExactFilter.args.phrase = existingExactFilter.args.phrase.concat(getEmptyRecordsValues());
				}
				else if (existingMatchFilter) {
					existingMatchFilter.args.patterns = existingMatchFilter.args.patterns.concat(getEmptyRecordsValues());
				}
				else { // create a new filter
					filters.push(
						createFilter(type, colId, colName, editable, args, null)
					);
				}
			}
			else { // create a new filter
				filters.push(
					createFilter(type, colId, colName, editable, args, null)
				);
			}
		}
		// For a column, if the new filter are EXACT or MATCHES filter and there is already an empty filter =>  Merge them into a filter with multi values (same filter badge)
		else if (colId !== WILDCARD && existingEmptyFilter) {
			filters = filters.filter(filter => filter.colId !== colId || filter.type !== QUALITY);
			const filterArgs = {};
			switch (type) {
			case EXACT:
				filterArgs.phrase = getEmptyRecordsValues().concat(args.phrase);
				break;
			case MATCHES:
			case MATCHES_WORDS:
				filterArgs.patterns = getEmptyRecordsValues().concat(args.patterns);
				break;
			}
			filters.push(createFilter(type, colId, colName, editable, filterArgs, null));
		}
		else {
			const sameColAndTypeFilter = find(filters, {
				colId,
				type,
			});
			if (sameColAndTypeFilter) { // update the existing filter
				switch (type) {
				case CONTAINS:
				case EXACT:
					sameColAndTypeFilter.args.phrase = sameColAndTypeFilter.args.phrase.concat(args.phrase);
					break;
				case INSIDE_RANGE:
					sameColAndTypeFilter.args.intervals = sameColAndTypeFilter.args.intervals.concat(args.intervals);
					break;
				case MATCHES:
				case MATCHES_WORDS:
					sameColAndTypeFilter.args.patterns = sameColAndTypeFilter.args.patterns.concat(args.patterns);
					break;
				}
			}
			else { // create a new filter
				filters.push(
					createFilter(type, colId, colName, editable, args, null)
				);
			}
		}
	}
	function fromTQL(tql, cols) {
		columns = cols;
		filters = [];
		if (tql) {
			parse(
				tql,
				onExactFilter,
				onContainsFilter,
				onContainsFilter,
				onCompliesFilter,
				onBetweenFilter,
				onEmptyFilter,
				onValidFilter,
				onInvalidFilter,
				onWordCompliesFilter,
			);
		}
		return filters;
	}
}
