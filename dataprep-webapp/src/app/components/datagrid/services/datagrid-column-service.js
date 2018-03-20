/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const COLUMN_INDEX_ID = 'tdpId';
export const INDEX_COLUMN_WIDTH = 60;
export const INITIAL_COLUMN_WIDTH = 120;

/**
 * @ngdoc service
 * @name data-prep.datagrid.service:DatagridColumnService
 * @description Datagrid private service that manage the grid columns and columns headers
 * LEXICON :
 * <ul>
 *     <li>Column : the slick grid column</li>
 *     <li>
 *         Header : the datagrid header directive.
 *         The header created by SlickGrid will be called by 'SlickGird Header'
 *     </li>
 * </ul>
 * @requires data-prep.datagrid.service:DatagridStyleService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.utils.service:ConverterService
 */
export default function DatagridColumnService($rootScope, $compile, $translate,
                                              PlaygroundService, DatagridStyleService, ConverterService) {
	'ngInject';

	let grid;
	let renewAllFlag;
	let availableHeaders = [];

	/**
	 * contains a backup of the columnsMetadata to find which has been moved after a reorder
	 * @type {Array}
	 */
	let originalColumns = [];

	const gridHeaderPreviewTemplate =
		'<div class="grid-header <%- diffClass %>">' +
		'   <div class="grid-header-title dropdown-button ng-binding"><%- name || "&nbsp;" %></div>' +
		'   <div class="grid-header-type ng-binding"><%- simpleType %></div>' +
		'</div>' +
		'<div class="quality-bar"><div class="record-unknown"></div></div>';

	return {
		init,
		renewAllColumns,
		createColumns,
		createHeader,
		columnsOrderChanged,
	};

	// --------------------------------------------------------------------------------------------
	// -----------------------------------------------GRID COLUMNS---------------------------------
	// --------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name createColumnDefinition
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object} col The column metadata to adapt
	 * @param {boolean} preview Flag that indicates if we are in the preview mode
	 * @description Adapt column metadata to slick column.
	 * <ul>
	 *     <li>
	 *         Non preview mode : The div id depending on index is important.
	 *         It is used as insertion point for the header directive.
	 *     </li>
	 *     <li>Preview mode : we inject directly a fake header</li>
	 * </ul>
	 * @returns {object} The adapted column item
	 */
	function createColumnDefinition(col, preview) {
		const template = preview ?
			_.template(gridHeaderPreviewTemplate)({
				name: col.name,
				diffClass: DatagridStyleService.getColumnPreviewStyle(col),
				simpleType: col.domainLabel || ConverterService.simplifyTypeLabel(col.type),
			}) :
			'';
		const translatedMsg = $translate.instant('APPLY_TO_ALL_CELLS');

		return {
			id: col.id,
			field: col.id,
			name: template,
			formatter: DatagridStyleService.columnFormatter(col),
			tdpColMetadata: col,
			minWidth: INITIAL_COLUMN_WIDTH,
			editor: preview ?
				null :
				Slick.Editors.TalendEditor( // eslint-disable-line new-cap
					PlaygroundService.editCell,
					translatedMsg
				),
			preview,
		};
	}

	/**
	 * @ngdoc method
	 * @name createColumns
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object[]} columnsMetadata Columns details
	 * @param {boolean} preview Flag that indicates if we are in preview mode
	 * @description Two modes :
	 * <ul>
	 *     <li>
	 *         Preview : we save actual headers
	 *         and simulate fake headers with the new preview columns
	 *     </li>
	 *     <li>
	 *         Classic : we map each column to a header.
	 *         This header can be a reused header if the column was the same as before,
	 *         or a new created one otherwise.
	 * </li>
	 * </ul>
	 */
	function createColumns(columnsMetadata, preview, colIndexNameTemplate) {
		// create new SlickGrid columns
		const colIndexArray = [];

		// Add index column
		colIndexArray.push({
			id: COLUMN_INDEX_ID,
			name: colIndexNameTemplate || '',
			field: COLUMN_INDEX_ID,
			maxWidth: INDEX_COLUMN_WIDTH,
			formatter: function formatterIndex(row, cell, value) {
				return '<div class="index-cell">' + value + '</div>';
			},

			resizable: false,
			selectable: false,
		});

		const columns = _.union(colIndexArray, _.map(columnsMetadata, function (col) {
			return createColumnDefinition(col, preview);
		}));

		originalColumns = columns;
		return columns;
	}

	/**
	 * @ngdoc method
	 * @name columnsOrderChanged
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object[]} columnsMetadata Columns details
	 * @param {object[]} originals the optional original columns
	 * if null the field will be used originalColumns
	 * @description method trigger on columns reorder
	 */
	function columnsOrderChanged(columnsMetadata, originals) {
		const original = originals || originalColumns;
		// the user started reordering but has abandoned his action at the end
		if (_.map(original, 'tdpColMetadata.id').join() === _.map(columnsMetadata, 'tdpColMetadata.id').join()) {
			return;
		}

		const result = findMovedCols(original, columnsMetadata);

		const params = {
			selected_column: result.target,
			scope: 'dataset',
			column_id: result.selected,
			column_name: result.name,
			dataset_action_display_type: 'column',
		};

		PlaygroundService.appendStep([{ action: 'reorder', parameters: params }]);
	}

	/**
	 * @ngdoc method
	 * @name findMovedCols
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param originalCols
	 * @param newCols
	 * @returns Object with fields :
	 *  <ul>
	 *    <li>selected: containing the column id to move</li>
	 *    <li>target: containing the column id where to move</li>
	 *    <li>name: the name of the moved field</li>
	 * @private
	 * @description find which columnMetadata has been moved between the two arrays
	 * during a reorder columns.
	 * We iterate on array and so some comparaisons.
	 */
	function findMovedCols(originalCols, newCols) {
		const result = {};
		let index = 0;
		let movedIndex = 0;
		let movedCol = null;
		_.forEach(originalCols, (col) => {
			if (!movedCol && col.id) {
				// move forward case
				if (col.id !== newCols[index].id &&
					originalCols[index + 1].id === newCols[index].id) {
					movedCol = col;
					movedIndex = index;
					// find new index of movedCol
					result.selected = movedCol.id;
					result.name = _.get(movedCol, 'tdpColMetadata.name');
					index = 0;
					_.forEach(newCols, (col) => {
						if (col.id && col.id === movedCol.id) {
							result.target = originalCols[index].id;
						}

						index++;
					});
					return result;
				}
				// move backward case
				else if (col.id !== newCols[index].id && col.id === newCols[index + 1].id) {
					movedCol = col;
					movedIndex = index;
					result.selected = newCols[movedIndex].id;
					result.name = _.get(newCols[movedIndex], 'tdpColMetadata.name');
					result.target = originalCols[movedIndex].id;
					return result;
				}
			}

			index++;
		});
		return result;
	}

	// --------------------------------------------------------------------------------------------
	// -----------------------------------------------GRID HEADERS---------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name destroyHeader
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object} headerDefinition The header definition
	 * that contains scope (the angular scope) and header (the element)
	 * @description Destroy the angular scope and header element
	 */
	function destroyHeader(headerDefinition) {
		headerDefinition.scope.$destroy();
		headerDefinition.header.remove();
	}

	/**
	 * @ngdoc method
	 * @name renewAllColumns
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {boolean} value The new flag value
	 * @description Set the 'renewAllFlag' with provided value to control
	 * whether the headers should be reused or recreated
	 */
	function renewAllColumns(value) {
		renewAllFlag = value;

		if (value) {
			_.forEach(availableHeaders, destroyHeader);
			availableHeaders = [];
		}
	}

	/**
	 * @ngdoc method
	 * @name createHeader
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @description [PRIVATE] Create a column header object containing
	 * <ul>
	 *     <li>the element directive</li>
	 *     <li>The directive scope</li>
	 *     <li>The column metadata</li>
	 * </ul>
	 */
	function createHeader(col, headerElement, headerScope) {
		$compile(headerElement)(headerScope);
		return {
			id: col.id,
			scope: headerScope,
			header: headerElement,
		};
	}

	/**
	 * @ngdoc method
	 * @name createIndexHeader
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @description [PRIVATE] Create the index column header object containing
	 * <ul>
	 *     <li>the element directive</li>
	 *     <li>The directive scope</li>
	 *     <li>The column metadata</li>
	 * </ul>
	 */
	function createIndexHeader() {
		const headerScope = $rootScope.$new(true);
		const headerElement = angular.element('<datagrid-index-header></datagrid-index-header>');
		$compile(headerElement)(headerScope);

		return {
			id: COLUMN_INDEX_ID,
			scope: headerScope,
			header: headerElement,
		};
	}

	/**
	 * @ngdoc method
	 * @name detachAndSaveHeader
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object} event The Slickgrid header destroy event
	 * @param {object} columnsArgs The column header arguments passed by SlickGrid
	 * @description This is part of the process to avoid recreation of the datagrid header
	 * when it is not necessary.
	 * It detach the element and save it with its scope, so it can be reused.
	 * If the 'renewAllFlag' is set to true, the headers are destroyed.
	 * So they are forced to be recreated.
	 */
	function detachAndSaveHeader(event, columnsArgs) {
		// No header to detach on preview
		const columnDef = columnsArgs.column;
		if (columnDef.preview) {
			return;
		}

		// Destroy the header if explicitly requested
		if (renewAllFlag) {
			destroyHeader(columnDef);
		}
		// Detach and save it otherwise
		else {
			const scope = columnDef.scope;
			const header = columnDef.header;

			if (scope && header) {
				header.detach();
				availableHeaders.push({
					id: columnDef.id,
					scope,
					header,
				});
			}
		}
	}

	/**
	 * @ngdoc method
	 * @name createAndAttachHeader
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object} event The Slickgrid header creation event
	 * @param {object} columnsArgs The column header arguments passed by SlickGrid
	 * @description This is part of the process to avoid recreation od the datagrid header
	 * when it is not necessary.
	 * It fetch an existing saved header to reuse it, or create it otherwise.
	 * The existing header is then updated with the new column metadata.
	 */
	function createAndAttachHeader(event, columnsArgs) {
		// No header to append on preview
		const columnDef = columnsArgs.column;
		if (columnDef.preview) {
			return;
		}
		const isIndexColumn = columnDef.id === COLUMN_INDEX_ID;

		// Get existing header and remove it from available headers list
		let headerDefinition = availableHeaders.find(header => header.id === columnDef.id);
		if (headerDefinition) {
			const headerIndex = availableHeaders.indexOf(headerDefinition);
			availableHeaders.splice(headerIndex, 1);
		}

		// Update column metadata in header if there is an available one
		if (headerDefinition) {
			if (!isIndexColumn) {
				headerDefinition.scope.column = columnDef.tdpColMetadata;
			}
		}
		// Create the header if no available created header
		else if (isIndexColumn) {
			headerDefinition = createIndexHeader();
		}
		else {
			const headerScope = $rootScope.$new(true);
			headerScope.column = columnDef.tdpColMetadata;
			const headerElement = angular.element('<datagrid-header column="column"></datagrid-header>');
			headerDefinition = createHeader(columnDef.tdpColMetadata, headerElement, headerScope);
		}

		// Update column definition
		columnDef.scope = headerDefinition.scope;
		columnDef.header = headerDefinition.header;

		// Append the header
		const node = angular.element(columnsArgs.node);
		node.append(headerDefinition.header);
	}

	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------INIT--------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name attachColumnHeaderEvents
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @description Attach listeners for header creation/destroy.
	 * The handler detach and save headers on destroy,
	 * attach (create them if necessary) and update them on render
	 */
	function attachColumnHeaderEvents() {
		grid.onBeforeHeaderCellDestroy.subscribe(detachAndSaveHeader);
		grid.onHeaderCellRendered.subscribe(createAndAttachHeader);
	}

	/**
	 * @ngdoc method
	 * @name init
	 * @methodOf data-prep.datagrid.service:DatagridColumnService
	 * @param {object} newGrid The new grid
	 * @description Initialize the grid and attach the column listeners
	 */
	function init(newGrid) {
		grid = newGrid;
		attachColumnHeaderEvents();
	}
}
