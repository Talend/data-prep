(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.transformation
     * @description This module contains the services to manipulate transformations
     * @requires data-prep.services.utils
     * @requires data-prep.services.state
     * @requires data-prep.services.playground
     */
    angular.module('data-prep.services.transformation', [
        'data-prep.services.utils',
        'data-prep.services.state',
        'data-prep.services.playground',
        'data-prep.services.filter'
    ]);
})();