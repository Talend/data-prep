/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

function SlickGridMock() {
	return {
        // mock functions
		initCellMock(cell, box) {
            // cell : the SlickGrid cell infos. Ex: {row: 1, cell: 2}
            // box : the SlickGrid cell box. Ex: {left: 300, right: 500, top: 10, bottom: 40}
			this.cell = cell;
			this.box = box;
		},
		initColumnsMock(columns) {
			this.columns = columns;
		},
		initRenderedRangeMock(range) {
			this.range = range;
		},
		initActiveCellMock(activeCell) {
			this.activeCell = activeCell;
		},
		initCellEditorMock(editor) {
			this.cellEditor = editor;
		},
		cssStyleConfig: {},

        // slickGrid events
		onActiveCellChanged: { subscribe() {} },
		onBeforeHeaderCellDestroy: { subscribe() {} },
		onClick: { subscribe() {} },
		onColumnsReordered: { subscribe() {} },
		onColumnsResized: { subscribe() {} },
		onHeaderCellRendered: { subscribe() {} },
		onHeaderClick: { subscribe() {} },
		onMouseEnter: { subscribe() {} },
		onMouseLeave: { subscribe() {} },
		onScroll: { subscribe() {} },
		onHeaderContextMenu: { subscribe() {} },

        // slickGrid functions
		autosizeColumns() {},
		invalidate() {},
		invalidateRows() {},
		getActiveCell() {
			return this.activeCell;
		},
		getColumns() {
			return this.columns;
		},
		getCellEditor() {
			return this.cellEditor;
		},
		getCellFromEvent() {
			return this.cell;
		},
		getCellNodeBox() {
			return this.box;
		},
		getRenderedRange() {
			return this.range;
		},
		render() {},
		resetActiveCell() {},
		resizeCanvas() {},
		scrollCellIntoView() {},
		setColumns(columns) {
			this.columns = columns;
		},
		setCellCssStyles(cssStyle, config) {
			this.cssStyleConfig[cssStyle] = config;
		},
		scrollRowToTop() {},
		updateRowCount() {},
		registerPlugin() {},
	};
}

module.exports = SlickGridMock;
