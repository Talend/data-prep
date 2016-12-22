/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
				isSearching: false,
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
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();

			// when
			SearchActionsService.dispatch(action);

			// then
			expect(StateService.toggleSearch).toHaveBeenCalled();
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setSearchInput).toHaveBeenCalledWith(null);
			expect(StateService.setSearchResults).toHaveBeenCalledWith(null);
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
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
			expect(StateService.setSearchInput).toHaveBeenCalledWith('');
			expect(SearchService.searchAll).not.toHaveBeenCalled();
			expect(StateService.setSearchResults).not.toHaveBeenCalled();
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

			// when
			SearchActionsService.dispatch(action);
			expect(StateService.setSearching).toHaveBeenCalledWith(true);
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).toHaveBeenCalledWith(['a', 'b', 'c']);
			expect(StateService.setSearching).toHaveBeenCalledWith(false);
		}));

		it('should do not perform search if search input has changed', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
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
		}));
	});
});
