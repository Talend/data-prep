/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

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
export default class DatagridExternalService {
    constructor($timeout, state, StatisticsService, SuggestionService, PreviewService, LookupService) {
        'ngInject';

        this.grid = null;
        this.scrollTimeout = null;
        this.lastSelectedTab = null;
        this.lastSelectedColumn = null;
        this.lastSelectedLine = null;

        this.$timeout = $timeout;
        this.state = state;

        this.StatisticsService = StatisticsService;
        this.SuggestionService = SuggestionService;
        this.PreviewService = PreviewService;
        this.LookupService = LookupService;
    }

    /**
     * @ngdoc method
     * @name updateSuggestionPanel
     * @methodOf data-prep.datagrid.service:DatagridExternalService
     * @description Set the selected column into external services except the index column. This will trigger actions that use this property
     * Ex : StatisticsService for dataviz, ColumnSuggestionService for transformation list
     */
    updateSuggestionPanel() {
        const column = this.state.playground.grid.selectedColumn;
        const line = this.state.playground.grid.selectedLine;

        const columnHasChanged = column !== this.lastSelectedColumn;
        const lineHasChanged = line !== this.lastSelectedLine;

        if(!columnHasChanged && !lineHasChanged) {
            return;
        }

        this.lastSelectedColumn = column;
        this.lastSelectedLine = line;
        this.lastSelectedTab = !column ? 'LINE' : 'COLUMN';

        //change tab
        this.SuggestionService.selectTab(this.lastSelectedTab);

        //reset charts if we have no selected column
        if(!this.lastSelectedColumn) {
            this.StatisticsService.reset();
        }

        //update line scope transformations if line has changed
        if(this.lastSelectedLine && lineHasChanged) {
            this.SuggestionService.setLine(this.lastSelectedLine);
        }

        //update column scope transformations and charts if we have a selected column that has changed
        if (this.lastSelectedColumn && columnHasChanged) {
            this.SuggestionService.setColumn(this.lastSelectedColumn);
            this.StatisticsService.updateStatistics();
            this.LookupService.updateTargetColumn();
        }
    }

    /**
     * @ngdoc method
     * @name attachGridScroll
     * @methodOf data-prep.datagrid.service:DatagridExternalService
     * @description Attach grid scroll listener. It will update the displayed range for preview
     */
    _attachGridScrollListener() {
        this.grid.onScroll.subscribe(() => {
            this.$timeout.cancel(this.scrollTimeout);
            this.scrollTimeout = this.$timeout(
                () => this.PreviewService.gridRangeIndex = this.grid.getRenderedRange(),
                500,
                false
            );
        });
    }

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.datagrid.service:DatagridExternalService
     * @param {object} newGrid The new grid
     * @description Initialize the grid
     */
    init(newGrid) {
        this.grid = newGrid;
        this._attachGridScrollListener();
    }
}