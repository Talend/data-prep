(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-bar
     * @description This module aggregate the filters components into a bar
     */
    angular.module('data-prep.filter-bar', [
        'data-prep.filter-search',
        'data-prep.filter-list',
        'data-prep.filter-monitor'
    ]);
})();