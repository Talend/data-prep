describe('Filter monitor directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(module('data-prep.filter-monitor'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'REMOVE_ALL_FILTER': 'Remove all filters',
            'NB_LINES_MATCHING_FILTERS': '{{percentage}}% of lines are matching your filter(s)'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<filter-monitor ' +
                'filters="filters" ' +
                'on-reset="removeAllFilters()" ' +
                'nb-lines="nbLines" ' +
                'nb-total-lines="nbTotalLines" ' +
                'percentage="percentage"></filter-monitor>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    describe('render', function() {
        it('should NOT render "remove all" icon when filters are empty', function() {
            //given
            scope.filters = [];

            //when
            createElement();

            //then
            expect(element.find('#reset-filters').length).toBe(0);
        });

        it('should render "remove all" icon when there are filters', function() {
            //given
            scope.filters = [{}];

            //when
            createElement();

            //then
            expect(element.find('#reset-filters').length).toBe(1);
            expect(element.find('#reset-filters').attr('title')).toBe('Remove all filters');
        });

        it('should render stats', function() {
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
    });

    describe('actions', function() {
        it('should execute reset callback on "remove all" icon click', function() {
            //given
            scope.filters = [{}];
            createElement();
            var ctrl = element.controller('filterMonitor');
            ctrl.onReset = jasmine.createSpy('onReset');

            //when
            element.find('#reset-filters').click();

            //then
            expect(ctrl.onReset).toHaveBeenCalled();
        });
    });
});