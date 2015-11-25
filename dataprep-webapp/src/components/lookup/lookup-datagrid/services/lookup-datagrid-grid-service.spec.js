/*global window:false */
/*global SlickGridMock:false */

describe('Lookup Datagrid grid service', function () {
	'use strict';

	var realSlickGrid = Slick;
	var dataViewMock, stateMock;

	beforeEach(module('data-prep.lookup'));

	beforeEach(function () {
		dataViewMock = new DataViewMock();
	});


	beforeEach(module('data-prep.datagrid', function ($provide) {
		stateMock = {playground: {lookupGrid: {
			dataView: dataViewMock
		}}};
		$provide.constant('state', stateMock);

		spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
		spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
	}));

	beforeEach(inject(function (LookupDatagridColumnService, LookupDatagridStyleService, LookupDatagridSizeService,
								LookupDatagridExternalService, LookupDatagridTooltipService) {
		spyOn(LookupDatagridColumnService, 'init').and.returnValue();
		spyOn(LookupDatagridStyleService, 'init').and.returnValue();
		spyOn(LookupDatagridSizeService, 'init').and.returnValue();
		spyOn(LookupDatagridExternalService, 'init').and.returnValue();
		spyOn(LookupDatagridTooltipService, 'init').and.returnValue();
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
		it('should init the other datagrid services', inject(function (LookupDatagridGridService, LookupDatagridColumnService,
																	   LookupDatagridStyleService, LookupDatagridSizeService,
																	   LookupDatagridExternalService, LookupDatagridTooltipService) {
			//when
			LookupDatagridGridService.initGrid();

			//then
			expect(LookupDatagridColumnService.init).toHaveBeenCalled();
			expect(LookupDatagridStyleService.init).toHaveBeenCalled();
			expect(LookupDatagridSizeService.init).toHaveBeenCalled();
			expect(LookupDatagridExternalService.init).toHaveBeenCalled();
			expect(LookupDatagridTooltipService.init).toHaveBeenCalled();
		}));

		it('should add grid listeners', inject(function (LookupDatagridGridService) {
			//when
			LookupDatagridGridService.initGrid();

			//then
			expect(stateMock.playground.lookupGrid.dataView.onRowCountChanged.subscribe).toHaveBeenCalled();
			expect(stateMock.playground.lookupGrid.dataView.onRowsChanged.subscribe).toHaveBeenCalled();
		}));
	});

	describe('grid handlers', function() {
		it('should update row count and render grid on row count change', inject(function (LookupDatagridGridService) {
			//given
			var grid = LookupDatagridGridService.initGrid();
			spyOn(grid, 'updateRowCount').and.returnValue();
			spyOn(grid, 'render').and.returnValue();

			//when
			var onRowCountChanged = stateMock.playground.lookupGrid.dataView.onRowCountChanged.subscribe.calls.argsFor(0)[0];
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
			var onRowsChanged = stateMock.playground.lookupGrid.dataView.onRowsChanged.subscribe.calls.argsFor(0)[0];
			onRowsChanged(null, args);

			//then
			expect(grid.invalidateRows).toHaveBeenCalledWith(args.rows);
			expect(grid.render).toHaveBeenCalled();
		}));
	});
});
