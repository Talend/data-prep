(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.actions-suggestions.directive:actionsSuggestions
     * @description Actions and suggestions list element
     * @restrict E
     * @usage
     *     <actions-suggestions></actions-suggestions>
     * */
    function ActionsSuggestions($timeout) {
        return {
            restrict: 'E',
            templateUrl: 'components/suggestions-stats/actions-suggestions/actions-suggestions.html',
            bindToController: true,
            controllerAs: 'actionsSuggestionsCtrl',
            controller: 'ActionsSuggestionsCtrl',
            link: function (scope, iElement, iAttrs, ctrl) {

                //Scroll the actual tab container to the bottom of the element to display
                ctrl.scrollToBottom = function scrollToBottom(elementToDisplay) {
                    $timeout(function () {
                        var splitHandler = angular.element('.split-handler').eq(0);
                        var tabContainer = iElement.find('.action-suggestion-tab-items').eq(0);
                        var etdContainer = elementToDisplay.find('>.content-container');

                        var tabOffset = tabContainer.offset();
                        var etdOffset = etdContainer.offset();

                        var etdHeight = etdContainer.context.clientHeight;

                        var availableTopSpace = etdOffset.top - tabOffset.top;
                        var scrollDistance;
                        if (availableTopSpace >= etdHeight) {
                            if (etdOffset.top > (splitHandler.offset().top - etdHeight)) {
                                scrollDistance = tabContainer[0].scrollTop + etdHeight;
                                tabContainer.animate({
                                    scrollTop: scrollDistance
                                }, 500);
                            }
                        }
                        else {
                            scrollDistance = tabContainer[0].scrollTop + availableTopSpace - angular.element('.accordion').height();
                            tabContainer.animate({
                                scrollTop: scrollDistance
                            }, 500);
                        }
                    }, 200, false);
                };
            }
        };
    }

    angular.module('data-prep.actions-suggestions')
        .directive('actionsSuggestions', ActionsSuggestions);
})();