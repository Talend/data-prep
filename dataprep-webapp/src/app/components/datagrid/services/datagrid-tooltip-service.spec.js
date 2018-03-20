/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DataViewMock from '../../../../mocks/DataView.mock';
import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Datagrid tooltip service', () => {
    'use strict';

    let gridMock;
    let stateMock;
    let dataViewMock;

    //invisible ruler mock
    let RulerMock = {
        text: function (savedText) {
            this.savedText = savedText;
        },

        width: function () {
            return this.savedText.length * 4;
        },

        height: function () {
            return ((this.savedText.match(new RegExp('\n', 'g')) || []).length + 1) * 20; //eslint-disable-line no-control-regex
        },
    };

    //record on the line
    const item = {
        '0000': 'tata',                                         // should not show tooltip
        '0001': '  tetetetetetetetetetetetetetetetetetetete ',  // should show tooltip with trailing spaces
        '0002': 'titititititititititititititititititi',         // should show tooltip because of length
        '0003': 'toto\ntoto',                                   // should show tooltip because of height
        '0004': '',                                             // should not show tooltip
        tdpId: 16678678678686786788888888888888888886872,      // should show tooltip because of length
    };

    //columns metadata
    const columns = [
        { id: '0000', name: 'col0' },
        { id: '0001', name: 'col1' },
        { id: '0002', name: 'col2' },
        { id: '0003', name: 'col3' },
        { id: '0004', name: 'col4' },
        { id: 'tdpId', name: '#' },
    ];

    beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
        dataViewMock = new DataViewMock();
        stateMock = {
            playground: {
                grid: {
                    dataView: dataViewMock,
                    showTooltip: false,
                    tooltip: {},
                    tooltipRuler: RulerMock,
                },
            },
        };

        $provide.constant('state', stateMock);
    }));

    beforeEach(inject((DatagridTooltipService) => {
        gridMock = new SlickGridMock();

        gridMock.initColumnsMock(columns);
        spyOn(gridMock.onMouseEnter, 'subscribe').and.returnValue();
        spyOn(gridMock.onMouseLeave, 'subscribe').and.returnValue();

        spyOn(stateMock.playground.grid.dataView, 'getItem').and.returnValue(item);
        DatagridTooltipService.tooltipRuler = RulerMock;
    }));

    beforeEach(() => {
        jasmine.clock().install();
    });

    afterEach(() => {
        jasmine.clock().uninstall();
    });

    describe('on initialization', () => {
        it('should attach listeners to the grid', inject((DatagridTooltipService) => {
            //when
            DatagridTooltipService.init(gridMock);

            //then
            expect(gridMock.onMouseEnter.subscribe).toHaveBeenCalled();
            expect(gridMock.onMouseLeave.subscribe).toHaveBeenCalled();
        }));
    });

    describe('on mouse enter', () => {
        it('should NOT show tooltip when no cell correspond to the event', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);
            gridMock.initCellMock(null); //grid.getCellFromEvent returns falsy param

            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
                //then
            catch (error) {
                expect(stateMock.playground.grid.tooltip).toEqual({});
                expect(stateMock.playground.grid.showTooltip).toBeFalsy();
            }
        }));

        it('should NOT show tooltip with content that fit in the box (column 0)', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 0 };                           // contains 'tata'
            gridMock.initCellMock(cell, box);


            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
                //then
            catch (error) {
                expect(stateMock.playground.grid.tooltip).toEqual({});
                expect(stateMock.playground.grid.showTooltip).toBeFalsy();
            }
        }));

        it('should show trailing spaces in the tooltip (column 1)', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 1 }; // contains '  tetetetetetetetetetetetetetetetetetetete '
            gridMock.initCellMock(cell, box);

            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({
                position: { x: 500, y: 300 },
                htmlStr: '<span class="hiddenChars">  </span>tetetetetetetetetetetetetetetetetetetete<span class="hiddenChars"> </span>',
            });
            expect(stateMock.playground.grid.showTooltip).toBeTruthy();
        }));

        it('should show tooltip after a 300ms delay with long line content (column 2)', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 2 }; // contains 'titititititititititititititititititi'
            gridMock.initCellMock(cell, box);

            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({
                position: { x: 500, y: 300 },
                htmlStr: 'titititititititititititititititititi',
            });
            expect(stateMock.playground.grid.showTooltip).toBeTruthy();
        }));

        it('should show tooltip after a 300ms delay with multiple line content (column 3)', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const cell = { row: 1, cell: 3 }; // contains 'toto<span class="hiddenCharsBreakLine">&nbsp;</span>\ntoto'
            gridMock.initCellMock(cell);

            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({
                position: { x: 500, y: 300 },
                htmlStr: 'toto↵\ntoto',
            });
            expect(stateMock.playground.grid.showTooltip).toBeTruthy();
        }));

        it('should NOT show tooltip with empty content (column 4)', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 4 };                           // contains ''
            gridMock.initCellMock(cell, box);


            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
                //then
            catch (error) {
                expect(stateMock.playground.grid.tooltip).toEqual({});
                expect(stateMock.playground.grid.showTooltip).toBeFalsy();
            }
        }));

        it('should show tooltip after a 300ms delay with column tdpId', inject(($timeout, DatagridTooltipService) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);
            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 5 }; //contains 16678678678686786788888888888888888886872
            gridMock.initCellMock(cell, box);

            //when
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({});
            expect(stateMock.playground.grid.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(stateMock.playground.grid.tooltip).toEqual({
                position: { x: 500, y: 300 },
                htmlStr: '1.6678678678686786e+40',
            });
            expect(stateMock.playground.grid.showTooltip).toBeTruthy();
        }));
    });

    describe('on mouse leave', () => {
        it('should hide tooltip after a 300ms delay', inject((DatagridTooltipService, $timeout) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);
            stateMock.playground.grid.showTooltip = true;

            //when
            const mouseLeaveHandler = gridMock.onMouseLeave.subscribe.calls.argsFor(0)[0];
            mouseLeaveHandler();

            //then
            expect(stateMock.playground.grid.showTooltip).toBe(true);

            //when
            $timeout.flush(300);

            //then
            expect(stateMock.playground.grid.showTooltip).toBe(false);
        }));

        it('should cancel existing update promise', inject((DatagridTooltipService, $timeout) => {
            //given
            DatagridTooltipService.init(gridMock, stateMock.playground.grid);

            const box = { left: 400, right: 500, top: 10, bottom: 40 }; //width: 100
            const cell = { row: 1, cell: 2 };                           // contains 'titititititititititititititititititi'
            gridMock.initCellMock(cell, box);

            //when : trigger a tooltip and flush 299ms (1ms before actual tooltip show)
            const mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            const event = { clientX: 500, clientY: 300 };
            mouseEnterHandler(event);
            jasmine.clock().tick(300);

            //then
            expect(stateMock.playground.grid.showTooltip).toBe(false);

            //when : tooltip hide
            const mouseLeaveHandler = gridMock.onMouseLeave.subscribe.calls.argsFor(0)[0];
            mouseLeaveHandler();
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }

                //then
            catch (error) {
                expect(stateMock.playground.grid.tooltip).toEqual({});
                expect(stateMock.playground.grid.showTooltip).toBeFalsy();
            }
        }));
    });
});
