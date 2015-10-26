describe('Datagrid service', function () {
    'use strict';

    var stateMock, dataViewMock;

    var originalData = {
        records: [
            {tdpId: 0, firstname: 'Tata'},
            {tdpId: 1, firstname: 'Tete'},
            {tdpId: 2, firstname: 'Titi'},
            {tdpId: 3, firstname: 'Toto'},
            {tdpId: 4, firstname: 'Tutu'},
            {tdpId: 5, firstname: 'Tyty'},
            {tdpId: 6, firstname: 'Papa'},
            {tdpId: 7, firstname: 'Pepe'},
            {tdpId: 8, firstname: 'Pipi'},
            {tdpId: 9, firstname: 'Popo'},
            {tdpId: 10, firstname: 'Pupu'},
            {tdpId: 11, firstname: 'Pypy'}
        ],
        columns: [{id: '0000', name: 'lastname'}, {id: '0001', name: 'firstname'}]
    };

    //diff result corresponding to gridRangeIndex
    var diff = {
        records: [
            {tdpId: 1, firstname: 'Tete'},
            {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, //insert new row
            {tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete'}, //row is deleted in preview
            {tdpId: 6, firstname: 'Papa'},
            {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}, //firstname is updated in preview
            {tdpId: 8, firstname: 'Pipi'}
        ],
        columns: [{id: '0000', name: 'lastname'}, {id: '0001', name: 'firstname'}, {id: '0002', name: 'age'}]
    };

    beforeEach(module('data-prep.services.playground', function ($provide) {
        dataViewMock = new DataViewMock();
        stateMock = {playground: {grid: {dataView: dataViewMock}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function (StateService) {
        spyOn(StateService, 'setCurrentData').and.returnValue();
    }));

    describe('grid data', function () {
        it('should update data records', inject(function (DatagridService, StateService) {
            //given
            stateMock.playground.dataset = {name: 'my dataset'};
            stateMock.playground.data = {columns: [], records: []};

            var data = {columns: [], 'records': [{tdpId: 1, col: 'value'}]};

            //when
            DatagridService.updateData(data);

            //then
            expect(StateService.setCurrentData).toHaveBeenCalledWith(data);
        }));
    });

    describe('focus on column', function () {
        it('should navigate to the column having the highest Id', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: '0000', name: 'column 1', type: 'string'},
                    {id: '0001', name: 'column 2', type: 'numeric'},
                    {id: '0002', name: 'column 2', type: 'numeric'},
                    {id: '0003', name: 'column 3', type: 'integer'}], records: []
            };

            //the result of the 2nd column duplication
            var data = {
                columns: [
                    {id: '0000', name: 'column 1', type: 'string'},
                    {id: '0001', name: 'column 2', type: 'numeric'},
                    {id: '0004', name: 'column 1', type: 'string'},
                    {id: '0002', name: 'column 2', type: 'numeric'},
                    {id: '0003', name: 'column 3', type: 'integer'}], records: []
            };

            //when
            DatagridService.updateData(data);

            //then
            expect(DatagridService.focusedColumn).toBe('0004');
        }));
    });

    describe('utils functions', function () {
        it('should return the rows containing non empty searched value', inject(function (DatagridService) {
            //given
            var data = {
                columns: [], records: [
                    {tdpId: 1, text: 'mon toto est ici'},
                    {tdpId: 2, text: 'ma tata est la'},
                    {tdpId: 3, text: 'la tata est ici'},
                    {tdpId: 4, text: 'mon toto est la'},
                    {tdpId: 5, text: 'mi titi est ici'},
                    {tdpId: 6, text: 'mi titi est la'},
                    {tdpId: 7, text: 'mi titi est ici'}
                ]
            };
            stateMock.playground.grid.dataView.setItems(data.records);

            //when
            var rowsId = DatagridService.getSameContentConfig('text', 'mi titi est ici', 'myClass');

            //then
            expect(rowsId).toEqual({4: {text: 'myClass'}, 6: {text: 'myClass'}});
        }));

        it('should return every column id', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };

            //when
            var allCols = DatagridService.getColumns(false, false);

            //then
            expect(allCols).toEqual([{id: 'col1', name: 'column 1'},
                {id: 'col2', name: 'column 2'},
                {id: 'col3', name: 'column 3'},
                {id: 'col4', name: 'column 4'},
                {id: 'col5', name: 'column 5'},
                {id: 'col6', name: 'column 6'},
                {id: 'col7', name: 'column 7'}]);
        }));

        it('should return non numeric col ids', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };

            //when
            var allCols = DatagridService.getColumns(true, false);

            //then
            expect(allCols).toEqual([{id: 'col1', name: 'column 1'},
                {id: 'col6', name: 'column 6'},
                {id: 'col7', name: 'column 7'}]);
        }));

        it('should return non boolean col ids', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };

            //when
            var allCols = DatagridService.getColumns(false, true);

            //then
            expect(allCols).toEqual([{id: 'col1', name: 'column 1'},
                {id: 'col2', name: 'column 2'},
                {id: 'col3', name: 'column 3'},
                {id: 'col4', name: 'column 4'},
                {id: 'col5', name: 'column 5'},
                {id: 'col7', name: 'column 7'}]);
        }));

        it('should return non boolean and non numeric col ids', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };

            //when
            var allCols = DatagridService.getColumns(true, true);

            //then
            expect(allCols).toEqual([{id: 'col1', name: 'column 1'},
                {id: 'col7', name: 'column 7'}]);
        }));

        it('should return all numeric columns', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };

            //when
            var allCols = DatagridService.getNumericColumns();

            //then
            expect(allCols).toEqual([
                {id: 'col2', name: 'column 2', type: 'numeric'},
                {id: 'col3', name: 'column 3', type: 'integer'},
                {id: 'col4', name: 'column 4', type: 'float'},
                {id: 'col5', name: 'column 5', type: 'double'}]);
        }));

        it('should return numeric columns excluding a specific column', inject(function (DatagridService) {
            //given
            stateMock.playground.data = {
                columns: [
                    {id: 'col1', name: 'column 1', type: 'string'},
                    {id: 'col2', name: 'column 2', type: 'numeric'},
                    {id: 'col3', name: 'column 3', type: 'integer'},
                    {id: 'col4', name: 'column 4', type: 'float'},
                    {id: 'col5', name: 'column 5', type: 'double'},
                    {id: 'col6', name: 'column 6', type: 'boolean'},
                    {id: 'col7', name: 'column 7', type: 'string'}
                ], records: []
            };
            var columnToExclude = stateMock.playground.data.columns[1];

            //when
            var allCols = DatagridService.getNumericColumns(columnToExclude);

            //then
            expect(allCols).toEqual([
                {id: 'col3', name: 'column 3', type: 'integer'},
                {id: 'col4', name: 'column 4', type: 'float'},
                {id: 'col5', name: 'column 5', type: 'double'}]);
        }));
    });

    describe('preview operations', function () {
        beforeEach(inject(function () {
            dataViewMock.setItems(originalData.records, 'tdpId');
            stateMock.playground.grid.dataView = dataViewMock;
            stateMock.playground.data = originalData;

            spyOn(dataViewMock, 'beginUpdate').and.returnValue();
            spyOn(dataViewMock, 'endUpdate').and.returnValue();
            spyOn(dataViewMock, 'insertItem').and.returnValue();
            spyOn(dataViewMock, 'deleteItem').and.returnValue();
            spyOn(dataViewMock, 'updateItem').and.returnValue();
        }));

        it('should create executor that match the preview data', inject(function (DatagridService) {
            //when
            var executor = DatagridService.previewDataExecutor(diff);

            //then
            expect(executor.columns).toBe(diff.columns);
            expect(executor.preview).toBe(true);
            expect(executor.instructions).toEqual([
                {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                {type: 'REPLACE', row: {tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete'}},
                {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
            ]);
        }));

        it('should apply nothing when executor is falsy', inject(function (DatagridService) {
            //given
            var executor = null;

            //when
            DatagridService.execute(executor);

            //then
            expect(stateMock.playground.grid.dataView.insertItem).not.toHaveBeenCalled();
            expect(stateMock.playground.grid.dataView.deleteItem).not.toHaveBeenCalled();
            expect(stateMock.playground.grid.dataView.updateItem).not.toHaveBeenCalled();

            expect(stateMock.playground.data).toBe(originalData);
        }));

        it('should apply executor', inject(function (DatagridService) {
            //given
            var executor = {
                columns: diff.columns,
                preview: true,
                instructions: [
                    {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                    {type: 'DELETE', row: {tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete'}},
                    {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
                ]
            };

            //when
            DatagridService.execute(executor);

            //then
            expect(stateMock.playground.grid.dataView.insertItem).toHaveBeenCalledWith(2, {
                tdpId: 2,
                firstname: 'Titi Bis',
                __tdpRowDiff: 'new'
            });
            expect(stateMock.playground.grid.dataView.deleteItem).toHaveBeenCalledWith(3);
            expect(stateMock.playground.grid.dataView.updateItem).toHaveBeenCalledWith(7, {
                tdpId: 7,
                firstname: 'Pepe 2',
                __tdpDiff: {firstname: 'update'}
            });

        }));

        it('should set focus on created columns when applying executor', inject(function (DatagridService) {
            //given
            var executor = {
                columns: diff.columns,
                preview: true,
                instructions: [
                    {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                    {type: 'DELETE', row: {tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete'}},
                    {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
                ]
            };

            //when
            DatagridService.execute(executor);

            //then
            expect(DatagridService.focusedColumn).toBe('0002');
        }));

        it('should NOT set focus on any column when applying executor if there are no created columns', inject(function (DatagridService) {
            //given
            var executor = {
                columns: originalData.columns,
                preview: true,
                instructions: [
                    {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                    {type: 'DELETE', row: {tdpId: 3, firstname: 'Toto', __tdpRowDiff: 'delete'}},
                    {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
                ]
            };

            //when
            DatagridService.execute(executor);

            //then
            expect(DatagridService.focusedColumn).toBeFalsy();
        }));

        it('should return reverter on executor application', inject(function (DatagridService) {
            //given
            var executor = {
                columns: diff.columns,
                preview: true,
                instructions: [
                    {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                    {type: 'DELETE', row: {tdpId: 3, firstname: 'Toto'}},
                    {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
                ]
            };

            //when
            var reverter = DatagridService.execute(executor);

            //then
            expect(reverter.columns).toBe(originalData.columns);
            expect(DatagridService.focusedColumn).toBe('0002');
            expect(reverter.preview).toBeFalsy();
            expect(reverter.instructions).toEqual([
                {type: 'DELETE', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}},
                {type: 'INSERT', row: {tdpId: 3, firstname: 'Toto'}, index: 3},
                {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe'}}
            ]);
        }));

        it('should replace service data', inject(function (DatagridService, StateService) {
            //given
            var executor = {
                columns: diff.columns,
                preview: true,
                instructions: [
                    {type: 'INSERT', row: {tdpId: 2, firstname: 'Titi Bis', __tdpRowDiff: 'new'}, index: 2},
                    {type: 'DELETE', row: {tdpId: 3, firstname: 'Toto'}},
                    {type: 'REPLACE', row: {tdpId: 7, firstname: 'Pepe 2', __tdpDiff: {firstname: 'update'}}}
                ]
            };

            //when
            DatagridService.execute(executor);

            //then

            var modifiedData = {
                columns: executor.columns,
                records: originalData.records,
                preview: executor.preview
            };

            expect(StateService.setCurrentData).toHaveBeenCalledWith(modifiedData);
            expect(DatagridService.focusedColumn).toBe('0002');
        }));
    });
});