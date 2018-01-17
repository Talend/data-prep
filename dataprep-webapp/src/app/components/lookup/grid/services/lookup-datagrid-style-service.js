/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.lookup.service:LookupDatagridStyleService
 * @description Datagrid private service that manage the grid style
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function LookupDatagridStyleService($timeout, ConverterService, DatagridStyleService, TextFormatService, $translate) {
	'ngInject';

	let grid;
	let columnClassTimeout;
	const invalidTitle = $translate.instant('INVALIDVALUE');

	return {
		init,
		updateColumnClass,
		columnFormatter,
	};

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name resetCellStyles
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Reset the cells css
     */
	function resetCellStyles() {
		grid.resetActiveCell();
	}

    /**
     * @ngdoc method
     * @name updateSelectionClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Add 'selected' class if the column is the selected one
     * @param {object} column The target column
     * @param {object} selectedCol The selected column
     */
	function updateSelectionClass(column, selectedCol) {
		if (column === selectedCol) {
			DatagridStyleService.addClass(column, 'selected');
		}
	}

    /**
     * @ngdoc method
     * @name updateColumnClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Set style classes on columns depending on its state (type, selection, ...)
     * @param {object} columns The columns array
     * @param {object} selectedCol The grid selected column
     */
	function updateColumnClass(columns, selectedCol) {
		_.forEach(columns, function (column) {
			if (column.id === 'tdpId') {
				column.cssClass = 'index-column';
			}
			else {
				column.cssClass = null;
				updateSelectionClass(column, selectedCol);
				DatagridStyleService.updateNumbersClass(column);
			}
		});
	}

    /**
     * @ngdoc method
     * @name columnFormatter
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Value formatter used in SlickGrid column definition. This is called to get a cell formatted value
     * @param {object} col The column to format
     */
	function columnFormatter(col) {
		const invalidValues = col.quality.invalidValues;
		const isInvalid = function isInvalid(value) {
			return invalidValues.indexOf(value) >= 0;
		};

		return function formatter(row, cell, value) {
            // hidden characters need to be shown
			const returnStr = TextFormatService.adaptToGridConstraints(value);
			if (isInvalid(value)) {
				return `${returnStr}<div title="${invalidTitle}" class="red-rect"></div>`;
			}
			return `${returnStr}<div class="invisible-rect"></div>`;
		};
	}

    /**
     * @ngdoc method
     * @name attachColumnHeaderCallback
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description attachColumnHeaderListeners callback
     */
	function attachColumnHeaderCallback(event, args) {
		resetCellStyles();
		updateColumnClass(grid.getColumns(), args.column);
		grid.invalidate();
	}

    /**
     * @ngdoc method
     * @name attachColumnHeaderListeners
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Attach style listener on headers. On header selection (on right click or left click) we update the column cells style
     */
	function attachColumnHeaderListeners() {
		grid.onHeaderContextMenu.subscribe(attachColumnHeaderCallback);
		grid.onHeaderClick.subscribe(attachColumnHeaderCallback);
	}

    /**
     * @ngdoc method
     * @name scheduleUpdateColumnClass
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @param {number} colIndex The selected column index
     * @description Cancel the previous scheduled task and schedule a new one to update columns classes.
     */
	function scheduleUpdateColumnClass(colIndex) {
		const columns = grid.getColumns();
		const column = columns[colIndex];

		$timeout.cancel(columnClassTimeout);
		columnClassTimeout = $timeout(function () {
			updateColumnClass(columns, column);
			grid.invalidate();
		}, 100, false);
	}

    /**
     * @ngdoc method
     * @name attachCellListeners
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @description Attach cell action listeners : update columns classes
     */
	function attachCellListeners() {
		grid.onActiveCellChanged.subscribe(function (e, args) {
			if (angular.isDefined(args.cell)) {
				scheduleUpdateColumnClass(args.cell);
			}
		});
	}

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.lookup.service:LookupDatagridStyleService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the style listeners
     */
	function init(newGrid) {
		grid = newGrid;
		attachColumnHeaderListeners();
		attachCellListeners();
	}
}
