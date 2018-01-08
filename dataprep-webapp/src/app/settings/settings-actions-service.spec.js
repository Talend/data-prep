/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Settings actions service', () => {
	beforeEach(angular.mock.module('app.settings'));

	beforeEach(inject((SettingsActionsHandlers) => {
		SettingsActionsHandlers.forEach((handler) => {
			spyOn(handler, 'dispatch').and.returnValue();
		});
	}));

	describe('dispatch', () => {
		it('should dispatch to all action handlers', inject((SettingsActionsService, SettingsActionsHandlers) => {
			// given
			const action = { type: 'menu:preparation' };

			// when
			SettingsActionsService.dispatch(action);

			// then
			SettingsActionsHandlers.forEach((handler) => {
				expect(handler.dispatch).toHaveBeenCalledWith(action);
			});
		}));
	});

	describe('createDispatcher', () => {
		it('should create a function that dispatch', inject((SettingsActionsService, SettingsActionsHandlers) => {
			// given
			const action = {
				type: 'menu:preparation',
				payload: {
					arg0: 'titi',
					arg1: 'default toto',
				}
			};
			const dispatcher = SettingsActionsService.createDispatcher(action);
			const payload = {
				arg1: 'toto',
				arg2: 'tata',
			};

			const expectedPayload = {
				type: 'menu:preparation',
				payload: {
					arg0: 'titi',
					arg1: 'toto',
					arg2: 'tata',
				},
				event: null,
			};

			// when
			dispatcher(null, payload);

			// then
			SettingsActionsHandlers.forEach((handler) => {
				expect(handler.dispatch).toHaveBeenCalledWith(expectedPayload);
			});
		}));
	});
});
