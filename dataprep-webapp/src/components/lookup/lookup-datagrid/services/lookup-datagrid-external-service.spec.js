describe('Lookup Datagrid external service', function () {
	'use strict';

	var gridMock;

	var gridColumns = [
		{id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0'}},
		{id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1111111111111'}},
		{id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2'}},
		{id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3'}},
		{id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4'}},
		{id: 'tdpId', field: 'tdpId', tdpColMetadata: {id: 'tdpId', name: 'tdpId'}}
	];

	beforeEach(module('data-prep.lookup'));

	beforeEach(inject(function (StateService) {
		/*global SlickGridMock:false */
		gridMock = new SlickGridMock();
		gridMock.initColumnsMock(gridColumns);

		spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
		spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
		spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();

		spyOn(StateService, 'setLookupGridSelection').and.returnValue();
	}));


	describe('on creation', function () {
		it('should add grid active cell change listener', inject(function (LookupDatagridExternalService) {
			//when
			LookupDatagridExternalService.init(gridMock);

			//then
			expect(gridMock.onActiveCellChanged.subscribe).toHaveBeenCalled();
		}));

		it('should add header click listener', inject(function (LookupDatagridExternalService) {
			//when
			LookupDatagridExternalService.init(gridMock);

			//then
			expect(gridMock.onHeaderClick.subscribe).toHaveBeenCalled();
		}));

		it('should add header right click listener', inject(function (LookupDatagridExternalService) {
			//when
			LookupDatagridExternalService.init(gridMock);

			//then
			expect(gridMock.onHeaderContextMenu.subscribe).toHaveBeenCalled();
		}));
	});

	describe('on active cell event', function () {
		it('should update current selectedColumn', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {cell: 1};

			//when
			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);

			//then
			expect(StateService.setLookupGridSelection).toHaveBeenCalled();
		}));

		it('should NOT update current selectedColumn on cell changed', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {cell: 1};


			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);
			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);

			//when
			onActiveCellChanged(null, {cell: 1});

			//then
			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);
		}));
	});


	describe('on header click event', function () {
		it('should update current selectedColumn on header left click', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {
				column: {id: '0001'}
			};

			//when
			var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
			onHeaderClick(null, args);

			expect(StateService.setLookupGridSelection).toHaveBeenCalled();
		}));

		it('should NOT update current selectedColumn on header left click twice', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {
				column: {id: '0001'}
			};

			var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
			onHeaderClick(null, args);

			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);

			//when
			onHeaderClick(null, args);

			//then
			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);
		}));

		it('should update current selectedColumn on header right click', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {
				column: {id: '0001'}
			};
			//when
			var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
			onHeaderContextMenu(null, args);

			expect(StateService.setLookupGridSelection).toHaveBeenCalled();
		}));

		it('should NOT update current selectedColumn on header right click twice', inject(function ($timeout, LookupDatagridExternalService, StateService) {
			//given
			LookupDatagridExternalService.init(gridMock);
			var args = {
				column: {id: '0001'}
			};
			var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
			onHeaderContextMenu(null, args);
			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);

			//when
			onHeaderContextMenu(null, args);

			//then
			expect(StateService.setLookupGridSelection.calls.count()).toBe(1);
		}));
	});
});