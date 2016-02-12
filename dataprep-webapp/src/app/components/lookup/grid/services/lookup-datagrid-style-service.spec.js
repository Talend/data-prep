/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Lookup Datagrid style service', function () {
	'use strict';

	var gridMock, gridColumns, stateMock;

	function assertColumnsHasNoStyles() {
		gridColumns.forEach(function(column) {
			expect(column.cssClass).toBeFalsy();
		});
	}

	beforeEach(angular.mock.module('data-prep.lookup', function ($provide) {
		stateMock = {playground: {lookup: {}}};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(function () {
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
	}));

	describe('on creation', function () {
		it('should add header click listener', inject(function (LookupDatagridStyleService) {
			//when
			LookupDatagridStyleService.init(gridMock);

			//then
			expect(gridMock.onHeaderClick.subscribe).toHaveBeenCalled();
		}));

		it('should add header right click listener', inject(function (LookupDatagridStyleService) {
			//when
			LookupDatagridStyleService.init(gridMock);

			//then
			expect(gridMock.onHeaderContextMenu.subscribe).toHaveBeenCalled();
		}));

		it('should add active cell changed listener', inject(function (LookupDatagridStyleService) {
			//when
			LookupDatagridStyleService.init(gridMock);

			//then
			expect(gridMock.onActiveCellChanged.subscribe).toHaveBeenCalled();
		}));
	});

	describe('on header click event', function () {
		it('should reset cell styles', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {column: gridColumns[1]};

			//when
			var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
			onHeaderClick(null, args);

			//then
			expect(gridMock.resetActiveCell).toHaveBeenCalled();
		}));

		it('should set selected column class', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {column: gridColumns[1]};

			assertColumnsHasNoStyles();

			//when
			var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
			onHeaderClick(null, args);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));

		it('should invalidate grid', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {column: gridColumns[1]};

			//when
			var onHeaderClick = gridMock.onHeaderClick.subscribe.calls.argsFor(0)[0];
			onHeaderClick(null, args);

			//then
			expect(gridMock.invalidate).toHaveBeenCalled();
		}));
	});

	describe('on header right click event', function () {
		it('should set reset cell styles', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			gridMock.setCellCssStyles('highlight', {'2': {'0000': 'highlight'}});

			var args = {column: gridColumns[1]};

			//when
			var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
			onHeaderContextMenu(null, args);

			//then
			expect(gridMock.resetActiveCell).toHaveBeenCalled();
		}));

		it('should set selected column class', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {column: gridColumns[1]};

			assertColumnsHasNoStyles();

			//when
			var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
			onHeaderContextMenu(null, args);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));

		it('should invalidate grid', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {column: gridColumns[1]};

			//when
			var onHeaderContextMenu = gridMock.onHeaderContextMenu.subscribe.calls.argsFor(0)[0];
			onHeaderContextMenu(null, args);

			//then
			expect(gridMock.invalidate).toHaveBeenCalled();
		}));
	});

	describe('on active cell changed event', function () {
		var dataViewMock;

		beforeEach(function () {
			dataViewMock = new DataViewMock();
			stateMock.playground.lookup.dataView = dataViewMock;
		});

		it('should set "selected" column class', inject(function ($timeout, LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {cell: 1};

			assertColumnsHasNoStyles();

			//when
			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);
			$timeout.flush(200);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));

		it('should NOT change "selected" column class if it has not changed', inject(function ($timeout, LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {cell: 1};

			assertColumnsHasNoStyles();

			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);
			$timeout.flush(200);

			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');

			//when
			onActiveCellChanged(null, args);
			$timeout.flush(200);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));

		it('should invalidate grid', inject(function ($timeout, LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {cell: 1};

			//when
			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);
			$timeout.flush(200);

			//then
			expect(gridMock.invalidate).toHaveBeenCalled();
		}));

		it('should do nothing when there is no active cell', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var args = {cell: undefined};

			//when
			var onActiveCellChanged = gridMock.onActiveCellChanged.subscribe.calls.argsFor(0)[0];
			onActiveCellChanged(null, args);

			//then
			expect(gridMock.invalidate).not.toHaveBeenCalled();
			assertColumnsHasNoStyles();
		}));
	});

	describe('update column styles', function() {
		it('should set "selected" class on active cell column when this is NOT a preview', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			assertColumnsHasNoStyles();
			var selectedColumn = gridColumns[1];

			//when
			LookupDatagridStyleService.updateColumnClass(gridColumns, selectedColumn);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('selected') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));

		it('should set "number" class on number column', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			assertColumnsHasNoStyles();

			//when
			LookupDatagridStyleService.updateColumnClass(gridColumns);

			//then
			expect(gridColumns[0].cssClass).toBeFalsy();
			expect(gridColumns[1].cssClass.indexOf('number') > -1).toBe(true);
			expect(gridColumns[2].cssClass).toBeFalsy();
			expect(gridColumns[3].cssClass).toBeFalsy();
			expect(gridColumns[4].cssClass).toBeFalsy();
			expect(gridColumns[5].cssClass).toBe('index-column');
		}));
	});

	describe('column formatter', function() {
		it('should adapt value into html with leading/trailing spaces management', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var col = {quality: {invalidValues: []}};
			var value = '  my value     ';
			var columnDef = gridColumns[1];
			var dataContext = {};

			//when
			var formatter = LookupDatagridStyleService.columnFormatter(col);
			var result = formatter(null, null, value, columnDef, dataContext);

			//then
			expect(result.indexOf('<span class="hiddenChars">  </span>my value<span class="hiddenChars">     </span>')).toBe(0);
		}));

		it('should add invisible rectangle on valid value', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var col = {quality: {invalidValues: []}};
			var value = 'my value';
			var columnDef = gridColumns[1];
			var dataContext = {};

			//when
			var formatter = LookupDatagridStyleService.columnFormatter(col);
			var result = formatter(null, null, value, columnDef, dataContext);

			//then
			expect(result.indexOf('<div class="invisible-rect"></div>') > 0).toBe(true);
		}));

		it('should add red rectangle on invalid value', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var col = {quality: {invalidValues: ['my value']}};
			var value = 'my value';
			var columnDef = gridColumns[1];
			var dataContext = {};

			//when
			var formatter = LookupDatagridStyleService.columnFormatter(col);
			var result = formatter(null, null, value, columnDef, dataContext);

			//then
			expect(result.indexOf('<div title="Invalid Value" class="red-rect"></div>') > 0).toBe(true);
		}));

		it('should add red rectangle on invalid value case of non TEXT domains (ieemail address)', inject(function (LookupDatagridStyleService) {
			//given
			LookupDatagridStyleService.init(gridMock);
			var col = {quality: {invalidValues: ['m&a>al<ej@talend']}};
			var value = 'm&a>al<ej@talend';
			var columnDef = gridColumns[1];
			var dataContext = {};

			//when
			var formatter = LookupDatagridStyleService.columnFormatter(col);
			var result = formatter(null, null, value, columnDef, dataContext);

			//then
			expect(result).toBe('m&amp;a&gt;al&lt;ej@talend<div title="Invalid Value" class="red-rect"></div>');
		}));
	});
});
