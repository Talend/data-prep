/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Datasets filters component', () => {
    let scope;
    let createElement;
    let element;
    let controller;

    beforeEach(angular.mock.module('data-prep.datasets-filters'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            RECENT_DATASETS: 'Recent Datasets',
            RECENT_DATASETS_DESCRIPTION: '10 Last modified datasets',
            FAVORITE_DATASETS: 'Favorite Datasets',
            FAVORITE_DATASETS_DESCRIPTION: '',
            ALL_DATASETS: 'All Datasets',
            ALL_DATASETS_DESCRIPTION: '',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element(`<datasets-filters on-select="loadDatasets(filter)"></datasets-filters>`);

            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('datasetsFilters');
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render filters list parts', () => {
            //when
            createElement();

            //then
            expect(element.find('.dataset-filter').length).toBe(3);
            expect(element.find('.dataset-filter-icon').length).toBe(3);
            expect(element.find('.dataset-filter-text').length).toBe(3);
            expect(element.find('.dataset-filter-title').length).toBe(3);
            expect(element.find('.dataset-filter-description').length).toBe(3);
        });

        it('should select 1st filter by default', () => {
            //when
            createElement();

            //then
            expect(element.find('.dataset-filter').eq(0).hasClass('selected-filter')).toBe(true);
            expect(element.find('.dataset-filter').eq(1).hasClass('selected-filter')).toBe(false);
            expect(element.find('.dataset-filter').eq(2).hasClass('selected-filter')).toBe(false);
            expect(element.find('.dataset-filter').eq(3).hasClass('selected-filter')).toBe(false);
        });

        it('should render filters list icons', () => {
            //when
            createElement();

            //then
            expect(element.find('.dataset-filter-icon').eq(0).find('img').attr('src')).toBe('/assets/images/inventory/recent-datasets.png');
            expect(element.find('.dataset-filter-icon').eq(1).find('.favorite').eq(0).attr('data-icon')).toBe('f');
            expect(element.find('.dataset-filter-icon').eq(2).find('img').attr('src')).toBe('/assets/images/inventory/all-datasets.png');
        });

        it('should render filters titles', () => {
            //when
            createElement();

            //then
            expect(element.find('.dataset-filter-title').eq(0).text().trim()).toBe('Recent Datasets');
            expect(element.find('.dataset-filter-title').eq(1).text().trim()).toBe('Favorite Datasets');
            expect(element.find('.dataset-filter-title').eq(2).text().trim()).toBe('All Datasets');
        });

        it('should render filters descriptions', () => {
            //when
            createElement();

            //then
            expect(element.find('.dataset-filter-description').eq(0).text().trim()).toBe('10 Last modified datasets');
            expect(element.find('.dataset-filter-description').eq(1).text().trim()).toBe('');
            expect(element.find('.dataset-filter-description').eq(2).text().trim()).toBe('');
        });
    });

    describe('events', () => {
        it('should call onSelect function on click', () => {
            //given
            createElement();
            controller.onSelect = jasmine.createSpy('onSelect');

            //when
            element.find('.dataset-filter').eq(1).click();
            scope.$digest();

            //then
            expect(controller.onSelect).toHaveBeenCalledWith({ filter: 'FAVORITE_DATASETS' });
        });

        it('should update the filter background on click', () => {
            //given
            createElement();
            controller.onSelect = jasmine.createSpy('onSelect');

            //when
            element.find('.dataset-filter').eq(1).click();
            scope.$digest();

            //then
            expect(element.find('.dataset-filter').eq(0).hasClass('selected-filter')).toBe(false);
            expect(element.find('.dataset-filter').eq(1).hasClass('selected-filter')).toBe(true);
        });
    });
});
