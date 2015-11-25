describe('Text format service', function () {
    'use strict';

    beforeEach(module('data-prep.services.utils'));

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
    });
});