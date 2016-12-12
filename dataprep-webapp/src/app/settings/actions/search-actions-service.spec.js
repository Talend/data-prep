/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Search actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should open preparation by redirecting to MenuActionsService', inject(($state, SearchActionsService, MenuActionsService) => {
			// given
			const action = {
				type: '@@search/OPEN',
				payload: {
					method: 'dispatch',
					args: [],
					id: 'acbd',
					inventoryType: 'preparation',
				},
			};
			const menuAction = {
				type: '@@router/GO_PREPARATION',
				payload: {
					method: 'go',
					args: ['playground.preparation'],
					id: 'acbd',
				},
			};
			spyOn(MenuActionsService, 'dispatch').and.returnValue();

			// when
			SearchActionsService.dispatch(action);

			// then
			expect(MenuActionsService.dispatch).toHaveBeenCalledWith(menuAction);
		}));
	});
});
