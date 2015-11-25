(function() {
    'use strict';

    function StatsDetails() {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/stats-details/stats-details.html',
            bindToController: true,
            controllerAs: 'statsDetailsCtrl',
            controller: 'StatsDetailsCtrl'
        };
    }

    angular.module('data-prep.stats-details')
        .directive('statsDetails', StatsDetails);
})();