(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-monitor
     * @description This module contains the controller and directives to manage the filter list
     * @requires data-prep.services.filter
     * @requires data-prep.services.state
     */
    angular.module('data-prep.filter-monitor', [
        'data-prep.services.filter',
        'data-prep.services.state'
    ]);
})();