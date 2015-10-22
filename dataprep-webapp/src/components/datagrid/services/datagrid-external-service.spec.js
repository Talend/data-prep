describe('Datagrid external service', function () {
    'use strict';

    var gridMock;

    var gridColumns = [
        {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0'}},
        {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1'}},
        {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2'}},
        {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3'}},
        {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4'}},
        {id: 'tdpId', field: 'tdpId', tdpColMetadata: {id: 'tdpId', name: 'tdpId'}}
    ];

    beforeEach(module('data-prep.datagrid'));

    beforeEach(inject(function (StatisticsService, SuggestionService) {
        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();
        gridMock.initColumnsMock(gridColumns);

        spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();
        spyOn(gridMock.onScroll, 'subscribe').and.returnValue();

        spyOn(StatisticsService, 'updateStatistics').and.returnValue();
        spyOn(SuggestionService, 'setColumn').and.returnValue();
        spyOn(SuggestionService, 'selectTab').and.returnValue();
    }));

    beforeEach(function () {
        jasmine.clock().install();
    });

    afterEach(function () {
        jasmine.clock().uninstall();
    });

    describe('on creation', function () {
        it('should add grid active cell change listener', inject(function (DatagridExternalService) {
            //when
            DatagridExternalService.init(gridMock);

            //then
            expect(gridMock.onActiveCellChanged.subscribe).toHaveBeenCalled();
        }));

        it('should add header click listener', inject(function (DatagridExternalService) {
            //when
            DatagridExternalService.init(gridMock);

            //then
            expect(gridMock.onHeaderClick.subscribe).toHaveBeenCalled();
        }));

        it('should add header right click listener', inject(function (DatagridExternalService) {
            //when
            DatagridExternalService.init(gridMock);

            //then
            expect(gridMock.onHeaderContextMenu.subscribe).toHaveBeenCalled();
        }));

        it('should add scroll listener', inject(function (DatagridExternalService) {
            //when
            DatagridExternalService.init(gridMock);

            //then
            expect(gridMock.onScroll.subscribe).toHaveBeenCalled();
        }));
    });

    describe('on active cell event', function () {
        it('should update playground right panel on active cell changed after a 200ms delay', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {cell: 1};
            var columnMetadata = gridColumns[1].tdpColMetadata;

            //when
            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, args);

            expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
            expect(SuggestionService.setColumn).not.toHaveBeenCalled();
            expect(SuggestionService.selectTab).not.toHaveBeenCalled();

            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            expect(SuggestionService.setColumn).toHaveBeenCalledWith(columnMetadata);
            expect(SuggestionService.selectTab).toHaveBeenCalledWith('COLUMN');

        }));

        it('should NOT update playground right panel on active cell changed if column and tab are the same', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {cell: 1};

            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, args);
            $timeout.flush(200);

            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);

            //when
            onActiveCellChanged(null, args);
            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);
        }));

        it('should NOT change suggestion tab on active cell changed if wanted tab is the same', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);

            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, {cell: 1});
            $timeout.flush();

            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);

            //when : same event that want to show CELL tab
            onActiveCellChanged(null, {cell: 2});
            $timeout.flush();

            //then : only stats and suggestions are updated, NOT selected tab
            expect(StatisticsService.updateStatistics.calls.count()).toBe(2);
            expect(SuggestionService.setColumn.calls.count()).toBe(2);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);
        }));

        it('should cancel the pending right panel update and schedule a new one', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var firstCallArgs = {cell: 1};
            var secondCallArgs = {cell: 2};
            var secondCallColumnMetadata = gridColumns[2].tdpColMetadata;

            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, firstCallArgs);

            $timeout.flush(199);

            expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
            expect(SuggestionService.setColumn).not.toHaveBeenCalled();
            expect(SuggestionService.selectTab).not.toHaveBeenCalled();

            //when
            onActiveCellChanged(null, secondCallArgs);

            $timeout.flush(1);
            expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
            expect(SuggestionService.setColumn).not.toHaveBeenCalled();
            expect(SuggestionService.selectTab).not.toHaveBeenCalled();

            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            expect(SuggestionService.setColumn).toHaveBeenCalledWith(secondCallColumnMetadata);
            expect(SuggestionService.selectTab).toHaveBeenCalledWith('COLUMN');
        }));
        
        it('should do nothing when no cell is active', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);

            //when
            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, {});
            try{
                $timeout.flush();
            }

            //then
            catch(error) {
                expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
                expect(SuggestionService.setColumn).not.toHaveBeenCalled();
                expect(SuggestionService.selectTab).not.toHaveBeenCalled();
                return;
            }
            throw new Error('should have thrown exception because there is nothing to flush');
        }));
    });

    describe('on header click event', function () {
        it('should update playground right panel on header click after a 200ms delay', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {
                column: {id: '0001'}
            };
            var columnMetadata = gridColumns[1].tdpColMetadata;

            //when
            var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
            onHeaderClick(null, args);

            expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
            expect(SuggestionService.setColumn).not.toHaveBeenCalled();

            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            expect(SuggestionService.setColumn).toHaveBeenCalledWith(columnMetadata);
            expect(SuggestionService.selectTab).toHaveBeenCalledWith('COLUMN');
        }));

        it('should update playground right panel on header right click after a 200ms delay', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {
                column: {id: '0001'}
            };
            var columnMetadata = gridColumns[1].tdpColMetadata;

            //when
            var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
            onHeaderContextMenu(null, args);

            expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();
            expect(SuggestionService.setColumn).not.toHaveBeenCalled();
            expect(SuggestionService.selectTab).not.toHaveBeenCalled();

            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            expect(SuggestionService.setColumn).toHaveBeenCalledWith(columnMetadata);
            expect(SuggestionService.selectTab).toHaveBeenCalledWith('COLUMN');
        }));

        it('should NOT update playground right panel on header click when column and tab are the same', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {
                column: {id: '0001'}
            };

            var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
            onHeaderClick(null, args);
            $timeout.flush(200);

            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);

            //when
            onHeaderClick(null, args);
            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);
        }));

        it('should NOT update playground right panel on header right click when column and tab are the same', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            DatagridExternalService.init(gridMock);
            var args = {
                column: {id: '0001'}
            };

            var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
            onHeaderContextMenu(null, args);
            $timeout.flush(200);

            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);

            //when
            onHeaderContextMenu(null, args);
            $timeout.flush(200);

            //then
            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);
        }));

        it('should NOT update suggestions and stats when column is the same', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given : active cell will update suggestions + stats for column 1, and set tab to CELL
            DatagridExternalService.init(gridMock);
            var activeCellArgs = {cell: 1};

            var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
            onActiveCellChanged(null, activeCellArgs);
            $timeout.flush(200);

            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);

            var headerClickArgs = {
                column: {id: '0001'}
            };

            //when
            var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
            onHeaderClick(null, headerClickArgs);

            $timeout.flush(200);

            //then : only tab is updated, NOT stats or suggestions
            expect(StatisticsService.updateStatistics.calls.count()).toBe(1);
            expect(SuggestionService.setColumn.calls.count()).toBe(1);
            expect(SuggestionService.selectTab.calls.count()).toBe(1);
        }));

        it('should reset suggestions and stats when column is index column', inject(function ($timeout, DatagridExternalService, StatisticsService, SuggestionService) {
            //given
            var headerClickArgs = {
                column: {id: 'tdpId'}
            };
            spyOn(StatisticsService, 'reset').and.returnValue();
            spyOn(SuggestionService, 'reset').and.returnValue();

            //when
            DatagridExternalService.updateSuggestionPanel(headerClickArgs.column, 'Cell');
            $timeout.flush();

            //then
            expect(StatisticsService.reset).toHaveBeenCalledWith(true, true, true);
            expect(SuggestionService.reset).toHaveBeenCalled();
        }));

    });

    describe('on scroll event', function () {
        it('should update preview range on scroll after a 200ms delay', inject(function (DatagridExternalService, PreviewService) {
            //given
            DatagridExternalService.init(gridMock);

            var range = [1, 3, 5];
            gridMock.initRenderedRangeMock(range);

            expect(PreviewService.gridRangeIndex).toBeFalsy();

            //when
            var onScroll = gridMock.onScroll.subscribe.calls.argsFor(0)[0];
            onScroll();
            jasmine.clock().tick(200);

            //then
            expect(PreviewService.gridRangeIndex).toBe(range);
        }));
    });
});
