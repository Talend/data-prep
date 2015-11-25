describe('Lookup Datagrid column service', function () {
	'use strict';

	var gridMock, columnsMetadata;

	beforeEach(module('data-prep.lookup'));

	beforeEach(inject(function () {
		columnsMetadata = [
			{id: '0000', name: 'col0', type: 'string'},
			{id: '0001', name: 'col1', type: 'integer'},
			{id: '0002', name: 'col2', type: 'string', domain: 'salary'}
		];

		/*global SlickGridMock:false */
		gridMock = new SlickGridMock();
	}));

	describe('on creation', function () {
		//it('should add SlickGrid header destroy handler', inject(function (LookupDatagridColumnService) {
		//	//given
		//	spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

		//	//when
		//	LookupDatagridColumnService.init(gridMock);

		//	//then
		//	expect(gridMock.onBeforeHeaderCellDestroy.subscribe).toHaveBeenCalled();
		//}));

		it('should add SlickGrid header creation handler', inject(function (LookupDatagridColumnService) {
			//given
			spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

			//when
			LookupDatagridColumnService.init(gridMock);

			//then
			expect(gridMock.onHeaderCellRendered.subscribe).toHaveBeenCalled();
		}));
	});

	describe('create columns', function() {
		var headers = [
			{element: {detach: function() {}}},
			{element: {detach: function() {}}},
			{element: {detach: function() {}}}];
		var formatter = function() {};

		beforeEach(inject(function(LookupDatagridColumnService, LookupDatagridStyleService) {
			LookupDatagridColumnService.init(gridMock);
			headers.forEach(function(header) {
				spyOn(header.element, 'detach').and.returnValue();
			});

			spyOn(LookupDatagridStyleService, 'columnFormatter').and.returnValue(formatter);
		}));

		it('should create new preview grid columns', inject(function(LookupDatagridColumnService) {
			//when
			var createdColumns = LookupDatagridColumnService.createColumns(columnsMetadata);

			//then
			expect(createdColumns[0].id).toEqual('tdpId');
			expect(createdColumns[0].field).toEqual('tdpId');
			expect(createdColumns[0].name).toEqual('<div class="lookup-slick-header-column-index">#</div>');
			expect(createdColumns[0].resizable).toBeFalsy();
			expect(createdColumns[0].selectable).toBeFalsy();
			expect(createdColumns[0].formatter(1,2,3)).toEqual('<div class="index-cell">3</div>');
			expect(createdColumns[0].maxWidth).toEqual(45);

			expect(createdColumns[1].id).toEqual('0000');
			expect(createdColumns[1].field).toEqual('0000');
			expect(createdColumns[1].name).toEqual('');
			expect(createdColumns[1].formatter).toEqual(formatter);
			expect(createdColumns[1].minWidth).toEqual(120);
			expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' } );

			expect(createdColumns[2].id).toEqual('0001');
			expect(createdColumns[2].field).toEqual('0001');
			expect(createdColumns[2].name).toEqual('');
			expect(createdColumns[2].formatter).toEqual(formatter);
			expect(createdColumns[2].minWidth).toEqual(120);
			expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' } );

			expect(createdColumns[3].id).toEqual('0002');
			expect(createdColumns[3].field).toEqual('0002');
			expect(createdColumns[3].name).toEqual('');
			expect(createdColumns[3].formatter).toEqual(formatter);
			expect(createdColumns[3].minWidth).toEqual(120);
			expect(createdColumns[3].tdpColMetadata).toEqual({ id: '0002', name: 'col2', type: 'string', domain: 'salary' } );
		}));

		it('should create new grid columns', inject(function(LookupDatagridColumnService) {
			//when
			var createdColumns = LookupDatagridColumnService.createColumns(columnsMetadata);

			//then
			expect(createdColumns[0].id).toEqual('tdpId');
			expect(createdColumns[0].field).toEqual('tdpId');
			expect(createdColumns[0].name).toEqual('<div class="lookup-slick-header-column-index">#</div>');
			expect(createdColumns[0].resizable).toBeFalsy();
			expect(createdColumns[0].selectable).toBeFalsy();
			expect(createdColumns[0].formatter(1,2,3)).toEqual('<div class="index-cell">3</div>');
			expect(createdColumns[0].maxWidth).toEqual(45);

			expect(createdColumns[1].id).toEqual('0000');
			expect(createdColumns[1].field).toEqual('0000');
			expect(createdColumns[1].name).toEqual('');
			expect(createdColumns[1].formatter).toEqual(formatter);
			expect(createdColumns[1].minWidth).toEqual(120);
			expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' } );

			expect(createdColumns[2].id).toEqual('0001');
			expect(createdColumns[2].field).toEqual('0001');
			expect(createdColumns[2].name).toEqual('');
			expect(createdColumns[2].formatter).toEqual(formatter);
			expect(createdColumns[2].minWidth).toEqual(120);
			expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' } );

			expect(createdColumns[3].id).toEqual('0002');
			expect(createdColumns[3].field).toEqual('0002');
			expect(createdColumns[3].name).toEqual('');
			expect(createdColumns[3].formatter).toEqual(formatter);
			expect(createdColumns[3].minWidth).toEqual(120);
			expect(createdColumns[3].tdpColMetadata).toEqual({ id: '0002', name: 'col2', type: 'string', domain: 'salary' });
		}));
	});

	describe('on column header destroy event', function() {
		//var columnDef;

		//beforeEach(inject(function(LookupDatagridColumnService) {
		//	spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

		//	columnDef = {
		//		header: {remove: function() {}, detach: function() {}},
		//		scope: {$destroy: function() {}}
		//	};

		//	spyOn(columnDef.header, 'detach').and.returnValue();
		//	spyOn(columnDef.header, 'remove').and.returnValue();
		//	spyOn(columnDef.scope, '$destroy').and.returnValue();

		//	LookupDatagridColumnService.init(gridMock);
		//}));

		//it('should do nothing when column is part of a preview', inject(function(LookupDatagridColumnService) {
		//	//given
		//	columnDef.preview = true;
		//	var columnsArgs = {
		//		id: '0001',
		//		column: columnDef
		//	};
		//	LookupDatagridColumnService.renewAllColumns(true);

		//	//when
		//	var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
		//	onBeforeHeaderCellDestroy(null, columnsArgs);

		//	//then
		//	expect(columnDef.header.detach).not.toHaveBeenCalled();
		//	expect(columnDef.header.remove).not.toHaveBeenCalled();
		//	expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
		//}));

		//it('should destroy header when renewAllFlag is set to true', inject(function(LookupDatagridColumnService) {
		//	//given
		//	columnDef.preview = false;
		//	var columnsArgs = {
		//		id: '0001',
		//		column: columnDef
		//	};
		//	LookupDatagridColumnService.renewAllColumns(true);

		//	//when
		//	var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
		//	onBeforeHeaderCellDestroy(null, columnsArgs);

		//	//then
		//	expect(columnDef.header.detach).not.toHaveBeenCalled();
		//	expect(columnDef.header.remove).toHaveBeenCalled();
		//	expect(columnDef.scope.$destroy).toHaveBeenCalled();
		//}));

		//it('should detach header when renewAllFlag is set to false', inject(function(LookupDatagridColumnService) {
		//	//given
		//	columnDef.preview = false;
		//	var columnsArgs = {
		//		id: '0001',
		//		column: columnDef
		//	};
		//	LookupDatagridColumnService.renewAllColumns(false);

		//	//when
		//	var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
		//	onBeforeHeaderCellDestroy(null, columnsArgs);

		//	//then
		//	expect(columnDef.header.detach).toHaveBeenCalled();
		//	expect(columnDef.header.remove).not.toHaveBeenCalled();
		//	expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
		//}));

		//it('should NOT detach header for index column', inject(function(LookupDatagridColumnService) {
		//	//given
		//	columnDef.preview = false;
		//	var columnsArgs = {
		//		id: 'tdpId',
		//		column: {
		//			id: 'tdpId',
		//			header: {remove: function() {}, detach: function() {}},
		//			scope: {$destroy: function() {}}
		//		}
		//	};
		//	LookupDatagridColumnService.renewAllColumns(false);

		//	//when
		//	var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
		//	onBeforeHeaderCellDestroy(null, columnsArgs);

		//	//then
		//	expect(columnDef.header.detach).not.toHaveBeenCalled();
		//}));
	});

	describe('on column header rendered event', function() {
		var availableScope;
		var availableHeader;

		//function saveHeader(id, scope, header) {
			//var columnsToDestroy = {
			//	id: id,
			//	scope: scope,
			//	header: header,
			//	preview: false
			//};
			//var headerToDetach = {
			//	column: columnsToDestroy
			//};

			//destroy to save header in the available headers
			//var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			//onBeforeHeaderCellDestroy(null, headerToDetach);
		//}

		beforeEach(inject(function(LookupDatagridColumnService) {
			//spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();
			spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

			LookupDatagridColumnService.init(gridMock);

			//save header in available headers list
			availableScope = {$destroy: function() {}, $digest: function() {}};
			availableHeader = angular.element('<div id="availableHeader"></div>');
			//saveHeader('0001', availableScope, availableHeader);

			spyOn(availableScope, '$digest').and.returnValue();
		}));

	//it('should attach and update available header that has the same id', inject(function() {
	//	//given
	//	var columnsArgs = {
	//		column:  {
	//			id: '0001',
	//			tdpColMetadata: {}
	//		},
	//		node: angular.element('<div></div>')[0]
	//	};

	//	//when
	//	var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
	//	onHeaderCellRendered(null, columnsArgs);

	//	//then
	//	expect(availableScope.column).toBe(columnsArgs.column.tdpColMetadata);
	//	expect(availableScope.$digest).toHaveBeenCalled();

	//	expect(columnsArgs.column.header).toBe(availableHeader);
	//	expect(columnsArgs.column.scope).toBe(availableScope);

	//	expect(angular.element(columnsArgs.node).find('#availableHeader').length).toBe(1);
	//}));

		it('should create and attach a new header', inject(function() {
			//given
			var columnsArgs = {
				column:  {
					id: '0002',
					tdpColMetadata: {}
				},
				node: angular.element('<div></div>')[0]
			};

			//when
			var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			//then
			expect(columnsArgs.column.scope).toBeDefined();
			expect(columnsArgs.column.header).toBeDefined();

			expect(columnsArgs.column.header).not.toBe(availableHeader);
			expect(columnsArgs.column.scope).not.toBe(availableScope);

			expect(angular.element(columnsArgs.node).find('lookup-datagrid-header').length).toBe(1);
		}));

		it('should do nothing if column is index column', inject(function() {
			//given
			var columnsArgs = {
				column:  {
					id: 'tdpId'
				},
				node: angular.element('<div></div>')[0]
			};

			//when
			var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			//then
			expect(columnsArgs.column.scope).not.toBeDefined();
			expect(columnsArgs.column.header).not.toBeDefined();

			expect(angular.element(columnsArgs.node).find('lookup-datagrid-header').length).toBe(0);
		}));
	});
});
