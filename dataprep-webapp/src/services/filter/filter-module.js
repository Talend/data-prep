(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.filter
     * @description This module contains the services to manage filters in the datagrid. It is responsible for the filter update within the SlickGrid grid
     * @requires data-prep.services.playground
     */
    angular.module('data-prep.services.filter', [
        'data-prep.services.playground',
        'data-prep.services.statistics',
        'data-prep.services.utils'
    ]);
})();