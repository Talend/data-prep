/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search', () => {
	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('state service', () => {
		it('should toggle', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.searchToggle).toBeTruthy();

			// when
			SearchStateService.toggle();

			// then
			expect(searchState.searchToggle).toBeFalsy();
		}));

		it('should set searching', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.searching).toBeFalsy();

			// when
			SearchStateService.setSearching(true);

			// then
			expect(searchState.searching).toBeTruthy();
		}));

		it('should set input search', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.searchInput).toBeNull();

			// when
			SearchStateService.setSearchInput('lorem ipsum');

			// then
			expect(searchState.searchInput).toBe('lorem ipsum');
		}));

		it('should set search results', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.searchResults).toBeNull();

			// when
			SearchStateService.setSearchResults([1, 2, 3]);

			// then
			expect(searchState.searchResults).toEqual([1, 2, 3]);
		}));

		it('should set search categories', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.searchCategories).toBeNull();

			// when
			SearchStateService.setSearchCategories([1, 2, 3]);

			// then
			expect(searchState.searchCategories).toEqual([1, 2, 3]);
		}));

		it('should set focused section index', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.focusedSectionIndex).toBeNull();
			// when
			SearchStateService.setFocusedSectionIndex(1);

			// then
			expect(searchState.focusedSectionIndex).toBe(1);
		}));

		it('should set focused item index', inject((searchState, SearchStateService) => {
			// given
			expect(searchState.focusedItemIndex).toBeNull();

			// when
			SearchStateService.setFocusedItemIndex(1);

			// then
			expect(searchState.focusedItemIndex).toBe(1);
		}));
	});
});
