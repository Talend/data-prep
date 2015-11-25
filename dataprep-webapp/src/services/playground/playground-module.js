(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.playground
     * @description This module contains the services to load the playground
     * @requires data-prep.services.dataset
     * @requires data-prep.services.filter
     * @requires data-prep.services.history
     * @requires data-prep.services.onboarding
     * @requires data-prep.services.preparation
     * @requires data-prep.services.recipe
     * @requires data-prep.services.state
     * @requires data-prep.services.statistics
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.playground', [
        'data-prep.services.dataset',
        'data-prep.services.filter',
        'data-prep.services.history',
        'data-prep.services.onboarding',
        'data-prep.services.preparation',
        'data-prep.services.recipe',
        'data-prep.services.statistics',
        'data-prep.services.state',
        'data-prep.services.utils',
        'data-prep.services.lookup'
    ]);
})();