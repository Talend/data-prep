(function() {
    'use strict';

    function StatsDetails($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/stats-details/stats-details.html',
            bindToController: true,
            controllerAs: 'statsDetailsCtrl',
            controller: 'StatsDetailsCtrl',
            link: function (scope, iElement, iAttrs, ctrl) {
                ctrl.resizePanels = function resizePanels() {
                    $timeout(function () {
                        //Force to resize tabs containers
                        var panel2 = angular.element('.split-pane2').eq(0);
                        var statHeaderPanelsSizeMargin = 45;
                        iElement.find('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');
                    }, 200, false);
                };
            }
        };
    }

    angular.module('data-prep.stats-details')
        .directive('statsDetails', StatsDetails);
})();