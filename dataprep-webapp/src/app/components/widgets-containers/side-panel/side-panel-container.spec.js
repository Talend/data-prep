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

describe('Side Panel container', () => {
	let scope;
	let createElement;
	let element;
	const body = angular.element('body');

	beforeEach(angular.mock.module('react-talend-components.containers'));

	beforeEach(inject(($rootScope, $compile, SettingsService) => {
		scope = $rootScope.$new();

		createElement = () => {
			element = angular.element('<side-panel id="\'side-panel\'" active="active"><side-panel/>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};

		SettingsService.setSettings(settings);
	}));

	afterEach(inject((SettingsService) => {
		SettingsService.clearSettings();
		scope.$destroy();
		element.remove();
	}));

	describe('render', () => {
		it('should create toggle button', () => {
			// when
			createElement();

			// then
			const toggleButton = element.find('#side-panel-toggle-dock');
			expect(toggleButton.attr('title')).toBe('Toggle side panel');
		});

		it('should create preparations button', () => {
			// when
			createElement();

			// then
			const preparationsButton = element.find('#side-panel-nav-preparations');
			expect(preparationsButton.attr('title')).toBe('Display Preparations');
		});

		it('should create datasets button', () => {
			// when
			createElement();

			// then
			const datasetsButton = element.find('#side-panel-nav-datasets');
			expect(datasetsButton.attr('title')).toBe('Display Datasets');
		});

		it('should set active based on the route state name', () => {
			// given
			createElement();

			// when
			scope.active = 'reactHome.datasets';
			scope.$digest();

			// then
			expect(element.find('nav > ul > li').eq(1).hasClass('active')).toBeFalsy();
			expect(element.find('nav > ul > li').eq(2).hasClass('active')).toBeTruthy();
		});
	});

	describe('onClick', () => {
		beforeEach(inject((SettingsActionsService) => {
			spyOn(SettingsActionsService, 'dispatch').and.returnValue();
		}));

		it('should dispatch toggle button click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const toggleButton = element.find('#side-panel-toggle-dock');
			toggleButton.click(e => e.preventDefault());
			toggleButton[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@sidepanel/TOGGLE');

		}));

		it('should dispatch preparations button click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const preparationsButton = element.find('#side-panel-nav-preparations');
			preparationsButton.click(e => e.preventDefault());
			preparationsButton[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@router/GO');
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].payload.args[0]).toBe('reactHome.preparations');
		}));

		it('should dispatch datasets button click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const datasetsButton = element.find('#side-panel-nav-datasets');
			datasetsButton.click(e => e.preventDefault());
			datasetsButton[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@router/GO');
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].payload.args[0]).toBe('reactHome.datasets');
		}));
	});
});
