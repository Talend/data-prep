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

describe('App header bar container', () => {
	let scope;
	let createElement;
	let element;
	const body = angular.element('body');

	beforeEach(angular.mock.module('@talend/react-components.containers'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			ONBOARDING: 'OnBoarding',
			FEEDBACK_TOOLTIP: 'Feedback',
			ONLINE_HELP_TOOLTIP: 'Help',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile, SettingsService) => {
		scope = $rootScope.$new();

		createElement = () => {
			element = angular.element('<app-header-bar></app-header-bar>');
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
		it('should create brand link', () => {
			// when
			createElement();

			// then
			const brand = element.find('.tc-header-bar > ul').eq(0).find('li').eq(1).find('.btn');
			expect(brand.text()).toBe('Data Preparation');
		});

		it('should create search icon', () => {
			// when
			createElement();

			// then
			const searchBar = element.find('.navbar-form');
			expect(searchBar.attr('role')).toBe('search');
			expect(searchBar.find('svg > use').eq(0).attr('xlink:href')).toBe('#talend-search');
		});

		it('should create onboarding icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('#onboarding\\:preparation');
			expect(onboardingIcon.attr('label')).toBe('Click here to discover the application');
		});

		it('should create feedback icon', () => {
			// when
			createElement();

			// then
			const feedbackIcon = element.find('#modal\\:feedback');
			expect(feedbackIcon.attr('label')).toBe('Send feedback to Talend');
		});

		it('should create user menu', () => {
			// when
			createElement();

			// then
			const userMenuToggle = element.find('#user\\:menu');
			expect(userMenuToggle.text().trim()).toBe('anonymousUser');
			const logoutMenuItem = element.find('#user\\:logout');
			expect(logoutMenuItem.text()).toBe('Logout');
		});

		it('should create products menu', () => {
			// when
			createElement();

			// then
			const productsToggle = element.find('#products\\:menu');
			expect(productsToggle.text()).toBeFalsy();

			expect(element.find('#product\\:producta').text()).toBe('Product A');
			expect(element.find('#product\\:productb').text()).toBe('Product B');
			expect(element.find('#product\\:productc').text()).toBe('Product C');
		});
	});

	describe('onClick', () => {
		beforeEach(inject((SettingsActionsService) => {
			spyOn(SettingsActionsService, 'dispatch').and.returnValue();
		}));

		it('should dispatch onboarding icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const onboardingIcon = element.find('#onboarding\\:preparation');
			onboardingIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@onboarding/START_TOUR');

		}));

		it('should dispatch feedback icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const feedbackIcon = element.find('#modal\\:feedback');
			feedbackIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@modal/SHOW');
		}));

		it('should dispatch feedback icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const feedbackIcon = element.find('#modal\\:feedback');
			feedbackIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@modal/SHOW');
		}));
	});
});
