/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DataViewMock from './../../../mocks/DataView.mock';

describe('Datagrid directive', () => {
    'use strict';

    let stateMock;
    let dataViewMock;
    let scope;
    let createElement;
    let element;
    let grid;
    const createdColumns = [{ id: 'tdpId' }, {
        id: '0000',
        tdpColMetadata: { id: '0000' },
    }, { id: '0001', tdpColMetadata: { id: '0001' } }, { id: '0002', tdpColMetadata: { id: '0002' } },];

    beforeEach(() => {
        dataViewMock = new DataViewMock();
        spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
        spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
    });

    beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
        stateMock = {
            playground: {
                filter: { gridFilters: [] },
                grid: { dataView: dataViewMock, selectedColumns: [{ id: '0001' }], selectedLine: { '0001': '1' } },
                lookup: { visibility: false },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $compile, DatagridGridService, DatagridColumnService, DatagridSizeService, DatagridStyleService, DatagridExternalService, StateService) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<datagrid></datagrid>');
            $compile(element)(scope);
            scope.$digest();

            angular.element('body').append(element);
            return element;
        };

        // decorate grid creation to keep the resulting grid ref and attach spy on its functions
        var realInitGrid = DatagridGridService.initGrid.bind(DatagridGridService);
        DatagridGridService.initGrid = (parentId) => {
            grid = realInitGrid(parentId);
            spyOn(grid, 'invalidate').and.returnValue();
            spyOn(grid, 'resizeCanvas').and.returnValue();
            spyOn(grid, 'scrollRowToTop').and.returnValue();
            spyOn(grid, 'setActiveCell').and.returnValue();

            return grid;
        };

        spyOn(DatagridGridService, 'initGrid').and.callThrough();
        spyOn(DatagridGridService, 'navigateToFocusedColumn').and.returnValue();
        spyOn(DatagridColumnService, 'createColumns').and.returnValue(createdColumns);
        spyOn(DatagridColumnService, 'renewAllColumns').and.returnValue();
        spyOn(DatagridSizeService, 'autosizeColumns').and.returnValue();
        spyOn(DatagridStyleService, 'highlightCellsContaining').and.returnValue();
        spyOn(DatagridStyleService, 'resetCellStyles').and.returnValue();
        spyOn(DatagridStyleService, 'resetStyles').and.returnValue();
        spyOn(DatagridStyleService, 'updateColumnsClass').and.returnValue();
        spyOn(DatagridExternalService, 'updateSuggestionPanel').and.returnValue();
        spyOn(DatagridExternalService, 'updateGridRangeIndex').and.returnValue();
        spyOn(StateService, 'setGridSelection').and.returnValue();
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('on data change', () => {
        let data;

        beforeEach(inject(($timeout) => {
            //given
            createElement();
            data = { metadata: { columns: [{ id: '0000' }, { id: '0001', tdpColMetadata: { id: '0001' } }] }, preview: false };

            //when
            stateMock.playground.data = data;
            scope.$digest();
            $timeout.flush(1);
        }));

        describe('init', () => {
            it('should init grid', inject((DatagridGridService) => {
                //then
                expect(DatagridGridService.initGrid).toHaveBeenCalledWith('#datagrid');
            }));

            it('should init grid only once', inject((DatagridGridService) => {
                //given
                expect(DatagridGridService.initGrid.calls.count()).toBe(1);

                //when
                stateMock.playground.data = {};
                scope.$digest();

                //then
                expect(DatagridGridService.initGrid.calls.count()).toBe(1);
            }));

            it('should init tooltip ruler', inject(() => {
                //then
                expect(stateMock.playground.grid.tooltipRuler).toBeDefined();
            }));
        });

        describe('grid update', () => {
            describe('column creation', () => {
                it('should create new columns', inject((DatagridColumnService) => {
                    //then
                    expect(DatagridColumnService.createColumns).toHaveBeenCalledWith(data.metadata.columns, data.preview);
                }));

                it('should reset renew all columns flag', inject((DatagridColumnService) => {
                    //then
                    expect(DatagridColumnService.renewAllColumns).toHaveBeenCalledWith(false);
                }));
            });

            describe('column size', () => {
                it('should auto size created columns (and set them in grid, done by autosize() function)', inject((DatagridSizeService) => {
                    //then
                    expect(DatagridSizeService.autosizeColumns).toHaveBeenCalledWith(createdColumns);
                }));
            });

            it('should execute the grid update only once when the second call is triggered before the first timeout', inject(($timeout, DatagridColumnService) => {
                //given
                expect(DatagridColumnService.createColumns.calls.count()).toBe(1);

                //when
                stateMock.playground.data = {};
                scope.$digest();

                expect(DatagridColumnService.createColumns.calls.count()).toBe(1);

                stateMock.playground.data = { metadata: {} };
                scope.$digest();
                $timeout.flush(500);

                //then
                expect(DatagridColumnService.createColumns.calls.count()).toBe(2);
            }));

            it('should focus on wanted column (not necessarily the selected column) in async mode with a 500ms delay', inject(($timeout, DatagridGridService) => {
                //given
                createElement();

                //when
                stateMock.playground.data = data;
                scope.$digest();
                expect(DatagridGridService.navigateToFocusedColumn).not.toHaveBeenCalled();
                $timeout.flush(500);

                //then
                expect(DatagridGridService.navigateToFocusedColumn).toHaveBeenCalled();
            }));
        });
    });

    describe('on metadata change', () => {
        beforeEach(() => {
            //given
            createElement();
            stateMock.playground.data = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: false };
            scope.$digest();

            //when
            stateMock.playground.dataset = {};
            scope.$digest();
        });

        it('should scroll to top', () => {
            //then
            expect(grid.scrollRowToTop).toHaveBeenCalledWith(0);
        });

        it('should force column recreation (no reuse)', inject((DatagridColumnService) => {
            //then
            expect(DatagridColumnService.renewAllColumns).toHaveBeenCalledWith(true);
        }));
    });

    describe('on resize', () => {
        it('should resize grid canvas on lookup visibility change', inject(($timeout) => {
            //given
            createElement();
            stateMock.playground.data = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: false };
            scope.$digest();

            $timeout.flush(250);
            expect(grid.resizeCanvas).not.toHaveBeenCalled();

            //when
            stateMock.playground.lookup.visibility = true;
            scope.$digest();
            $timeout.flush(250);

            //then
            expect(grid.resizeCanvas).toHaveBeenCalled();
        }));

        it('should change grid height on lookup visibility change', inject(($timeout) => {
            //given
            createElement();
            stateMock.playground.data = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: false };
            scope.$digest();
            let ctrl = element.controller('datagrid');

            $timeout.flush();
            expect(ctrl.datagridHeight).toEqual('100%');

            //when
            stateMock.playground.lookup.visibility = true;
            scope.$digest();
            $timeout.flush();

            //then
            expect(ctrl.datagridHeight).toEqual('calc(100% - 315px)');
        }));

    });

    describe('on filter change', () => {
        beforeEach(() => {
            //given
            createElement();
            stateMock.playground.data = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: false };
            scope.$digest();

            //when
            stateMock.playground.filter.gridFilters = [{}];
            scope.$digest();
        });

        it('should reset cell styles', inject((DatagridStyleService) => {
            //then
            expect(DatagridStyleService.resetCellStyles).toHaveBeenCalled();
        }));

        it('should scroll to top', () => {
            //then
            expect(grid.scrollRowToTop).toHaveBeenCalledWith(0);
        });

        it('should update grid range index for future preview', inject((DatagridExternalService) => {
            //then
            expect(DatagridExternalService.updateGridRangeIndex).toHaveBeenCalled();
        }));
    });

    describe('on grid selection change', () => {
        const data = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: false };
        const previewData = { metadata: { columns: [{ id: '0000' }, { id: '0001' }] }, preview: true };

        it('should set active cell in grid', inject(() => {
            //given
            createElement();
            stateMock.playground.data = data;
            scope.$digest();
            expect(grid.setActiveCell).not.toHaveBeenCalled();

            //when
            stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
            stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
            scope.$digest();

            //then
            expect(grid.setActiveCell).toHaveBeenCalled();
        }));

        describe('grid style', () => {
            it('should reset grid styles when there is no selected line', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.resetStyles).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = null;
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                expect(DatagridStyleService.resetStyles).not.toHaveBeenCalled();
                $timeout.flush(1);

                //then
                expect(DatagridStyleService.resetStyles).toHaveBeenCalledWith(stateMock.playground.grid.selectedColumns);
            }));

            it('should only update columns styles (not the entire grid style) when there is a selected line', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.updateColumnsClass).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                expect(DatagridStyleService.updateColumnsClass).not.toHaveBeenCalled();
                $timeout.flush(1);

                //then
                expect(DatagridStyleService.updateColumnsClass).toHaveBeenCalledWith(stateMock.playground.grid.selectedColumns);
            }));

            it('should only update columns styles when there is more than one selected column', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.updateColumnsClass).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }, { id: '0002' }];
                scope.$digest();
                expect(DatagridStyleService.resetStyles).not.toHaveBeenCalled();
                $timeout.flush(1);

                //then
                expect(DatagridStyleService.resetStyles).toHaveBeenCalledWith(stateMock.playground.grid.selectedColumns);
            }));

            it('should only update columns styles when there is no selected column', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.updateColumnsClass).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [];
                scope.$digest();
                expect(DatagridStyleService.updateColumnsClass).not.toHaveBeenCalled();
                $timeout.flush(1);

                //then
                expect(DatagridStyleService.updateColumnsClass).toHaveBeenCalledWith(stateMock.playground.grid.selectedColumns);
            }));
        });

        describe('highlight', () => {
            it('should reset highlighted cells on active cell change', inject(($timeout, DatagridStyleService) => {
                //given
                spyOn(DatagridStyleService, 'resetHighlightStyles').and.returnValue();
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.resetHighlightStyles).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();

                //then
                expect(DatagridStyleService.resetHighlightStyles).toHaveBeenCalled();
            }));

            it('should highlight cells containing the same value as selected cell in async mode with a 500ms delay', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();
                $timeout.flush(500);

                //then
                expect(DatagridStyleService.highlightCellsContaining).toHaveBeenCalledWith('0001', 'tata');
                expect(grid.invalidate).toHaveBeenCalled();
            }));

            it('should NOT highlight cells when in preview mode', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = previewData;
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = { '0000': 'toto', '0001': 'tata' };
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                $timeout.flush(500);

                //then
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();
            }));

            it('should NOT highlight cells when there is no selected line', inject(($timeout, DatagridStyleService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedLine = null;
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                $timeout.flush(500);

                //then
                expect(DatagridStyleService.highlightCellsContaining).not.toHaveBeenCalled();
            }));
        });

        describe('suggestion panel', () => {
            it('should update suggestions panel in async mode', inject(($timeout, DatagridExternalService) => {
                //given
                createElement();
                stateMock.playground.data = data;
                expect(DatagridExternalService.updateSuggestionPanel).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                expect(DatagridExternalService.updateSuggestionPanel).not.toHaveBeenCalled();
                $timeout.flush(500);

                //then
                expect(DatagridExternalService.updateSuggestionPanel).toHaveBeenCalled();
            }));

            it('should NOT update suggestion panel when in preview mode', inject(($timeout, DatagridExternalService) => {
                //given
                createElement();
                stateMock.playground.data = previewData;
                expect(DatagridExternalService.updateSuggestionPanel).not.toHaveBeenCalled();

                //when
                stateMock.playground.grid.selectedColumns = [{ id: '0001' }];
                scope.$digest();
                $timeout.flush(1);

                //then
                expect(DatagridExternalService.updateSuggestionPanel).not.toHaveBeenCalled();
            }));
        });
    });
});
