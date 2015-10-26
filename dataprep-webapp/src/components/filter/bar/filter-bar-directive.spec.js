describe('Filter bar directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(module('data-prep.filter-bar'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'FILTER': 'Search and filter',
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<filter-bar></filter-bar>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render filter title', function() {
        //when
        createElement();

        //then
        expect(element.find('.title').eq(0).text()).toBe('Search and filter: ');
    });

    it('should render filter search', function() {
        //when
        createElement();

        //then
        expect(element.find('filter-search').length).toBe(1);
    });

    it('should render filter list', function() {
        //when
        createElement();

        //then
        expect(element.find('filter-list').length).toBe(1);
    });

    it('should render filter monitor', function() {
        //when
        createElement();

        //then
        expect(element.find('filter-monitor').length).toBe(1);
    });
});