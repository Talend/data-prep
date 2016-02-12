/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Lookup Datagrid grid service', function () {
    'use strict';

    var realSlickGrid = Slick;
    var dataViewMock, stateMock;

    beforeEach(angular.mock.module('data-prep.lookup'));

    beforeEach(function () {
        dataViewMock = new DataViewMock();
    });


    beforeEach(angular.mock.module('data-prep.datagrid', function ($provide) {
        stateMock = {
            playground: {
                lookup: {
                    dataView: dataViewMock
                }
            }
        };
        $provide.constant('state', stateMock);

        spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
        spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
    }));

    beforeEach(inject(function (LookupDatagridColumnService, LookupDatagridStyleService, LookupDatagridTooltipService) {
        spyOn(LookupDatagridColumnService, 'init').and.returnValue();
        spyOn(LookupDatagridStyleService, 'init').and.returnValue();
        spyOn(LookupDatagridTooltipService, 'init').and.returnValue();
    }));

    beforeEach(inject(function ($window) {
        $window.Slick = {
            Grid: SlickGridMock
        };
    }));

    afterEach(inject(function ($window) {
        $window.Slick = realSlickGrid;
    }));

    describe('on creation', function () {
        it('should init the other datagrid services', inject(function (LookupDatagridGridService, LookupDatagridColumnService,
                                                                       LookupDatagridStyleService, LookupDatagridTooltipService) {
            //when
            LookupDatagridGridService.initGrid();

            //then
            expect(LookupDatagridColumnService.init).toHaveBeenCalled();
            expect(LookupDatagridStyleService.init).toHaveBeenCalled();
            expect(LookupDatagridTooltipService.init).toHaveBeenCalled();
        }));

        it('should add grid listeners', inject(function (LookupDatagridGridService) {
            //when
            LookupDatagridGridService.initGrid();

            //then
            expect(stateMock.playground.lookup.dataView.onRowCountChanged.subscribe).toHaveBeenCalled();
            expect(stateMock.playground.lookup.dataView.onRowsChanged.subscribe).toHaveBeenCalled();
        }));
    });

    describe('grid handlers', function () {
        it('should update row count and render grid on row count change', inject(function (LookupDatagridGridService) {
            //given
            var grid = LookupDatagridGridService.initGrid();
            spyOn(grid, 'updateRowCount').and.returnValue();
            spyOn(grid, 'render').and.returnValue();

            //when
            var onRowCountChanged = stateMock.playground.lookup.dataView.onRowCountChanged.subscribe.calls.argsFor(0)[0];
            onRowCountChanged();

            //then
            expect(grid.updateRowCount).toHaveBeenCalled();
            expect(grid.render).toHaveBeenCalled();
        }));

        it('should invalidate rows and render grid on rows changed', inject(function (LookupDatagridGridService) {
            //given
            var grid = LookupDatagridGridService.initGrid();
            spyOn(grid, 'invalidateRows').and.returnValue();
            spyOn(grid, 'render').and.returnValue();

            var args = {rows: []};

            //when
            var onRowsChanged = stateMock.playground.lookup.dataView.onRowsChanged.subscribe.calls.argsFor(0)[0];
            onRowsChanged(null, args);

            //then
            expect(grid.invalidateRows).toHaveBeenCalledWith(args.rows);
            expect(grid.render).toHaveBeenCalled();
        }));
    });
});
