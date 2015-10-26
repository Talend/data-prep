(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.playground
     * @description This module contains the controller and directives to manage the playground
     * @requires talend.widget
     * @requires data-prep.datagrid
     * @requires data-prep.export
     * @requires data-prep.filter-bar
     * @requires data-prep.history-control
     * @requires data-prep.lookup
     * @requires data-prep.recipe
     * @requires data-prep.services.onboarding
     * @requires data-prep.services.preparation
     * @requires data-prep.services.playground
     * @requires data-prep.services.recipe
     * @requires data-prep.services.state
     * @requires data-prep.suggestions-stats
     */
    angular.module('data-prep.playground', [
        'ui.router',
        'pascalprecht.translate',
        'talend.widget',
        'data-prep.datagrid',
        'data-prep.export',
        'data-prep.filter-bar',
        'data-prep.history-control',
        'data-prep.lookup',
        'data-prep.recipe',
        'data-prep.suggestions-stats',
        'data-prep.services.onboarding',
        'data-prep.services.preparation',
        'data-prep.services.playground',
        'data-prep.services.recipe',
        'data-prep.services.state'
    ]);
})();