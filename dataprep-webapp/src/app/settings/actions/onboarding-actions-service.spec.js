/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Onboarding actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should trigger onboarding', inject((OnboardingService, OnboardingActionsService) => {
			// given
			const action = {
				type: '@@onboarding/START_TOUR',
				payload: {
					method: 'startTour',
					args: [
						'preparation',
					],
				}
			};
			spyOn(OnboardingService, 'startTour').and.returnValue();

			// when
			OnboardingActionsService.dispatch(action);

			// then
			expect(OnboardingService.startTour).toHaveBeenCalledWith('preparation');
		}));
	});
});
