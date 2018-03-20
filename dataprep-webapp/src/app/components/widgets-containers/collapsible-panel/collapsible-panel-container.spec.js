/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

	beforeEach(angular.mock.module('@talend/react-components.containers'));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	describe('Collapsible rendering', () => {
		beforeEach(inject(($rootScope, $compile, SettingsService) => {
			scope = $rootScope.$new();

			createElement = () => {
				element = angular.element('<collapsible-panel item="exportFullRun"></collapsible-panel>');
				$compile(element)(scope);
				scope.$digest();
			};

			SettingsService.setSettings(settings);
		}));

		describe('Collapsible header', () => {
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
					content: [],
				};

				// when
				createElement();

				// then
				expect(element.find('.panel-heading').length).toBe(1);
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
				const panelHeader = element.find('.panel-heading > div > div');
				expect(panelHeader.eq(0).text().trim()).toBe(simpleItem.label);
				expect(panelHeader.eq(1).text().trim()).toBe(badgeItem.label);
				expect(panelHeader.eq(1).find('span').hasClass('label')).toBe(true);
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
				const panelHeader = element.find('.panel-heading > div');
				expect(panelHeader.length).toBe(1);
				expect(panelHeader.eq(0).find('span').eq(0).text().trim()).toBe(simpleItem.label);
				expect(panelHeader.eq(0).find('span').eq(1).text().trim()).toBe(badgeItem.label);
			});
		});

		describe('Default Collapsible content', () => {
			it('should render content', () => {
				// given
				scope.exportFullRun = {
					header: [statusItem],
					content: [content],
				};

				// when
				createElement();

				// then
				expect(element.find('.panel-body').length).toBe(1);
				expect(element.find('.panel-body').eq(0).text().trim()).toBe(`${content.label}${content.description}`);
			});
		});

		describe('Collapsible with descriptive content', () => {
			beforeEach(inject(($rootScope, $compile, SettingsService) => {
				scope = $rootScope.$new();

				createElement = () => {
					element = angular.element('<collapsible-panel item="version"></collapsible-panel>');
					$compile(element)(scope);
					scope.$digest();
				};

				SettingsService.setSettings(settings);
			}));

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
				theme: 'descriptive-panel',
			};

			it('should render body header', () => {
				// given
				scope.version = version;

				// when
				createElement();

				// then
				const panelHeader = element.find('.panel-body > div');
				expect(panelHeader.eq(0).find('span').eq(0).text().trim()).toBe(version.content.head[0].label);
				expect(panelHeader.eq(0).find('span').eq(1).text().trim()).toBe(version.content.head[1].label);
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

	describe('Collapsible events', () => {
		beforeEach(inject((SettingsActionsService, SettingsService) => {
			SettingsService.setSettings(settings);
			spyOn(SettingsActionsService, 'dispatch').and.returnValue();
		}));

		beforeEach(inject(($rootScope, $compile, SettingsService) => {
			scope = $rootScope.$new();

			createElement = () => {
				element = angular.element('<collapsible-panel item="version"></collapsible-panel>');
				angular.element('body').append(element);
				$compile(element)(scope);
				scope.$digest();
			};
			SettingsService.setSettings(settings);
		}));

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

		it('should render a collapsible descriptive panel header buttons', () => {
			// given
			scope.version = version;

			// when
			createElement();

			// then
			expect(element.find('.panel-heading > button').length).toBe(2);
		});

		it('should trigger onSelect function', inject((SettingsActionsService) => {
			// given
			scope.version = version;
			createElement();

			// when
			element.find('.panel-heading > button').eq(0).click();
			scope.$digest();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@router/GO_PREPARATION_READ_ONLY');
		}));

		it('should trigger onToggle function', inject((SettingsActionsService) => {
			// given
			scope.version = version;
			createElement();

			// when
			element.find('.panel-heading > button').eq(1).click();
			scope.$digest();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@version/VERSION_TOGGLE');
		}));
	});
});
