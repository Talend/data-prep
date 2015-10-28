(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.datagrid
     * @description This module contains the controller and directives for the datagrid
     * @requires data-prep.datagrid-header
     * @requires 'data-prep.services.statistics'
     * @requires 'data-prep.services.state'
     * @requires 'data-prep.services.utils'
     */
    angular.module('data-prep.datagrid', [
        'data-prep.datagrid-header',
        'data-prep.services.statistics',
        'data-prep.services.state',
        'data-prep.services.utils'
    ]);
})();