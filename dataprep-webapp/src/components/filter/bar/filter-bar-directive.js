(function() {
    'use strict';

    function FilterBar(state, FilterService) {
        return {
            restrict: 'E',
            templateUrl: 'components/filter/bar/filter-bar.html',
            scope: {},
            bindToController: true,
            controller: function (){
                this.filterService = FilterService;
                this.state = state;
            },
            controllerAs: 'filterBarCtrl'
        };
    }

    angular.module('data-prep.filter-bar')
        .directive('filterBar', FilterBar);
})();