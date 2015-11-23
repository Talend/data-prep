(function() {
    'use strict';

    function SuggestionsStats($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/suggestions-stats.html',
            scope: {
                metadata: '='
            },
            bindToController: true,
            controllerAs: 'suggestionsStatsCtrl',
            controller: 'SuggestionsStatsCtrl',
            link: function(scope, iElement) {

                //Resize action-suggestion and stat-detail panels
                $timeout(function(){

                    //These elements are created by bg-splitter
                    var handler = iElement.find('.split-handler');
                    var panel1 = iElement.find('.split-pane1');
                    var panel2 = iElement.find('.split-pane2');

                    var drag = false;

                    var actionHeaderPanelsSizeMargin = 160;
                    var statHeaderPanelsSizeMargin = 45;

                    iElement.bind('mousemove', function () {
                        if (!drag) {
                            return;
                        }
                        iElement.find('.action-suggestion-tab-items').css('height', panel1.height()- actionHeaderPanelsSizeMargin + 'px');
                        iElement.find('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');
                    });

                    handler.bind('mousedown', function (ev) {
                        ev.preventDefault();
                        drag = true;
                    });

                    angular.element(document).bind('mouseup', function () {
                        drag = false;
                    });

                    //Resize panel when window is resized
                    $(window).resize(function() {
                        iElement.find('.action-suggestion-tab-items').css('height', panel1.height()- actionHeaderPanelsSizeMargin + 'px');
                        iElement.find('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');
                    });

                    //Initialization of the right panel
                    // 325px : to have at least 5 actions in the top panel
                    panel1.css('height', '310px');
                    handler.css('top', '310px');
                    panel2.css('top', '310px');

                    iElement.find('.action-suggestion-tab-items').css('height', panel1.height()- actionHeaderPanelsSizeMargin + 'px');
                    iElement.find('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');

                }, 0, false);
            }
        };
    }

    angular.module('data-prep.suggestions-stats')
        .directive('suggestionsStats', SuggestionsStats);
})();