describe('Lookup Grid state service', function () {
	'use strict';

	var data = {
		columns: [
			{ id: '0000', 'name':'identif'},
			{ id: '0001', 'name':'code'},
			{ id: '0002', 'name':'firstname'},
			{ id: '0003', 'name':'lastname'}
		],
		records: [
			{tdpId: 0, firstname: 'Tata'},
			{tdpId: 1, firstname: 'Tetggggge'},
			{tdpId: 2, firstname: 'Titi'},
			{tdpId: 3, firstname: 'Toto'},
			{tdpId: 4, name: 'AMC Gremlin'},
			{tdpId: 5, firstname: 'Tyty'},
			{tdpId: 6, firstname: 'Papa'},
			{tdpId: 7, firstname: 'Pepe'},
			{tdpId: 8, firstname: 'Pipi'},
			{tdpId: 9, firstname: 'Popo'},
			{tdpId: 10, firstname: 'Pupu'},
			{tdpId: 11, firstname: 'Pypy'}
		]
	};

	var addedToLookup = [
		{'id':'0000' , 'name':'identif' , isAdded: false},
		{'id':'0001' , 'name':'code' , isAdded: false},
		{'id':'0002' , 'name':'firstname' , isAdded: false},
		{'id':'0003' , 'name':'lastname' , isAdded: false}
	];

	var addedToLookupWithSelected = [
		{'id':'0000','name':'identif',isAdded: false},
		{'id':'0001','name':'code',isAdded: false},
		{'id':'0002','name':'firstname',isAdded: true},
		{'id':'0003','name':'lastname',isAdded: true}
	];

	var metadata = {
		'id': '8f215b5b-b678-41f1-8a17-dccced6d40fe',
		'favorite': false,
		'records': 410,
		'nbLinesHeader': 1,
		'nbLinesFooter': 0,
		'type': 'text/csv',
		'formatGuess': 'formatGuess#csv',
		'certificationStep': 'NONE',
		'location': {
			'type': 'local'
		}
	};

	beforeEach(module('data-prep.services.state'));

	beforeEach(inject(function(gridState) {
		gridState.dataView = new DataViewMock();
	}));

	describe('data', function() {
		it('should set data to DataView', inject(function (lookupGridState, LookupGridStateService) {
			//given
			expect(lookupGridState.dataView.getItems()).not.toBe(data.records);

			//when
			LookupGridStateService.setData(data);

			//then
			expect(lookupGridState.dataView.getItems()).toBe(data.records);
			expect(lookupGridState.addedToLookup).toEqual(addedToLookup);
		}));

		it('should create and set to false all the columns that will be added to the lookup', inject(function (lookupGridState, LookupGridStateService) {
			//given
			expect(lookupGridState.dataView.getItems()).toBe(data.records);
			lookupGridState.addedToLookup = [];

			//when
			LookupGridStateService.setData(data);

			//then
			expect(lookupGridState.addedToLookup).toEqual(addedToLookup);
		}));

		it('should update column selection metadata with the new metadata corresponding to the selected id', inject(function (lookupGridState, LookupGridStateService) {
			//given
			var oldMetadata = {id: '0001'};
			lookupGridState.selectedColumn = oldMetadata;

			//when
			LookupGridStateService.setData(data);

			//then
			expect(lookupGridState.selectedColumn).not.toBe(oldMetadata);
			expect(lookupGridState.selectedColumn).toBe(data.columns[1]);
		}));

		it('should update column selection metadata with the 1st column when actual selected column is not in the new columns', inject(function (lookupGridState, LookupGridStateService) {
			//given
			var oldMetadata = {id: '0018'};
			lookupGridState.selectedColumn = oldMetadata;

			//when
			LookupGridStateService.setData(data);

			//then
			expect(lookupGridState.selectedColumn).not.toBe(oldMetadata);
			expect(lookupGridState.selectedColumn).toBe(data.columns[0]);
		}));

		it('should update column selection metadata with the first column metadata when there is no selected column yet', inject(function (lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.selectedColumn = null;

			//when
			LookupGridStateService.setData(data);

			//then
			expect(lookupGridState.selectedColumn).toBe(data.columns[0]);
		}));

	});

	describe('grid event result state', function() {
		it('should set focused columns', inject(function (lookupGridState, LookupGridStateService) {
			//given
			expect(lookupGridState.columnFocus).toBeFalsy();

			//when
			LookupGridStateService.setColumnFocus('0001');

			//then
			expect(lookupGridState.columnFocus).toBe('0001');
		}));

		it('should set grid selection', inject(function (lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.selectedColumn = null;
			lookupGridState.selectedLine = null;

			//when
			LookupGridStateService.setGridSelection('0001');

			//then
			expect(lookupGridState.selectedColumn).toBe('0001');
		}));

		it('should set columns list to be added', inject(function (lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.addedToLookup = addedToLookupWithSelected;
			lookupGridState.lookupColumnsToAdd = null;

			//when
			LookupGridStateService.setLookupColumnsToAdd();

			//then
			expect(lookupGridState.lookupColumnsToAdd).toEqual([ { id: '0002', name: 'firstname' }, { id: '0003', name: 'lastname' } ] );
		}));

		it('should collect the columns Ids to be added ', inject(function (lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.addedToLookup = addedToLookupWithSelected;

			//when
			LookupGridStateService.setGridSelection('0001');

			//then
			expect(lookupGridState.lookupColumnsToAdd).toEqual([ { id: '0002', name: 'firstname' }, { id: '0003', name: 'lastname' } ]);
		}));

		it('should not collect the columns Ids to be added ', inject(function (lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.addedToLookup = addedToLookupWithSelected;
			var addedCols = ['0018'];
			lookupGridState.lookupColumnsToAdd = addedCols;

			//when
			LookupGridStateService.setGridSelection(null);

			//then
			expect(lookupGridState.lookupColumnsToAdd).toBe(addedCols);
		}));
	});

	describe('reset', function() {
		it('should reset event result state', inject(function(lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.columnFocus = '0001';
			lookupGridState.selectedColumn = '0001';
			lookupGridState.selectedLine = 2;
			lookupGridState.lookupColumnsToAdd = ['0000'];
			lookupGridState.addedToLookup = [{id:'0001', isAdded: true, name:'vfvf'}];

			//when
			LookupGridStateService.reset();

			//then
			expect(lookupGridState.columnFocus).toBe(null);
			expect(lookupGridState.selectedColumn).toBe(null);
			expect(lookupGridState.selectedLine).toBe(null);
			expect(lookupGridState.lookupColumnsToAdd).toEqual([]);
			expect(lookupGridState.addedToLookup).toEqual([]);
		}));
	});

	describe('Lookup Dataset', function() {
		it('should set lookup dataset', inject(function(lookupGridState, LookupGridStateService) {
			//given
			lookupGridState.dataset = null;

			//when
			LookupGridStateService.setDataset(metadata);

			//then
			expect(lookupGridState.dataset).toBe(metadata);
		}));
	});
});
