(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-search
     * @description This module contains the controller and directives to manage the filter input with suggestions
     * as the user type in the input.
     * @requires data-prep.services.filter
     */
    angular.module('data-prep.filter-search', [
        'MassAutoComplete',
        'pascalprecht.translate',
        'data-prep.services.filter'
    ]);
})();