/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';
import { introJs } from 'intro.js';

/**
 * The step template with title and content
 */
const template =
    '<div class="introjs-tooltiptitle"><%= title %></div>' +
    '<div class="introjs-tooltipcontent"><%= content %></div>';

const TOUR_OPTIONS_KEY = 'org.talend.dataprep.tour_options';

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

	constructor($timeout, $state, $window, state, recipeTour, playgroundTour, preparationTour) {
		'ngInject';

		this.$timeout = $timeout;
		this.$state = $state;
		this.$window = $window;
		this.state = state;
		this.recipeTour = recipeTour;
		this.playgroundTour = playgroundTour;
		this.preparationTour = preparationTour;
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
		return _.map(configs, config => ({
			element: config.element,
			position: config.position,
			intro: _.template(template)(config),
		}));
	}

    /**
     * @ngdoc method
     * @name getTourOptions
     * @methodOf data-prep.services.onboarding.service:OnboardingService
     * @description Get options from localStorage
     * @returns {object} The saved tour config
     */
	getTourOptions() {
		const tourOptionsString = this.$window.localStorage.getItem(TOUR_OPTIONS_KEY);
		return tourOptionsString ? JSON.parse(tourOptionsString) : {};
	}

    /**
     * @ngdoc method
     * @name setTourOptions
     * @methodOf data-prep.services.onboarding.service:OnboardingService
     * @param {object} options The options to save
     * @description Set options in localStorage
     */
	setTourOptions(options) {
		this.$window.localStorage.setItem(TOUR_OPTIONS_KEY, JSON.stringify(options));
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
		const options = this.getTourOptions();
		options[tour] = true;
		this.setTourOptions(options);
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
		const tourOptions = this.getTourOptions();
		return !tourOptions[tour];
	}

    /**
     * @ngdoc method
     * @name startTour
     * @methodOf data-prep.services.onboarding.service:OnboardingService
     * @param {String} tour The tour Id
     * @description Configure and start an onboarding tour
     */
	startTour(tour) {
		const isOnDatasetsRoute = this.$state.current.name === 'nav.index.datasets';
		if (isOnDatasetsRoute) {
			this.$state.go('nav.index.preparations', { folderId: this.state.inventory.homeFolderId });
		}

		this.$timeout(() => {
			this.currentTour = introJs()
                .setOptions({
	nextLabel: 'NEXT',
	prevLabel: 'BACK',
	skipLabel: 'SKIP',
	doneLabel: 'LET ME TRY',
	steps: this.createIntroSteps(this.getTour(tour)),
})
                .oncomplete(() => {
	this.setTourDone(tour);
})
                .onexit(() => {
	this.setTourDone(tour);
	if (isOnDatasetsRoute) {
		this.$state.go('nav.index.datasets');
	}

	this.currentTour = null;
});
			this.currentTour.start();
		}, 200, false);
	}
}
