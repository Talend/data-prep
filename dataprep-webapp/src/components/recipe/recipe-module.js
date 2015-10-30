(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.recipe
     * @description This module contains the controller and directives to manage the recipe
     * @requires talend.widget
     * @requires data-prep.services.playground
     * @requires data-prep.services.recipe
     * @requires data-prep.services.preparation
     * @requires data-prep.services.state
     * @requires data-prep.recipe-bullet
     * @requires data-prep.transformation-form
     */
    angular.module('data-prep.recipe', [
        'pascalprecht.translate',
        'talend.widget',
        'data-prep.services.playground',
        'data-prep.services.recipe',
        'data-prep.services.preparation',
        'data-prep.services.state',
        'data-prep.recipe-bullet',
        'data-prep.transformation-form',
        'data-prep.services.filter',
        'data-prep.services.utils'
    ]);
})();