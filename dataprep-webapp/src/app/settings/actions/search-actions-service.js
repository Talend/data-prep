/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class SearchActionsService {

	constructor(state, SearchService, StateService) {
		'ngInject';

		this.state = state;
		this.searchService = SearchService;
		this.stateService = StateService;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@search/TOGGLE': {
			if (this.state.search.searchToggle) {
				this.stateService.setSearching(false);
				this.stateService.setSearchInput(null);
				this.stateService.setSearchResults(null);
			}
			this.stateService.toggleSearch();
			break;
		}
		case '@@search/ALL': {
			const searchInput =
				action.payload &&
				action.payload.searchInput;
			this.stateService.setSearchInput(searchInput);
			if (searchInput) {
				this.stateService.setSearching(true);
				this.searchService
					.searchAll(searchInput)
					.then((results) => {
						if (this.state.search.searchInput === searchInput) {
							this.stateService.setSearchResults(results);
						}
					})
					.finally(() => {
						this.stateService.setSearching(false);
					});
			}
			else {
				this.stateService.setSearching(false);
			}
			break;
		}
		}
	}
}
