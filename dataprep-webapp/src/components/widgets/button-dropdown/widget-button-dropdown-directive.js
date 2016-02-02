(function () {
    'use strict';

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
    function TalendButtonDropdown($timeout) {
        return {
            restrict: 'E',
            transclude: true,
            //template: '<talend-dropdown class="button-dropdown" close-on-select="true">' +
            //            '<div class="dropdown-container">' +
            //            	'<button class="button-dropdown-main" ng-click="buttonDropdownCtrl.buttonAction()">' +
            //                    '<i data-icon="{{buttonDropdownCtrl.buttonIcon}}" class="button-dropdown-main-icon" ng-if="buttonDropdownCtrl.buttonIcon"></i>' +
            //                    '<div class="button-dropdown-main-text">{{buttonDropdownCtrl.buttonText}}<div>' +
            //                '</button>' +
            //            	'<div class="line-separator"></div>' +
	         //               '<button class="button-dropdown-side dropdown-action"></button>' +
        	 //               '<ng-transclude class="dropdown-menu"></ng-transclude>' +
            //            '</div>' +
            //            '</talend-dropdown>',
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
            controller: function () {},
            controllerAs: 'buttonDropdownCtrl',
            link: function (scope, iElement, attrs) {
                if (!attrs.buttonAction) {
                    $timeout(function () {
                        var action =  iElement.find('.dropdown-action').eq(0);

                        iElement.find('.button-dropdown-main')
                            .on('click', function() {
                                action.click();
                            });
                    });
                }
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendButtonDropdown', TalendButtonDropdown);
})();
