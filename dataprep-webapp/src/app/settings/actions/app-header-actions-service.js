/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { actions } from '../settings';

const PREPARATION_ONBOARDING = 'preparation';
const HELP_URL = 'https://help.talend.com/pages/viewpage.action?pageId=266307043&utm_medium=dpdesktop&utm_source=header';

export default class AppHeaderActionsService {
	constructor($window, OnboardingService, StateService) {
		'ngInject';
		this.$window = $window;
		this.OnboardingService = OnboardingService;
		this.StateService = StateService;
	}

	dispatch(action) {
		switch (action.type) {
		case actions.APPBAR_ONBOARDING:
			this.OnboardingService.startTour(PREPARATION_ONBOARDING);
			break;
		case actions.APPBAR_FEEDBACK:
			this.StateService.showFeedback();
			break;
		case actions.APPBAR_HELP:
			this.$window.open(HELP_URL);
			break;
		}
	}
}
