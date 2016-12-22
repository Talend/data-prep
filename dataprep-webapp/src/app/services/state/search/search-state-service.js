/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const searchState = {
	searchToggle: true,
	isSearching: false,
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
	};

	/**
	 * @ngdoc method
	 * @name setSearchInput
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Display the feedback
	 */
	function toggle() {
		searchState.searchToggle = !searchState.searchToggle;
	}

	/**
	 * @ngdoc method
	 * @name setSearchInput
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Display the feedback
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
	function setSearching(isSearching) {
		searchState.isSearching = isSearching;
	}

	/**
	 * @ngdoc method
	 * @name setSearchResults
	 * @methodOf data-prep.services.state.service:SearchStateService
	 * @description Hide the feedback
	 */
	function setSearchResults(results) {
		searchState.searchResults = results;
	}
}
