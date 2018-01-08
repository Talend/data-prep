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
 * @name data-prep.lookup.service:DatagridGridService
 * @description Datagrid private service that init the grid
 * @requires data-prep.state.service:StateService
 * @requires data-prep.state.constant:state
 * @requires data-prep.lookup.service:LookupDatagridStyleService
 * @requires data-prep.lookup.service:LookupDatagridColumnService
 */
export default function LookupDatagridGridService($timeout, $window, state, DatagridGridService, DatagridTooltipService, StateService, LookupDatagridStyleService, LookupDatagridColumnService) {
	'ngInject';

	let grid = null;
	let lastSelectedColumn;
	const gridServices = [
		DatagridTooltipService,
		LookupDatagridColumnService,
		LookupDatagridStyleService,
	];

	return {
		initGrid,
	};

	/**
	 * @ngdoc method
	 * @name updateSelectedLookupColumn
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @param {Object} column The selected column
	 * @description Set the selected column into state services
	 */
	function updateSelectedLookupColumn(column) {
		const columnHasChanged = column.tdpColMetadata !== lastSelectedColumn;
		if (!columnHasChanged) {
			return;
		}

		lastSelectedColumn = column.tdpColMetadata;
		$timeout(function () {
			// if the selected column is the index col: column.tdpColMetadata === undefined
			StateService.setLookupSelectedColumn(column.tdpColMetadata);
		});
	}

	/**
	 * @ngdoc method
	 * @name attachCellListeners
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @description Attach listeners for saving the state of column id
	 */
	function attachCellListeners() {
		grid.onActiveCellChanged.subscribe(function (e, args) {
			if (angular.isDefined(args.cell)) {
				const column = grid.getColumns()[args.cell];
				updateSelectedLookupColumn(column);
			}
		});
	}

	/**
	 * @ngdoc method
	 * @name attachColumnListeners
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @description Attach header selection listeners on right click or left click
	 */
	function attachColumnListeners() {
		function attachColumnCallback(args) {
			const columnId = args.column.id;
			const column = _.find(grid.getColumns(), { id: columnId });
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
	 * @name initGridServices
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @description Init other grid services with the new created grid
	 */
	function initGridServices() {
		_.forEach(gridServices, function (service) {
			service.init(grid, state.playground.lookup);
		});
	}

	/**
	 * @ngdoc method
	 * @name attachGridResizeListener
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @description Attach listeners on window resize
	 */
	function attachGridResizeListener() {
		$window.addEventListener('resize', function () {
			grid.resizeCanvas();
		}, true);
	}

	/**
	 * @ngdoc method
	 * @name initGrid
	 * @methodOf data-prep.lookup.service:LookupDatagridGridService
	 * @description Create Slick grid and initiate other lookup-datagrid services
	 * @param {string} elementId The element where the grid will be inserted in the DOM. The element must exists
	 */
	function initGrid(elementId) {
		// create grid
		const options = {
			autoEdit: false,
			editable: false,
			enableAddRow: false,
			enableCellNavigation: true,
			enableTextSelectionOnCells: false,
			syncColumnCellResize: false,
			frozenColumn: 0,
			forceFitColumns: true,
		};
		grid = new Slick.Grid(elementId, state.playground.lookup.dataView, [{ id: 'tdpId' }], options);
		grid.registerPlugin(new Slick.AutoColumnSize());

		// listeners
		DatagridGridService.attachLongTableListeners(state.playground.lookup);
		attachCellListeners();
		attachColumnListeners();
		attachGridResizeListener();

		// init other services
		initGridServices();

		return grid;
	}
}
