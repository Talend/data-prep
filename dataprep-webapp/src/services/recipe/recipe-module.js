(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.recipe
     * @description This module contains the services to manipulate the recipe
     * @requires data-prep.services.preparation
     * @requires data-prep.services.transformation
     * @requires data-prep.services.playground
     * @requires data-prep.services.state
     */
    angular.module('data-prep.services.recipe', [
        'data-prep.services.preparation',
        'data-prep.services.transformation',
        'data-prep.services.playground',
        'data-prep.services.state',
        'data-prep.services.filter'
    ]);
})();