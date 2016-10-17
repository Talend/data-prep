/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

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

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();

		createElement = () => {
			element = angular.element('<app-header-bar></app-header-bar>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
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

		it('should create onboarding icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('a#onboarding-icon');
			expect(onboardingIcon.attr('data-icon')).toBe('y');
			expect(onboardingIcon.attr('name')).toBe('OnBoarding');
		});

		it('should create feedback icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('a#message-icon');
			expect(onboardingIcon.attr('data-icon')).toBe('H');
			expect(onboardingIcon.attr('name')).toBe('Feedback');
		});

		it('should create help icon', () => {
			// when
			createElement();

			// then
			const onboardingIcon = element.find('a#online-help-icon');
			expect(onboardingIcon.attr('data-icon')).toBe('l');
			expect(onboardingIcon.attr('name')).toBe('Help');
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
			const onboardingIcon = element.find('a#onboarding-icon');
			onboardingIcon.click((e) => { e.preventDefault(); });
			onboardingIcon[0].click();
			
			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('bar:preparation-onboarding');

		}));

		it('should dispatch feedback icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const feedbackIcon = element.find('a#message-icon');
			feedbackIcon.click((e) => { e.preventDefault(); });
			feedbackIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('bar:feedback');
		}));

		it('should dispatch help icon click', inject((SettingsActionsService) => {
			// given
			createElement();

			// when
			const helpIcon = element.find('a#online-help-icon');
			helpIcon.click((e) => { e.preventDefault(); });
			helpIcon[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('bar:help');
		}));
	});
});
