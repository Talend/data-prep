/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter bar directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    var stateMock;
    beforeEach(angular.mock.module('data-prep.filter-bar', function ($provide) {
        stateMock = { playground: {
                filter: {
                    gridFilters: [{}],
                },
            }, };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            REMOVE_ALL_FILTERS: 'Remove all filters',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile, $templateCache) {
        scope = $rootScope.$new();
        $templateCache.put('assets/images/filter/delete.svg', '<svg></svg>');

        createElement = function () {
            element = angular.element('<filter-bar></filter-bar>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render "remove all" icon when there are filters', function () {
        //when
        createElement();

        //then
        expect(element.find('#reset-filters').length).toBe(1);
        expect(element.find('#reset-filters').attr('title')).toBe('Remove all filters');
    });

    it('should NOT render "remove all" icon when there are not filters', function () {
        //when
        stateMock.playground.filter.gridFilters = [];
        createElement();

        //then
        expect(element.find('#reset-filters').length).toBe(0);
    });

    it('should execute reset callback on "remove all" icon click', function () {
        //given
        createElement();

        var ctrl = element.controller('filterBar');
        ctrl.filterService.removeAllFilters = jasmine.createSpy('removeAllFilters');

        //when
        element.find('#reset-filters').click();

        //then
        expect(ctrl.filterService.removeAllFilters).toHaveBeenCalled();
    });

    it('should render filter search', function () {
        //when
        createElement();

        //then
        expect(element.find('filter-search').length).toBe(1);
    });

    it('should render filter list', function () {
        //when
        createElement();

        //then
        expect(element.find('filter-list').length).toBe(1);
    });

    it('should render filter monitor', function () {
        //when
        createElement();

        //then
        expect(element.find('filter-monitor').length).toBe(1);
    });
});
