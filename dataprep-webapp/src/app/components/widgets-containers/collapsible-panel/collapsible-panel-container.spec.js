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

const statusItem = {
	displayMode: 'status',
	status: 'inProgress',
	label: 'inProgress',
	icon: 'fa fa-check',
	actions: [],
};

const statusItemWithActions = {
	displayMode: 'status',
	status: 'inProgress',
	label: 'in progress',
	icon: 'fa fa-check',
	actions: ['user:logout', 'modal:feedback'],
};

const actionItem = {
	displayMode: 'action',
	action: 'user:logout',
};

const simpleItem = {
	label: 'by Charles',
	bsStyle: 'default',
	tooltipPlacement: 'top',
};

const badgeItem = {
	displayMode: 'badge',
	label: 'XML',
	bsStyle: 'default',
	tooltipPlacement: 'top',
};

const content = {
	label: 'Content',
	description: 'Description3',
};

describe('CollapsiblePanel container', () => {
	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('react-talend-components.containers'));

	beforeEach(inject(($rootScope, $compile, SettingsService) => {
		scope = $rootScope.$new();

		createElement = () => {
			element = angular.element('<collapsible-panel item="exportFullRun"></collapsible-panel>');
			$compile(element)(scope);
			scope.$digest();
		};

		SettingsService.setSettings(settings);
	}));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	it('should render adapted header only', () => {
		// given
		scope.exportFullRun = {
			header: [statusItem],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('.panel-heading').length).toBe(1);
		expect(element.find('.panel-body').length).toBe(0);
	});

	it('should render adapted header with content', () => {
		// given
		scope.exportFullRun = {
			header: [statusItem],
			content: [content],
		};

		// when
		createElement();

		// then
		expect(element.find('.panel-heading').length).toBe(1);
		expect(element.find('.panel-body').length).toBe(1);
		expect(element.find('.panel-body').eq(0).text().trim()).toBe(`${content.label}${content.description}`);
	});

	it('should render adapted status header', () => {
		// given
		scope.exportFullRun = {
			header: [statusItem],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('.tc-status').length).toBe(1);
		expect(element.find('.tc-status-label').eq(0).text().trim()).toBe(statusItem.label);
		expect(element.find('.tc-status button').length).toBe(0);
	});

	it('should render adapted status with actions', () => {
		// given
		scope.exportFullRun = {
			header: [statusItemWithActions],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('.tc-status').length).toBe(1);
		expect(element.find('.tc-status-label').eq(0).text().trim()).toBe(statusItemWithActions.label);
		expect(element.find('.tc-status button').length).toBe(2);
	});

	it('should render adapted action item', () => {
		// given
		scope.exportFullRun = {
			header: [actionItem],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('button').length).toBe(1);
	});

	it('should render simple and badge text', () => {
		// given
		scope.exportFullRun = {
			header: [simpleItem, badgeItem],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('.panel-heading > div').eq(0).text().trim()).toBe(simpleItem.label);
		expect(element.find('.panel-heading > div').eq(1).text().trim()).toBe(badgeItem.label);
	});

	it('should render simple and badge text in the same group', () => {
		// given
		scope.exportFullRun = {
			header: [[simpleItem, badgeItem]],
			content: [],
		};

		// when
		createElement();

		// then
		expect(element.find('.panel-heading > div').length).toBe(1);

		expect(element.find('.panel-heading > div').eq(0).find('span').eq(0).text().trim()).toBe(simpleItem.label);
		expect(element.find('.panel-heading > div').eq(0).find('span').eq(1).text().trim()).toBe(badgeItem.label);
	});
});
