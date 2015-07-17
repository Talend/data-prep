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
     * @param {string} buttonText The text to display in the main button
     * @param {function} buttonAction The callback to execute on main button click
     */
    function TalendButtonDropdown() {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'components/widgets/button-dropdown/button-dropdown.html',
            scope: {
                buttonIcon: '@',
                buttonText: '@',
                buttonAction: '&'
            },
            bindToController: true,
            controller: function() {},
            controllerAs: 'buttonDropdownCtrl'
        };
    }

    angular.module('talend.widget')
        .directive('talendButtonDropdown', TalendButtonDropdown);
})();
