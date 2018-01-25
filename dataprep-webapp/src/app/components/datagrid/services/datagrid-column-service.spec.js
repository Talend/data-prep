/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Datagrid column service', () => {
	let gridMock;
	let columnsMetadata;

	beforeEach(angular.mock.module('data-prep.datagrid'));

	beforeEach(inject(() => {
		columnsMetadata = [
			{ id: '0000', name: 'col0', type: 'string' },
			{ id: '0001', name: 'col1', type: 'integer' },
			{ id: '0002', name: 'col2', type: 'string', domain: 'salary', domainLabel: 'Salary label' },
		];

		gridMock = new SlickGridMock();
	}));

	describe('on creation', () => {
		it('should add SlickGrid header destroy handler', inject((DatagridColumnService) => {
			// given
			spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

			// when
			DatagridColumnService.init(gridMock);

			// then
			expect(gridMock.onBeforeHeaderCellDestroy.subscribe).toHaveBeenCalled();
		}));

		it('should add SlickGrid header creation handler', inject((DatagridColumnService) => {
			// given
			spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

			// when
			DatagridColumnService.init(gridMock);

			// then
			expect(gridMock.onHeaderCellRendered.subscribe).toHaveBeenCalled();
		}));
	});

	describe('create columns', () => {
		const headers = [
			{
				element: {
					detach: () => {
					},
				},
			},
			{
				element: {
					detach: () => {
					},
				},
			},
			{
				element: {
					detach: () => {
					},
				},
			}];
		const formatter = () => {
		};

		beforeEach(inject((DatagridColumnService, DatagridStyleService) => {
			DatagridColumnService.init(gridMock);
			headers.forEach((header) => {
				spyOn(header.element, 'detach').and.returnValue();
			});

			spyOn(DatagridStyleService, 'getColumnPreviewStyle').and.returnValue('');
			spyOn(DatagridStyleService, 'columnFormatter').and.returnValue(formatter);
		}));

		it('should create new preview grid columns', inject((DatagridColumnService) => {
			// when
			const createdColumns = DatagridColumnService.createColumns(columnsMetadata, true, false);

			// then
			expect(createdColumns[0].id).toEqual('tdpId');
			expect(createdColumns[0].field).toEqual('tdpId');
			expect(createdColumns[0].name).toEqual('');
			expect(createdColumns[0].resizable).toBeFalsy();
			expect(createdColumns[0].selectable).toBeFalsy();
			expect(createdColumns[0].formatter(1, 2, 3)).toEqual('<div class="index-cell">3</div>');
			expect(createdColumns[0].maxWidth).toEqual(60);

			expect(createdColumns[1].id).toEqual('0000');
			expect(createdColumns[1].field).toEqual('0000');
			expect(createdColumns[1].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col0</div>   <div class="grid-header-type ng-binding">text</div></div><div class="quality-bar"><div class="record-unknown"></div></div>');
			expect(createdColumns[1].formatter).toEqual(formatter);
			expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' });

			expect(createdColumns[2].id).toEqual('0001');
			expect(createdColumns[2].field).toEqual('0001');
			expect(createdColumns[2].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col1</div>   <div class="grid-header-type ng-binding">integer</div></div><div class="quality-bar"><div class="record-unknown"></div></div>');
			expect(createdColumns[2].formatter).toEqual(formatter);
			expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' });

			expect(createdColumns[3].id).toEqual('0002');
			expect(createdColumns[3].field).toEqual('0002');
			expect(createdColumns[3].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col2</div>   <div class="grid-header-type ng-binding">Salary label</div></div><div class="quality-bar"><div class="record-unknown"></div></div>');
			expect(createdColumns[3].formatter).toEqual(formatter);
			expect(createdColumns[3].tdpColMetadata).toEqual({
				id: '0002',
				name: 'col2',
				type: 'string',
				domain: 'salary',
				domainLabel: 'Salary label',
			});
		}));

		it('should escape html to prevent XSS injection', inject((DatagridColumnService) => {
			// when
			columnsMetadata = [
				{ id: '0000', name: '<h1>NAME</h1>', type: 'string' },
			];
			const createdColumns = DatagridColumnService.createColumns(columnsMetadata, true, false);

			// then
			expect(createdColumns[1].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">&lt;h1&gt;NAME&lt;/h1&gt;</div>   <div class="grid-header-type ng-binding">text</div></div><div class="quality-bar"><div class="record-unknown"></div></div>');
		}));

		it('should create new grid columns', inject((DatagridColumnService) => {
			// when
			const createdColumns = DatagridColumnService.createColumns(columnsMetadata, false, false);

			// then
			expect(createdColumns[0].id).toEqual('tdpId');
			expect(createdColumns[0].field).toEqual('tdpId');
			expect(createdColumns[0].name).toEqual('');
			expect(createdColumns[0].resizable).toBeFalsy();
			expect(createdColumns[0].selectable).toBeFalsy();
			expect(createdColumns[0].formatter(1, 2, 3)).toEqual('<div class="index-cell">3</div>');
			expect(createdColumns[0].maxWidth).toEqual(60);

			expect(createdColumns[1].id).toEqual('0000');
			expect(createdColumns[1].field).toEqual('0000');
			expect(createdColumns[1].name).toEqual('');
			expect(createdColumns[1].formatter).toEqual(formatter);
			expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' });

			expect(createdColumns[2].id).toEqual('0001');
			expect(createdColumns[2].field).toEqual('0001');
			expect(createdColumns[2].name).toEqual('');
			expect(createdColumns[2].formatter).toEqual(formatter);
			expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' });

			expect(createdColumns[3].id).toEqual('0002');
			expect(createdColumns[3].field).toEqual('0002');
			expect(createdColumns[3].name).toEqual('');
			expect(createdColumns[3].formatter).toEqual(formatter);
			expect(createdColumns[3].tdpColMetadata).toEqual({
				id: '0002',
				name: 'col2',
				type: 'string',
				domain: 'salary',
				domainLabel: 'Salary label',
			});
		}));
	});

	describe('on column header destroy event', () => {
		let columnDef;

		beforeEach(inject((DatagridColumnService) => {
			spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

			columnDef = {
				header: {
					remove: () => {
					},
					detach: () => {
					},
				},
				scope: {
					$destroy: () => {
					},
				},
			};

			spyOn(columnDef.header, 'detach').and.returnValue();
			spyOn(columnDef.header, 'remove').and.returnValue();
			spyOn(columnDef.scope, '$destroy').and.returnValue();

			DatagridColumnService.init(gridMock);
		}));

		it('should do nothing when column is part of a preview', inject((DatagridColumnService) => {
			// given
			columnDef.preview = true;
			const columnsArgs = {
				id: '0001',
				column: columnDef,
			};
			DatagridColumnService.renewAllColumns(true);

			// when
			const onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			onBeforeHeaderCellDestroy(null, columnsArgs);

			// then
			expect(columnDef.header.detach).not.toHaveBeenCalled();
			expect(columnDef.header.remove).not.toHaveBeenCalled();
			expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
		}));

		it('should destroy header when renewAllFlag is set to true', inject((DatagridColumnService) => {
			// given
			columnDef.preview = false;
			const columnsArgs = {
				id: '0001',
				column: columnDef,
			};
			DatagridColumnService.renewAllColumns(true);

			// when
			const onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			onBeforeHeaderCellDestroy(null, columnsArgs);

			// then
			expect(columnDef.header.detach).not.toHaveBeenCalled();
			expect(columnDef.header.remove).toHaveBeenCalled();
			expect(columnDef.scope.$destroy).toHaveBeenCalled();
		}));

		it('should detach header when renewAllFlag is set to false', inject((DatagridColumnService) => {
			// given
			columnDef.preview = false;
			const columnsArgs = {
				id: '0001',
				column: columnDef,
			};
			DatagridColumnService.renewAllColumns(false);

			// when
			const onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			onBeforeHeaderCellDestroy(null, columnsArgs);

			// then
			expect(columnDef.header.detach).toHaveBeenCalled();
			expect(columnDef.header.remove).not.toHaveBeenCalled();
			expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
		}));

		it('should NOT detach header for index column', inject((DatagridColumnService) => {
			// given
			columnDef.preview = false;
			const columnsArgs = {
				id: 'tdpId',
				column: {
					id: 'tdpId',
					header: {
						remove: () => {
						},
						detach: () => {
						},
					},
					scope: {
						$destroy: () => {
						},
					},
				},
			};
			DatagridColumnService.renewAllColumns(false);

			// when
			const onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			onBeforeHeaderCellDestroy(null, columnsArgs);

			// then
			expect(columnDef.header.detach).not.toHaveBeenCalled();
		}));
	});

	describe('on column header rendered event', () => {
		let availableScope;
		let availableHeader;

		function saveHeader(id, scope, header) {
			const columnsToDestroy = {
				id,
				scope,
				header,
				preview: false,
			};
			const headerToDetach = {
				column: columnsToDestroy,
			};

			// destroy to save header in the available headers
			const onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
			onBeforeHeaderCellDestroy(null, headerToDetach);
		}

		beforeEach(inject((DatagridColumnService) => {
			spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();
			spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

			DatagridColumnService.init(gridMock);

			// save header in available headers list
			availableScope = {
				$destroy: () => {
				},
			};
			availableHeader = angular.element('<div id="availableHeader"></div>');
			saveHeader('0001', availableScope, availableHeader);
		}));

		it('should attach and update available header that has the same id', inject(() => {
			// given
			const columnsArgs = {
				column: {
					id: '0001',
					tdpColMetadata: {},
				},
				node: angular.element('<div></div>')[0],
			};

			// when
			const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			// then
			expect(availableScope.column).toBe(columnsArgs.column.tdpColMetadata);
			expect(columnsArgs.column.header).toBe(availableHeader);
			expect(columnsArgs.column.scope).toBe(availableScope);

			expect(angular.element(columnsArgs.node).find('#availableHeader').length).toBe(1);
		}));

		it('should create and attach a new header', inject(() => {
			// given
			const columnsArgs = {
				column: {
					id: '0002',
					tdpColMetadata: {},
				},
				node: angular.element('<div></div>')[0],
			};

			// when
			const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			// then
			expect(columnsArgs.column.scope).toBeDefined();
			expect(columnsArgs.column.header).toBeDefined();

			expect(columnsArgs.column.header).not.toBe(availableHeader);
			expect(columnsArgs.column.scope).not.toBe(availableScope);

			expect(angular.element(columnsArgs.node).find('datagrid-header').length).toBe(1);
		}));

		it('should do nothing if column is from preview', inject(() => {
			// given
			const columnsArgs = {
				column: {
					id: '0002',
					tdpColMetadata: {},
					preview: true,
				},
				node: angular.element('<div></div>')[0],
			};

			// when
			const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			// then
			expect(columnsArgs.column.scope).not.toBeDefined();
			expect(columnsArgs.column.header).not.toBeDefined();

			expect(angular.element(columnsArgs.node).find('datagrid-header').length).toBe(0);
		}));

		it('should create and attach index column header', inject(() => {
			// given
			const columnsArgs = {
				column: {
					id: 'tdpId',
				},
				node: angular.element('<div></div>')[0],
			};

			// when
			const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
			onHeaderCellRendered(null, columnsArgs);

			// then
			expect(columnsArgs.column.scope).toBeDefined();
			expect(columnsArgs.column.header).toBeDefined();

			expect(angular.element(columnsArgs.node).find('datagrid-index-header').length).toBe(1);
		}));
	});

	describe('on column reorder event', () => {
		beforeEach(inject((PlaygroundService) => {
			spyOn(PlaygroundService, 'appendStep');
		}));

		it('should call PlaygroundService move columns 2 steps', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000', name: 'beer' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0002',
					scope: 'dataset',
					column_id: '0000',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should find move columns 2 steps', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000', name: 'beer' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0002',
					scope: 'dataset',
					column_id: '0000',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should find move columns simple swap', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0001', tdpColMetadata: { id: '0001', name: 'beer' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0002',
					scope: 'dataset',
					column_id: '0001',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should find move columns 3 steps', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000', name: 'beer' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
				{ id: '0000', tdpColMetadata: { id: '0000' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0003',
					scope: 'dataset',
					column_id: '0000',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should find not moved', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000', name: 'beer' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			expect(PlaygroundService.appendStep).not.toHaveBeenCalledWith();
		}));

		it('should find move columns 2 steps backward', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002', name: 'beer' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];
			const newCols = [
				{ id: '0002', tdpColMetadata: { id: '0002', name: 'beer' } },
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0003', tdpColMetadata: { id: '0003' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0000',
					scope: 'dataset',
					column_id: '0002',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));

		it('should find move columns 2 steps backward in the middle', inject((DatagridColumnService, PlaygroundService) => {
			// given
			const original = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0003', tdpColMetadata: { id: '0003', name: 'beer' } },
				{ id: '0004', tdpColMetadata: { id: '0004' } },
			];
			const newCols = [
				{ id: '0000', tdpColMetadata: { id: '0000' } },
				{ id: '0003', tdpColMetadata: { id: '0003', name: 'beer' } },
				{ id: '0001', tdpColMetadata: { id: '0001' } },
				{ id: '0002', tdpColMetadata: { id: '0002' } },
				{ id: '0004', tdpColMetadata: { id: '0004' } },
			];

			// when
			DatagridColumnService.columnsOrderChanged(newCols, original);

			// then
			const expectedParams = [{
				action: 'reorder',
				parameters: {
					selected_column: '0001',
					scope: 'dataset',
					column_id: '0003',
					column_name: 'beer',
					dataset_action_display_type: 'column',
				},
			}];

			expect(PlaygroundService.appendStep).toHaveBeenCalledWith(expectedParams);
		}));
	});
});
