describe('stats details directive', function () {
    'use strict';

    var scope, element, createElement;

    beforeEach(module('data-prep.stats-details'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            scope = $rootScope.$new();
            element = angular.element('<stats-details></stats-details>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should set column name in title', inject(function (StatisticsService) {
        //given
        createElement();

        StatisticsService.statistics = {
            common: {
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8
            },
            specific: {
                MIN: 10,
                MAX: 11,
                MEAN: 12,
                VARIANCE: 13
            }
        };
        scope.$apply();

        //when
        var event = angular.element.Event('click');
        element.find('li').eq(1).trigger(event);

        //then
        expect(element.find('.stat-table').length).toBe(2);

        expect(element.find('.stat-table').eq(0).find('tr').eq(0).text().trim().replace(/ /g,'')).toBe('COUNT:\n4');
        expect(element.find('.stat-table').eq(0).find('tr').eq(1).text().trim().replace(/ /g,'')).toBe('DISTINCT_COUNT:\n5');
        expect(element.find('.stat-table').eq(0).find('tr').eq(2).text().trim().replace(/ /g,'')).toBe('DUPLICATE_COUNT:\n6');
        expect(element.find('.stat-table').eq(0).find('tr').eq(3).text().trim().replace(/ /g,'')).toBe('VALID:\n9');
        expect(element.find('.stat-table').eq(0).find('tr').eq(4).text().trim().replace(/ /g,'')).toBe('EMPTY:\n7');
        expect(element.find('.stat-table').eq(0).find('tr').eq(5).text().trim().replace(/ /g,'')).toBe('INVALID:\n8');

        expect(element.find('.stat-table').eq(1).find('tr').eq(0).text().trim().replace(/ /g,'')).toBe('MIN:\n10');
        expect(element.find('.stat-table').eq(1).find('tr').eq(1).text().trim().replace(/ /g,'')).toBe('MAX:\n11');
        expect(element.find('.stat-table').eq(1).find('tr').eq(2).text().trim().replace(/ /g,'')).toBe('MEAN:\n12');
        expect(element.find('.stat-table').eq(1).find('tr').eq(3).text().trim().replace(/ /g,'')).toBe('VARIANCE:\n13');
    }));
});
