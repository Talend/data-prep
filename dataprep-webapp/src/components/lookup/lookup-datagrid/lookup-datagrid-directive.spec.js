describe('Lookup datagrid directive', function() {
	'use strict';

	var stateMock, dataViewMock, scope, createElement, element, grid,
		createdColumns = [
			{id: 'tdpId'},
			{id: '0000', tdpColMetadata: {id: '0000'}},
			{id: '0001', tdpColMetadata: {id: '0001'}},
			{id: '0002', tdpColMetadata: {id: '0002'}}
		];

	beforeEach(function () {
		dataViewMock = new DataViewMock();
		spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
		spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
	});


	beforeEach(module('data-prep.lookup', function ($provide) {
		stateMock = {playground: {
			lookupGrid: {dataView: dataViewMock}
		}};
		$provide.constant('state', stateMock);
	}));

	beforeEach(module('htmlTemplates'));

	beforeEach(inject(function($rootScope, $compile, LookupDatagridGridService, LookupDatagridColumnService, LookupDatagridSizeService, LookupDatagridStyleService, LookupDatagridExternalService, StateService) {
		scope = $rootScope.$new();
		createElement = function() {
			element = angular.element('<lookup-datagrid></lookup-datagrid>');
			$compile(element)(scope);
			scope.$digest();

			angular.element('body').append(element);
			return element;
		};

		// decorate grid creation to keep the resulting grid ref and attach spy on its functions
		var realInitGrid = LookupDatagridGridService.initGrid;
		LookupDatagridGridService.initGrid = function(parentId) {
			grid = realInitGrid(parentId);
			spyOn(grid, 'invalidate').and.returnValue();
			return grid;
		};

		spyOn(LookupDatagridGridService, 'initGrid').and.callThrough();
		spyOn(LookupDatagridColumnService, 'createColumns').and.returnValue(createdColumns);
		spyOn(LookupDatagridColumnService, 'renewAllColumns').and.returnValue();
		spyOn(LookupDatagridSizeService, 'autosizeColumns').and.returnValue();
		spyOn(LookupDatagridStyleService, 'updateColumnClass').and.returnValue();
		spyOn(LookupDatagridStyleService, 'resetCellStyles').and.returnValue();
		spyOn(StateService, 'setLookupGridSelection').and.returnValue();
	}));

	beforeEach(function() {
		jasmine.clock().install();
	});

	afterEach(function() {
		jasmine.clock().uninstall();

		scope.$destroy();
		element.remove();
	});

	describe('on data change', function() {
		var data;

		beforeEach(inject(function() {
			//given
			createElement();
			data = {columns: [{id: '0000'}, {id: '0001', tdpColMetadata: {id: '0001'}}]};

			//when
			stateMock.playground.lookupData = data;
			scope.$digest();
			jasmine.clock().tick(1);
		}));

		describe('init', function() {
			it('should init grid', inject(function(LookupDatagridGridService) {
				//then
				expect(LookupDatagridGridService.initGrid).toHaveBeenCalledWith('#lookup-datagrid');
			}));

			it('should init grid only once', inject(function(LookupDatagridGridService) {
				//given
				expect(LookupDatagridGridService.initGrid.calls.count()).toBe(1);

				//when
				stateMock.playground.lookupData = {};
				scope.$digest();

				//then
				expect(LookupDatagridGridService.initGrid.calls.count()).toBe(1);
			}));

			it('should init tooltip ruler', inject(function(LookupDatagridTooltipService) {
				//then
				expect(LookupDatagridTooltipService.tooltipRuler).toBeDefined();
			}));
		});

		describe('grid update', function() {

			describe('column creation', function() {
				it('should create new columns', inject(function(LookupDatagridColumnService) {
					//then
					expect(LookupDatagridColumnService.createColumns).toHaveBeenCalledWith(data.columns);
				}));

				it('should reset renew all columns flag', inject(function(LookupDatagridColumnService) {
					//then
					expect(LookupDatagridColumnService.renewAllColumns).toHaveBeenCalledWith();
				}));
			});

			describe('column style', function() {
				it('should reset cell styles when there is a selected cell', inject(function(LookupDatagridStyleService) {
					//given
					stateMock.playground.lookupGrid.selectedColumn = {id: '0001'};

					//when
					stateMock.playground.lookupData = {};
					scope.$digest();
					jasmine.clock().tick(1);

					//then
					expect(LookupDatagridStyleService.updateColumnClass).toHaveBeenCalledWith(createdColumns, createdColumns[2]);
				}));

				it('should update selected column style', inject(function(LookupDatagridStyleService) {
					//given
					stateMock.playground.lookupGrid.selectedColumn = {id: '0001'};
					expect(LookupDatagridStyleService.updateColumnClass).not.toHaveBeenCalledWith(createdColumns, data.columns[1]);

					//when
					stateMock.playground.lookupData = {};
					scope.$digest();
					jasmine.clock().tick(1);

					//then
					expect(LookupDatagridStyleService.updateColumnClass).toHaveBeenCalledWith(createdColumns, data.columns[1]);
				}));
			});

			describe('column size', function() {
				it('should auto size created columns (and set them in grid, done by autosize() function)', inject(function(LookupDatagridSizeService) {
					//then
					expect(LookupDatagridSizeService.autosizeColumns).toHaveBeenCalledWith(createdColumns);
				}));
			});

			it('should execute the grid update only once when the second call is triggered before the first timeout', inject(function(LookupDatagridGridService, LookupDatagridColumnService) {
				//given
				expect(LookupDatagridColumnService.createColumns.calls.count()).toBe(1);

				stateMock.playground.lookupGrid.selectedColumn = {id: '0001'};

				//when
				stateMock.playground.lookupData = {};
				scope.$digest();

				expect(LookupDatagridColumnService.createColumns.calls.count()).toBe(1);

				stateMock.playground.lookupData = {};
				scope.$digest();
				jasmine.clock().tick(300);

				//then
				expect(LookupDatagridColumnService.createColumns.calls.count()).toBe(2);
			}));
		});
	});
});
