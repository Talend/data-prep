/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import {
	HOME_403_ROUTE,
} from '../../index-route';

describe('Home controller', () => {
	let scope;
	let createController;
	let $stateMock;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $componentController, StateService) => {
		scope = $rootScope.$new(true);
		createController = ($stateMock) => $componentController('home', { $scope: scope, $state: $stateMock });

		spyOn(StateService, 'setHomeSidePanelDock').and.returnValue();
	}));

	it('should configure side panel', inject((StorageService, StateService) => {
		// given
		$stateMock = { params: {}, current: { name: '' } };
		const ctrl = createController($stateMock);

		spyOn(StorageService, 'getSidePanelDock').and.returnValue(true);

		// when
		ctrl.$onInit();

		// then
		expect(StateService.setHomeSidePanelDock).toHaveBeenCalledWith(true);
	}));
});
