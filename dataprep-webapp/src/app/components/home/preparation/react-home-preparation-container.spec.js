/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

describe('Home Preparation Container', () => {
	let scope;
	let createElement;
	let element;
	let StateMock;

	beforeEach(angular.mock.module('data-prep.home', ($provide) => {
		StateMock = {
			inventory: {
				breadcrumb: []
			},
		};
		$provide.constant('state', StateMock);
	}));

	beforeEach(inject(($q, $rootScope, $compile, StateService, FolderService, SettingsService) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<react-home-preparation></react-home-preparation>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};

		spyOn(StateService, 'setFetchingInventoryPreparations').and.returnValue();
		spyOn(FolderService, 'init').and.returnValue($q.when());

		SettingsService.setSettings(settings);
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render breadcrumbs', () => {
		//when
		createElement();

		//then
		expect(element.find('breadcrumbs').length).toBe(1);
	});

	it('should render preparation-list', () => {
		//when
		createElement();

		//then
		expect(element.find('react-preparation-list ').length).toBe(1);
	});
});
