/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

function SlickGridMock () {
    return {
        //mock functions
        initCellMock: function(cell, box) {
            //cell : the SlickGrid cell infos. Ex: {row: 1, cell: 2}
            //box : the SlickGrid cell box. Ex: {left: 300, right: 500, top: 10, bottom: 40}
            this.cell = cell;
            this.box = box;
        },
        initColumnsMock: function(columns) {
            this.columns = columns;
        },
        initRenderedRangeMock: function(range) {
            this.range = range;
        },
        initActiveCellMock: function(activeCell) {
            this.activeCell = activeCell;
        },
        cssStyleConfig: {},

        //slickGrid events
        onActiveCellChanged: {subscribe: function() {}},
        onBeforeHeaderCellDestroy: {subscribe: function() {}},
        onClick: {subscribe: function() {}},
        onColumnsReordered: {subscribe: function() {}},
        onColumnsResized: {subscribe: function() {}},
        onHeaderCellRendered: {subscribe: function() {}},
        onHeaderClick: {subscribe: function() {}},
        onMouseEnter: {subscribe: function() {}},
        onMouseLeave: {subscribe: function() {}},
        onScroll: {subscribe: function() {}},
        onHeaderContextMenu: {subscribe: function() {}},

        //slickGrid functions
        autosizeColumns: function() {},
        invalidate: function() {},
        invalidateRows: function() {},
        getActiveCell: function() {return this.activeCell;},
        getColumns: function() {return this.columns;},
        getCellFromEvent: function() {return this.cell;},
        getCellNodeBox: function() {return this.box;},
        getRenderedRange: function() {return this.range;},
        render: function() {},
        resetActiveCell: function() {},
        resizeCanvas: function() {},
        scrollCellIntoView: function() {},
        setColumns: function(columns) {this.columns = columns;},
        setCellCssStyles: function(cssStyle, config) {this.cssStyleConfig[cssStyle] = config;},
        scrollRowToTop: function() {},
        updateRowCount: function() {}
    };
}
