import { chain, find } from 'lodash';
import moment from 'moment-jdateformatparser';

const SPECIAL_JAVA_PATTERNS = [
	{
		java: 'yyyy-MM-dd HH:mm:ss.S',
		js: 'YYYY-MM-DD HH:mm:ss.S',
	},
];

/**
 * @name convertJavaDateFormatToMomentDateFormat
 * @description convert Java Date Format To Moment Date Format
 * @param {string} javaDateFormat The Java Date Format
 */
function convertJavaDateFormatToMomentDateFormat(javaDateFormat) {
	let openQuote = false;
	let pattern = javaDateFormat;

	// * simple quote (') is used in java petterns to escape things.
	// In moment, we use brackets ([])
	// * escaped quotes ('') should be converted to simple quote
	// * words between quotes ('content') should be converted
	// to words between brackets ([content])
	pattern = pattern
		.replace(/\'\'/g, '#tdpQuote') // escape ('') to a unique replacement word
		.replace(/\'/g, () => {        // deal with word between quotes --> words between brackets
			openQuote = !openQuote;
			return openQuote ? '[' : ']';
		})
		.replace(/#tdpQuote/g, '\'');  // replace original ('') to simple quotes

	// toMomentFormatString will modify all the characters (even those between branckets)
	// we save those escaped parts, convert the pattern and replace the parts
	// that should be escaped
	const patternEscapedParts = pattern.match(/\[.*\]/g);
	const patternToApply = find(SPECIAL_JAVA_PATTERNS, { java: pattern });
	pattern = patternToApply ? patternToApply.js : moment().toMomentFormatString(pattern);
	let escapedPartIndex = 0;
	pattern = pattern.replace(/\[.*\]/g, () => {
		return patternEscapedParts[escapedPartIndex++];
	});

	return pattern;
}

/**
 * @name escapeRegex
 * @description Escape regex chars
 * @param {string} value The value to adapt
 * @returns {string} The value with regex chars escaped
 */
function escapeRegex(value) {
	return value.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '[$&]');
}

/**
 * @name convertPatternToRegexp
 * @description Convert pattern to regex
 * @param {string} pattern The pattern
 */
function convertPatternToRegexp(pattern) {
	let regexp = '';

	for (let i = 0, len = pattern.length; i < len; i++) {
		switch (pattern[i]) {
		case 'A':
			regexp += '[A-ZÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞ]';
			break;
		case 'a':
			regexp += '[a-zßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ]';
			break;
		case '9':
			regexp += '[0-9]';
			break;
		case 'h':
		case 'H':
		case 'k':
		case 'K':
		case 'C':
		case 'G':
			regexp += '.';
			break;
		default:
			regexp += escapeRegex(pattern[i]);
		}
	}

	return '^' + regexp + '$';
}

/**
 * @name isDatePattern
 * @description Check if the pattern is a date pattern
 * @param {string} pattern The pattern to check
 */
function isDatePattern(pattern) {
	return (pattern.indexOf('d') > -1 ||
	pattern.indexOf('M') > -1 ||
	pattern.indexOf('y') > -1 ||
	pattern.indexOf('m') > -1 ||
	pattern.indexOf('s') > -1);
}

/**
 * @name valueMatchDatePatternFn
 * @description Create a predicate that check if a value match the date pattern
 * @param {string} pattern The date pattern to match
 */
function valueMatchDatePatternFn(pattern) {
	const datePattern = convertJavaDateFormatToMomentDateFormat(pattern);
	return value => value && moment(value, datePattern, true).isValid();
}

/**
 * @name valueMatchRegexFn
 * @description Create a predicate that check if a value match the regex pattern
 * @param {string} pattern The pattern to match
 */
function valueMatchRegexFn(pattern) {
	const regex = convertPatternToRegexp(pattern);
	return value => value && value.match(regex);
}

/**
 * @name valueMatchPatternFn
 * @description Create the adequat predicate that match the pattern. It can be empty, a date pattern, or an alphanumeric pattern
 * @param {string} pattern The pattern to match
 */
function valueMatchPatternFn(pattern) {
	if (pattern === '') {
		return value => value === '';
	}
	else if (isDatePattern(pattern)) {
		return valueMatchDatePatternFn(pattern);
	}
	else {
		return valueMatchRegexFn(pattern);
	}
}

/**
 * @name patternOccurrenceWorker
 * @description Web worker function to execute to get the pattern filtered occurrences
 * @param {object} parameters {columnId: The column id, patternFrequencyTable: The pattern frequencies to update, filteredRecords: The filtered records to process for the filtered occurrences number}
 */
function patternOccurrenceWorker(parameters) {
	const {
		columnId,
		patternFrequencyTable = [],
		wordPatternFrequencyTable = [],
		filteredRecords,
	} = parameters;

	patternFrequencyTable.forEach((patternFrequency) => {
		const pattern = patternFrequency.pattern;
		const matchingFn = valueMatchPatternFn(pattern);

		patternFrequency.filteredOccurrences = !filteredRecords ?
			patternFrequency.occurrences :
			chain(filteredRecords)
				.map(columnId)
				.filter(value => matchingFn(value))
				.groupBy(value => value)
				.mapValues('length')
				.reduce((accu, value) => accu + value, 0)
				.value();
	});

	wordPatternFrequencyTable.forEach((patternFrequency) => {
		patternFrequency.filteredOccurrences = patternFrequency.occurrences;
	});

	return {
		patternFrequencyTable,
		wordPatternFrequencyTable,
	};
}

/* eslint-disable no-undef */
onmessage = (e) => {
	const result = patternOccurrenceWorker(e.data);
	postMessage(result);
};
/* eslint-enable no-undef */
