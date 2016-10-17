/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import { actions } from '../settings';

describe('App header actions service', () => {
	beforeEach(angular.mock.module('app.settings.actions'));

	describe('dispatch', () => {
		it('should start home onboarding', inject((AppHeaderActionsService, OnboardingService) => {
			// given
			const action = { type: actions.APPBAR_ONBOARDING };
			spyOn(OnboardingService, 'startTour').and.returnValue();

			// when
			AppHeaderActionsService.dispatch(action);

			// then
			expect(OnboardingService.startTour).toHaveBeenCalledWith('preparation');
		}));
		
		it('should open feedback', inject((AppHeaderActionsService, StateService) => {
			// given
			const action = { type: actions.APPBAR_FEEDBACK };
			spyOn(StateService, 'showFeedback').and.returnValue();

			// when
			AppHeaderActionsService.dispatch(action);

			// then
			expect(StateService.showFeedback).toHaveBeenCalled();
		}));
		
		it('should open help tab', inject(($window, AppHeaderActionsService) => {
			// given
			const helpUrl = 'https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header';
			const action = { type: actions.APPBAR_HELP };
			spyOn($window, 'open').and.returnValue();

			// when
			AppHeaderActionsService.dispatch(action);

			// then
			expect($window.open).toHaveBeenCalledWith(helpUrl);
		}));
	});
});
