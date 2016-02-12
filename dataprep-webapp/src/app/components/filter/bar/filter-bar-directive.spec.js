/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter bar directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.filter-bar'));
    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
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