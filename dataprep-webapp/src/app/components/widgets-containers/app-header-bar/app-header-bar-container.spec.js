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

describe('App header bar container', () => {
	let scope;
	let createElement;
	let element;
	const body = angular.element('body');

	beforeEach(angular.mock.module('react-talend-components.containers'));

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
			const brand = element.find('.navbar-brand');
			expect(brand.text()).toBe('Data Preparation');
			expect(brand.attr('title')).toBe('Talend Data Preparation');
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
			expect(onboardingIcon.attr('name')).toBe('Click here to discover the application');
			expect(onboardingIcon.find('svg > use').eq(0).attr('xlink:href')).toBe('#talend-board');
		});

		it('should create feedback icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('#modal\\:feedback');
			expect(onboardingIcon.attr('name')).toBe('Send feedback to Talend');
			expect(onboardingIcon.find('svg > use').eq(0).attr('xlink:href')).toBe('#talend-bubbles');
		});

		it('should create help icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('#external\\:help');
			expect(onboardingIcon.attr('name')).toBe('Open Online Help');
			expect(onboardingIcon.find('svg > use').eq(0).attr('xlink:href')).toBe('#talend-question-circle');
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

		it('should dispatch help icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const helpIcon = element.find('#external\\:help');
			helpIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@external/OPEN_WINDOW');
		}));
	});
});
