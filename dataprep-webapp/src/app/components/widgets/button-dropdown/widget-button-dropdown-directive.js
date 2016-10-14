/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './button-dropdown.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendButtonDropdown
 * @description This directive create a button dropdown element.
 * @restrict EA
 * @usage
 <talend-button-dropdown button-text="Click Me" button-action="buttonAction()">
 <ul>
 <li>Menu 1</li>
 <li>Menu 2</li>
 </ul>
 </talend-button-dropdown>
 * @param {string} buttonId The id of the main button
 * @param {string} buttonAdditionalClass The additional class of the main button
 * @param {string} buttonIcon The icon to display in the main button
 * @param {string} buttonText The text to display in the main button
 * @param {string} buttonDropdownTitle The text to display in the main button
 * @param {string} buttonTitle The tooltip to display in the main button
 * @param {function} buttonAction The callback to execute on main button click
 */
export default function TalendButtonDropdown($window, $timeout) {
    'ngInject';
    return {
        restrict: 'E',
        transclude: true,
        templateUrl: template,
        scope: {
            buttonId: '@id',
            buttonAdditionalClass: '@',
            buttonTitle: '@',
            buttonIcon: '@',
            buttonText: '@',
            buttonDropdownTitle: '@',
            buttonAction: '&',
            closeOnSelect: '<',
        },
        bindToController: true,
        controller: () => {
        },
        controllerAs: 'buttonDropdownCtrl',
        link: {
            post: (scope, iElement, attrs) => {
                if (!attrs.buttonAction) {
                    $timeout(function () {
                        const action = iElement.find('.dropdown-action').eq(0);

                        iElement.find('.button-dropdown-main')
                            .on('click', function () {
                                action.click();
                            });
                    });
                }

                //TODO: QUICKFIX
                const windowElement = angular.element($window);
                const buttonDropdownSide = iElement.find('.button-dropdown-side');
                const menu = iElement.find('.dropdown-menu');

                function positionMenu() {
                    if (menu.attr('display') !== 'none') {
                        const position = buttonDropdownSide[0].getBoundingClientRect();
                        const menuMinHeight = 100;
                        let menuTopPosition = position.bottom;

                        // when menu bottom is outside of the window, we position the menu at the top of the button
                        if (menuTopPosition + menuMinHeight > (windowElement.height() - 50)) {
                            menu.css('top', - menuMinHeight);
                        } else {
                            menu.css('top', '100%');
                        }
                    }
                }

                // Click : hide/show menu on left click
                buttonDropdownSide.click(positionMenu);
            }
        },
    };
}
