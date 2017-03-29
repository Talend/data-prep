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

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	describe('Default Collapsible', () => {
		beforeEach(inject(($rootScope, $compile, SettingsService) => {
			scope = $rootScope.$new();

			createElement = () => {
				element = angular.element('<collapsible-panel item="exportFullRun"></collapsible-panel>');
				$compile(element)(scope);
				scope.$digest();
			};

			SettingsService.setSettings(settings);
		}));

		it('should render adapted header only', () => {
			// given
			scope.exportFullRun = {
				header: [statusItem],
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
			expect(element.find('button').length).toBe(2);
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
			expect(element.find('.panel-heading > div > div').eq(0).text().trim()).toBe(simpleItem.label);
			expect(element.find('.panel-heading > div > div').eq(1).text().trim()).toBe(badgeItem.label);
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

	describe('Collapsible as descriptive panel', () => {
		beforeEach(inject(($rootScope, $compile, SettingsService) => {
			scope = $rootScope.$new();

			createElement = () => {
				element = angular.element('<collapsible-panel item="version"></collapsible-panel>');
				$compile(element)(scope);
				scope.$digest();
			};

			SettingsService.setSettings(settings);
		}));

		const onSelect = jasmine.createSpy('onSelect');
		const onToggle = jasmine.createSpy('onToggle');
		const version = {
			header: [
				{
					label: 'Version 1',
					bsStyle: 'default',
					tooltipPlacement: 'top',
					className: 'title',
				},
				{
					label: '05/02/2017 14:44:55',
					bsStyle: 'default',
					tooltipPlacement: 'top',
					className: 'detail',
				},
			],
			content: {
				head: [
					{
						label: '21 steps',
						bsStyle: 'default',
						tooltipPlacement: 'top',
					}, {
						label: 'by Henry-Mayeul de Benque',
						bsStyle: 'default',
						tooltipPlacement: 'top',
						className: 'text-right',
					},
				],
				description: `Lorem ipsum`
			},
			onSelect: 'version:select',
			onToggle: 'version:toggle',
			theme: 'descriptive-panel',
		};

		it('should render a selectable header', () => {
			// given
			scope.version = version;

			// when
			createElement();

			// then
			expect(element.find('.panel-heading > button').length).toBe(2);
		});

		it('should render header content', () => {
			// given
			scope.version = version;

			// when
			createElement();

			// then
			expect(element.find('.panel-heading > button').eq(0).find('span').eq(0).text().trim()).toBe(version.header[0].label);
			expect(element.find('.panel-heading > button').eq(0).find('span').eq(1).text().trim()).toBe(version.header[1].label);
		});

		it('should render body header', () => {
			// given
			scope.version = version;

			// when
			createElement();

			// then
			expect(element.find('.panel-body > div').eq(0).find('span').eq(0).text().trim()).toBe(version.content.head[0].label);
			expect(element.find('.panel-body > div').eq(0).find('span').eq(1).text().trim()).toBe(version.content.head[1].label);
		});

		it('should render body description', () => {
			// given
			scope.version = version;

			// when
			createElement();

			// then
			expect(element.find('.panel-body .content-description').eq(0).text().trim()).toBe(version.content.description);
		});
	});
});
