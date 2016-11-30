/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const searchInput = 'barcelona';

const results = [{}, {}];

describe('Inventory Search controller', () => {
	let component;
	let scope;

	beforeEach(angular.mock.module('data-prep.inventory-search'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();
		component = $componentController('inventorySearch', { $scope: scope });
	}));

	describe('search', () => {
		it('should call search service', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(results));

			// when
			component.search(searchInput);
			scope.$digest();

			// then
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
		}));

		it('should set results', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(results));

			// when
			component.search(searchInput);
			scope.$digest();

			// then
			expect(component.results).toEqual(results);
		}));

		it('should NOT set results when they are out of date', inject(($q, SearchService) => {
			// given
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(results));

			// when
			component.search(searchInput);
			component.currentInput = 'other';
			scope.$digest();

			// then
			expect(component.results).not.toEqual(results);
		}));

		it('should set empty array as results when there are no result', inject(($q, SearchService) => {
			// given
			const results = [];
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(results));
			component.results = null;

			// when
			component.search(searchInput);
			scope.$digest();

			// then
			expect(component.results).toEqual(results);
		}));
	});
});
