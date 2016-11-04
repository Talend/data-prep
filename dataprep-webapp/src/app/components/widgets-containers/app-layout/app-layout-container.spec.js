/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import angular from 'angular';
import settings from '../../../../mocks/Settings.mock';


describe('App Layout Container', () => {
	'use strict';

	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('react-talend-components.containers'));

	beforeEach(inject(($rootScope, $compile, SettingsService) => {
		scope = $rootScope.$new();
		createElement = () => {
			element = angular.element('<app-layout></app-layout>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};

		SettingsService.setSettings(settings);
	}));

	afterEach(inject((SettingsService) => {
		SettingsService.clearSettings();
		scope.$destroy();
		element.remove();
	}));

	it('should render app layout', inject(() => {
		//when
		createElement();

		//then
		expect(element.find('.app').length).toBe(1);
		expect(element.find('.header app-header-bar').length).toBe(1);
		expect(element.find('.content .sidemenu side-panel').length).toBe(1);
		expect(element.find('.content .main').length).toBe(1);
	}));
});
