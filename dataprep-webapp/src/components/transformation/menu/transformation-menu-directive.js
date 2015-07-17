(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.transformation-menu.directive:TransformMenu
     * @description This directive display a transformation menu item, and create the parameters form if needed
     * @restrict E
     * @usage
     <transform-menu
         column="column"
         menu="menu">
     </transform-menu>
     * @param {object} column The target column of this transformation menu item
     * @param {object} menu The menu item to display
     */
    function TransformMenu() {
        return {
            restrict: 'E',
            templateUrl: 'components/transformation/menu/transformation-menu.html',
            scope: {
                column: '=',
                menuItems: '='
            },
            bindToController: true,
            controllerAs: 'menuCtrl',
            controller: 'TransformMenuCtrl'
        };
    }

    angular.module('data-prep.transformation-menu')
        .directive('transformMenu', TransformMenu);
})();