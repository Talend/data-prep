/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { introJs } from 'intro.js';
import {
	HOME_403_ROUTE,
	HOME_DATASETS_ROUTE,
	HOME_PREPARATIONS_ROUTE,
} from '../../index-route';

/**
 * @ngdoc service
 * @name data-prep.services.onboarding.service:OnboardingService
 * @description OnboardingService service. This service exposes functions to start onboarding tours
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.onboarding.constant:recipeTour
 * @requires data-prep.services.onboarding.constant:playgroundTour
 * @requires data-prep.services.onboarding.constant:preparationTour
 */
export default class OnboardingService {

	constructor($timeout, $translate, $state, $window, state, recipeTour, playgroundTour, preparationTour, HelpService, StorageService) {
		'ngInject';

		this.$timeout = $timeout;
		this.$translate = $translate;
		this.$state = $state;
		this.$window = $window;

		this.state = state;
		this.recipeTour = recipeTour;
		this.playgroundTour = playgroundTour;
		this.preparationTour = preparationTour;

		this.HelpService = HelpService;
		this.StorageService = StorageService;
	}

	/**
	 * @ngdoc method
	 * @name createIntroSteps
	 * @methodOf data-prep.services.onboarding.service:OnboardingService
	 * @param {Array} configs The array of configs, one config for each step
	 * @description Create the Intro.js steps
	 * @returns {Array} The Intro.js steps
	 */
	createIntroSteps(configs) {
		return configs.map((config) => {
			const step = {
				element: config.element,
				position: config.position,
				intro: `<div class="introjs-tooltiptitle">${this.$translate.instant(config.title)}</div><div class="introjs-tooltipcontent">${this.$translate.instant(config.content)}</div>`,
			};
			const { intro } = step;
			if (this.HelpService.hasPlaceholders(intro)) {
				step.intro = this.HelpService.replacePlaceholders(intro);
			}
			return step;
		});
	}

	/**
	 * @ngdoc method
	 * @name getTour
	 * @methodOf data-prep.services.onboarding.service:OnboardingService
	 * @param {String} tour The tour Id
	 * @description Get tour details
	 * @returns {Array} Tour details
	 */
	getTour(tour) {
		switch (tour) {
		case 'playground':
			return this.playgroundTour;
		case 'recipe':
			return this.recipeTour;
		case 'preparation':
			return this.preparationTour;
		}
	}

	/**
	 * @ngdoc method
	 * @name setTourDone
	 * @methodOf data-prep.services.onboarding.service:OnboardingService
	 * @param {String} tour The tour Id
	 * @description Set tour options as done in localStorage
	 */
	setTourDone(tour) {
		const options = this.StorageService.getTourOptions();
		options[tour] = true;
		this.StorageService.setTourOptions(options);
	}

	/**
	 * @ngdoc method
	 * @name shouldStartTour
	 * @methodOf data-prep.services.onboarding.service:OnboardingService
	 * @description Check if the tour should be started depending on the saved options
	 * @param {String} tour The tour Id
	 * @return {boolean} True if the tour has not been completed yet
	 */
	shouldStartTour(tour) {
		const tourOptions = this.StorageService.getTourOptions();
		return !tourOptions[tour];
	}

	/**
	 * @ngdoc method
	 * @name startTour
	 * @methodOf data-prep.services.onboarding.service:OnboardingService
	 * @param {String} tour The tour Id
	 * @param {Integer} timeout The timeout
	 * @description Configure and start an onboarding tour
	 */
	startTour(tour, timeout = 200) {
		if (this.$state.current.name === HOME_403_ROUTE) {
			return;
		}
		const isOnDatasetsRoute = this.$state.current.name === HOME_DATASETS_ROUTE;
		if (isOnDatasetsRoute) {
			this.$state.go(HOME_PREPARATIONS_ROUTE, { folderId: this.state.inventory.homeFolder.id });
		}
		const onTourDone = () => {
			this.setTourDone(tour);
			if (isOnDatasetsRoute) {
				this.$state.go(HOME_DATASETS_ROUTE);
			}

			this.currentTour = null;
		};
		this.$timeout(() => {
			this.currentTour = introJs()
				.setOptions({
					nextLabel: this.$translate.instant('NEXT'),
					prevLabel: this.$translate.instant('BACK'),
					skipLabel: this.$translate.instant('SKIP'),
					doneLabel: this.$translate.instant('LET_ME_TRY'),
					steps: this.createIntroSteps(this.getTour(tour)),
				})
				.oncomplete(onTourDone)
				.onexit(onTourDone);
			this.currentTour.start();
		}, timeout, false);
	}
}
