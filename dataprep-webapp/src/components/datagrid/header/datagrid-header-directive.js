(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.datagrid-header.directive:DatagridHeader
     * @description This directive takes care of a datagrid header item. It creates the header dropdown menu.
     * On creation, it calculate the quality bar values.
     *
     * Watchers:
     * <ul>
     *    <li>Close transformation menu on retrieve error, base on {@link data-prep.datagrid-header.controller:DatagridHeaderCtrl controller}.transformationsRetrieveError flag</li>
     * </ul>
     *
     * @restrict E
     * @usage
     <datagrid-header
            column="column">
     </datagrid-header>
     * @param {object} column The column metadata
     */
    function DatagridHeader() {
        return {
            restrict: 'E',
            templateUrl: 'components/datagrid/header/datagrid-header.html',
            scope: {
                column: '='
            },
            bindToController: true,

            controllerAs: 'datagridHeaderCtrl',
            controller: 'DatagridHeaderCtrl',
            link: {
                post: function (scope, iElement, iAttrs, ctrl) {
                    /**
                     * Close transformation menu on retrieve error
                     */
                    scope.$watch(
                        function () {
                            return ctrl.transformationsRetrieveError;
                        },
                        function (newValue) {
                            if (newValue) {
                                var headerDropdownAction = iElement.find('.dropdown-action').eq(0);
                                headerDropdownAction.click();
                            }
                        });

                    iElement.on('$destroy', function() {
                        scope.$destroy();
                    });
                }
            }
        };
    }

    angular.module('data-prep.datagrid-header')
        .directive('datagridHeader', DatagridHeader);
})();
