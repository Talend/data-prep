/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import { INDEX_COLUMN_WIDTH, INITIAL_COLUMN_WIDTH } from './datagrid-column-service';

/**
 * @ngdoc service
 * @name data-prep.datagrid.service:DatagridSizeService
 * @description Datagrid private service that manage the grid sizes
 */
export default function DatagridSizeService($window, state) {
	'ngInject';

	let grid;

	return {
		init,
		autosizeColumns,
	};

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name getLocalStorageKey
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @description Get the actual dataset column sizes key. This key is used in localStorage
     */
	function getLocalStorageKey() {
		return 'org.talend.dataprep.col_size_' + state.playground.dataset.id;
	}

    /**
     * @ngdoc method
     * @name autosizeColumns
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @description Compute columns sizes and update them in the grid. The sizes are saved in
     * localstorage if not already saved. They are then used to set the last saved sized.
     * WARNING : this set columns in the grid, which trigger a repaint
     */
	function autosizeColumns(gridColumns) {
		const localKey = getLocalStorageKey();
		const sizesStr = $window.localStorage.getItem(localKey);
		const sizes = (sizesStr && JSON.parse(sizesStr)) || {};
		_.forEach(gridColumns, function (col) {
			col.minWidth = INDEX_COLUMN_WIDTH;
			col.width = sizes[col.id] || INITIAL_COLUMN_WIDTH;
		});

		grid.setColumns(gridColumns);
		saveColumnSizes();
	}

    /**
     * @ngdoc method
     * @name saveColumnSizes
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @description Save the columns sizes of the dataset in localstorage
     */
	function saveColumnSizes() {
		const localKey = getLocalStorageKey();
		const sizes = {};

		_.forEach(grid.getColumns(), function (col) {
			sizes[col.id] = col.width;
		});

		$window.localStorage.setItem(localKey, JSON.stringify(sizes));
	}

    /**
     * @ngdoc method
     * @name attachGridResizeListener
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @description Attach listeners on window resize
     */
	function attachGridResizeListener() {
		$window.addEventListener('resize', function () {
			grid.resizeCanvas();
		}, true);
	}

    /**
     * @ngdoc method
     * @name attachColumnResizeListener
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @description Attach listeners for column resize
     */
	function attachColumnResizeListener() {
		grid.onColumnsResized.subscribe(saveColumnSizes);
	}

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.datagrid.service:DatagridSizeService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the column listeners
     */
	function init(newGrid) {
		grid = newGrid;
		attachGridResizeListener();
		attachColumnResizeListener();
	}
}
