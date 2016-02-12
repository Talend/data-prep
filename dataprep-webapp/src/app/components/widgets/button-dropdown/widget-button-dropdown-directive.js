/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

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
 * @param {string} buttonIcon The icon to display in the main button
 * @param {string} buttonText The text to display in the main button
 * @param {function} buttonAction The callback to execute on main button click
 */
export default function TalendButtonDropdown($timeout) {
    'ngInject';
    return {
        restrict: 'E',
        transclude: true,
        template: '<div class="button-dropdown">' +
            '<button class="button-dropdown-main" ng-click="buttonDropdownCtrl.buttonAction()">' +
                '<i data-icon="{{buttonDropdownCtrl.buttonIcon}}" class="button-dropdown-main-icon" ng-if="buttonDropdownCtrl.buttonIcon"></i>' +
                '<div class="button-dropdown-main-text">{{buttonDropdownCtrl.buttonText}}<div>' +
            '</button>' +
            '<div class="line-separator"></div>' +
            '<talend-dropdown close-on-select="true">' +
                '<button class="button-dropdown-side dropdown-action dropdown-container"></button>' +
                '<ng-transclude class="dropdown-menu"></ng-transclude>' +
            '</talend-dropdown>' +
        '</div>',
        scope: {
            buttonIcon: '@',
            buttonText: '@',
            buttonAction: '&'
        },
        bindToController: true,
        controller: function () {
        },
        controllerAs: 'buttonDropdownCtrl',
        link: function (scope, iElement, attrs) {
            if (!attrs.buttonAction) {
                $timeout(function () {
                    var action = iElement.find('.dropdown-action').eq(0);

                    iElement.find('.button-dropdown-main')
                        .on('click', function () {
                            action.click();
                        });
                });
            }
        }
    };
}
