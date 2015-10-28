describe('Actions suggestions search filter', function () {
    'use strict';

    var transformations = [
        {
            'name': 'ceil_value',
            'label': 'Ceil value',
            'labelHtml': 'Ceil value',
            'description': 'f',
            'category': 'w',
            'categoryHtml': 'w'
        },
        {
            'name': 'floor_value',
            'label': 'Floor value',
            'labelHtml': 'Floor value',
            'description': 's',
            'category': 'x',
            'categoryHtml': 'x'
        },
        {
            'name': 'round_value',
            'label': 'Round value',
            'labelHtml': 'Round value',
            'description': 'k',
            'category': 'f',
            'categoryHtml': 'f'
        }
    ];

    beforeEach(module('data-prep.actions-suggestions'));

    it('should filter action list by label', inject(function ($filter) {
        //given
        var actionSearchFilter = $filter('actionsSuggestionsSearchFilter');
        var result = [];

        //when
        result = actionSearchFilter(transformations, '');
        //then
        expect(result.length).toBe(3);
        expect(result[0].label).toBe('Ceil value');
        expect(result[1].label).toBe('Floor value');
        expect(result[2].label).toBe('Round value');

        //when
        result = actionSearchFilter(transformations, 'o');
        //then
        expect(result.length).toBe(2);
        expect(result[0].label).toBe('Floor value');
        expect(result[1].label).toBe('Round value');

        //when
        result = actionSearchFilter(transformations, 'oo');
        //then
        expect(result.length).toBe(1);
        expect(result[0].label).toBe('Floor value');

        //when
        result = actionSearchFilter(transformations, 'ooo');
        //then
        expect(result.length).toBe(0);

    }));

    it('should filter action list by description', inject(function ($filter) {
        //given
        var actionSearchFilter = $filter('actionsSuggestionsSearchFilter');
        var result = [];

        //when
        result = actionSearchFilter(transformations, 's');
        //then
        expect(result.length).toBe(1);
        expect(result[0].label).toBe('Floor value');

    }));

    it('should filter action list by category', inject(function ($filter) {
        //given
        var actionSearchFilter = $filter('actionsSuggestionsSearchFilter');
        var result = [];

        //when
        result = actionSearchFilter(transformations, 'w');
        //then
        expect(result.length).toBe(1);
        expect(result[0].label).toBe('Ceil value');

    }));

    it('should filter action list by multi criteria', inject(function ($filter) {
        //given
        var actionSearchFilter = $filter('actionsSuggestionsSearchFilter');
        var result = [];

        //when
        result = actionSearchFilter(transformations, 'f');
        //then
        expect(result.length).toBe(3);
        expect(result[0].label).toBe('Ceil value');
        expect(result[1].label).toBe('Floor value');
        expect(result[2].label).toBe('Round value');

    }));
});
