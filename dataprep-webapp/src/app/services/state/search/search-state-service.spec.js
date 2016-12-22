/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search', function () {
	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('state service', function () {
		it('should toggle', inject(function (searchState, SearchStateService) {
			// when
			SearchStateService.toggle();

			// then
			expect(searchState.searchToggle).toBeFalsy();
		}));

		it('should set searching', inject(function (searchState, SearchStateService) {
			// given
			expect(searchState.isSearching).toBeFalsy();

			// when
			SearchStateService.setSearching(true);

			// then
			expect(searchState.isSearching).toBeTruthy();
		}));

		it('should set input search', inject(function (searchState, SearchStateService) {
			// when
			SearchStateService.setSearchInput('lorem ipsum');

			// then
			expect(searchState.searchInput).toBe('lorem ipsum');
		}));

		it('should set search results', inject(function (searchState, SearchStateService) {
			// when
			SearchStateService.setSearchResults([1, 2, 3]);

			// then
			expect(searchState.searchResults).toEqual([1, 2, 3]);
		}));
	});
});
