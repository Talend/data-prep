/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Settings actions service', () => {
	beforeEach(angular.mock.module('app.settings'));

	beforeEach(inject((AppHeaderActionsService, MenuActionsService) => {
		spyOn(AppHeaderActionsService, 'dispatch').and.returnValue();
		spyOn(MenuActionsService, 'dispatch').and.returnValue();
	}));

	describe('dispatch', () => {
		it('should dispatch to all action handlers', inject((SettingsActionsService, AppHeaderActionsService, MenuActionsService) => {
			// given
			const action = { type: 'menu:preparation' };

			// when
			SettingsActionsService.dispatch(action);

			// then
			expect(AppHeaderActionsService.dispatch).toHaveBeenCalledWith(action);
			expect(MenuActionsService.dispatch).toHaveBeenCalledWith(action);
		}));
	});
	
	describe('createDispatcher', () => {
		it('should create a function that dispatch', inject((SettingsActionsService, AppHeaderActionsService, MenuActionsService) => {
			// given
			const type = 'menu:preparation';
			const dispatcher = SettingsActionsService.createDispatcher(type);
			const payload = {
				arg1: 'toto',
				arg2: 'tata',
			};
			
			const expectedPayload = {
				type: 'menu:preparation',
				payload: {
					arg1: 'toto',
					arg2: 'tata',
				},
			};

			// when
			dispatcher(payload);

			// then
			expect(AppHeaderActionsService.dispatch).toHaveBeenCalledWith(expectedPayload);
			expect(MenuActionsService.dispatch).toHaveBeenCalledWith(expectedPayload);
		}));
	});
});
