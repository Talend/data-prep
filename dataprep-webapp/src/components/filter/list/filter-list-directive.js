(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.filter-list.directive:FilterList
     * @description This directive display the filter list as badges. It consumes the filter list from {@link data-prep.services.filter.service:FilterService FilterService}
     * @restrict E
     * @requires data-prep.services.filter.service:FilterService
     */
    function FilterList() {
        return {
            restrict: 'E',
            templateUrl: 'components/filter/list/filter-list.html',
            scope: {
                filters: '=',
                onFilterChange: '&',
                onFilterRemove: '&'
            },
            bindToController: true,
            controllerAs: 'filterCtrl',
            controller: 'FilterListCtrl'
        };
    }

    angular.module('data-prep.filter-list')
        .directive('filterList', FilterList);
})();