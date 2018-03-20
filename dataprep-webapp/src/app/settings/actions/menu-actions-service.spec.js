/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

		beforeEach(inject((MenuActionsService) => {
			spyOn(MenuActionsService, 'executeRouterAction');
		}));

		it('should change route', inject((MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE]
				},
				event: { button: 1 },
			};
			// when
			MenuActionsService.dispatch(action);

			// then
			expect(MenuActionsService.executeRouterAction).toHaveBeenCalledWith({ button: 1 }, 'go', [HOME_PREPARATIONS_ROUTE]);
		}));

		it('should change route with folder parameters', inject((MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_FOLDER',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE],
					id: 'acbd'
				},
				event: { button: 1 },
			};

			// when
			MenuActionsService.dispatch(action);

			// then
			expect(MenuActionsService.executeRouterAction).toHaveBeenCalledWith({ button: 1 }, 'go', [HOME_PREPARATIONS_ROUTE], { folderId: 'acbd' });
		}));

		it('should change route with current folder parameters', inject((MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_CURRENT_FOLDER',
				payload: {
					method: 'go',
					args: [HOME_PREPARATIONS_ROUTE],
				},
				event: { button: 1 },
			};

			// when
			MenuActionsService.dispatch(action);

			// then
			expect(MenuActionsService.executeRouterAction).toHaveBeenCalledWith({ button: 1 }, 'go', [HOME_PREPARATIONS_ROUTE], { folderId: 'currentFolderId' });
		}));

		it('should change route with preparation id', inject((MenuActionsService) => {
			// given
			const action = {
				type: '@@router/GO_PREPARATION',
				payload: {
					method: 'go',
					args: [PLAYGROUND_PREPARATION_ROUTE],
					id: 'acbd'
				},
				event: { button: 1 },
			};

			// when
			MenuActionsService.dispatch(action);

			// then
			expect(MenuActionsService.executeRouterAction).toHaveBeenCalledWith({ button: 1 }, 'go', [PLAYGROUND_PREPARATION_ROUTE], { prepid: 'acbd' });
		}));
	});

	describe('executeRouterAction', () => {
		beforeEach(inject(($state, $window) => {
			spyOn($state, 'go');
			spyOn($state, 'href').and.returnValue('absoluteURL');
			spyOn($window, 'open');
		}));

		it('should call $state go', inject(($state, MenuActionsService) => {
			// when
			MenuActionsService.executeRouterAction(undefined, 'go', [PLAYGROUND_PREPARATION_ROUTE], { prepid: 'acbd' });

			// then
			expect($state.go).toHaveBeenCalledWith(PLAYGROUND_PREPARATION_ROUTE, { prepid: 'acbd' });
		}));

		it('should open in new tab on ctrl-click', inject(($window, MenuActionsService) => {
			// when
			MenuActionsService.executeRouterAction({ button: 0, ctrlKey: true }, 'go', [PLAYGROUND_PREPARATION_ROUTE], { prepid: 'acbd' });

			// then
			expect($window.open).toHaveBeenCalledWith('absoluteURL', '_blank');
		}));

		it('should open in new tab on metaKey-click', inject(($window, MenuActionsService) => {
			// when
			MenuActionsService.executeRouterAction({ button: 0, metaKey: true }, 'go', [PLAYGROUND_PREPARATION_ROUTE], { prepid: 'acbd' });

			// then
			expect($window.open).toHaveBeenCalledWith('absoluteURL', '_blank');
		}));

		it('should open in new tab on mousewheel-click', inject(($window, MenuActionsService) => {
			// when
			MenuActionsService.executeRouterAction({ button: 1 }, 'go', [PLAYGROUND_PREPARATION_ROUTE], { prepid: 'acbd' });

			// then
			expect($window.open).toHaveBeenCalledWith('absoluteURL', '_blank');
		}));
	});
});
