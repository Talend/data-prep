(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.lookup.service:LookupDatagridExternalService
     * @description Datagrid private service that manages the selected column action to the outer world (non dratagrid)
     * @requires data-prep.services.state.service:StateService
     */
    function LookupDatagridExternalService(StateService) {
        var grid;
        var lastSelectedColumn;

        return {
            init: init
        };

        /**
         * @ngdoc method
         * @name updateSelectedLookupColumn
         * @methodOf data-prep.lookup.service:LookupDatagridExternalService
         * @param {Object} column The selected column
         * @description Set the selected column into state services
         */
        function updateSelectedLookupColumn(column) {
            var columnHasChanged = column.tdpColMetadata !== lastSelectedColumn;
            if (!columnHasChanged) {
                return;
            }
            lastSelectedColumn = column.tdpColMetadata;
            StateService.setLookupGridSelection(column);
        }

        /**
         * @ngdoc method
         * @name attachCellListeners
         * @methodOf data-prep.lookup.service:LookupDatagridExternalService
         * @description Attach cell selection listeners
         */
        function attachCellListeners() {
            //change selected cell column background
            grid.onActiveCellChanged.subscribe(function (e, args) {
                if (angular.isDefined(args.cell)) {
                    var column = grid.getColumns()[args.cell];
                    updateSelectedLookupColumn(column);
                }
            });
        }

        /**
         * @ngdoc method
         * @name attachColumnListeners
         * @methodOf data-prep.lookup.service:LookupDatagridExternalService
         * @description Attach header selection listeners on right click or left click
         */
        function attachColumnListeners() {
            function attachColumnCallback(args) {
                var columnId = args.column.id;
                var column = _.find(grid.getColumns(), {id: columnId});
                updateSelectedLookupColumn(column);
            }

            grid.onHeaderContextMenu.subscribe(function (e, args) {
                attachColumnCallback(args);
            });

            grid.onHeaderClick.subscribe(function (e, args) {
                attachColumnCallback(args);
            });
        }

        /**
         * @ngdoc method
         * @name init
         * @methodOf data-prep.lookup.service:LookupDatagridExternalService
         * @param {object} newGrid The new grid
         * @description Initialize the grid
         */
        function init(newGrid) {
            grid = newGrid;
            attachCellListeners();
            attachColumnListeners();
        }
    }

    angular.module('data-prep.lookup')
        .service('LookupDatagridExternalService', LookupDatagridExternalService);
})();