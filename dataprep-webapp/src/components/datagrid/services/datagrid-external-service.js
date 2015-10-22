(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.datagrid.service:DatagridExternalService
     * @description Datagrid private service that manage the selected column action to the outer world (non dratagrid)
     * @requires data-prep.services.statistics.service:StatisticsService
     * @requires data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @requires data-prep.services.playground.service:PreviewService
     */
    function DatagridExternalService($timeout, StatisticsService, SuggestionService, PreviewService) {
        var grid;
        var suggestionTimeout;
        var scrollTimeout;
        var lastSelectedTab;
        var lastSelectedColumn;

        return {
            init: init,
            updateSuggestionPanel: updateSuggestionPanel
        };

        /**
         * @ngdoc method
         * @name updateSuggestionPanel
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @param {string} column The selected column
         * @param {string} tab The suggestion tab to select
         * @description Set the selected column into external services except the index column. This will trigger actions that use this property
         * Ex : StatisticsService for dataviz, ColumnSuggestionService for transformation list
         */

        function updateSuggestionPanel(column, tab) {
            if (column.id === 'tdpId') {
                $timeout(function () {
                    SuggestionService.reset();
                    StatisticsService.reset(true, true, true);
                });
            }
            else {
                var tabHasChanged = tab !== lastSelectedTab;
                var columnHasChanged = column.tdpColMetadata !== lastSelectedColumn;

                if (!tabHasChanged && !columnHasChanged) {
                    return;
                }

                $timeout.cancel(suggestionTimeout);

                suggestionTimeout = $timeout(function () {
                    lastSelectedColumn = column.tdpColMetadata;
                    lastSelectedTab = tab;

                    if (tabHasChanged) {
                        SuggestionService.selectTab(lastSelectedTab);
                    }
                    if (columnHasChanged) {
                        StatisticsService.updateStatistics();
                        SuggestionService.setColumn(lastSelectedColumn);
                    }
                }, 200);
            }
        }

        /**
         * @ngdoc method
         * @name attachCellListeners
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @description Attach cell selection listeners
         */
        function attachCellListeners() {
            //change selected cell column background
            grid.onActiveCellChanged.subscribe(function (e, args) {
                if (angular.isDefined(args.cell)) {
                    var column = grid.getColumns()[args.cell];
                    updateSuggestionPanel(column, 'COLUMN'); //TODO : change this to CELL when cell actions are supported
                }
            });
        }

        /**
         * @ngdoc method
         * @name attachColumnListeners
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @description Attach header selection listeners on right click or left click
         */
        function attachColumnListeners() {
            function attachColumnCallback(args) {
                var columnId = args.column.id;
                var column = _.find(grid.getColumns(), {id: columnId});
                updateSuggestionPanel(column, 'COLUMN');
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
         * @name attachGridScroll
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @description Attach grid scroll listener. It will update the displayed range for preview
         */
        function attachGridScrollListener() {
            grid.onScroll.subscribe(function () {
                clearTimeout(scrollTimeout);
                scrollTimeout = setTimeout(function () {
                    PreviewService.gridRangeIndex = grid.getRenderedRange();
                }, 200);
            });
        }

        /**
         * @ngdoc method
         * @name init
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @param {object} newGrid The new grid
         * @description Initialize the grid
         */
        function init(newGrid) {
            grid = newGrid;
            attachCellListeners();
            attachColumnListeners();
            attachGridScrollListener();
        }
    }

    angular.module('data-prep.datagrid')
        .service('DatagridExternalService', DatagridExternalService);
})();