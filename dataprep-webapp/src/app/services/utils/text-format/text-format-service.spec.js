/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Text format service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.utils'));

    describe('lookup-datagrid constraints', function () {
        it('should return value when it is empty', inject(function (TextFormatService) {
            //given
            var value = '';

            //when
            var result = TextFormatService.adaptToGridConstraints(value);

            //then
            expect(result).toBe(value);
        }));

        it('should add a span on leading spaces', inject(function (TextFormatService) {
            //given
            var value = '  my value';

            //when
            var result = TextFormatService.adaptToGridConstraints(value);

            //then
            expect(result).toBe('<span class="hiddenChars">  </span>my value');
        }));

        it('should add a span on trailing spaces', inject(function (TextFormatService) {
            //given
            var value = 'my value  ';

            //when
            var result = TextFormatService.adaptToGridConstraints(value);

            //then
            expect(result).toBe('my value<span class="hiddenChars">  </span>');
        }));

        it('should add a line breaking arrow at the end of each line', inject(function (TextFormatService) {
            //given
            var value = 'my \nnew\nvalue';

            //when
            var result = TextFormatService.adaptToGridConstraints(value);

            //then
            expect(result).toBe('my ↵\nnew↵\nvalue');
        }));

        it('should escape tags', inject(function (TextFormatService) {
            //given
            var value = '<b>my value</b>';

            //when
            var result = TextFormatService.adaptToGridConstraints(value);

            //then
            expect(result).toBe('&lt;b&gt;my value&lt;/b&gt;');
        }));
    });

    describe('regex', function() {
        it('should escape regex special chars with brackets', inject(function(TextFormatService) {
            //given
            var value = 'azerty - [] {} () *+?.,\\^$|# qwerty';

            //when
            var result = TextFormatService.escapeRegex(value);

            //then
            expect(result).toBe('azerty[ ][-][ ][[][]][ ][{][}][ ][(][)][ ][*][+][?][.][,][\\][^][$][|][#][ ]qwerty');
        }));

        it('should escape regex special chars except star (used as wildcard)', inject(function(TextFormatService) {
            //given
            var value = 'azerty - [] {} () *+?.,\\^$|# qwerty';

            //when
            var result = TextFormatService.escapeRegexpExceptStar(value);

            //then
            expect(result).toBe('azerty \\- \\[\\] \\{\\} \\(\\) .*\\+\\?\\.,\\\\\\^\\$\\|# qwerty');
        }));
    });

    describe('date pattern', function() {
        it('should convert pattern to regexp with escaped special chars', inject(function(TextFormatService) {
            //given
            var pattern = 'a A 9 5-8 *$';
            var expectedRegexp = '^[a-z][ ][A-Z][ ][0-9][ ]5[-]8[ ][*][$]$';

            //when
            var result = TextFormatService.convertPatternToRegexp(pattern);

            //then
            expect(result).toBe(expectedRegexp);
        }));

        it('should java date pattern to moment pattern', inject(function(TextFormatService) {
            //given
            var pattern = 'd/M/yyyy \'o\'\'clock\' mcdo';
            var expectedPattern = 'D/M/YYYY [o\'clock] mcDo';

            //when
            var result = TextFormatService.convertJavaDateFormatToMomentDateFormat(pattern);

            //then
            expect(result).toBe(expectedPattern);
        }));
    });
});