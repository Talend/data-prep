/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import SingleColumnResizePlugin from '../plugins/single-column-resize-plugin';
import { COLUMN_INDEX_ID } from './datagrid-column-service';

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

	constructor($timeout, state, StateService,
        DatagridService, DatagridStyleService, DatagridColumnService,
        DatagridSizeService, DatagridExternalService, DatagridTooltipService) {
		'ngInject';

		this.grid = null;
		this.changeActiveTimeout = null;

		this.$timeout = $timeout;
		this.state = state;
		this.StateService = StateService;
		this.DatagridService = DatagridService;
		this.DatagridColumnService = DatagridColumnService;

		this.gridServices = [
			DatagridColumnService,
			DatagridStyleService,
			DatagridSizeService,
			DatagridExternalService,
			DatagridTooltipService,
		];
	}

    /**
     * @ngdoc method
     * @name attachLongTableListeners
     * @methodOf data-prep.datagrid.service:DatagridGridService
     * @description Attaches listeners for data update to reRender the grid
     */
	attachLongTableListeners(grid) {
		grid.dataView.onRowCountChanged.subscribe(() => {
			this.grid.updateRowCount();
			this.grid.render();
		});
		grid.dataView.onRowsChanged.subscribe((e, args) => {
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
				if (angular.isDefined(args.cell)) {
					const column = this.grid.getColumns()[args.cell];
					const columnMetadata = column && column.tdpColMetadata ? [column.tdpColMetadata] : [];
					this.StateService.setGridSelection(columnMetadata, args.row);
				}
			});
		});

		this.grid.onHeaderContextMenu.subscribe((e, args) => {
			if (args.column.id === COLUMN_INDEX_ID) {
				return;
			}
			this.$timeout(() => this.StateService.setGridSelection([args.column.tdpColMetadata], null));
		});

		this.grid.onHeaderClick.subscribe((e, args) => {
			this.$timeout(() => {
                // multi selection disabled in lookup mode
				const multiSelectionEnabled = !this.state.playground.lookup.visibility;
				const column = args.column && args.column.tdpColMetadata;
				if (!column) {
					return;
				}

				if (multiSelectionEnabled && (e.ctrlKey || e.metaKey)) {
					this.StateService.toggleColumnSelection(column);
				}
				else if (multiSelectionEnabled && e.shiftKey) {
					this.StateService.changeRangeSelection(column);
				}
				else {
					this.StateService.setGridSelection([column]);
				}
			});
		});

		this.grid.onColumnsReordered.subscribe((e, args) => {
			this.$timeout(() => this.DatagridColumnService.columnsOrderChanged(args.grid.getColumns()));
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
			const columnIndex = _.findIndex(this.grid.getColumns(), { id: this.DatagridService.focusedColumn });
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
			service.init(this.grid, this.state.playground.grid);
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
        // create grid
		const options = {
			autoEdit: false,
			editable: !this.state.playground.isReadOnly,
			enableAddRow: false,
			enableCellNavigation: true,
			enableColumnReorder: !this.state.playground.isReadOnly,
			enableTextSelectionOnCells: false,
			syncColumnCellResize: false,
			frozenColumn: 0,
			asyncEditorLoading: true,
			asyncEditorLoadDelay: 150,
		};
		this.grid = new Slick.Grid(elementId, this.state.playground.grid.dataView, [{ id: 'tdpId' }], options);
		this.grid.registerPlugin(new Slick.AutoColumnSize());
		SingleColumnResizePlugin.patch(this.grid);

        // listeners
		this.attachLongTableListeners(this.state.playground.grid);
		this._attachGridStateListeners();

        // init other services
		this._initGridServices();
		return this.grid;
	}
}
