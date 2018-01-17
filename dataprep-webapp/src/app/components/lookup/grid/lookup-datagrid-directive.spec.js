/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import DataViewMock from './../../../../mocks/DataView.mock';

describe('Lookup datagrid directive', () => {
    'use strict';

    let stateMock;
    let dataViewMock;
    let scope;
    let createElement;
    let element;
    let grid;

    let createdColumns = [
        { id: 'tdpId' },
        { id: '0000', tdpColMetadata: { id: '0000' } },
        { id: '0001', tdpColMetadata: { id: '0001' } },
        { id: '0002', tdpColMetadata: { id: '0002' } },
    ];

    beforeEach(() => {
        dataViewMock = new DataViewMock();
        spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
        spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
    });

    beforeEach(angular.mock.module('data-prep.lookup', ($provide) => {
        stateMock = {
            playground: {
                metadata: {
                    columns: [],
                },
                lookup: { dataView: dataViewMock },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $compile, LookupDatagridGridService, LookupDatagridColumnService, LookupDatagridStyleService) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<lookup-datagrid></lookup-datagrid>');
            $compile(element)(scope);
            scope.$digest();

            angular.element('body').append(element);
            return element;
        };

        // decorate grid creation to keep the resulting grid ref and attach spy on its functions
        const realInitGrid = LookupDatagridGridService.initGrid;
        LookupDatagridGridService.initGrid = (parentId) => {
            grid = realInitGrid(parentId);
            spyOn(grid, 'setColumns').and.returnValue();
            spyOn(grid, 'invalidate').and.returnValue();
            return grid;
        };

        spyOn(LookupDatagridGridService, 'initGrid').and.callThrough();
        spyOn(LookupDatagridStyleService, 'updateColumnClass').and.returnValue();
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('on data change', () => {
        let data;

        beforeEach(inject(($timeout, DatagridColumnService) => {
            //given
            spyOn(DatagridColumnService, 'createColumns').and.returnValue(createdColumns);

            createElement();
            data = {
                metadata: {
                    columns: [
                        { id: '0000' },
                        { id: '0001', tdpColMetadata: { id: '0001' } },
                    ],
                },
            };

            //when
            stateMock.playground.lookup.data = data;
            scope.$digest();
            $timeout.flush(1);
        }));

        describe('init', () => {
            it('should init grid', inject((LookupDatagridGridService) => {
                //then
                expect(LookupDatagridGridService.initGrid).toHaveBeenCalledWith('#lookup-datagrid');
            }));

            it('should init grid only once', inject((LookupDatagridGridService) => {
                //given
                expect(LookupDatagridGridService.initGrid.calls.count()).toBe(1);

                //when
                stateMock.playground.lookup.data = { metadata: {} };
                scope.$digest();

                //then
                expect(LookupDatagridGridService.initGrid.calls.count()).toBe(1);
            }));

        });

        describe('grid update', () => {
            describe('column creation', () => {
                it('should create new columns', inject((DatagridColumnService) => {
                    //then
                    expect(DatagridColumnService.createColumns).toHaveBeenCalledWith(data.metadata.columns, null, '<div class="lookup-slick-header-column-index"></div>');
                }));
            });

            describe('column style', () => {
                it('should reset cell styles when there is a selected column', inject(($timeout, LookupDatagridStyleService) => {
                    //given
                    stateMock.playground.lookup.selectedColumn = { id: '0001' };

                    //when
                    stateMock.playground.lookup.data = { metadata: {} };
                    scope.$digest();
                    $timeout.flush(1);

                    //then
                    expect(LookupDatagridStyleService.updateColumnClass).toHaveBeenCalledWith(createdColumns, data.metadata.columns[1]);
                }));

                it('should reset cell styles when there is NOT a selected cell', inject(($timeout, LookupDatagridStyleService) => {
                    //given
                    stateMock.playground.lookup.selectedColumn = undefined;

                    //when
                    stateMock.playground.lookup.data = { metadata: {} };
                    scope.$digest();
                    $timeout.flush(1);

                    //then
                    expect(LookupDatagridStyleService.updateColumnClass).toHaveBeenCalledWith(createdColumns, null);
                }));

                it('should update selected column style', inject(($timeout, LookupDatagridStyleService) => {
                    //given
                    stateMock.playground.lookup.selectedColumn = { id: '0001' };
                    expect(LookupDatagridStyleService.updateColumnClass).not.toHaveBeenCalledWith(createdColumns, data.metadata.columns[1]);

                    //when
                    stateMock.playground.lookup.data = { metadata: {} };
                    scope.$digest();
                    $timeout.flush(1);

                    //then
                    expect(LookupDatagridStyleService.updateColumnClass).toHaveBeenCalledWith(createdColumns, data.metadata.columns[1]);
                }));
            });

            describe('with new columns', () => {
                it('should create new columns', inject(() => {
                    //then
                    expect(grid.setColumns).toHaveBeenCalledWith(createdColumns);
                }));
            });

            it('should execute the grid update only once when the second call is triggered before the first timeout', inject(($timeout, DatagridColumnService) => {
                //given
                expect(DatagridColumnService.createColumns.calls.count()).toBe(1);

                stateMock.playground.lookup.selectedColumn = { id: '0001' };

                //when
                stateMock.playground.lookup.data = { metadata: {} };
                scope.$digest();

                expect(DatagridColumnService.createColumns.calls.count()).toBe(1);

                stateMock.playground.lookup.data = { metadata: {} };
                scope.$digest();
                $timeout.flush(1);

                //then
                expect(DatagridColumnService.createColumns.calls.count()).toBe(2);
            }));
        });
    });
});
