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
 * @name data-prep.datagrid.service:DatagridStyleService
 * @description Datagrid private service that manage the grid style
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default class DatagridStyleService {
	constructor(ConverterService, TextFormatService, $translate) {
		'ngInject';

		this.grid = null;
		this.hightlightedColumnId = null;
		this.hightlightedContent = null;

		this.ConverterService = ConverterService;
		this.TextFormatService = TextFormatService;

		this.invalidTitle = $translate.instant('INVALIDVALUE');
	}

	/**
	 * @ngdoc method
	 * @name resetCellStyles
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Reset the cells css
	 */
	resetHighlightStyles() {
		this.hightlightedColumnId = null;
		this.hightlightedContent = null;
	}

	/**
	 * @ngdoc method
	 * @name resetCellStyles
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Reset the cells css
	 */
	resetCellStyles() {
		this.grid.resetActiveCell();
		this.resetHighlightStyles();
	}

	/**
	 * @ngdoc method
	 * @name addClass
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Add a css class to a column
	 * @param {object} column The target column
	 * @param {string} newClass The class to add
	 */
	addClass(column, newClass) {
		column.cssClass = (column.cssClass || '') + ' ' + newClass;
	}

	/**
	 * @ngdoc method
	 * @name updateSelectionsClass
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Add 'selected' class if the column is the selected one
	 * @param {object} column The target column
	 * @param {array} selectedCols The selected columns
	 */
	updateSelectionsClass(column, selectedCols) {
		if ((selectedCols.length === 1 && column.id === selectedCols[0].id) ||
			(selectedCols.length > 1 && _.find(selectedCols, { id: column.id }))) {
			this.addClass(column, 'selected');
			column.headerCssClass = 'selected';
		}
		else {
			column.headerCssClass = null;
		}
	}

	/**
	 * @ngdoc method
	 * @name updateNumbersClass
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Add the 'number' class to the column if its type is a number type
	 * @param {object} column the target column
	 */
	updateNumbersClass(column) {
		const simplifiedType = this.ConverterService.simplifyType(column.tdpColMetadata.type);
		if (simplifiedType === 'integer' || simplifiedType === 'decimal') {
			this.addClass(column, 'numbers');
		}
	}

	/**
	 * @ngdoc method
	 * @name updateColumnsClass
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Set style classes on columns depending on its state (type, selection, ...)
	 * @param {array} selectedCols The grid selected columns
	 */
	updateColumnsClass(selectedCols) {
		const columns = this.grid.getColumns();
		columns.forEach((column) => {
			if (column.id === 'tdpId') {
				column.cssClass = 'index-column';
			}
			else {
				column.cssClass = null;
				this.updateSelectionsClass(column, selectedCols);
				this.updateNumbersClass(column);
			}
		});
		this.grid.setColumns(columns);
	}

	/**
	 * @ngdoc method
	 * @name columnFormatter
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Value formatter used in SlickGrid column definition. This is called to get a cell formatted value
	 * @param {object} col The column to format
	 */
	columnFormatter(col) {
		function formatter(row, cell, value, columnDef, dataContext) {
			let classNames = (col.id === this.hightlightedColumnId) && (value === this.hightlightedContent) ?
				'highlight' :
				'';

			// hidden characters need to be shown
			const returnStr = this.TextFormatService.adaptToGridConstraints(value);

			// entire row modification preview
			switch (dataContext.__tdpRowDiff) { // eslint-disable-line no-underscore-dangle
			case 'delete':
				classNames += ' cellDeletedValue';
				break;
			case 'new':
				classNames += ' cellNewValue';
				break;
			}

			// cell modification preview
			if (dataContext.__tdpDiff && dataContext.__tdpDiff[columnDef.id]) { // eslint-disable-line no-underscore-dangle
				switch (dataContext.__tdpDiff[columnDef.id]) { // eslint-disable-line no-underscore-dangle
				case 'update':
					classNames += ' cellUpdateValue';
					break;
				case 'new':
					classNames += ' cellNewValue';
					break;
				case 'delete':
					classNames += ' cellDeletedValue';
					break;
				}
			}

			const formattedValue = `<div class="${classNames}">${returnStr}</div>`;
			const isInvalid = dataContext.__tdpInvalid && dataContext.__tdpInvalid.indexOf(columnDef.id) > -1;
			const indicator = isInvalid ?
				`<div title="${this.invalidTitle}" class="red-rect"></div>` :
				'<div class="invisible-rect"></div>';

			return formattedValue + indicator;
		}

		return formatter.bind(this);
	}

	/**
	 * @ngdoc method
	 * @name getColumnPreviewStyle
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Get the column header preview style
	 * @param {object} col The column metadata
	 */
	getColumnPreviewStyle(col) {
		switch (col.__tdpColumnDiff) { // eslint-disable-line no-underscore-dangle
		case 'new':
			return 'newColumn';
		case 'delete':
			return 'deletedColumn';
		case 'update':
			return 'updatedColumn';
		default:
			return '';
		}
	}

	/**
	 * @ngdoc method
	 * @name resetStyles
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @description Reset the cell styles and update the columns style
	 * @param {array} selectedCols The selected columns
	 */
	resetStyles(selectedCols) {
		this.resetCellStyles();
		this.updateColumnsClass(selectedCols);
	}

	/**
	 * @ngdoc method
	 * @name highlightCellsContaining
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @param {String} colId The column id
	 * @param {String} content The value to highlight
	 * @description Highlight the cells in column that contains the same value as content
	 */
	highlightCellsContaining(colId, content) {
		this.hightlightedColumnId = colId;
		this.hightlightedContent = content;

		const hasCellEditor = this.grid.getCellEditor();
		if (hasCellEditor) {
			const activeLine = this.grid.getActiveCell().row;
			const range = this.grid.getRenderedRange();
			const indexes = _.range(range.top, range.bottom + 1).filter(index => index !== activeLine);

			this.grid.invalidateRows(indexes);
			this.grid.render();
		}
		else {
			this.grid.invalidate();
		}
	}

	/**
	 * @ngdoc method
	 * @name init
	 * @methodOf data-prep.datagrid.service:DatagridStyleService
	 * @param {object} newGrid The new grid
	 * @description Initialize the grid and attach the style listeners
	 */
	init(newGrid) {
		this.grid = newGrid;
	}
}
