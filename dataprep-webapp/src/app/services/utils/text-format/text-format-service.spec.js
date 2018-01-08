/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Text format service', () => {
	'use strict';

	beforeEach(angular.mock.module('data-prep.services.utils'));

	describe('lookup-datagrid constraints', () => {
		it('should return value when it is empty', inject((TextFormatService) => {
			// given
			const value = '';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe(value);
		}));

		it('should add a span on leading spaces', inject((TextFormatService) => {
			// given
			const value = '  my value';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe('<span class="hiddenChars">  </span>my value');
		}));

		it('should add a span on trailing spaces', inject((TextFormatService) => {
			// given
			const value = 'my value  ';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe('my value<span class="hiddenChars">  </span>');
		}));

		it('should add a line breaking arrow at the end of each line', inject((TextFormatService) => {
			// given
			const value = 'my \nnew\nvalue';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe('my ↵\nnew↵\nvalue');
		}));

		it('should add 4 spaces for a tab', inject((TextFormatService) => {
			// given
			const value = '\tmy new\tvalue\t';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe('<span class="hiddenChars">    </span>my new\tvalue<span class="hiddenChars">    </span>');
		}));

		it('should escape tags', inject((TextFormatService) => {
			// given
			const value = '<b>my value</b>';

			// when
			const result = TextFormatService.adaptToGridConstraints(value);

			// then
			expect(result).toBe('&lt;b&gt;my value&lt;/b&gt;');
		}));
	});

	describe('regex', () => {
		it('should escape regex special chars with brackets', inject((TextFormatService) => {
			// given
			const value = 'azerty - [] {} () *+?.,\\^$|# qwerty';

			// when
			const result = TextFormatService.escapeRegex(value);

			// then
			expect(result).toBe('azerty[ ][-][ ][[][]][ ][{][}][ ][(][)][ ][*][+][?][.][,][\\][^][$][|][#][ ]qwerty');
		}));

		it('should escape regex special chars except star (used as wildcard)', inject((TextFormatService) => {
			// given
			const value = 'azerty - [] {} () *+?.,\\^$|# qwerty';

			// when
			const result = TextFormatService.escapeRegexpExceptStar(value);

			// then
			expect(result).toBe('azerty \\- \\[\\] \\{\\} \\(\\) .*\\+\\?\\.,\\\\\\^\\$\\|# qwerty');
		}));
	});

	describe('date pattern', () => {
		it('should convert pattern to regexp with escaped special chars', inject((TextFormatService) => {
			// given
			const pattern = 'a A 9 5-8 *$';
			const expectedRegexp = '^[a-z][ ][A-Z][ ][0-9][ ]5[-]8[ ][*][$]$';

			// when
			const result = TextFormatService.convertPatternToRegexp(pattern);

			// then
			expect(result).toBe(expectedRegexp);
		}));

		it('should convert java date pattern to moment pattern', inject((TextFormatService) => {
			// given
			const pattern = 'd/M/yyyy \'o\'\'clock\' mcdo';
			const expectedPattern = 'D/M/YYYY [o\'clock] mcDo';

			// when
			const result = TextFormatService.convertJavaDateFormatToMomentDateFormat(pattern);

			// then
			expect(result).toBe(expectedPattern);
		}));

		it('should convert special java date pattern to moment pattern', inject((TextFormatService) => {
			// given
			const pattern = 'yyyy-MM-dd HH:mm:ss.S';
			const expectedPattern = 'YYYY-MM-DD HH:mm:ss.S';

			// when
			const result = TextFormatService.convertJavaDateFormatToMomentDateFormat(pattern);

			// then
			expect(result).toBe(expectedPattern);
		}));
	});

	describe('text', () => {
		it('should replace text with highlighted text', inject((TextFormatService) => {
			// given
			const object = {
				name: 'test',
				id: '000',
			};

			// when
			TextFormatService.highlight(object, 'name', 'e', 'highlight');

			// then
			expect(object.name).toBe('t<span class="highlight">e</span>st');
		}));

		it('should return highlighted text', inject((TextFormatService) => {
			// when
			const result = TextFormatService.highlightWords('test', 'e', 'highlight');

			// then
			expect(result).toBe('t<span class="highlight">e</span>st');
		}));

		it('should return original text when there is nothing to highlight', inject((TextFormatService) => {
			// when
			const result = TextFormatService.highlightWords('test', 'not_present_in_text', 'highlight');

			// then
			expect(result).toBe('test');
		}));
	});
});
