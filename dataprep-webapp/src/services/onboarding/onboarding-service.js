(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.onboarding.service:OnboardingService
     * @description OnboardingService service. This service exposes functions to start onboarding tours
     * @requires data-prep.services.onboarding.constant:datasetTour
     * @requires data-prep.services.onboarding.constant:playgroundTour
     * @requires data-prep.services.onboarding.constant:recipeTour
     */
    function OnboardingService($window, datasetTour, playgroundTour, recipeTour) {

        var TOUR_OPTIONS_KEY = 'org.talend.dataprep.tour_options';

        /**
         * @ngdoc property
         * @name template
         * @propertyOf data-prep.services.onboarding.service:OnboardingService
         * @description The step template with title and content
         */
        var template = '<div class="introjs-tooltiptitle"><%= title %></div>' +
            '<div class="introjs-tooltipcontent"><%= content %></div>';

        return {
            shouldStartTour: shouldStartTour,
            startTour: startTour
        };

        /**
         * @ngdoc method
         * @name createIntroSteps
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @param {Array} configs The array of configs, one config for each step
         * @description Create the Intro.js steps
         * @returns {Array} The Intro.js steps
         */
        function createIntroSteps(configs) {
            return _.map(configs, function (config) {
                return {
                    element: config.element,
                    position: config.position,
                    intro: _.template(template)(config)
                };
            });
        }

        /**
         * @ngdoc method
         * @name getTourOptions
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @description Get options from localStorage
         * @returns {object} The saved tour config
         */
        function getTourOptions() {
            var tourOptionsString = $window.localStorage.getItem(TOUR_OPTIONS_KEY);
            return tourOptionsString ? JSON.parse(tourOptionsString) : {};
        }

        /**
         * @ngdoc method
         * @name setTourOptions
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @param {object} options The options to save
         * @description Set options in localStorage
         */
        function setTourOptions(options) {
            $window.localStorage.setItem(TOUR_OPTIONS_KEY, JSON.stringify(options));
        }

        /**
         * @ngdoc method
         * @name getTour
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @param {String} tour The tour Id
         * @description Get tour details
         * @returns {Array} Tour details
         */
        function getTour(tour) {
            switch (tour) {
                case 'dataset':
                    return datasetTour;
                case 'playground':
                    return playgroundTour;
                case 'recipe':
                    return recipeTour;
            }
        }

        /**
         * @ngdoc method
         * @name setTourDone
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @param {String} tour The tour Id
         * @description Set tour options as done in localStorage
         */
        function setTourDone(tour) {
            var options = getTourOptions();
            options[tour] = true;
            setTourOptions(options);
        }

        /**
         * @ngdoc method
         * @name shouldStartTour
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @description Check if the tour should be started depending on the saved options
         * @param {String} tour The tour Id
         * @return {boolean} True if the tour has not been completed yet
         */
        function shouldStartTour(tour) {
            var tourOptions = getTourOptions();
            return !tourOptions[tour];
        }

        /**
         * @ngdoc method
         * @name startTour
         * @methodOf data-prep.services.onboarding.service:OnboardingService
         * @param {String} tour The tour Id
         * @description Configure and start an onboarding tour
         */
        function startTour(tour) {
            introJs()
                .setOptions({
                    nextLabel: 'NEXT',
                    prevLabel: 'BACK',
                    skipLabel: 'SKIP',
                    doneLabel: 'LET ME TRY',
                    steps: createIntroSteps(getTour(tour))
                })
                .oncomplete(function () {
                    setTourDone(tour);
                })
                .onexit(function () {
                    setTourDone(tour);
                })
                .start();
        }
    }

    angular.module('data-prep.services.onboarding')
        .service('OnboardingService', OnboardingService);
})();
