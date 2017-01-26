/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Home controller', () => {
	let scope;
	let createController;
	let $stateMock;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $componentController, OnboardingService, StateService) => {
		scope = $rootScope.$new(true);
		createController = ($stateMock) => $componentController('home', { $scope: scope, $state: $stateMock });

		spyOn(StateService, 'setHomeSidePanelDock').and.returnValue();
		spyOn(OnboardingService, 'startTour').and.returnValue();
	}));

	describe('onboarding', () => {
		it('should start onboarding', inject(($timeout, OnboardingService) => {
			// given
			$stateMock = { params: {} };
			const ctrl = createController($stateMock);

			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);

			// when
			ctrl.$onInit();
			$timeout.flush(1000);

			// then
			expect(OnboardingService.startTour).toHaveBeenCalledWith('preparation');
		}));

		it('should NOT start onboarding when a prepId is provided', inject(($timeout, OnboardingService) => {
			// given
			$stateMock = { params: { prepid: '123b9ca6749a75' } };
			const ctrl = createController($stateMock);

			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);

			// when
			ctrl.$onInit();
			$timeout.flush();

			// then
			expect(OnboardingService.startTour).not.toHaveBeenCalled();
		}));

		it('should NOT start onboarding when a datasetId is provided', inject(($timeout, OnboardingService) => {
			// given
			$stateMock = { params: { datasetid: '123b9ca6749a75' } };
			const ctrl = createController($stateMock);

			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);

			// when
			ctrl.$onInit();
			$timeout.flush();

			// then
			expect(OnboardingService.startTour).not.toHaveBeenCalled();
		}));

		it('should NOT start onboarding when it is not required anymore', inject(($timeout, OnboardingService) => {
			// given
			$stateMock = { params: {} };
			const ctrl = createController($stateMock);

			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false);

			// when
			ctrl.$onInit();
			$timeout.flush();

			// then
			expect(OnboardingService.startTour).not.toHaveBeenCalled();
		}));
	});

	it('should configure side panel', inject((OnboardingService, StorageService, StateService) => {
		// given
		$stateMock = { params: {} };
		const ctrl = createController($stateMock);

		spyOn(OnboardingService, 'shouldStartTour').and.returnValue(false);
		spyOn(StorageService, 'getSidePanelDock').and.returnValue(true);

		// when
		ctrl.$onInit();

		// then
		expect(StateService.setHomeSidePanelDock).toHaveBeenCalledWith(true);
	}));
});
