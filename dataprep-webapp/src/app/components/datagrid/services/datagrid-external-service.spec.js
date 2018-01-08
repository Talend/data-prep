/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Datagrid external service', () => {
    let gridMock;
    let stateMock;

    const gridColumns = [
        { id: '0000', field: 'col0', tdpColMetadata: { id: '0000', name: 'col0' } },
        { id: '0001', field: 'col1', tdpColMetadata: { id: '0001', name: 'col1' } },
        { id: '0002', field: 'col2', tdpColMetadata: { id: '0002', name: 'col2' } },
        { id: '0003', field: 'col3', tdpColMetadata: { id: '0003', name: 'col3' } },
        { id: '0004', field: 'col4', tdpColMetadata: { id: '0004', name: 'col4' } },
        { id: 'tdpId', field: 'tdpId', tdpColMetadata: { id: 'tdpId', name: 'tdpId' } },
    ];

    beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
        stateMock = {
            playground: {
                preparation : {id: 'abcd'},
                grid: {},
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject((StatisticsService, StateService, TransformationService, LookupService, StorageService) => {
        gridMock = new SlickGridMock();
        gridMock.initColumnsMock(gridColumns);

        spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();
        spyOn(gridMock.onScroll, 'subscribe').and.returnValue();

        spyOn(StateService, 'selectTransformationsTab').and.returnValue();
        spyOn(StatisticsService, 'updateStatistics').and.returnValue();
        spyOn(StatisticsService, 'reset').and.returnValue();
        spyOn(TransformationService, 'initTransformations').and.returnValue();
        spyOn(StorageService, 'setSelectedColumns').and.returnValue();
    }));

    beforeEach(() => {
        jasmine.clock().install();
    });

    afterEach(() => {
        jasmine.clock().uninstall();
    });

    describe('on creation', () => {
        it('should add scroll listener', inject((DatagridExternalService) => {
            // when
            DatagridExternalService.init(gridMock);

            // then
            expect(gridMock.onScroll.subscribe).toHaveBeenCalled();
        }));
    });

    describe('update grid range index', () => {
        it('should add scroll listener', inject((PreviewService, DatagridExternalService) => {
            // given
            const range = [5, 15];
            gridMock.initRenderedRangeMock(range);
            DatagridExternalService.init(gridMock);

            // when
            DatagridExternalService.updateGridRangeIndex();

            // then
            expect(PreviewService.gridRangeIndex).toBe(range);
        }));
    });

    describe('update right panel', () => {
        describe('tab selection', () => {
            it('should select "COLUMN" tab if no tab is provided', inject((DatagridExternalService, StateService) => {
                // given
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                expect(StateService.selectTransformationsTab).not.toHaveBeenCalled();

                // when
                DatagridExternalService.updateSuggestionPanel();

                // then
                expect(StateService.selectTransformationsTab).toHaveBeenCalledWith('column');
            }));

            it('should select "LINE" tab when there is no selected column', inject((DatagridExternalService, StateService) => {
                // given
                stateMock.playground.grid.selectedLine = { tdpId: 125 };
                stateMock.playground.grid.selectedColumns = [];
                expect(StateService.selectTransformationsTab).not.toHaveBeenCalled();

                // when
                DatagridExternalService.updateSuggestionPanel();

                // then
                expect(StateService.selectTransformationsTab).toHaveBeenCalledWith('line');
            }));

            it('should init dataset tab actions', inject((DatagridExternalService, TransformationService) => {
                // given
                stateMock.playground.grid.selectedColumns = [];

                // when
                DatagridExternalService.updateSuggestionPanel();

                // then
                expect(TransformationService.initTransformations).toHaveBeenCalledWith('dataset');
            }));
        });

        describe('charts', () => {
            it('should reset charts when there is no selected column', inject((DatagridExternalService, StatisticsService) => {
                // given
                stateMock.playground.grid.selectedLine = { tdpId: 125 };
                stateMock.playground.grid.selectedColumns = [];
                expect(StatisticsService.reset).not.toHaveBeenCalled();

                // when
                DatagridExternalService.updateSuggestionPanel();

                // then
                expect(StatisticsService.reset).toHaveBeenCalled();
            }));

            it('should update charts when there is a selected column', inject((DatagridExternalService, StatisticsService) => {
                // given
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();

                // when
                DatagridExternalService.updateSuggestionPanel();

                // then
                expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            }));

            it('should reset statistics when multiple columns are selected', inject((DatagridExternalService, StatisticsService) => {
                //given
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }, { id: '0002' }];
                DatagridExternalService.lastSelectedColumn = stateMock.playground.grid.selectedColumns[0];
                DatagridExternalService.lastSelectedColumnsNumber = 1;

                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(StatisticsService.reset).toHaveBeenCalled();
            }));
        });

        describe('transformations', () => {
            describe('line scope', () => {
                it('should update when there is a selected line', inject((DatagridExternalService, TransformationService) => {
                    // given
                    stateMock.playground.grid.selectedLine = { tdpId: 125 };
                    stateMock.playground.grid.selectedColumns = [];

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(TransformationService.initTransformations).toHaveBeenCalledWith('line');
                }));

                it('should NOT update when there is no selected line', inject((DatagridExternalService, TransformationService) => {
                    // given
                    stateMock.playground.grid.selectedLine = null;
                    stateMock.playground.grid.selectedColumns = [{ id: '0001' }];

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(TransformationService.initTransformations).not.toHaveBeenCalledWith('line');
                }));

                it('should NOT update when selected line has not changed', inject((DatagridExternalService, TransformationService) => {
                    // given
                    const selectedLine = { tdpId: 125 };
                    stateMock.playground.grid.selectedLine = selectedLine;
                    DatagridExternalService.lastSelectedLine = selectedLine;

                    stateMock.playground.grid.selectedColumns = [{ id: '0001' }];

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(TransformationService.initTransformations).not.toHaveBeenCalledWith('line');
                }));
            });

            describe('column scope', () => {
                it('should update when there is a selected column', inject((DatagridExternalService, TransformationService) => {
                    // given
                    stateMock.playground.grid.selectedColumns = [{ id: '0001' }];

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(TransformationService.initTransformations).toHaveBeenCalled();
                }));

                it('should save selected column in LocalStorage', inject((DatagridExternalService, StorageService) => {
                    // given
                    stateMock.playground.preparation = {id: 'abcd'};
                    stateMock.playground.grid.selectedColumns = [{ id: '0001' }, { id: '0002' }];
                    expect(StorageService.setSelectedColumns).not.toHaveBeenCalled();

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(StorageService.setSelectedColumns).toHaveBeenCalledWith('abcd', ['0001', '0002']);
                }));

                it('should NOT update when there is no selected column', inject((DatagridExternalService, TransformationService) => {
                    // given
                    stateMock.playground.grid.selectedLine = { tdpId: 125 };
                    stateMock.playground.grid.selectedColumns = [];

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then : it should init only line transfo, not column
                    expect(TransformationService.initTransformations).toHaveBeenCalledWith('line');
                }));

                it('should NOT update when selected column has not changed', inject((DatagridExternalService, TransformationService) => {
                    // given
                    const selectedColumn = { id: '0001' };
                    stateMock.playground.grid.selectedColumns = [selectedColumn];
                    DatagridExternalService.lastSelectedColumn = selectedColumn;
                    DatagridExternalService.lastSelectedColumnsNumber = 1;

                    // when
                    DatagridExternalService.updateSuggestionPanel();

                    // then
                    expect(TransformationService.initTransformations).not.toHaveBeenCalledWith('column');
                }));
            });
        });
    });
});
