/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter monitor directive', () => {
    'use strict';

    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('data-prep.filter-monitor'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            FILTERS: 'Filters',
            NB_LINES_MATCHING_FILTERS: '{{percentage}}% of lines are matching your filter(s)',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.toogle = () => {
        };

        createElement = () => {
            element = angular.element('<filter-monitor ' +
                'filters="filters" ' +
                'on-toogle="toogle()" ' +
                'nb-lines="nbLines" ' +
                'nb-total-lines="nbTotalLines" ' +
                'percentage="percentage" state="state"></filter-monitor>');
            $compile(element)(scope);
            scope.$digest();
        };

        spyOn(scope, 'toogle');
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render filters label', () => {
            //when
            createElement();

            //then
            expect(element.find('#filter-monitor-left').text().trim()).toBe('Filters');
        });

        it('should NOT render "remove all" icon when filters are empty', () => {
            //given
            scope.filters = [];

            //when
            createElement();

            //then
            expect(element.find('#reset-filters').length).toBe(0);
        });

        it('should render stats', () => {
            //given
            scope.percentage = 25;
            scope.nbLines = 50;
            scope.nbTotalLines = 200;

            //when
            createElement();

            //then
            const statsElement = element.find('#filters-monitor-stats').eq(0);
            expect(statsElement.attr('title')).toBe('25% of lines are matching your filter(s)');
            expect(statsElement.text().trim()).toBe('50/200');
        });
    });

    describe('actions', () => {
        it('should execute reset callback on "toogle" icon click', () => {
            //given
            scope.filters = [{}];
            createElement();

            //when
            const ngModelController = element.find('input').controller('ngModel');
            ngModelController.$setViewValue('true');

            //then
            expect(scope.toogle).toHaveBeenCalled();
        });
    });
});
