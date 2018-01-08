/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import i18n from '../../../../i18n/en.json';

beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
	$translateProvider.translations('en', i18n);
	$translateProvider.preferredLanguage('en');
}));

describe('stats details directive', function () {
    'use strict';

    var stateMock;
    var scope;
    var element;
    var createElement;

    beforeEach(angular.mock.module('data-prep.stats-details', function ($provide) {
        stateMock = {
            playground: {
                statistics: {},
                grid: {
                    selectedColumns: [{}]
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

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

    it('should render stats', function () {
        //given
        createElement();

        stateMock.playground.statistics.details = {
            common: {
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8,
            },
            specific: {
                MIN: 10,
                MAX: 11,
                MEAN: 12,
                VARIANCE: 13,
            },
        };
        scope.$apply();

        //when
        var event = angular.element.Event('click');
        element.find('li').eq(1).trigger(event);

        //then
        expect(element.find('.stat-table').length).toBe(2);

        expect(element.find('.stat-table').eq(0).find('tr').eq(0).text().trim().replace(/ /g, '')).toBe('Count:\n4');
        expect(element.find('.stat-table').eq(0).find('tr').eq(1).text().trim().replace(/ /g, '')).toBe('Distinct:\n5');
        expect(element.find('.stat-table').eq(0).find('tr').eq(2).text().trim().replace(/ /g, '')).toBe('Duplicate:\n6');
        expect(element.find('.stat-table').eq(0).find('tr').eq(3).text().trim().replace(/ /g, '')).toBe('Valid:\n9');
        expect(element.find('.stat-table').eq(0).find('tr').eq(4).text().trim().replace(/ /g, '')).toBe('Empty:\n7');
        expect(element.find('.stat-table').eq(0).find('tr').eq(5).text().trim().replace(/ /g, '')).toBe('Invalid:\n8');

        expect(element.find('.stat-table').eq(1).find('tr').eq(0).text().trim().replace(/ /g, '')).toBe('MIN:\n10');
        expect(element.find('.stat-table').eq(1).find('tr').eq(1).text().trim().replace(/ /g, '')).toBe('MAX:\n11');
        expect(element.find('.stat-table').eq(1).find('tr').eq(2).text().trim().replace(/ /g, '')).toBe('Mean:\n12');
        expect(element.find('.stat-table').eq(1).find('tr').eq(3).text().trim().replace(/ /g, '')).toBe('Variance:\n13');
    });
});
