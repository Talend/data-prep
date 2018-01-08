/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';
import moment from 'moment-jdateformatparser';

const SPECIAL_JAVA_PATTERNS = [
	{
		java: 'yyyy-MM-dd HH:mm:ss.S',
		js: 'YYYY-MM-DD HH:mm:ss.S',
	},
];

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:TextFormatService
 * @description Text formatting service. It helps to adapt values in lookup-datagrid (show spaces, newlines, etc)
 */
export default function TextFormatService() {
	return {
		adaptToGridConstraints,
		escapeRegex,
		escapeRegexpExceptStar,
		convertPatternToRegexp,
		convertJavaDateFormatToMomentDateFormat,
		highlight,
		highlightWords,
		valueMatchPatternFn,
	};

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------Regex------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name escapeRegex
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Escape regex chars
	 * @param {string} value The value to adapt
	 * @returns {string} The value with regex chars escaped
	 */
	function escapeRegex(value) {
		return value.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '[$&]');
	}

	/**
	 * @ngdoc method
	 * @name escapeRegExpExceptStar
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Escape all regexp characters except * wildcard,
	 * and adapt * wildcard to regexp (* --> .*)
	 * @param {string} str The string to escape
	 */
	function escapeRegexpExceptStar(str) {
		return str.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, '\\$&').replace(/\*/g, '.*');
	}

	// --------------------------------------------------------------------------------------------
	// ----------------------------------------------GRID------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name adaptToGridConstraints
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Adapt value to the lookup-datagrid display constraint
	 * (show spaces, escape, etc)
	 * @param {string} value The value to adapt
	 */
	function adaptToGridConstraints(value) {
		if (!value) {
			return value;
		}

		const constraints = [
			escapeHtmlTags,
			addLineBreaks,
			addTrailingAndLeadingSpacesDivs,
		];

		_.forEach(constraints, function (constraintFn) {
			value = constraintFn(value);
		});

		return value;
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------GRID CONSTRAINTS-------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name addLineBreaks
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Add line break char
	 * @param {string} value The value to adapt
	 */
	function addLineBreaks(value) {
		return value.replace(new RegExp('\n', 'g'), '↵\n'); // eslint-disable-line no-control-regex
	}

	/**
	 * @ngdoc method
	 * @name addTrailingAndLeadingSpacesDivs
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Add divs with "hiddenChars" class to show the leading and trailing spaces
	 * @param {string} value The value to adapt
	 */
	function addTrailingAndLeadingSpacesDivs(value) {
		let returnStr = '';
		const hiddenCharsRegExpMatch = value.match(/(^\s*)?([\s\S]*?)(\s*$)/);

		// leading hidden chars found
		if (hiddenCharsRegExpMatch[1]) {
			returnStr = '<span class="hiddenChars">' +
				hiddenCharsRegExpMatch[1].replace(
					new RegExp('\t', 'g'), // eslint-disable-line no-control-regex
					'    '
				) +
				'</span>';
		}

		// trimmed value
		returnStr += hiddenCharsRegExpMatch[2];

		// trailing hidden chars
		if (hiddenCharsRegExpMatch[3]) {
			returnStr += '<span class="hiddenChars">' +
				hiddenCharsRegExpMatch[3].replace(
					new RegExp('\t', 'g'), // eslint-disable-line no-control-regex
					'    '
				) +
				'</span>';
		}

		return returnStr;
	}

	/**
	 * @ngdoc method
	 * @name escapeHtmlTags
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Escape the tags
	 * @param {string} value The string value to replace
	 */
	function escapeHtmlTags(value) {
		return (value + '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	}

	/**
	 * @ngdoc method
	 * @name convertPatternToRegexp
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Convert pattern to regex
	 * @param {string} pattern The pattern
	 */
	function convertPatternToRegexp(pattern) {
		let regexp = '';
		for (let i = 0, len = pattern.length; i < len; i++) {
			switch (pattern[i]) {
			case 'A':
				regexp += '[A-Z]';
				break;
			case 'a':
				regexp += '[a-z]';
				break;
			case '9':
				regexp += '[0-9]';
				break;
			default:
				regexp += escapeRegex(pattern[i]);
			}
		}

		return '^' + regexp + '$';
	}

	/**
	 * @ngdoc method
	 * @name convertJavaDateFormatToMomentDateFormat
	 * @methodOf data-prep.services.utils:TextFormatService
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
		const patternToApply = _.find(SPECIAL_JAVA_PATTERNS, { java: pattern });
		pattern = patternToApply ? patternToApply.js : moment().toMomentFormatString(pattern);
		let escapedPartIndex = 0;
		pattern = pattern.replace(/\[.*\]/g, () => {
			return patternEscapedParts[escapedPartIndex++];
		});

		return pattern;
	}

	/**
	 * @ngdoc method
	 * @name highlight
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @param {Object} object
	 * @param {Integer} key
	 * @param {String} highlightText text to highlight
	 * @param {String} highlightCssClass css class to highlight
	 * @description Highlight an item of the object and replace it
	 */
	function highlight(object, key, highlightText, highlightCssClass) {
		const originalValue = object[key];
		object[key] = highlightWords(originalValue, highlightText, highlightCssClass);
	}

	/**
	 * @ngdoc method
	 * @name highlightWords
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @param {string} value The value to process
	 * @param {string} words The phrase to highlight
	 * @param {String} highlightCssClass css class to apply
	 * @description Highlight a phrase in a string value
	 */
	function highlightWords(value, words, highlightCssClass) {
		if (value.toLowerCase().indexOf(words.toLowerCase()) === -1) {
			return value;
		}

		return value.replace(
			new RegExp('(' + escapeRegex(words) + ')', 'gi'),
			'<span class="' + highlightCssClass + '">$1</span>'
		);
	}

	// --------------------------------------------------------------------------------------------
	// -------------------------------------------PATTERN------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name isDatePattern
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Check if the pattern is a date pattern
	 * @param {string} pattern The pattern to check
	 */
	function isDatePattern(pattern) {
		return (pattern.indexOf('d') > -1 ||
		pattern.indexOf('M') > -1 ||
		pattern.indexOf('y') > -1 ||
		pattern.indexOf('H') > -1 ||
		pattern.indexOf('h') > -1 ||
		pattern.indexOf('m') > -1 ||
		pattern.indexOf('s') > -1);
	}

	/**
	 * @ngdoc method
	 * @name valueMatchDatePatternFn
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Create a predicate that check if a value match the date pattern
	 * @param {string} pattern The date pattern to match
	 */
	function valueMatchDatePatternFn(pattern) {
		const datePattern = convertJavaDateFormatToMomentDateFormat(pattern);
		return value => value && moment(value, datePattern, true).isValid();
	}

	/**
	 * @ngdoc method
	 * @name valueMatchRegexFn
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Create a predicate that check if a value match the regex pattern
	 * @param {string} pattern The pattern to match
	 */
	function valueMatchRegexFn(pattern) {
		const regex = convertPatternToRegexp(pattern);
		return function (value) {
			return value && value.match(regex);
		};
	}

	/**
	 * @ngdoc method
	 * @name valueMatchPatternFn
	 * @methodOf data-prep.services.utils:TextFormatService
	 * @description Create the adequat predicate that match the pattern. It can be empty, a date pattern, or an alphanumeric pattern
	 * @param {string} pattern The pattern to match
	 */
	function valueMatchPatternFn(pattern) {
		if (pattern === '') {
			return function (value) {
				return value === '';
			};
		}
		else if (isDatePattern(pattern)) {
			return valueMatchDatePatternFn(pattern);
		}
		else {
			return valueMatchRegexFn(pattern);
		}
	}
}
