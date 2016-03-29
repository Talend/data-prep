/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid external service', () => {
    'use strict';

    var gridMock, stateMock;

    var gridColumns = [
        {id: '0000', field: 'col0', tdpColMetadata: {id: '0000', name: 'col0'}},
        {id: '0001', field: 'col1', tdpColMetadata: {id: '0001', name: 'col1'}},
        {id: '0002', field: 'col2', tdpColMetadata: {id: '0002', name: 'col2'}},
        {id: '0003', field: 'col3', tdpColMetadata: {id: '0003', name: 'col3'}},
        {id: '0004', field: 'col4', tdpColMetadata: {id: '0004', name: 'col4'}},
        {id: 'tdpId', field: 'tdpId', tdpColMetadata: {id: 'tdpId', name: 'tdpId'}}
    ];

    beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
        stateMock = {
            playground: {
                grid: {}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject((StatisticsService, SuggestionService, LookupService) => {
        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();
        gridMock.initColumnsMock(gridColumns);

        spyOn(gridMock.onActiveCellChanged, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderClick, 'subscribe').and.returnValue();
        spyOn(gridMock.onHeaderContextMenu, 'subscribe').and.returnValue();
        spyOn(gridMock.onScroll, 'subscribe').and.returnValue();

        spyOn(StatisticsService, 'updateStatistics').and.returnValue();
        spyOn(StatisticsService, 'reset').and.returnValue();
        spyOn(SuggestionService, 'setColumn').and.returnValue();
        spyOn(SuggestionService, 'setLine').and.returnValue();
        spyOn(SuggestionService, 'selectTab').and.returnValue();

        spyOn(LookupService, 'updateTargetColumn').and.returnValue();
    }));

    beforeEach(() => {
        jasmine.clock().install();
    });

    afterEach(() => {
        jasmine.clock().uninstall();
    });

    describe('on creation', () => {
        it('should add scroll listener', inject((DatagridExternalService) => {
            //when
            DatagridExternalService.init(gridMock);

            //then
            expect(gridMock.onScroll.subscribe).toHaveBeenCalled();
        }));
    });

    describe('update right panel', () => {

        describe('tab selection', () => {
            it('should select "COLUMN" tab if no tab is provided', inject((DatagridExternalService, SuggestionService) => {
                //given
                stateMock.playground.grid.selectedColumn = {id: '0001'};
                expect(SuggestionService.selectTab).not.toHaveBeenCalled();

                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(SuggestionService.selectTab).toHaveBeenCalledWith('COLUMN');
            }));

            it('should select "LINE" tab when there is no selected column', inject((DatagridExternalService, SuggestionService) => {
                //given
                stateMock.playground.grid.selectedLine = {tdpId: 125};
                stateMock.playground.grid.selectedColumn = null;
                expect(SuggestionService.selectTab).not.toHaveBeenCalled();

                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(SuggestionService.selectTab).toHaveBeenCalledWith('LINE');
            }));
        });

        describe('charts', () => {
            it('should reset charts when there is no selected column', inject((DatagridExternalService, StatisticsService) => {
                //given
                stateMock.playground.grid.selectedLine = {tdpId: 125};
                stateMock.playground.grid.selectedColumn = null;
                expect(StatisticsService.reset).not.toHaveBeenCalled();

                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(StatisticsService.reset).toHaveBeenCalled();
            }));

            it('should update charts when there is a selected column', inject((DatagridExternalService, StatisticsService) => {
                //given
                stateMock.playground.grid.selectedColumn = {id: '0001'};
                expect(StatisticsService.updateStatistics).not.toHaveBeenCalled();

                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(StatisticsService.updateStatistics).toHaveBeenCalled();
            }));

            it('should load Lookup Panel when a new column is selected', inject((DatagridExternalService, LookupService) => {
                //given
                stateMock.playground.grid.selectedColumn = {id: '0001'};
                expect(LookupService.updateTargetColumn).not.toHaveBeenCalled();

                //when
                DatagridExternalService.updateSuggestionPanel(null, null);

                //then
                expect(LookupService.updateTargetColumn).toHaveBeenCalled();
            }));
        });

        describe('transformations', () => {
            it('should do nothing if selected column/line have not changed', inject((DatagridExternalService, SuggestionService) => {
                //when
                DatagridExternalService.updateSuggestionPanel();

                //then
                expect(SuggestionService.setLine).not.toHaveBeenCalled();
                expect(SuggestionService.setColumn).not.toHaveBeenCalled();
            }));

            describe('line scope', () => {
                it('should update when there is a selected line', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    stateMock.playground.grid.selectedLine = {tdpId: 125};
                    expect(SuggestionService.setLine).not.toHaveBeenCalled();

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setLine).toHaveBeenCalledWith(stateMock.playground.grid.selectedLine);
                }));

                it('should NOT update when there is no selected line', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    stateMock.playground.grid.selectedLine = null;
                    stateMock.playground.grid.selectedColumn = {id: '0001'};
                    expect(SuggestionService.setLine).not.toHaveBeenCalled();

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setLine).not.toHaveBeenCalled();
                }));

                it('should NOT update when selected line has not changed', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    const selectedLine = {tdpId: 125};
                    stateMock.playground.grid.selectedLine = selectedLine;
                    DatagridExternalService.lastSelectedLine = selectedLine;
                    expect(SuggestionService.setLine).not.toHaveBeenCalled();

                    stateMock.playground.grid.selectedColumn = {id: '0001'};

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setLine).not.toHaveBeenCalled();
                }));
            });

            describe('column scope', () => {
                it('should update when there is a selected column', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    stateMock.playground.grid.selectedColumn = {id: '0001'};
                    expect(SuggestionService.setColumn).not.toHaveBeenCalled();

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setColumn).toHaveBeenCalled();
                }));

                it('should NOT update when there is no selected column', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    stateMock.playground.grid.selectedLine = {tdpId: 125};
                    stateMock.playground.grid.selectedColumn = null;
                    expect(SuggestionService.setColumn).not.toHaveBeenCalled();

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setColumn).not.toHaveBeenCalled();
                }));

                it('should NOT update when selected column has not changed', inject((DatagridExternalService, SuggestionService) => {
                    //given
                    const selectedColumn = {id: '0001'};
                    stateMock.playground.grid.selectedColumn = selectedColumn;
                    DatagridExternalService.lastSelectedColumn = selectedColumn;
                    expect(SuggestionService.setColumn).not.toHaveBeenCalled();

                    //when
                    DatagridExternalService.updateSuggestionPanel();

                    //then
                    expect(SuggestionService.setColumn).not.toHaveBeenCalled();
                }));
            });
        });
    });
});
