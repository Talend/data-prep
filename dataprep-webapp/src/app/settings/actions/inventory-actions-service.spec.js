/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
	});
});
