/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

const filters = [
	{
		id: 'RECENT',
		imageUrl: '/assets/images/inventory/recent-datasets.png',
		title: 'RECENT_DATASETS',
		description: 'RECENT_DATASETS_DESCRIPTION',
	},
	{
		id: 'FAVORITE',
		icon: 'f',
		title: 'FAVORITE_DATASETS',
		description: 'FAVORITE_DATASETS_DESCRIPTION',
	},
	{
		id: 'ALL',
		imageUrl: '/assets/images/inventory/all-datasets.png',
		title: 'ALL_DATASETS',
		description: 'ALL_DATASETS_DESCRIPTION',
	}
];

describe('Theme filters component', () => {
	let scope;
	let createElement;
	let element;
	let controller;

	beforeEach(angular.mock.module('data-prep.preparation-creator'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			'RECENT_DATASETS': 'Recent Datasets',
			'RECENT_DATASETS_DESCRIPTION': '10 Last modified datasets',
			'FAVORITE_DATASETS': 'Favorite Datasets',
			'FAVORITE_DATASETS_DESCRIPTION': '',
			'ALL_DATASETS': 'All Datasets',
			'ALL_DATASETS_DESCRIPTION': '',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		scope.filters = filters;
		scope.onSelectCB = jasmine.createSpy('onSelectCB');

		createElement = () => {
			element = angular.element(
				`<theme-filters filters="filters"
                                selected-filter="selectedFilter"
                                on-select="onSelectCB(filter)"
                                disable-selection="disableSelection"></theme-filters>`
			);

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
		it('should render filters list', () => {
			//when
			createElement();

			//then
			expect(element.find('.theme-filter').length).toBe(3);
			expect(element.find('.theme-filter-icon').length).toBe(3);
			expect(element.find('.theme-filter-text').length).toBe(3);
			expect(element.find('.theme-filter-title').length).toBe(3);
			expect(element.find('.theme-filter-description').length).toBe(3);
		});

		it('should render filters list icons', () => {
			//when
			createElement();

			//then
			expect(element.find('.theme-filter-icon').eq(0).find('img').attr('src')).toBe('/assets/images/inventory/recent-datasets.png');
			expect(element.find('.theme-filter-icon').eq(1).find('>div').eq(0).attr('data-icon')).toBe('f');
			expect(element.find('.theme-filter-icon').eq(2).find('img').attr('src')).toBe('/assets/images/inventory/all-datasets.png');
		});

		it('should render filters titles', () => {
			//when
			createElement();

			//then
			expect(element.find('.theme-filter-title').eq(0).text().trim()).toBe('Recent Datasets');
			expect(element.find('.theme-filter-title').eq(1).text().trim()).toBe('Favorite Datasets');
			expect(element.find('.theme-filter-title').eq(2).text().trim()).toBe('All Datasets');
		});

		it('should render filters descriptions', () => {
			//when
			createElement();

			//then
			expect(element.find('.theme-filter-description').eq(0).text().trim()).toBe('10 Last modified datasets');
			expect(element.find('.theme-filter-description').eq(1).text().trim()).toBe('');
			expect(element.find('.theme-filter-description').eq(2).text().trim()).toBe('');
		});

		it('should render selected filter', () => {
			//given
			scope.selectedFilter = filters[1];

			//when
			createElement();

			//then
			expect(element.find('.theme-filter').eq(0).hasClass('selected')).toBe(false);
			expect(element.find('.theme-filter').eq(1).hasClass('selected')).toBe(true);
			expect(element.find('.theme-filter').eq(2).hasClass('selected')).toBe(false);
		});
	});

	describe('events', () => {
		it('should call onSelect callback on click', () => {
			//given
			createElement();
			expect(scope.onSelectCB).not.toHaveBeenCalled();

			//when
			element.find('.theme-filter').eq(1).click();
			scope.$digest();

			//then
			expect(scope.onSelectCB).toHaveBeenCalledWith(filters[1]);
		});

		it('should NOT call onSelect callback when selection is disabled', () => {
			//given
			scope.disableSelection = true;
			createElement();
			expect(scope.onSelectCB).not.toHaveBeenCalled();

			//when
			element.find('.theme-filter').eq(1).click();
			scope.$digest();

			//then
			expect(scope.onSelectCB).not.toHaveBeenCalled();
		});
	});
});
