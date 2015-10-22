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
                        var actionHeaderPanelsSizeMargin = 120;
                        var statHeaderPanelsSizeMargin = 35;

                        angular.element('.action-suggestion-tab-items').css('height', panel1.height()- actionHeaderPanelsSizeMargin + 'px');
                        angular.element('.stat-detail-tab-items').css('height', panel2.height()- statHeaderPanelsSizeMargin + 'px');
                    },200);
                };

                scope.$watch(
                    function () {
                        return ctrl.suggestionService.showAllAction ;
                    },
                    function () {
                        ctrl.resizePanels();
                    }
                );

                scope.$watch(
                    function () {
                        return ctrl.suggestionService.searchActionString ;
                    },
                    function () {

                        var highlightText = function(actions){
                            angular.forEach(actions, function(item){
                                item.labelHtml = item.labelHtml.replace(new RegExp('(<span class="highlighted">)', 'g'), '');
                                item.labelHtml = item.labelHtml.replace(new RegExp('(</span>)', 'g'), '');

                                item.categoryHtml = item.categoryHtml.replace(new RegExp('(<span class="highlighted">)', 'g'), '');
                                item.categoryHtml = item.categoryHtml.replace(new RegExp('(</span>)', 'g'), '');

                                if(ctrl.suggestionService.searchActionString ){
                                    if(item.labelHtml.toLowerCase().indexOf(ctrl.suggestionService.searchActionString ) !== -1){
                                        item.labelHtml = item.labelHtml.replace(new RegExp('('+ctrl.suggestionService.searchActionString +')', 'gi'),
                                            '<span class="highlighted">$1</span>');
                                    }
                                    if(item.categoryHtml.toLowerCase().indexOf(ctrl.suggestionService.searchActionString ) !== -1){
                                        item.categoryHtml = item.categoryHtml.replace(new RegExp('('+ctrl.suggestionService.searchActionString +')', 'gi'),
                                            '<span class="highlighted">$1</span>');
                                    }
                                }

                            });
                        };

                        if(!ctrl.suggestionService.showAllAction) {
                            highlightText(ctrl.columnSuggestions);
                        } else {
                            angular.forEach(ctrl.columnSuggestions, function(actions){
                                highlightText(actions);
                            });
                            ctrl.columnSuggestionService.updateTransformations();
                        }
                    }
                );
            }
        };
    }

    angular.module('data-prep.actions-suggestions')
        .directive('actionsSuggestions', ActionsSuggestions);
})();