/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


describe('home controller', () => {
	let scope;
	let createController;
	let $stateMock;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $componentController, OnboardingService) => {
		scope = $rootScope.$new();
		createController = ($stateMock) => $componentController('home', { $scope: scope, $state: $stateMock });

		spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
		spyOn(OnboardingService, 'startTour').and.returnValue();
	}));

	it('should call onboarding service', inject(($timeout, OnboardingService) => {
		// given
		$stateMock = {
			current: {
				name: 'home.preparations'
			},
			params: {},
		};
		const ctrl = createController($stateMock);

		// when
		ctrl.$onInit();
		$timeout.flush(1000);

		// then
		expect(OnboardingService.startTour).toHaveBeenCalledWith('preparation');
	}));
});
