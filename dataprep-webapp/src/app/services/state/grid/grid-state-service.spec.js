/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DataViewMock from '../../../../mocks/DataView.mock';

describe('Grid state service', () => {
    'use strict';

    const data = {
        metadata: {
            columns: [
                { id: '0000', type: 'integer' },
                { id: '0001', type: 'string' },
                { id: '0002', type: 'decimal' },
                { id: '0003', type: 'date' },
            ],
        },
        records: [
            { tdpId: 0, firstname: 'Tata' },
            { tdpId: 1, firstname: 'Tetggggge' },
            { tdpId: 2, firstname: 'Titi' },
            { tdpId: 3, firstname: 'Toto' },
            { tdpId: 4, name: 'AMC Gremlin' },
            { tdpId: 5, firstname: 'Tyty' },
            { tdpId: 6, firstname: 'Papa' },
            { tdpId: 7, firstname: 'Pepe' },
            { tdpId: 8, firstname: 'Pipi' },
            { tdpId: 9, firstname: 'Popo' },
            { tdpId: 10, firstname: 'Pupu' },
            { tdpId: 11, firstname: 'Pypy' },
        ],
    };

    const previewData = {
        metadata: {
            columns: [
                { id: '0000' },
                { id: '0001' },
                { id: '0002' },
                { id: '0003' },
            ],
        },
        records: [
            { tdpId: 0, firstname: 'Tata' },
            { tdpId: 1, firstname: 'Tetggggge' },
            { tdpId: 2, firstname: 'Titi' },
            { tdpId: 3, firstname: 'Toto' },
            { tdpId: 4, name: 'AMC Gremlin' },
            { tdpId: 5, firstname: 'Tyty' },
            { tdpId: 6, firstname: 'Papa' },
            { tdpId: 7, firstname: 'Pepe' },
            { tdpId: 8, firstname: 'Pipi' },
            { tdpId: 9, firstname: 'Popo' },
            { tdpId: 10, firstname: 'Pupu' },
            { tdpId: 11, firstname: 'Pypy' },
        ],
        preview: true,
    };

    beforeEach(angular.mock.module('data-prep.services.state'));

    beforeEach(inject((gridState) => {
        gridState.dataView = new DataViewMock();
    }));

    describe('filter', () => {
        it('should set filters to DataView', inject((gridState, GridStateService) => {
            //given
            gridState.selectedColumns = [];
            gridState.dataView.setItems(data.records, 'tdpId');
            const filterFnCol1 = () => (item) => (item.col1.indexOf('toto') > -1);
            const filterFnCol2 = () => (item) => (item.col2.indexOf('toto') > -1);
            const filters = [
                { filterFn: filterFnCol1 },
                { filterFn: filterFnCol2 },
            ];

            //when
            GridStateService.setFilter(filters, data);

            //then
            const filterFn = gridState.dataView.filter;
            expect(filterFn({ col1: 'mon toto', col2: 'toto tata titi' })).toBe(true);
            expect(filterFn({ col1: 'mon tutu', col2: 'toto tata titi' })).toBe(false);
            expect(filterFn({ col1: 'mon toto', col2: 'tutu tata titi' })).toBe(false);
        }));

        it('should grid line stats', inject((gridState, GridStateService) => {
            //given
            gridState.selectedColumns = [];
            gridState.dataView.setItems(data.records.slice(0, 2), 'tdpId');
            const filterFnCol1 = () => (item) => (item.col1.indexOf('toto') > -1);
            const filterFnCol2 = () => (item) => (item.col2.indexOf('toto') > -1);
            const filters = [
                { filterFn: filterFnCol1 },
                { filterFn: filterFnCol2 },
            ];

            //when
            GridStateService.setFilter(filters, data);

            //then
            expect(gridState.nbLines).toBe(2);
            expect(gridState.nbTotalLines).toBe(12);
            expect(gridState.displayLinesPercentage).toBe('17');
        }));
    });

    describe('data', () => {
        it('should set data to DataView', inject((gridState, GridStateService) => {
            //given
            expect(gridState.dataView.getItems()).not.toBe(data.records);
            gridState.selectedColumns = [];

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.dataView.getItems()).toBe(data.records);
        }));

        it('should grid line stats', inject((gridState, GridStateService) => {
            //given
            gridState.nbLines = null;
            gridState.nbTotalLines = null;
            gridState.displayLinesPercentage = null;
            gridState.selectedColumns = [];

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.nbLines).toBe(12);
            expect(gridState.nbTotalLines).toBe(12);
            expect(gridState.displayLinesPercentage).toBe('100');
        }));

        it('should update numeric columns', inject((gridState, GridStateService) => {
            //given
            gridState.numericColumns = [];
            gridState.selectedColumns = [];

            //when
            GridStateService.setData(data);

            //then
            expect(gridState.numericColumns).toEqual([
                { id: '0000', type: 'integer' },
                { id: '0002', type: 'decimal' },
            ]);
        }));

        describe('selection', () => {
            it('should update line selection row with the new row corresponding to the selected index (not id)', inject((gridState, GridStateService) => {
                //given
                const oldRow = { tdpId: '0125' };
                gridState.lineIndex = 2;
                gridState.selectedLine = oldRow;
                gridState.selectedColumns = [];

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedLine).toBe(data.records[2]);
            }));

            it('should NOT change selected line row when data is preview data', inject((gridState, GridStateService) => {
                const oldRow = { tdpId: '0125' };
                gridState.lineIndex = 2;
                gridState.selectedLine = oldRow;
                gridState.selectedColumns = [];

                //when
                GridStateService.setData(previewData);

                //then
                expect(gridState.selectedLine).toBe(oldRow);
            }));

            it('should NOT change select line row when there is no selected line yet', inject((gridState, GridStateService) => {
                gridState.lineIndex = null;
                gridState.selectedLine = null;
                gridState.selectedColumns = [];

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedLine).toBe(null);
            }));

            it('should update columns metadata with the new metadata', inject((gridState, GridStateService) => {
                //given
                const oldMetadata1 = { id: '0001' };
                const oldMetadata2 = { id: '0002' };
                gridState.selectedColumns = [oldMetadata1, oldMetadata2];

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedColumns[0]).not.toBe(oldMetadata1);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[1]);

                expect(gridState.selectedColumns[1]).not.toBe(oldMetadata2);
                expect(gridState.selectedColumns[1]).toBe(data.metadata.columns[2]);
            }));

            it('should update column metadata with the new metadata corresponding to the selected id', inject((gridState, GridStateService) => {
                //given
                const oldMetadata = { id: '0001' };
                gridState.selectedColumns = [oldMetadata];

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedColumns[0]).not.toBe(oldMetadata);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[1]);
            }));

            it('should update column metadata with the 1st column when actual selected column is not in the new columns', inject((gridState, GridStateService) => {
                //given
                const oldMetadata = { id: '0018' };
                gridState.selectedColumns = [oldMetadata];

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedColumns[0]).not.toBe(oldMetadata);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[0]);
            }));

            it('should update column metadata with the first column metadata when there is no selected column yet', inject((gridState, GridStateService) => {
                //given
                gridState.selectedColumns = [];
                gridState.lineIndex = null;

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[0]);
            }));

            it('should NOT update column metadata when there is no selected column but a selected line', inject((gridState, GridStateService) => {
                //given
                gridState.selectedColumns = [];
                gridState.lineIndex = 2;

                //when
                GridStateService.setData(data);

                //then
                expect(gridState.selectedColumns).toEqual([]);
            }));

            it('should not change selected column when data is preview data', inject((gridState, GridStateService) => {
                //given
                const oldMetadata = { id: '0001' };
                gridState.selectedColumns = [oldMetadata];

                //when
                GridStateService.setData(previewData);

                //then
                expect(gridState.selectedColumns[0]).toBe(oldMetadata);
            }));
        });
    });

    describe('grid event state', () => {
        it('should set focused columns', inject((gridState, GridStateService) => {
            //given
            expect(gridState.columnFocus).toBeFalsy();

            //when
            GridStateService.setColumnFocus('0001');

            //then
            expect(gridState.columnFocus).toBe('0001');
        }));

        it('should set grid selection', inject((gridState, GridStateService) => {
            //given
            gridState.dataView.setItems(data.records, 'tdpId');
            gridState.selectedColumns = [];
            gridState.selectedLine = null;

            //when
            GridStateService.setGridSelection(['0001'], 2);

            //then
            expect(gridState.selectedColumns[0]).toBe('0001');
            expect(gridState.selectedLine).toBe(data.records[2]);
        }));

        it('should set lineIndex to null when we have no line selection', inject((gridState, GridStateService) => {
            //given
            gridState.dataView.setItems(data.records, 'tdpId');
            gridState.lineIndex = 1;
            gridState.selectedLine = { tdpId: '125' };

            //when
            GridStateService.setGridSelection('0001');

            //then
            expect(gridState.lineIndex).toBe(null);
            expect(gridState.selectedLine).toBe(null);
        }));
    });

    describe('reset', () => {
        it('should reset event result state', inject((gridState, GridStateService) => {
            //given
            gridState.columnFocus = '0001';
            gridState.selectedColumns = ['0001'];
            gridState.semanticDomains = [{}];
            gridState.primitiveTypes = [{}];
            gridState.selectedLine = 2;
            gridState.numericColumns = [{}, {}];
            gridState.filteredRecords = [{}, {}];
            gridState.filteredOccurences = { toto: 3 };
            gridState.columns = [{}];
            gridState.showTooltip = true;
            gridState.tooltip = {'htmlstr' : ''};
            gridState.tooltipRuler = {};

            //when
            GridStateService.reset();

            //then
            expect(gridState.columnFocus).toBe(null);
            expect(gridState.selectedColumns).toEqual([]);
            expect(gridState.semanticDomains).toEqual(null);
            expect(gridState.primitiveTypes).toEqual(null);
            expect(gridState.selectedLine).toBe(null);
            expect(gridState.numericColumns).toEqual([]);
            expect(gridState.filteredRecords).toEqual([]);
            expect(gridState.filteredOccurences).toEqual({});
            expect(gridState.columns).toEqual([]);
            expect(gridState.showTooltip).toEqual(false);
            expect(gridState.tooltip).toEqual({});
            expect(gridState.tooltipRuler).toEqual(null);
        }));
    });

    describe('selected colums', () => {
        describe('toggleColumnSelection', () => {
            it('should select a column', inject((gridState, GridStateService) => {
                //given
                const col = { id: '0001' };
                const newCol = { id: '0002' };
                gridState.selectedColumns = [col];

                //when
                GridStateService.toggleColumnSelection(newCol);

                //then
                expect(gridState.selectedColumns.length).toBe(2);
                expect(gridState.selectedColumns[0]).toBe(col);
                expect(gridState.selectedColumns[1]).toBe(newCol);
            }));

            it('should unselect a selected column', inject((gridState, GridStateService) => {
                //given
                const col1 = { id: '0001' };
                const col2 = { id: '0002' };
                gridState.selectedColumns = [col1, col2];

                //when
                GridStateService.toggleColumnSelection(col2);

                //then
                expect(gridState.selectedColumns.length).toBe(1);
                expect(gridState.selectedColumns[0]).toBe(col1);
            }));
        });

        describe('changeRangeSelection', () => {

            it('should select a columns', inject((gridState, GridStateService) => {
                //given
                gridState.columns =  data.metadata.columns;
                gridState.selectedColumns = [];

                //when
                GridStateService.changeRangeSelection(data.metadata.columns[1]);

                //then
                expect(gridState.selectedColumns.length).toBe(1);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[1]);
            }));


            it('should select a columns range if min < index', inject((gridState, GridStateService) => {
                //given
                gridState.columns =  data.metadata.columns;
                gridState.selectedColumns = [data.metadata.columns[2], data.metadata.columns[3]];

                //when
                GridStateService.changeRangeSelection(data.metadata.columns[1]);

                //then
                expect(gridState.selectedColumns.length).toBe(3);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[1]);
                expect(gridState.selectedColumns[1]).toBe(data.metadata.columns[2]);
                expect(gridState.selectedColumns[2]).toBe(data.metadata.columns[3]);
            }));

            it('should select a columns range if min >= index', inject((gridState, GridStateService) => {
                //given
                gridState.columns =  data.metadata.columns;
                gridState.selectedColumns = [data.metadata.columns[0], data.metadata.columns[1]];

                //when
                GridStateService.changeRangeSelection(data.metadata.columns[2]);

                //then
                expect(gridState.selectedColumns.length).toBe(3);
                expect(gridState.selectedColumns[0]).toBe(data.metadata.columns[0]);
                expect(gridState.selectedColumns[1]).toBe(data.metadata.columns[1]);
                expect(gridState.selectedColumns[2]).toBe(data.metadata.columns[2]);
            }));

        });
    });

    describe('domains & types', () => {
        it('should set semantic domains', inject((gridState, GridStateService) => {
            // given
            const semanticDomains = [];

            // when
            GridStateService.setSemanticDomains(semanticDomains);

            // then
            expect(gridState.semanticDomains).toBe(semanticDomains);
        }));

        it('should set primitive types', inject((gridState, GridStateService) => {
            // given
            const primitiveTypes = [];

            // when
            GridStateService.setPrimitiveTypes(primitiveTypes);

            // then
            expect(gridState.primitiveTypes).toBe(primitiveTypes);
        }));
    });

    describe('filtered values', () => {
        const filteredRecords = [
            { '0000': '0000', '0001': 'Jimmy', '0002': 'Somsanith', '0003': 'DEV' },
            { '0000': '0001', '0001': 'Charles', '0002': 'Nguyen', '0003': 'DEV' },
            { '0000': '0002', '0001': 'Stéphane', '0002': 'Mallet', '0003': 'CP' },
        ];

        beforeEach(inject((gridState) => {
            gridState.dataView.setItems(filteredRecords);
        }));

        it('should update filtered records on filter change', inject((gridState, GridStateService) => {
            //given
            gridState.filteredRecords = null;
            gridState.selectedColumns = [{ id: '0003' }];

            //when
            GridStateService.setFilter([], { columns: [], records: filteredRecords });

            //then
            expect(gridState.filteredRecords).toEqual(filteredRecords);
        }));

        it('should update filtered records occurrences on filter change', inject((gridState, GridStateService) => {
            //given
            gridState.filteredOccurences = null;
            gridState.selectedColumns = [{ id: '0003' }];

            //when
            GridStateService.setFilter([], { columns: [], records: filteredRecords });

            //then
            expect(gridState.filteredOccurences).toEqual({ DEV: 2, CP: 1 });
        }));

        it('should update filtered records on data change', inject((gridState, GridStateService) => {
            //given
            gridState.filteredRecords = null;
            gridState.selectedColumns = [{ id: '0003' }];

            //when
            GridStateService.setData({
                metadata: { columns: [{ id: '0003', type: 'string' }] },
                records: filteredRecords,
            });

            //then
            expect(gridState.filteredRecords).toEqual(filteredRecords);
        }));

        it('should update filtered records occurrences on data change', inject((gridState, GridStateService) => {
            //given
            gridState.filteredOccurences = null;
            gridState.selectedColumns = [{ id: '0003' }];

            //when
            GridStateService.setData({
                metadata: { columns: [{ id: '0003', type: 'string' }] },
                records: filteredRecords,
            });

            //then
            expect(gridState.filteredOccurences).toEqual({ DEV: 2, CP: 1 });
        }));

        it('should update filtered records occurrences on grid selection', inject((gridState, GridStateService) => {
            //given
            gridState.filteredRecords = filteredRecords;
            gridState.filteredOccurences = null;
            const column = { id: '0003' };

            //when
            GridStateService.setGridSelection([column], null);

            //then
            expect(gridState.filteredOccurences).toEqual({ DEV: 2, CP: 1 });
        }));
    });
});
