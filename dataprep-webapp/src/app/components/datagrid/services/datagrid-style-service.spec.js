/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid style service', () => {
    'use strict';

    var gridMock, gridColumns, stateMock;

    function assertColumnsHasNoStyles() {
        gridColumns.forEach(function (column) {
            expect(column.cssClass).toBeFalsy();
        });
    }

    beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
        stateMock = {playground: {grid: {}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(() => {
        gridColumns = [
            {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0', type: 'string'}},
            {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1', type: 'integer'}},
            {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2', type: 'string'}},
            {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3', type: 'string'}},
            {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4', type: 'string'}},
            {id: 'tdpId', field: 'tdpId', tdpColMetadata: {id: 'tdpId', name: '#'}}
        ];

        gridMock = new SlickGridMock();
        gridMock.initColumnsMock(gridColumns);

        spyOn(gridMock.onClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();
        spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
        spyOn(gridMock, 'resetActiveCell').and.returnValue();
        spyOn(gridMock, 'invalidate').and.returnValue();
    });

    describe('reset cell styles', () => {
        it('should reset cell styles configuration', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            DatagridStyleService.hightlightedColumnId = '0000';
            DatagridStyleService.hightlightedContent = 'toto';

            //when
            DatagridStyleService.resetCellStyles();

            //then
            expect(DatagridStyleService.hightlightedColumnId).toBeFalsy();
            expect(DatagridStyleService.hightlightedContent).toBeFalsy();
        }));

        it('should reset grid active cell', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);

            //when
            DatagridStyleService.resetCellStyles();

            //then
            expect(gridMock.resetActiveCell).toHaveBeenCalled();
        }));
    });

    describe('highlight cells', () => {
        it('should set highlight configuration', inject((DatagridStyleService) => {
            //given
            var colId = '0000';
            var content = 'toto';

            DatagridStyleService.init(gridMock);
            expect(DatagridStyleService.hightlightedColumnId).toBeFalsy();
            expect(DatagridStyleService.hightlightedContent).toBeFalsy();

            //when
            DatagridStyleService.highlightCellsContaining(colId, content);

            //then
            expect(DatagridStyleService.hightlightedColumnId).toBe(colId);
            expect(DatagridStyleService.hightlightedContent).toBe(content);
        }));
    });

    describe('update column styles', () => {
        it('should set "selected" class on active cell column when this is NOT a preview', inject((DatagridStyleService) => {
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

        it('should set "number" class on number column', inject((DatagridStyleService) => {
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

    describe('reset style', () => {
        it('should reset cell styles', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            DatagridStyleService.hightlightedColumnId = '0000';
            DatagridStyleService.hightlightedContent = 'toto';

            //when
            DatagridStyleService.resetStyles(gridColumns[1].id);

            //then
            expect(DatagridStyleService.hightlightedColumnId).toBeFalsy();
            expect(DatagridStyleService.hightlightedContent).toBeFalsy();
        }));

        it('should update column styles', inject((DatagridStyleService) => {
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

    describe('column formatter', () => {
        it('should adapt value into html with leading/trailing spaces management', inject((DatagridStyleService) => {
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
            expect(result.indexOf('<span class="hiddenChars">  </span>my value<span class="hiddenChars">     </span>') > 0).toBe(true);
        }));

        it('should add a space " " as value on empty cell', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            var col = {quality: {invalidValues: []}};
            var value = '';
            var columnDef = gridColumns[1];

            //when
            var formatter = DatagridStyleService.columnFormatter(col);
            var result = formatter(null, null, value, columnDef, {});

            //then
            expect(result.indexOf('<div class=""> </div>')).toBe(0);
        }));

        describe('indicator', () => {
            it('should add invisible rectangle on valid value', inject((DatagridStyleService) => {
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

            it('should add red rectangle on invalid value', inject((DatagridStyleService) => {
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
        });

        describe('new', () => {
            it('should add "new" class on a new row', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                var col = {quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {__tdpRowDiff: 'new'};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class=" cellNewValue">my value</div><div class="invisible-rect"></div>');
            }));

            it('should add "new" class on a new cell', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                var col = {quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {__tdpDiff: {'0001': 'new'}};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class=" cellNewValue">my value</div><div class="invisible-rect"></div>');
            }));
        });

        describe('update', () => {
            it('should add "update" class on an updated cell', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                var col = {quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {__tdpDiff: {'0001': 'update'}};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class=" cellUpdateValue">my value</div><div class="invisible-rect"></div>');
            }));
        });

        describe('deleted', () => {
            it('should add "deleted" class on deleted row', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                var col = {quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {__tdpRowDiff: 'delete'};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class=" cellDeletedValue">my value</div><div class="invisible-rect"></div>');
            }));

            it('should add "delete" class on a deleted cell', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                var col = {quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {__tdpDiff: {'0001': 'delete'}};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class=" cellDeletedValue">my value</div><div class="invisible-rect"></div>');
            }));
        });

        describe('highlight', () => {
            it('should add "highlight" class', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                DatagridStyleService.hightlightedColumnId = '0001';
                DatagridStyleService.hightlightedContent = 'my value';

                var col = {id: '0001', quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class="highlight">my value</div><div class="invisible-rect"></div>');
            }));

            it('should NOT add "highlight" class if the column is not the target', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                DatagridStyleService.hightlightedColumnId = '0000';
                DatagridStyleService.hightlightedContent = 'my value';

                var col = {id: '0001', quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class="">my value</div><div class="invisible-rect"></div>');
            }));

            it('should NOT add "highlight" class if content does not match', inject((DatagridStyleService) => {
                //given
                DatagridStyleService.init(gridMock);
                DatagridStyleService.hightlightedColumnId = '0001';
                DatagridStyleService.hightlightedContent = 'my specific value';

                var col = {id: '0001', quality: {invalidValues: []}};
                var value = 'my value';
                var columnDef = gridColumns[1];
                var dataContext = {};

                //when
                var formatter = DatagridStyleService.columnFormatter(col);
                var result = formatter(null, null, value, columnDef, dataContext);

                //then
                expect(result).toBe('<div class="">my value</div><div class="invisible-rect"></div>');
            }));
        });
    });

    describe('column preview style', () => {
        it('should return "new" column style', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'new'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('newColumn');
        }));

        it('should return "deleted" column style', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'delete'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('deletedColumn');
        }));

        it('should return "updated" column style', inject((DatagridStyleService) => {
            //given
            DatagridStyleService.init(gridMock);
            var col = {__tdpColumnDiff: 'update'};

            //when
            var diffClass = DatagridStyleService.getColumnPreviewStyle(col);

            //then
            expect(diffClass).toBe('updatedColumn');
        }));

        it('should return empty string on no change diff', inject((DatagridStyleService) => {
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
