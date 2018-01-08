/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

const searchInput = 'lorem ipsum';

describe('Search actions service', () => {
	let stateMock;

	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		stateMock = {
			search: {
				searchToggle: true,
				searching: false,
				searchInput,
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch', () => {
		it('should toggle search input', inject((state, StateService, SearchActionsService) => {
			// given
			const action = {
				type: '@@search/TOGGLE',
				payload: {
				},
			};

			spyOn(StateService, 'toggleSearch').and.returnValue();
			spyOn(StateService, 'setSearching').and.returnValue();
			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);

			// then
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(null);
			expect(StateService.toggleSearch).toHaveBeenCalled();
		}));

		it('should change focused section and item indexes', inject((state, StateService, SearchActionsService) => {
			// given
			state.search.searchResults = [];

			const action = {
				type: '@@search/FOCUS',
				payload: {
					focusedSectionIndex: 0,
					focusedItemIndex: 0,
				},
			};

			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);

			// then
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(0);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(0);
		}));

		it('should do nothing if search input is empty', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput: '',
				},
			};
			spyOn(StateService, 'setSearching').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue();
			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(SearchService.searchAll).not.toHaveBeenCalled();

			expect(StateService.setSearchResults).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(null);
		}));

		it('should search everywhere if search input is not empty', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput,
				},
			};
			spyOn(StateService, 'setSearching').and.returnValue();
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(['a', 'b', 'c']));
			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			expect(StateService.setSearching).toHaveBeenCalledWith(true);
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).toHaveBeenCalledWith(['a', 'b', 'c']);
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(null);
		}));

		it('should not set search result if search input is out of date', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput,
				},
			};
			spyOn(StateService, 'setSearching').and.returnValue();
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(['a', 'b', 'c']));
			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			stateMock.search.searchInput = 'lorem ipsum dolor';
			expect(StateService.setSearching).toHaveBeenCalledWith(true);
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).not.toHaveBeenCalled();
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(null);
		}));


		it('should search doc if search input is not empty', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/DOC',
				payload: {
					searchInput,
				},
			};
			spyOn(StateService, 'setSearching').and.returnValue();
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();
			spyOn(SearchService, 'searchDocumentation').and.returnValue($q.when(['a', 'b', 'c']));
			spyOn(StateService, 'setFocusedSectionIndex').and.returnValue();
			spyOn(StateService, 'setFocusedItemIndex').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			expect(StateService.setSearching).toHaveBeenCalledWith(true);
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchService.searchDocumentation).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).toHaveBeenCalledWith(['a', 'b', 'c']);
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setFocusedSectionIndex).toHaveBeenCalledWith(null);
			expect(StateService.setFocusedItemIndex).toHaveBeenCalledWith(null);
		}));
	});
});
