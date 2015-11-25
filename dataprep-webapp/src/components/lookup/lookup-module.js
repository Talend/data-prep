(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.lookup
     * @description This module contains the dataset lookup
     * @requires talend.widget
     * @requires data-prep.lookup-datagrid-header
     * @requires 'data-prep.services.statistics'
     * @requires 'data-prep.services.state'
     * @requires 'data-prep.services.utils'
     */
    angular.module('data-prep.lookup', [
        'talend.widget',
        'data-prep.services.state',
        'data-prep.services.dataset',
        'data-prep.services.transformation',
        'data-prep.lookup-datagrid-header',
        'data-prep.services.statistics',
        'data-prep.services.utils',
        'data-prep.services.lookup'
    ]);
})();