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
 * @param {function} search Function called when input changes
 * @param {string} placeholder Input placeholder
 * @param {string} searchingText Displayed text while searching
 * @param {string} customRender If 'true' then typeahead will not render result, the rendering is done by user and transcluded
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
            customRender: '@',
            submit: '&'
        },
        bindToController: true,
        controller: TypeaheadCtrl,
        controllerAs: 'typeaheadCtrl',
        link: {
            post: (scope, iElement, iAttrs, ctrl) => {
                const body = angular.element('body').eq(0);
                const input = iElement.find('.input-search');
                const selectedClass = 'selected';

                function hideResults() {
                    $timeout(() => ctrl.hideResults());
                }

                function showResults() {
                    $timeout(() => ctrl.showResults());
                }

                function scrollToSelectedItem(container, next) {
                    container.stop();

                    const nextTop = next.offset().top;
                    const containerTop = container.offset().top;
                    if (nextTop < containerTop) {
                        container.animate({
                            scrollTop: container.scrollTop() - (containerTop - nextTop)
                        }, 'fast');
                    }

                    const nextBottom = nextTop + next.height();
                    const containerBottom = containerTop + container.height();
                    if (nextBottom > containerBottom) {
                        container.animate({
                            scrollTop: container.scrollTop() + (nextBottom - containerBottom)
                        }, 'fast');
                    }
                }

                function getItemList() {
                    return iElement.find('ul').eq(0).find('>li');
                }

                function getCurrentItem() {
                    return iElement.find('ul').eq(0).find('li.' + selectedClass).eq(0);
                }

                input.keydown((event) => {
                    let current, next;

                    switch (event.keyCode) {
                        case 27: //ESC
                            ctrl.hideResults();
                            scope.$digest();
                            break;
                        case 38: //UP
                            current = getCurrentItem();
                            next = (!current.length || current.is(':first-child')) ?
                                getItemList().last() :
                                current.prev();
                            break;
                        case 40: //DOWN
                            if (!ctrl.visible) {
                                showResults();
                                break;
                            }

                            current = getCurrentItem();
                            next = (!current.length || current.is(':last-child')) ?
                                getItemList().eq(0) :
                                current.next();
                            break;
                        case 13: //ENTER
                            if (!ctrl.visible) {
                                showResults();
                                break;
                            }
                            current = getCurrentItem();
                            if (current.length) {
                                if (current.children().eq(0).is('a')) {
                                    $window.open(current.children().eq(0).attr('href'), current.children().eq(0).attr('target'));
                                }
                                else {
                                    current.children().click();
                                }
                            } else {
                                ctrl.submit({value: ctrl.searchString});
                                //clean the value
                                ctrl.searchString = '';
                            }
                            break;
                    }

                    if (next) {
                        current.removeClass(selectedClass);
                        next.addClass(selectedClass);

                        const container = iElement.find('> .typeahead-result').eq(0);
                        scrollToSelectedItem(container, next);
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