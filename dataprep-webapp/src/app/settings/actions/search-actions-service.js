/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const PREPARATION = 'preparation';
const DATASET = 'dataset';
const FOLDER = 'folder';
const DOCUMENTATION = 'documentation';

export default class SearchActionsService {

	constructor(SearchService, StateService) {
		'ngInject';

		this.searchService = SearchService;
		this.stateService = StateService;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@search/TOGGLE': {
			this.stateService.toggleSearch();
			break;
		}
		case '@@search/ALL': {
			const searchInput =
				action.payload &&
				action.payload.searchInput;
			if (searchInput) {
				this.stateService.setSearchInput(searchInput);
				this.searchService
					.searchAll(searchInput)
					.then(results => this.stateService.setSearchResults(results));
			}
			break;
		}
		case '@@search/OPEN': {
			switch (action.payload.inventoryType) {
			case PREPARATION:
			case DATASET:
			case FOLDER: {
				break;
			}
			case DOCUMENTATION: {
				const newAction = {
					type: '@@external/OPEN_WINDOW',
					payload: {
						method: 'open',
						args: [
							action.payload.url,
						],
					},
				};
				break;
			}
			}
			break;
		}
		}
	}
}
