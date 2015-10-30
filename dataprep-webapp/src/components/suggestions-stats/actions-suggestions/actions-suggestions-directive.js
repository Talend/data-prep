(function() {
    'use strict';

    function ActionsSuggestions($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/actions-suggestions/actions-suggestions.html',
            bindToController: true,
            controllerAs: 'actionsSuggestionsCtrl',
            controller: 'ActionsSuggestionsCtrl',
            link: function (scope, iElement, iAttrs, ctrl) {

                ctrl.scrollToBottom = function scrollToBottom(elm) {
                    $timeout(function(){
                        var contentContainer = elm.find('>.content-container');
                        var availablelTopSpace = contentContainer.offset().top - angular.element('.action-suggestion-tab-items').offset().top;
                        var scrollTo = 0;
                        if(availablelTopSpace >= contentContainer.context.clientHeight) {
                            if (contentContainer.offset().top > (angular.element('.split-handler').offset().top -  contentContainer.context.clientHeight)){
                                scrollTo = angular.element('.action-suggestion-tab-items')[0].scrollTop + contentContainer.context.clientHeight;
                                angular.element('.action-suggestion-tab-items').animate({
                                    scrollTop: scrollTo
                                }, 500);
                            }
                        } else {
                            scrollTo = angular.element('.action-suggestion-tab-items')[0].scrollTop + availablelTopSpace - angular.element('.accordion').height();
                            angular.element('.action-suggestion-tab-items').animate({
                                scrollTop: scrollTo
                            }, 500);
                        }
                    },200);
                };

                ctrl.resizePanels = function resizePanels() {
                    $timeout(function(){
                        //Force to resize tabs containers
                        var panel1 = angular.element('.split-pane1');
                        var panel2 = angular.element('.split-pane2');
                        var actionHeaderPanelsSizeMargin = 160;
                        var statHeaderPanelsSizeMargin = 35;


                        angular.element('.action-suggestion-tab-items').css('height', panel1.height()- actionHeaderPanelsSizeMargin + 'px');
                        angular.element('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');
                    },200);
                };
            }
        };
    }

    angular.module('data-prep.actions-suggestions')
        .directive('actionsSuggestions', ActionsSuggestions);
})();