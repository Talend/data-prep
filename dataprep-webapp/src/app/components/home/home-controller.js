/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import {
	HOME_403_ROUTE,
	HOME_404_ROUTE,
} from '../../index-route';

export default class HomeCtrl {
	constructor($state, $timeout, OnboardingService, StateService, StorageService, state) {
		'ngInject';
		this.$state = $state;
		this.$timeout = $timeout;
		this.state = state;
		this.OnboardingService = OnboardingService;
		this.StateService = StateService;
		this.StorageService = StorageService;
	}

	$onInit() {
		this.configureSidePanel();
		this.startOnboarding();
	}

	configureSidePanel() {
		const docked = this.StorageService.getSidePanelDock();
		this.StateService.setHomeSidePanelDock(docked);
	}

	startOnboarding() {
		const tourId = 'preparation';

		if (
			this.$state.current.name === HOME_403_ROUTE ||
			this.$state.current.name === HOME_404_ROUTE ||
			this.$state.params.prepid ||
			this.$state.params.datasetid ||
			!this.OnboardingService.shouldStartTour(tourId)
		) {
			return;
		}

		this.$timeout(() => this.OnboardingService.startTour(tourId), 1000, false);
	}
}
