/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

describe('Home Dataset Container', () => {
	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($q, $rootScope, $compile, StateService, DatasetService, SettingsService) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<home-dataset></home-dataset>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};

		spyOn(StateService, 'setFetchingInventoryDatasets').and.returnValue();
		spyOn(DatasetService, 'init').and.returnValue($q.when());

		SettingsService.setSettings(settings);
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render dataset-list', () => {
		//when
		createElement();

		//then
		expect(element.find('inventory-list').length).toBe(1);
	});

	it('should render inputUpdateDataset', () => {
		//when
		createElement();

		//then
		expect(element.find('#inputUpdateDataset').length).toBe(1);
	});

	it('should render display mode dropdown', () => {
		createElement();
		expect(element.find('#datasets-list-display-mode').length).toBe(1);
	});
});
