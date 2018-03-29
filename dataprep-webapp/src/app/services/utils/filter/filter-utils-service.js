/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import d3 from 'd3';

const RANGE_SEPARATOR = ' .. ';

export default function FilterUtilsService($filter) {
	'ngInject';

	const service = {
		getRangeLabelFor,
		getDateLabel,
		getDateFormat,
	};
	return service;

	//----------------------------------------------------------------------------------------------
	// ---------------------------------------------------UTILS-------------------------------------
	//----------------------------------------------------------------------------------------------
	function getRangeLabelFor(interval, isDateRange) {
		let label;
		const formatDate = d3.time.format('%Y-%m-%d');
		const formatNumber = d3.format(',');
		let min;
		let max;
		if (isDateRange) {
			min = formatDate(new Date(interval.min));
			max = formatDate(new Date(interval.max));
		}
		else if (angular.isNumber(interval.min)) {
			min = formatNumber(interval.min);
			max = formatNumber(interval.max);
		}
		else {
			min = interval.min;
			max = interval.max;
		}

		if (min === max) {
			label = `[${min}]`;
		}
		else {
			const closing = interval.excludeMax ? '[' : ']';
			label = `[${min}${RANGE_SEPARATOR}${max}${closing}`;
		}

		return label;
	}


	/**
	 * @ngdoc method
	 * @name getDateFormat
	 * @methodOf data-prep.services.statistics.service:StatisticsService
	 * @param {String} pace The histogram time pace
	 * @param {Date} startDate The range starting date
	 * @description Returns the date pattern that fit the pace at the starting date
	 */
	function getDateFormat(pace, startDate) {
		switch (pace) {
		case 'CENTURY':
		case 'DECADE':
		case 'YEAR':
			return 'yyyy';
		case 'HALF_YEAR':
			return '\'H\'' + ((startDate.getMonth() / 6) + 1) + ' yyyy';
		case 'QUARTER':
			return 'Q' + ((startDate.getMonth() / 3) + 1) + ' yyyy';
		case 'MONTH':
			return 'MMM yyyy';
		case 'WEEK':
			return 'Www yyyy';
		default:
			return 'mediumDate';
		}
	}

	/**
	 * @ngdoc method
	 * @name getDateLabel
	 * @methodOf data-prep.services.statistics.service:StatisticsService
	 * @param {string} pace The histogram time pace
	 * @param {Date} minDate The range starting date
	 * @param {Date} maxDate The range ending date
	 * @description Returns the range label
	 */
	function getDateLabel(pace, minDate, maxDate) {
		const dateFilter = $filter('date');
		const format = getDateFormat(pace, minDate);

		switch (pace) {
		case 'YEAR':
		case 'HALF_YEAR':
		case 'QUARTER':
		case 'MONTH':
		case 'WEEK':
		case 'DAY':
			return dateFilter(minDate, format);
		default:
			return '[' + dateFilter(minDate, format) + ', ' + dateFilter(maxDate, format) + '[';
		}
	}
}
