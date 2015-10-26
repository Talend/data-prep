(function() {
    'use strict';

    function FilterMonitor() {
        return {
            restrict: 'E',
            templateUrl: 'components/filter/monitor/filter-monitor.html',
            scope: {
                filters: '=',
                onReset: '&',
                nbLines: '=',
                nbTotalLines: '=',
                percentage: '='
            },
            bindToController: true,
            controller: function() {},
            controllerAs: 'filterMonitorCtrl'
        };
    }

    angular.module('data-prep.filter-monitor')
        .directive('filterMonitor', FilterMonitor);
})();