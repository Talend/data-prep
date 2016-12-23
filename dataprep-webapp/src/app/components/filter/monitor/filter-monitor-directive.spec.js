/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter monitor directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    beforeEach(angular.mock.module('data-prep.filter-monitor'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            FILTERS: 'Filters',
            NB_LINES_MATCHING_FILTERS: '{{percentage}}% of lines are matching your filter(s)',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.toogle = () => {
        };

        createElement = function () {
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

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('render', function () {
        it('should render filters label', function () {
            //when
            createElement();

            //then
            expect(element.find('#filter-monitor-left').text().trim()).toBe('Filters');
        });

        it('should NOT render "remove all" icon when filters are empty', function () {
            //given
            scope.filters = [];

            //when
            createElement();

            //then
            expect(element.find('#reset-filters').length).toBe(0);
        });

        it('should render stats', function () {
            //given
            scope.percentage = 25;
            scope.nbLines = 50;
            scope.nbTotalLines = 200;

            //when
            createElement();

            //then
            var statsElement = element.find('#filters-monitor-stats').eq(0);
            expect(statsElement.attr('title')).toBe('25% of lines are matching your filter(s)');
            expect(statsElement.text().trim()).toBe('50/200');
        });

        it('should render filter search', () => {
            //when
            createElement();

            //then
            expect(element.find('filter-search').length).toBe(1);
        });
    });

    describe('actions', function () {
        it('should execute reset callback on "toogle" icon click', function () {
            //given
            scope.filters = [{}];
            createElement();

            //when
            var ngModelController = element.find('input').controller('ngModel');
            ngModelController.$setViewValue('true');

            //then
            expect(scope.toogle).toHaveBeenCalled();
        });
    });
});
