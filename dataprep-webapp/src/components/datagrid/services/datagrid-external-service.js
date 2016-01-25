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
     * @requires data-prep.services.lookup.service:LookupService
     *
     */
    function DatagridExternalService($timeout, state, StatisticsService, SuggestionService, PreviewService, LookupService) {
        var grid;
        var suggestionTimeout;
        var scrollTimeout;
        var lastSelectedTab;
        var lastSelectedColumn;
        var lastSelectedLine;

        return {
            init: init,
            updateSuggestionPanel: updateSuggestionPanel
        };

        /**
         * @ngdoc method
         * @name updateSuggestionPanel
         * @methodOf data-prep.datagrid.service:DatagridExternalService
         * @param {boolean} updateImmediately Update suggestions without timeout
         * @description Set the selected column into external services except the index column. This will trigger actions that use this property
         * Ex : StatisticsService for dataviz, ColumnSuggestionService for transformation list
         */

        function updateSuggestionPanel(updateImmediately) {
            var column = state.playground.grid.selectedColumn;
            var line = state.playground.grid.selectedLine;

            var columnHasChanged = column !== lastSelectedColumn;
            var lineHasChanged = line !== lastSelectedLine;

            if(!columnHasChanged && !lineHasChanged) {
                return;
            }

            $timeout.cancel(suggestionTimeout);
            suggestionTimeout = $timeout(function () {
                lastSelectedColumn = column;
                lastSelectedLine = line;
                lastSelectedTab = !column ? 'LINE' : 'COLUMN';

                //change tab
                SuggestionService.selectTab(lastSelectedTab);

                //reset charts if we have no selected column
                if(!lastSelectedColumn) {
                    StatisticsService.reset(true, true, true);
                }

                //update line scope transformations if line has changed
                if(lastSelectedLine && lineHasChanged) {
                    SuggestionService.setLine(lastSelectedLine);
                }

                //update column scope transformations and charts if we have a selected column that has changed
                if (lastSelectedColumn && columnHasChanged) {
                    StatisticsService.updateStatistics();
                    SuggestionService.setColumn(lastSelectedColumn);
                    LookupService.updateTargetColumn();
                }
            }, updateImmediately ? 0 : 300);
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
            attachGridScrollListener();
        }
    }

    angular.module('data-prep.datagrid')
        .service('DatagridExternalService', DatagridExternalService);
})();