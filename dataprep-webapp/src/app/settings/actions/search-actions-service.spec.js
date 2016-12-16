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
				searchInput,
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch', () => {
		it('should toggle search input', inject((StateService, SearchActionsService) => {
			// given
			const action = {
				type: '@@search/TOGGLE',
				payload: {
					method: 'open',
					args: [],
					url: 'http://www.google.fr',
				},
			};
			spyOn(StateService, 'toggleSearch').and.returnValue();

			// when
			SearchActionsService.dispatch(action);

			// then
			expect(StateService.toggleSearch).toHaveBeenCalled();
		}));

		it('should do nothing if search input is empty', inject(($q, $rootScope, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput: '',
				},
			};
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue();
			spyOn(StateService, 'setSearchResults').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(StateService.setSearchInput).toHaveBeenCalledWith('');
			expect(SearchService.searchAll).not.toHaveBeenCalled();
			expect(StateService.setSearchResults).not.toHaveBeenCalled();
		}));

		it('should search everywhere if search input is not empty', inject(($q, $rootScope, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput,
				},
			};
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(['a', 'b', 'c']));
			spyOn(StateService, 'setSearchResults').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			$rootScope.$digest();

			// then
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).toHaveBeenCalledWith(['a', 'b', 'c']);
		}));

		it('should do not perform search if search input has changed', inject(($q, $rootScope, state, StateService, SearchActionsService, SearchService) => {
			// given
			const action = {
				type: '@@search/ALL',
				payload: {
					searchInput,
				},
			};
			spyOn(StateService, 'setSearchInput').and.returnValue();
			spyOn(SearchService, 'searchAll').and.returnValue($q.when(['a', 'b', 'c']));
			spyOn(StateService, 'setSearchResults').and.returnValue();

			// when
			SearchActionsService.dispatch(action);
			stateMock.search.searchInput = 'lorem ipsum dolor';
			$rootScope.$digest();

			// then
			expect(StateService.setSearchInput).toHaveBeenCalledWith(searchInput);
			expect(SearchService.searchAll).toHaveBeenCalledWith(searchInput);
			expect(StateService.setSearchResults).not.toHaveBeenCalled();
		}));
	});
});
