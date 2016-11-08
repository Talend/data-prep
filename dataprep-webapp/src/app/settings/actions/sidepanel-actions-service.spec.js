/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Sidepanel actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should toggle sidepanel', inject((StateService, SidePanelActionsService) => {
			// given
			const action = {
				"type": "@@sidepanel/TOGGLE",
				"payload": {
					"method": "toggleHomeSidepanel",
					"args": []
				}
			};
			spyOn(StateService, 'toggleHomeSidepanel').and.returnValue();

			// when
			SidePanelActionsService.dispatch(action);

			// then
			expect(StateService.toggleHomeSidepanel).toHaveBeenCalled();
		}));
	});
});
