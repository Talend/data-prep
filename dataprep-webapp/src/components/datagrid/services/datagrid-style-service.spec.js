describe('Datagrid style service', function () {
    'use strict';

    var gridMock, gridColumns, stateMock;

    function assertColumnsHasNoStyles() {
        gridColumns.forEach(function(column) {
            expect(column.cssClass).toBeFalsy();
        });
    }

    beforeEach(module('data-prep.datagrid', function ($provide) {
        stateMock = {playground: {grid: {}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(function () {
        jasmine.clock().install();
        gridColumns = [
            {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0', type: 'string'}},
            {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1', type: 'integer'}},
            {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2', type: 'string'}},
            {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3', type: 'string'}},
            {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4', type: 'string'}},
            {id: 'tdpId', field: 'tdpId', tdpColMetadata: {id: 'tdpId', name: '#'}}
        ];

        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();
        gridMock.initColumnsMock(gridColumns);

        spyOn(gridMock.onClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();
        spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
        spyOn(gridMock, 'resetActiveCell').and.returnValue();
        spyOn(gridMock, 'invalidate').and.returnValue();
    });

    afterEach(function() {
        jasmine.clock().uninstall();
    });

    describe('reset cell styles', function() {
        it('should reset cell styles configuration', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            gridMock.setCellCssStyles('highlight', {'2': {'0000': 'highlight'}});

            //when
            DatagridStyleService.resetCellStyles();

            //then
            expect(gridMock.cssStyleConfig.highlight).toEqual({});
        }));

        it('should reset grid active cell', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);

            //when
            DatagridStyleService.resetCellStyles();

            //then
            expect(gridMock.resetActiveCell).toHaveBeenCalled();
        }));
    });

    describe('highlight cells', function() {
        it('should set highlight configuration', inject(function (DatagridService, DatagridStyleService) {
            //given
            var colId = '0000';
            var content = 'toto';
            var highlightClass = 'highlight';
            var highLightConfig = {'2': {'0000': 'highlight'}};
            spyOn(DatagridService, 'getSameContentConfig').and.returnValue(highLightConfig);

            DatagridStyleService.init(gridMock);
            expect(gridMock.cssStyleConfig[highlightClass]).toBeFalsy();

            //when
            DatagridStyleService.highlightCellsContaining(colId, content);

            //then
            expect(DatagridService.getSameContentConfig).toHaveBeenCalledWith(colId, content, highlightClass);
            expect(gridMock.cssStyleConfig[highlightClass]).toBe(highLightConfig);
        }));
    });

    describe('update column styles', function() {
        it('should set "selected" class on active cell column when this is NOT a preview', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            assertColumnsHasNoStyles();
            var selectedColumnId = gridColumns[1].id;

            //when
            DatagridStyleService.updateColumnClass(selectedColumnId);

            //then
            expect(gridColumns[0].cssClass).toBeFalsy();
            expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
            expect(gridColumns[2].cssClass).toBeFalsy();
            expect(gridColumns[3].cssClass).toBeFalsy();
            expect(gridColumns[4].cssClass).toBeFalsy();
            expect(gridColumns[5].cssClass).toBe('index-column');
        }));

        it('should set "number" class on number column', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            assertColumnsHasNoStyles();

            //when
            DatagridStyleService.updateColumnClass();

            //then
            expect(gridColumns[0].cssClass).toBeFalsy();
            expect(gridColumns[1].cssClass.indexOf('number') > -1).toBe(true);
            expect(gridColumns[2].cssClass).toBeFalsy();
            expect(gridColumns[3].cssClass).toBeFalsy();
            expect(gridColumns[4].cssClass).toBeFalsy();
            expect(gridColumns[5].cssClass).toBe('index-column');
        }));
    });

    describe('reset style', function() {
        it('should reset cell styles', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            gridMock.setCellCssStyles('highlight', {'2': {'0000': 'highlight'}});

            //when
            DatagridStyleService.resetStyles(gridColumns[1].id);

            //then
            expect(gridMock.resetActiveCell).toHaveBeenCalled();
            expect(gridMock.cssStyleConfig.highlight).toEqual({});
        }));

        it('should update column styles', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            assertColumnsHasNoStyles();
            var selectedColumnId = gridColumns[1].id;

            //when
            DatagridStyleService.resetStyles(selectedColumnId);

            //then
            expect(gridColumns[0].cssClass).toBeFalsy();
            expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
            expect(gridColumns[1].cssClass.indexOf('number') > -1).toBe(true);
            expect(gridColumns[2].cssClass).toBeFalsy();
            expect(gridColumns[3].cssClass).toBeFalsy();
            expect(gridColumns[4].cssClass).toBeFalsy();
            expect(gridColumns[5].cssClass).toBe('index-column');
        }));
    });

    describe('column formatter', function() {
        it('should adapt value into html with leading/trailing spaces management', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = '  my value     ';
            var columnDef = gridColumns[1];
            var dataContext = {};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result.indexOf('<span class="hiddenChars">  </span>my value<span class="hiddenChars">     </span>')).toBe(0);
        }));

        it('should add invisible rectangle on valid value', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result.indexOf('<div class="invisible-rect"></div>') > 0).toBe(true);
        }));

        it('should add red rectangle on invalid value', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: ['my value']}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result.indexOf('<div title="Invalid Value" class="red-rect"></div>') > 0).toBe(true);
        }));

        it('should add red rectangle on invalid value case of non TEXT domains (ieemail address)', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: ['m&a>al<ej@talend']}};
            var value = 'm&a>al<ej@talend';
            var columnDef = gridColumns[1];
            var dataContext = {};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('m&amp;a&gt;al&lt;ej@talend<div title="Invalid Value" class="red-rect"></div>');
        }));

        it('should add "deleted" class on deleted row', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpRowDiff : 'delete'};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellDeletedValue">my value</div>');
        }));

        it('should add a space " " as value on empty cell in a deleted row', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = '';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpRowDiff : 'delete'};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellDeletedValue"> </div>');
        }));

        it('should add "new" class on a new row', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpRowDiff : 'new'};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellNewValue">my value</div>');
        }));

        it('should add a space " " as value on empty cell in a new row', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = '';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpRowDiff : 'new'};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellNewValue"> </div>');
        }));

        it('should add "update" class on an updated cell', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpDiff : {'0001': 'update'}};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellUpdateValue">my value</div>');
        }));

        it('should add "new" class on a new cell', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpDiff : {'0001': 'new'}};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellNewValue">my value</div>');
        }));

        it('should add "delete" class on a deleted cell', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = 'my value';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpDiff : {'0001': 'delete'}};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellDeletedValue">my value</div>');
        }));

        it('should add a space " " as value on empty deleted cell', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = '';
            var columnDef = gridColumns[1];
            var dataContext = {__tdpDiff : {'0001': 'delete'}};

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, dataContext);

            //then
            expect(result).toBe('<div class="cellDeletedValue"> </div>');
        }));
    });

    describe('column preview style', function() {
        it('should return "new" column style', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'new'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('newColumn');
        }));

        it('should return "deleted" column style', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'delete'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('deletedColumn');
        }));

        it('should return "updated" column style', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'update'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('updatedColumn');
        }));

        it('should return empty string on no change diff', inject(function (DatagridStyleService) {
            //given
            DatagridStyleService.init(gridMock);
            var col = {};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('');
        }));
    });
});
