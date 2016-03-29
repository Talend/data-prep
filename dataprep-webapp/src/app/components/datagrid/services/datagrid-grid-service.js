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
 * @name data-prep.datagrid.service:DatagridGridService
 * @description Datagrid private service that init the grid
 * @requires data-prep.state.service:StateService
 * @requires data-prep.datagrid.service:DatagridService
 * @requires data-prep.datagrid.service:DatagridStyleService
 * @requires data-prep.datagrid.service:DatagridColumnService
 * @requires data-prep.datagrid.service:DatagridSizeService
 * @requires data-prep.datagrid.service:DatagridExternalService
 * @requires data-prep.datagrid.service:DatagridTooltipService
 */
export default class DatagridGridService {

    constructor($timeout, state, StateService, DatagridService,
        DatagridStyleService, DatagridColumnService, DatagridSizeService, DatagridExternalService, DatagridTooltipService) {
        'ngInject';

        this.grid = null;
        this.changeActiveTimeout = null;

        this.$timeout = $timeout;
        this.state = state;
        this.StateService = StateService;
        this.DatagridService = DatagridService;

        this.gridServices = [
            DatagridColumnService,
            DatagridStyleService,
            DatagridSizeService,
            DatagridExternalService,
            DatagridTooltipService
        ];
    }

    /**
     * @ngdoc method
     * @name attachLongTableListeners
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description Attaches listeners for data update to reRender the grid
     */
    _attachLongTableListeners() {
        this.state.playground.grid.dataView.onRowCountChanged.subscribe(() => {
            this.grid.updateRowCount();
            this.grid.render();
        });
        this.state.playground.grid.dataView.onRowsChanged.subscribe((e, args) => {
            this.grid.invalidateRows(args.rows);
            this.grid.render();
        });
    }

    /**
     * @ngdoc method
     * @name attachGridStateListeners
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description Attach listeners for saving the state of column id and line selection number
     */
    _attachGridStateListeners() {
        this.grid.onActiveCellChanged.subscribe((e, args) => {
            this.$timeout.cancel(this.changeActiveTimeout);
            this.changeActiveTimeout = this.$timeout(() => {
                let columnMetadata = this.state.playground.grid.selectedColumn;
                if (angular.isDefined(args.cell)) {
                    const column = this.grid.getColumns()[args.cell];
                    columnMetadata = column && column.tdpColMetadata;
                }
                this.StateService.setGridSelection(columnMetadata, args.row);
            });
        });

        this.grid.onHeaderContextMenu.subscribe((e, args) => {
            this.$timeout(() => this.StateService.setGridSelection(args.column.tdpColMetadata, null));
        });

        this.grid.onHeaderClick.subscribe((e, args) => {
            this.$timeout(() => this.StateService.setGridSelection(args.column.tdpColMetadata, null));
        });
    }

    /**
     * @ngdoc method
     * @name navigateToFocusedColumn
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description navigates between columns
     */
    navigateToFocusedColumn() {
        if (this.DatagridService.focusedColumn) {
            const columnIndex = _.findIndex(this.grid.getColumns(), {id: this.DatagridService.focusedColumn});
            const renderedRows = this.grid.getRenderedRange();
            const centerRow = +((renderedRows.bottom - renderedRows.top) / 2).toFixed(0);
            this.grid.scrollCellIntoView(renderedRows.top + centerRow, columnIndex, false);
        }
    }

    /**
     * @ngdoc method
     * @name initGridServices
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description Init other grid services with the new created grid
     */
    _initGridServices() {
        _.forEach(this.gridServices, (service) => {
            service.init(this.grid);
        });
    }

    /**
     * @ngdoc method
     * @name initGrid
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description Create Slick grid and initiate other datagrid services
     The dataview is initiated and held by {@link data-prep.services.playground.service:DatagridService DatagridService}
     * @param {string} elementId The element where the grid will be inserted in the DOM. The element must exists
     */
    initGrid(elementId) {
        //create grid
        const options = {
            autoEdit: false,
            editable: true,
            enableAddRow: false,
            enableCellNavigation: true,
            enableTextSelectionOnCells: false,
            syncColumnCellResize: false,
            frozenColumn: 0,
            asyncEditorLoading: true,
            asyncEditorLoadDelay: 150
        };
        this.grid = new Slick.Grid(elementId, this.state.playground.grid.dataView, [{id: 'tdpId'}], options);

        //listeners
        this._attachLongTableListeners();
        this._attachGridStateListeners();

        //init other services
        this._initGridServices();
        return this.grid;
    }
}