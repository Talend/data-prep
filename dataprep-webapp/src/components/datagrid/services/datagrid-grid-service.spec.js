/*global window:false */
/*global SlickGridMock:false */

describe('Datagrid grid service', function () {
    'use strict';

    var realSlickGrid = Slick;
    var dataViewMock, stateMock;

    beforeEach(module('data-prep.datagrid'));
    beforeEach(module('data-prep.suggestions-stats'));

    beforeEach(function () {
        dataViewMock = new DataViewMock();
    });


    beforeEach(module('data-prep.datagrid', function ($provide) {
        stateMock = {playground: {grid: {
            dataView: dataViewMock
        }}};
        $provide.constant('state', stateMock);

        spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
        spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
    }));

    beforeEach(inject(function (DatagridColumnService, DatagridStyleService, DatagridSizeService,
                                DatagridExternalService, DatagridTooltipService) {
        spyOn(DatagridColumnService, 'init').and.returnValue();
        spyOn(DatagridStyleService, 'init').and.returnValue();
        spyOn(DatagridSizeService, 'init').and.returnValue();
        spyOn(DatagridExternalService, 'init').and.returnValue();
        spyOn(DatagridTooltipService, 'init').and.returnValue();
    }));

    beforeEach(function () {
        window.Slick = {
            Grid: SlickGridMock
        };
    });

    afterEach(function () {
        window.Slick = realSlickGrid;
    });

    describe('on creation', function() {
        it('should init the other datagrid services', inject(function (DatagridGridService, DatagridColumnService,
                                                                       DatagridStyleService, DatagridSizeService,
                                                                       DatagridExternalService, DatagridTooltipService) {
            //when
            DatagridGridService.initGrid();

            //then
            expect(DatagridColumnService.init).toHaveBeenCalled();
            expect(DatagridStyleService.init).toHaveBeenCalled();
            expect(DatagridSizeService.init).toHaveBeenCalled();
            expect(DatagridExternalService.init).toHaveBeenCalled();
            expect(DatagridTooltipService.init).toHaveBeenCalled();
        }));

        it('should add grid listeners', inject(function (DatagridGridService) {
            //when
            DatagridGridService.initGrid();

            //then
            expect(stateMock.playground.grid.dataView.onRowCountChanged.subscribe).toHaveBeenCalled();
            expect(stateMock.playground.grid.dataView.onRowsChanged.subscribe).toHaveBeenCalled();
        }));
    });

    describe('grid handlers', function() {
        it('should update row count and render grid on row count change', inject(function (DatagridGridService) {
            //given
            var grid = DatagridGridService.initGrid();
            spyOn(grid, 'updateRowCount').and.returnValue();
            spyOn(grid, 'render').and.returnValue();

            //when
            var onRowCountChanged = stateMock.playground.grid.dataView.onRowCountChanged.subscribe.calls.argsFor(0)[0];
            onRowCountChanged();

            //then
            expect(grid.updateRowCount).toHaveBeenCalled();
            expect(grid.render).toHaveBeenCalled();
        }));

        it('should invalidate rows and render grid on rows changed', inject(function (DatagridGridService) {
            //given
            var grid = DatagridGridService.initGrid();
            spyOn(grid, 'invalidateRows').and.returnValue();
            spyOn(grid, 'render').and.returnValue();

            var args = {rows: []};

            //when
            var onRowsChanged = stateMock.playground.grid.dataView.onRowsChanged.subscribe.calls.argsFor(0)[0];
            onRowsChanged(null, args);

            //then
            expect(grid.invalidateRows).toHaveBeenCalledWith(args.rows);
            expect(grid.render).toHaveBeenCalled();
        }));
    });

    describe('column navigation for focus purposes', function() {
        it('should go to the selected column after', inject(function (DatagridStyleService, DatagridService, DatagridGridService) {
            //given
            var gridColumns = [
                {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0', type: 'string'}},
                {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1', type: 'integer'}},
                {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2', type: 'string'}},
                {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3', type: 'string'}},
                {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4', type: 'string'}}
            ];
            var grid = DatagridGridService.initGrid();

            grid.setColumns(gridColumns);
            DatagridService.focusedColumn = '0002';

            spyOn(grid, 'scrollCellIntoView').and.returnValue();
            spyOn(grid, 'getRenderedRange').and.returnValue({top:100, bottom:150});

            //when
            DatagridGridService.navigateToFocusedColumn();

            //then
            expect(grid.scrollCellIntoView).toHaveBeenCalledWith(125, 2, false);
        }));

        it('should do nothing when no column should be focused', inject(function (DatagridStyleService, DatagridService, DatagridGridService) {
            //given
            var gridColumns = [
                {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0', type: 'string'}},
                {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1', type: 'integer'}},
                {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2', type: 'string'}},
                {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3', type: 'string'}},
                {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4', type: 'string'}}
            ];
            var grid = DatagridGridService.initGrid();

            grid.setColumns(gridColumns);
            DatagridService.focusedColumn = null;

            spyOn(grid, 'scrollCellIntoView').and.returnValue();
            spyOn(grid, 'getRenderedRange').and.returnValue({top:100, bottom:150});

            //when
            DatagridGridService.navigateToFocusedColumn();

            //then
            expect(grid.scrollCellIntoView).not.toHaveBeenCalled();
        }));
    });
});
