(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.lookup-datagrid-header.directive:DatagridHeader
     * @description This directive creates the lookup datagrid column header
     * @restrict E
     * @usage
     <lookup-datagrid-header
     added="added"
     column="column">
     </lookup-datagrid-header>
     * @param {object} column The column metadata
     * @param {object} added checkbox ng-model
     */
    function LookupDatagridHeader() {
        return {
            restrict: 'E',
            templateUrl: 'components/lookup/lookup-datagrid/header/lookup-datagrid-header.html',
            scope: {
                column: '=',
                added: '='
            },
            bindToController: true,
            controllerAs: 'lookupDatagridHeaderCtrl',
            controller: 'LookupDatagridHeaderCtrl',
            link: {
                post: function (scope, iElement) {
                    var gridHeader = iElement.find('.whole-grid-header').eq(0);

                    attachClickListener();
                    /**
                     * @ngdoc method
                     * @name attachClickListener
                     * @methodOf data-prep.lookup-datagrid-header.directive:LookupDatagridHeader
                     * @description Attach a 'Click' event listener on grid header
                     */
                    function attachClickListener() {
                        gridHeader.mousedown(function(event) {
                            if (event.which === 3) { //Right click
                                event.stopPropagation();
                                gridHeader.click();
                            }
                        });
                    }
                }
            }
        };
    }

    angular.module('data-prep.lookup-datagrid-header')
        .directive('lookupDatagridHeader', LookupDatagridHeader);
})();
