(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.quality-bar
     * @description This module contains the controller and directives to manage the lookup-datagrid quality-bar
     * @requires data-prep.services.filter
     * @requires data-prep.services.transformation
     */
    angular.module('data-prep.lookup-quality-bar', [
        'data-prep.services.filter',
        'data-prep.services.transformation'
    ]);
})();