/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Inventory actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		beforeEach(inject((StateService) => {
			spyOn(StateService, 'setPreparationsDisplayMode').and.returnValue();
			spyOn(StateService, 'enableInventoryEdit').and.returnValue();
			spyOn(StateService, 'disableInventoryEdit').and.returnValue();
		}));

		it('should change inventory list display mode', inject((StateService, InventoryActionsService) => {
			// given
			const action = {
				type: '@@inventory/DISPLAY_MODE',
				payload: {
					method: "setPreparationsDisplayMode",
					args: []
				}
			};

			// when
			InventoryActionsService.dispatch(action);

			// then
			expect(StateService.setPreparationsDisplayMode).toHaveBeenCalledWith(action.payload);
		}));

		it('should enable edit mode', inject((StateService, InventoryActionsService) => {
			// given
			const action = {
				type: '@@inventory/EDIT',
				payload: {
					method: "enableInventoryEdit",
					args: []
				}
			};

			// when
			InventoryActionsService.dispatch(action);

			// then
			expect(StateService.enableInventoryEdit).toHaveBeenCalledWith(action.payload);
		}));

		it('should disable edit mode', inject((StateService, InventoryActionsService) => {
			// given
			const action = {
				type: '@@inventory/CANCEL_EDIT',
				payload: {
					method: "disableInventoryEdit",
					args: []
				}
			};

			// when
			InventoryActionsService.dispatch(action);

			// then
			expect(StateService.disableInventoryEdit).toHaveBeenCalledWith(action.payload);
		}));
	});
});
