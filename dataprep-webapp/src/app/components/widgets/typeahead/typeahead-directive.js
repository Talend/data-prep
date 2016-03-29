/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import TypeaheadCtrl from './typeahead-controller';

/**
 * @ngdoc directive
 * @name talend.widget.directive:Typeahead
 * @description This directive create an input with a dropdown element.
 * @param {function} search function called when input changes
 */
export default function Typeahead($timeout, $window) {
    'ngInject';

    return {
        restrict: 'E',
        transclude: true,
        templateUrl: 'app/components/widgets/typeahead/typeahead.html',
        scope: {
            search: '&',
            placeholder: '@',
            searchingText: '@',
            customRender: '@'
        },
        bindToController: true,
        controller: TypeaheadCtrl,
        controllerAs: 'typeaheadCtrl',
        link: {
            post: (scope, iElement, iAttrs, ctrl) => {
                const body = angular.element('body').eq(0);
                const input = iElement.find('.input-search');
                const container = iElement.find('.typeahead-result');
                const offset = 20;

                function hideResults() {
                    $timeout(() => ctrl.hideResults());
                }

                function showResults() {
                    $timeout(() => ctrl.showResults());
                }

                input.keydown((event) => {
                    let menu = iElement.find('ul');
                    let selected = menu.find('li.selected');
                    let current;
                    let listItems = menu.find('li');

                    listItems.removeClass('selected');

                    function scrollToSelectedItem() {
                        if (current.offset().top < current.height()) {
                            container.animate({
                                scrollTop: container.scrollTop() + current.offset().top - current.height() + offset
                            });
                        }
                        if ((current.offset().top + current.height()) > container.height()) {
                            container.animate({
                                scrollTop: container.scrollTop() + current.offset().top - container.height() + offset
                            });
                        }
                    }

                    switch (event.keyCode) {
                        case 27:
                            ctrl.hideResults();
                            scope.$digest();
                            break;
                        case 40:
                            if(!ctrl.visible){
                                showResults();
                            } else {
                                if (!selected.length || selected.is(':last-child')) {
                                    current = listItems.eq(0);
                                } else {
                                    current = selected.next();
                                }
                            }
                            break;
                        case 38:
                            if (!selected.length || selected.is(':first-child')) {
                                current = listItems.last();
                            } else {
                                current = selected.prev();
                            }
                            break;
                        case 13:
                            if(!ctrl.visible){
                                showResults();
                            } else {
                                if (selected.length) {
                                    if (selected.children().eq(0).is('a')) {
                                        $window.open(selected.children().eq(0).attr('href'),'_blank');
                                    } else {
                                        selected.children().click();
                                    }
                                }
                            }
                            break;
                    }
                    if (current) {
                        current.addClass('selected');
                        scrollToSelectedItem();
                    }
                });

                input.click((event) => event.stopPropagation());
                body.click(hideResults);

                iElement.on('$destroy', () => scope.$destroy());
                scope.$on('$destroy', () => body.off('click', hideResults));
            }
        }
    };
}