import _ from 'lodash';
import moment from 'moment-jdateformatparser';

/**
 * @ngdoc method
 * @name isInDateLimits
 * @methodOf data-prep.services.utils.service:DateService
 * @description Predicate that test if a date is in the range
 * @param {number} minTimestamp The range min timestamp
 * @param {number} maxTimestamp The range max timestamp
 * @param {Array} patterns The date patterns to use for date parsing
 */
function isInDateLimits(minTimestamp, maxTimestamp, patterns) {
	return (value) => {
		const parsedMoment = _.chain(patterns)
			.map(pattern => moment(value, pattern, true))
			.find(momentDate => momentDate.isValid())
			.value();

		if (!parsedMoment) {
			return false;
		}

		const time = parsedMoment.toDate().getTime();
		return time === minTimestamp || (time > minTimestamp && time < maxTimestamp);
	};
}

/**
 * @ngdoc method
 * @name dateOccurrenceWorker
 * @methodOf data-prep.services.statistics.service:StatisticsService
 * @description Web worker function to execute to get the date pattern filtered occurrences
 * @param {object} parameters {rangeData: The range data, patterns: The patterns to use for date parsing, filteredOccurences: The filtered occurrences}
 */
function dateOccurrenceWorker(parameters) {
	const rangeData = parameters.rangeData;
	const patterns = parameters.patterns;
	const filteredOccurrences = parameters.filteredOccurrences;

	_.forEach(rangeData, (range) => {
		const minTimestamp = range.data.min;
		const maxTimestamp = range.data.max;

		range.filteredOccurrences = !filteredOccurrences ?
			range.occurrences :
			_.chain(filteredOccurrences)
				.keys()
				.filter(isInDateLimits(minTimestamp, maxTimestamp, patterns))
				.map(key => filteredOccurrences[key])
				.reduce((accu, value) => accu + value, 0)
				.value();
	});

	return rangeData;
}

/* eslint-disable no-undef */
onmessage = (e) => {
	const result = dateOccurrenceWorker(e.data);
	postMessage(result);
};
/* eslint-enable no-undef */
