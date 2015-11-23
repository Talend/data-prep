describe('Grid state service', function () {
    'use strict';

    var data = {
        columns: [
            {id: '0000'},
            {id: '0001'},
            {id: '0002'},
            {id: '0003'}
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

    var previewData = {
        columns: [
            {id: '0000'},
            {id: '0001'},
            {id: '0002'},
            {id: '0003'}
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
        ],
        preview: true
    };

    beforeEach(module('data-prep.services.state'));

    beforeEach(inject(function(gridState) {
        gridState.dataView = new DataViewMock();
    }));

    describe('filter', function() {
        it('should set filters to DataView', inject(function (gridState, GridStateService) {
            //given
            gridState.dataView.setItems(data.records, 'tdpId');
            var filterFnCol1 = function() {
                return function (item) {
                    return item.col1.indexOf('toto') > -1;
                };
            };
            var filterFnCol2 = function() {
                return function (item) {
                    return item.col2.indexOf('toto') > -1;
                };
            };
            var filters = [
                {filterFn: filterFnCol1},
                {filterFn: filterFnCol2}
            ];

            //when
            GridStateService.setFilter(filters, data);

            //then
            expect(gridState.dataView.filter({
                col1: 'mon toto', col2: 'toto tata titi'
            })).toBe(true);
            expect(gridState.dataView.filter({
                col1: 'mon tutu', col2: 'toto tata titi'
            })).toBe(false);
            expect(gridState.dataView.filter({
                col1: 'mon toto', col2: 'tutu tata titi'
            })).toBe(false);
        }));

        it('should grid line stats', inject(function (gridState, GridStateService) {
            //given
            gridState.dataView.setItems(data.records.slice(0, 2), 'tdpId');
            var filterFnCol1 = function() {
                return function (item) {
                    return item.col1.indexOf('toto') > -1;
                };
            };
            var filterFnCol2 = function() {
                return function (item) {
                    return item.col2.indexOf('toto') > -1;
                };
            };
            var filters = [
                {filterFn: filterFnCol1},
                {filterFn: filterFnCol2}
            ];

            //when
            GridStateService.setFilter(filters, data);

            //then
            expect(gridState.nbLines).toBe(2);
            expect(gridState.nbTotalLines).toBe(12);
            expect(gridState.displayLinesPercentage).toBe('17');
        }));
    });

    describe('data', function() {
        it('should set data to DataView', inject(function (gridState, GridStateService) {
            //given
            expect(gridState.dataView.getItems()).not.toBe(data.records);

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.dataView.getItems()).toBe(data.records);
        }));

        it('should grid line stats', inject(function (gridState, GridStateService) {
            //given
            gridState.nbLines = null;
            gridState.nbTotalLines = null;
            gridState.displayLinesPercentage = null;

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.nbLines).toBe(12);
            expect(gridState.nbTotalLines).toBe(12);
            expect(gridState.displayLinesPercentage).toBe('100');
        }));

        it('should update column selection metadata with the new metadata corresponding to the selected id', inject(function (gridState, GridStateService) {
            //given
            var oldMetadata = {id: '0001'};
            gridState.selectedColumn = oldMetadata;

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.selectedColumn).not.toBe(oldMetadata);
            expect(gridState.selectedColumn).toBe(data.columns[1]);
        }));

        it('should update column selection metadata with the 1st column when actual selected column is not in the new columns', inject(function (gridState, GridStateService) {
            //given
            var oldMetadata = {id: '0018'};
            gridState.selectedColumn = oldMetadata;

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.selectedColumn).not.toBe(oldMetadata);
            expect(gridState.selectedColumn).toBe(data.columns[0]);
        }));

        it('should update column selection metadata with the first column metadata when there is no selected column yet', inject(function (gridState, GridStateService) {
            //given
            gridState.selectedColumn = null;

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.selectedColumn).toBe(data.columns[0]);
        }));

        it('should not change selected column when data is preview data', inject(function (gridState, GridStateService) {
            //given
            var oldMetadata = {id: '0001'};
            gridState.selectedColumn = oldMetadata;

            //when
            GridStateService.setData(previewData);

            //then
            expect(gridState.selectedColumn).toBe(oldMetadata);
        }));
    });

    describe('grid event result state', function() {
        it('should set focused columns', inject(function (gridState, GridStateService) {
            //given
            expect(gridState.columnFocus).toBeFalsy();

            //when
            GridStateService.setColumnFocus('0001');

            //then
            expect(gridState.columnFocus).toBe('0001');
        }));

        it('should set grid selection', inject(function (gridState, GridStateService) {
            //given
            gridState.selectedColumn = null;
            gridState.selectedLine = null;

            //when
            GridStateService.setGridSelection('0001', 18);

            //then
            expect(gridState.selectedColumn).toBe('0001');
            expect(gridState.selectedLine).toBe(18);
        }));
    });

    describe('reset', function() {
        it('should reset event result state', inject(function(gridState, GridStateService) {
            //given
            gridState.columnFocus = '0001';
            gridState.selectedColumn = '0001';
            gridState.selectedLine = 2;

            //when
            GridStateService.reset();

            //then
            expect(gridState.columnFocus).toBe(null);
            expect(gridState.selectedColumn).toBe(null);
            expect(gridState.selectedLine).toBe(null);
        }));
    });

    describe('filtered values', function() {
        var filteredRecords = [
            {'0000': '0000', '0001': 'Jimmy', '0002': 'Somsanith', '0003': 'DEV'},
            {'0000': '0001', '0001': 'Charles', '0002': 'Nguyen', '0003': 'DEV'},
            {'0000': '0002', '0001': 'Stéphane', '0002': 'Mallet', '0003': 'CP'}
        ];

        beforeEach(inject(function(gridState) {
            gridState.dataView.setItems(filteredRecords);
        }));

        it('should update filtered records on filter change', inject(function(gridState, GridStateService) {
            //given
            gridState.filteredRecords = null;
            gridState.selectedColumn = {
                id: '0003'
            };

            //when
            GridStateService.setFilter([], {columns: [], records: filteredRecords});

            //then
            expect(gridState.filteredRecords).toEqual(filteredRecords);
        }));

        it('should update filtered records occurrences on filter change', inject(function(gridState, GridStateService) {
            //given
            gridState.filteredOccurences = null;
            gridState.selectedColumn = {
                id: '0003'
            };

            //when
            GridStateService.setFilter([], {columns: [], records: filteredRecords});

            //then
            expect(gridState.filteredOccurences).toEqual({
                'DEV': 2,
                'CP': 1
            });
        }));

        it('should update filtered records on data change', inject(function(gridState, GridStateService) {
            //given
            gridState.filteredRecords = null;
            gridState.selectedColumn = {
                id: '0003'
            };

            //when
            GridStateService.setData({columns: [{id: '0003'}], records: filteredRecords});

            //then
            expect(gridState.filteredRecords).toEqual(filteredRecords);
        }));

        it('should update filtered records occurrences on data change', inject(function(gridState, GridStateService) {
            //given
            gridState.filteredOccurences = null;
            gridState.selectedColumn = {
                id: '0003'
            };

            //when
            GridStateService.setData({columns: [{id: '0003'}], records: filteredRecords});

            //then
            expect(gridState.filteredOccurences).toEqual({
                'DEV': 2,
                'CP': 1
            });
        }));

        it('should update filtered records occurrences on grid selection', inject(function(gridState, GridStateService) {
            //given
            gridState.filteredRecords = filteredRecords;
            gridState.filteredOccurences = null;

            var column = {
                id: '0003'
            };

            //when
            GridStateService.setGridSelection(column, null);

            //then
            expect(gridState.filteredOccurences).toEqual({
                'DEV': 2,
                'CP': 1
            });
        }));
    });
});
