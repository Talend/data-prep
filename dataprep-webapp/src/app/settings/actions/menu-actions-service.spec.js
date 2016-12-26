/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import {
	HOME_PREPARATIONS_ROUTE,
	PLAYGROUND_DATASET_ROUTE,
	PLAYGROUND_PREPARATION_ROUTE,
} from '../../index-route';

describe('Menu actions service', () => {
	let stateMock;
	
	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		stateMock = {
			inventory: {
				folder: {
					metadata: { id: 'currentFolderId' }
				}
			}
		};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch', () => {
		it('should change route', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE]
				}
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE);
		}));

		it('should change route with dataset parameters', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_DATASET',
				payload: {
					method: 'go',
					args: [PLAYGROUND_DATASET_ROUTE],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith(PLAYGROUND_DATASET_ROUTE, { datasetid: 'acbd' });
		}));

		it('should change route with folder parameters', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_FOLDER',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, { folderId: 'acbd' });
		}));
		
		it('should change route with current folder parameters', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_CURRENT_FOLDER',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE],
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, { folderId: 'currentFolderId' });
		}));

		it('should change route with preparation id', inject(($state, MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_PREPARATION',
				payload: {
					method: 'go',
					args: [PLAYGROUND_PREPARATION_ROUTE],
					id: 'acbd'
				},
			};
			spyOn($state, 'go').and.returnValue();

			// when
			MenuActionsService.dispatch(action);

			// then
			expect($state.go).toHaveBeenCalledWith(PLAYGROUND_PREPARATION_ROUTE, { prepid: 'acbd' });
		}));
	});
});
