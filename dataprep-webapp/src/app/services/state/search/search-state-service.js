/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const searchState = {
	searchToggle: true,
	searching: false,
	searchInput: null,
	searchResults: null,
	searchCategories: null,
	focusedSectionIndex: null,
	focusedItemIndex: null,
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:SearchStateService
 * @description Manage the state of the feedback
 */
export function SearchStateService() {
	return {
		toggle,
		setSearching,
		setSearchInput,
		setSearchResults,
		setSearchCategories,
		setFocusedSectionIndex,
		setFocusedItemIndex,
	};

	/**
	 * @ngdoc method
	 * @name toggle
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Toggle search input
	 */
	function toggle() {
		searchState.searchToggle = !searchState.searchToggle;
	}

	/**
	 * @ngdoc method
	 * @name setSearchInput
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Set search input
	 */
	function setSearchInput(searchInput) {
		searchState.searchInput = searchInput;
	}

	/**
	 * @ngdoc method
	 * @name setSearching
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Indicate if search is performing
	 */
	function setSearching(searching) {
		searchState.searching = searching;
	}

	/**
	 * @ngdoc method
	 * @name setSearchResults
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Set the search results
	 */
	function setSearchResults(searchResults) {
		searchState.searchResults = searchResults;
	}

	/**
	 * @ngdoc method
	 * @name setSearchCategories
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Set the search result categories
	 */
	function setSearchCategories(searchCategories) {
		searchState.searchCategories = searchCategories;
	}

	/**
	 * @ngdoc method
	 * @name setFocusedSectionIndex
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Set the focused section index
	 */
	function setFocusedSectionIndex(focusedSectionIndex) {
		searchState.focusedSectionIndex = focusedSectionIndex;
	}

	/**
	 * @ngdoc method
	 * @name setFocusedItemIndex
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Set the focused item index
	 */
	function setFocusedItemIndex(focusedItemIndex) {
		searchState.focusedItemIndex = focusedItemIndex;
	}
}
