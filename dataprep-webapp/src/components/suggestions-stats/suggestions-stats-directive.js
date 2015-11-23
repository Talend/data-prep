(function () {
    'use strict';

    function SuggestionsStats($timeout, $window) {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/suggestions-stats.html',
            scope: {
                metadata: '='
            },
            bindToController: true,
            controllerAs: 'suggestionsStatsCtrl',
            controller: 'SuggestionsStatsCtrl',
            link: function (scope, iElement) {

                $timeout(function () {
                    var windowElement = angular.element($window);
                    var documentElement = angular.element(document);
                    var handler = iElement.find('.split-handler').eq(0);
                    var panel1 = iElement.find('.split-pane1').eq(0);
                    var panel2 = iElement.find('.split-pane2').eq(0);

                    var drag = false;
                    var actionHeaderPanelsSizeMargin = 160;
                    var statHeaderPanelsSizeMargin = 45;

                    function resize() {
                        iElement.find('.action-suggestion-tab-items').css('height', panel1.height() - actionHeaderPanelsSizeMargin + 'px');
                        iElement.find('.stat-detail-tab-items').css('height', panel2.height() - statHeaderPanelsSizeMargin + 'px');
                    }

                    //drag event : resize the panels
                    function startDrag() {
                        drag = true;
                    }
                    function finishDrag() {
                        drag = false;
                    }

                    handler.bind('mousedown', startDrag);
                    documentElement.bind('mouseup',finishDrag);
                    iElement.bind('mousemove', function () {
                        if (drag) {
                            resize();
                        }
                    });

                    //Resize panel when window is resized
                    windowElement.on('resize', resize);

                    //Initialization of the right panel
                    // 325px : to have at least 5 actions in the top panel
                    panel1.css('height', '310px');
                    handler.css('top', '310px');
                    panel2.css('top', '310px');
                    resize();

                    //on remove/destroy, clean events
                    iElement.on('$destroy', function () {
                        scope.$destroy();
                    });
                    scope.$on('$destroy', function () {
                        documentElement.off('mouseup', finishDrag);
                        windowElement.off('resize', resize);
                    });
                }, 0, false);
            }
        };
    }

    angular.module('data-prep.suggestions-stats')
        .directive('suggestionsStats', SuggestionsStats);
})();