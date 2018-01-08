/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Sidepanel actions service', () => {
	let stateMock;

	beforeEach(angular.mock.module('app.settings.actions', ($provide) => {
		stateMock = { home: { sidePanelDocked: true }};
		$provide.constant('state', stateMock);
	}));

	describe('dispatch', () => {
		beforeEach(inject((StorageService, StateService, SidePanelActionsService) => {
			// given
			const action = {
				"type": "@@sidepanel/TOGGLE",
				"payload": {
					"method": "toggleHomeSidepanel",
					"args": []
				}
			};
			spyOn(StateService, 'toggleHomeSidepanel').and.returnValue();
			spyOn(StorageService, 'setSidePanelDock').and.returnValue();

			// when
			SidePanelActionsService.dispatch(action);
		}));

		it('should toggle sidepanel', inject((StateService) => {
			// then
			expect(StateService.toggleHomeSidepanel).toHaveBeenCalled();
		}));

		it('should save docked state in local storage', inject((StorageService) => {
			// given
			const docked = stateMock.home.sidePanelDocked;

			// then
			expect(StorageService.setSidePanelDock).toHaveBeenCalledWith(docked);
		}));
	});
});
