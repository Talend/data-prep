/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:TextFormatService
 * @description Text formatting service. It helps to adapt values in lookup-datagrid (show spaces, newlines, etc)
 */
export default function TextFormatService() {
    return {
        adaptToGridConstraints: adaptToGridConstraints,
        escapeRegex: escapeRegex,
        escapeRegexpExceptStar: escapeRegexpExceptStar,
        convertPatternToRegexp: convertPatternToRegexp,
        convertJavaDateFormatToMomentDateFormat: convertJavaDateFormatToMomentDateFormat,
        highlight: highlight
    };

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------Regex----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
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
     * @description Escape all regexp characters except * wildcard, and adapt * wildcard to regexp (* --> .*)
     * @param {string} str The string to escape
     */
    function escapeRegexpExceptStar(str) {
        return str.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, '\\$&').replace(/\*/g, '.*');
    }

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------GRID-----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name adaptToGridConstraints
     * @methodOf data-prep.services.utils:TextFormatService
     * @description Adapt value to the lookup-datagrid display constraint (show spaces, escape, etc)
     * @param {string} value The value to adapt
     */
    function adaptToGridConstraints(value) {
        if (!value) {
            return value;
        }

        var constraints = [
            escapeHtmlTags,
            addLineBreaks,
            addTrailingAndLeadingSpacesDivs
        ];

        _.forEach(constraints, function (constraintFn) {
            value = constraintFn(value);
        });

        return value;
    }

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------GRID CONSTRAINTS-----------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name addLineBreaks
     * @methodOf data-prep.services.utils:TextFormatService
     * @description Add line break char
     * @param {string} value The value to adapt
     */
    function addLineBreaks(value) {
        return value.replace(new RegExp('\n', 'g'), '↵\n'); //eslint-disable-line no-control-regex
    }

    /**
     * @ngdoc method
     * @name addTrailingAndLeadingSpacesDivs
     * @methodOf data-prep.services.utils:TextFormatService
     * @description Add divs with "hiddenChars" class to show the leading and trailing spaces
     * @param {string} value The value to adapt
     */
    function addTrailingAndLeadingSpacesDivs(value) {
        var returnStr = '';
        var hiddenCharsRegExpMatch = value.match(/(^\s*)?([\s\S]*?)(\s*$)/);

        //leading hidden chars found
        if (hiddenCharsRegExpMatch[1]) {
            returnStr = '<span class="hiddenChars">' + hiddenCharsRegExpMatch[1] + '</span>';
        }

        //trimmed value
        returnStr += hiddenCharsRegExpMatch[2];

        //trailing hidden chars
        if (hiddenCharsRegExpMatch[3]) {
            returnStr += '<span class="hiddenChars">' + hiddenCharsRegExpMatch[3] + '</span>';
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
        var regexp = '';
        for (var i = 0, len = pattern.length; i < len; i++) {
            switch (pattern[i]) {
                case 'A':
                    regexp += '[A-Z]';
                    break;
                case'a':
                    regexp += '[a-z]';
                    break;
                case'9':
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
        var openQuote = false;
        var pattern = javaDateFormat;

        // simple quote (') is used in java petterns to escape things. In moment, we use brackets ([])
        // escaped quotes ('') should be converted to simple quote
        // words between quotes ('content') should be converted to words between brackets ([content])
        pattern = pattern.replace(/\'\'/g, '#tdpQuote')     //escape ('') to a unique replacement word
            .replace(/\'/g, function () {                    //deal with word between quotes --> words between brackets
                openQuote = !openQuote;
                return openQuote ? '[' : ']';
            })
            .replace(/#tdpQuote/g, '\'');                   //replace original ('') to simple quotes

        // toMomentFormatString will modify all the characters (even those between branckets)
        // we save those escaped parts, convert the pattern and replace the parts that should be escaped
        var patternEscapedParts = pattern.match(/\[.*\]/g);
        pattern = moment().toMomentFormatString(pattern);
        var escapedPartIndex = 0;
        pattern = pattern.replace(/\[.*\]/g, function () {
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
     * @param {String} hightlightCssClass css class to highlight
     * @description highlight an item of the object
     */
    function highlight(object, key, highlightText, hightlightCssClass) {
        let originalValue = object[key];
        if (originalValue.toLowerCase().indexOf(highlightText.toLowerCase()) !== -1) {
            object[key] = originalValue.replace(
                new RegExp('(' + escapeRegex(highlightText) + ')', 'gi'),
                '<span class="' + hightlightCssClass + '">$1</span>');
        }
    }
}