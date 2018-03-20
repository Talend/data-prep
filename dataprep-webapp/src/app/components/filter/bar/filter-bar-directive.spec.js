/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter bar directive', () => {
    'use strict';

    let scope;
    let createElement;
    let element;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.filter-bar', ($provide) => {
        stateMock = { playground: {
                filter: {
                    gridFilters: [{}],
                },
            }, };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            REMOVE_ALL_FILTERS: 'Remove all filters',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<filter-bar></filter-bar>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render "remove all" icon when there are filters', () => {
        //when
        createElement();

        //then
        expect(element.find('#reset-filters').length).toBe(1);
        expect(element.find('#reset-filters').attr('title')).toBe('Remove all filters');
    });

    it('should NOT render "remove all" icon when there are not filters', () => {
        //when
        stateMock.playground.filter.gridFilters = [];
        createElement();

        //then
        expect(element.find('#reset-filters').length).toBe(0);
    });

    it('should execute reset callback on "remove all" icon click', inject((FilterManagerService) => {
        //given
        createElement();

        spyOn(FilterManagerService, 'removeAllFilters').and.returnValue();

        //when
        element.find('#reset-filters').click();

        //then
        expect(FilterManagerService.removeAllFilters).toHaveBeenCalled();
    }));

    it('should render filter search', () => {
        //when
        createElement();

        //then
        expect(element.find('filter-search').length).toBe(1);
    });

    it('should render filter list', () => {
        //when
        createElement();

        //then
        expect(element.find('filter-list').length).toBe(1);
    });

    it('should render filter monitor', () => {
        //when
        createElement();

        //then
        expect(element.find('filter-monitor').length).toBe(1);
    });
});
