/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Datagrid tooltip service', function() {
    'use strict';

    var gridMock, stateMock, dataViewMock;

    //invisible ruler mock
    var RulerMock = {
        text: function(savedText) {
            this.savedText = savedText;
        },
        width: function() {
            return this.savedText.length * 4;
        },
        height: function() {
            return ((this.savedText.match(new RegExp('\n', 'g')) || []).length + 1) * 20; //eslint-disable-line no-control-regex
        }
    };

    //record on the line
    var item = {
        '0000': 'tata',                                         // should not show tooltip
        '0001': '  tetetetetetetetetetetetetetetetetetetete ',  // should show tooltip with trailing spaces
        '0002': 'titititititititititititititititititi',         // should show tooltip because of length
        '0003': 'toto\ntoto',                                   // should show tooltip because of height
        '0004': '',                                             // should not show tooltip
        'tdpId': 16678678678686786788888888888888888886872      // should show tooltip because of length
    };

    //columns metadata
    var columns = [
        {id: '0000', name: 'col0'},
        {id: '0001', name: 'col1'},
        {id: '0002', name: 'col2'},
        {id: '0003', name: 'col3'},
        {id: '0004', name: 'col4'},
        {id: 'tdpId', name: '#'}
    ];

    beforeEach(angular.mock.module('data-prep.datagrid', function ($provide) {
        dataViewMock = new DataViewMock();
        stateMock = {playground: {grid: {
            dataView: dataViewMock
        }}};

        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function(DatagridTooltipService) {
        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();

        gridMock.initColumnsMock(columns);
        spyOn(gridMock.onMouseEnter, 'subscribe').and.returnValue();
        spyOn(gridMock.onMouseLeave, 'subscribe').and.returnValue();

        spyOn(stateMock.playground.grid.dataView, 'getItem').and.returnValue(item);
        DatagridTooltipService.tooltipRuler = RulerMock;
    }));

    beforeEach(function () {
        jasmine.clock().install();
    });

    afterEach(function () {
        jasmine.clock().uninstall();
    });

    describe('on creation', function() {
        it('should hide tooltip', inject(function(DatagridTooltipService) {
            //then
            expect(DatagridTooltipService.showTooltip).toBe(false);
        }));

        it('should create empty tooltip infos', inject(function(DatagridTooltipService) {
            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
        }));
    });

    describe('on initialization', function() {
        it('should attach listeners to the grid', inject(function(DatagridTooltipService) {
            //when
            DatagridTooltipService.init(gridMock);

            //then
            expect(gridMock.onMouseEnter.subscribe).toHaveBeenCalled();
            expect(gridMock.onMouseLeave.subscribe).toHaveBeenCalled();
        }));
    });

    describe('on mouse enter', function() {
        it('should NOT show tooltip when no cell correspond to the event', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);
            gridMock.initCellMock(null); //grid.getCellFromEvent returns falsy param

            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
            //then
            catch(error) {
                expect(DatagridTooltipService.tooltip).toEqual({});
                expect(DatagridTooltipService.showTooltip).toBeFalsy();
            }
        }));

        it('should NOT show tooltip with content that fit in the box (column 0)', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);

            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 0};                           // contains 'tata'
            gridMock.initCellMock(cell, box);


            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
            //then
            catch(error) {
                expect(DatagridTooltipService.tooltip).toEqual({});
                expect(DatagridTooltipService.showTooltip).toBeFalsy();
            }
        }));

        it('should show trailing spaces in the tooltip (column 1)', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);

            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 1}; // contains '  tetetetetetetetetetetetetetetetetetetete '
            gridMock.initCellMock(cell, box);

            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(DatagridTooltipService.tooltip).toEqual({
                position: {x: 500, y: 300},
                htmlStr: '<span class="hiddenChars">  </span>tetetetetetetetetetetetetetetetetetetete<span class="hiddenChars"> </span>'
            });
            expect(DatagridTooltipService.showTooltip).toBeTruthy();
        }));

        it('should show tooltip after a 300ms delay with long line content (column 2)', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);

            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 2}; // contains 'titititititititititititititititititi'
            gridMock.initCellMock(cell, box);

            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(DatagridTooltipService.tooltip).toEqual({
                position: {x: 500, y: 300},
                htmlStr: 'titititititititititititititititititi'
            });
            expect(DatagridTooltipService.showTooltip).toBeTruthy();
        }));

        it('should show tooltip after a 300ms delay with multiple line content (column 3)', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);

            var cell = {row: 1, cell: 3}; // contains 'toto<span class="hiddenCharsBreakLine">&nbsp;</span>\ntoto'
            gridMock.initCellMock(cell);

            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(DatagridTooltipService.tooltip).toEqual({
                position: {x: 500, y: 300},
                htmlStr: 'toto↵\ntoto'
            });
            expect(DatagridTooltipService.showTooltip).toBeTruthy();
        }));

        it('should NOT show tooltip with empty content (column 4)', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);

            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 4};                           // contains ''
            gridMock.initCellMock(cell, box);


            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            try {
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }
            //then
            catch(error) {
                expect(DatagridTooltipService.tooltip).toEqual({});
                expect(DatagridTooltipService.showTooltip).toBeFalsy();
            }
        }));

        it('should show tooltip after a 300ms delay with column tdpId', inject(function($timeout, DatagridTooltipService) {
            //given
            DatagridTooltipService.init(gridMock);
            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 5}; //contains 16678678678686786788888888888888888886872
            gridMock.initCellMock(cell, box);

            //when
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);

            //then
            expect(DatagridTooltipService.tooltip).toEqual({});
            expect(DatagridTooltipService.showTooltip).toBeFalsy();

            //when
            jasmine.clock().tick(300);
            $timeout.flush();

            //then
            expect(DatagridTooltipService.tooltip).toEqual({
                position: {x: 500, y: 300},
                htmlStr: '1.6678678678686786e+40'
            });
            expect(DatagridTooltipService.showTooltip).toBeTruthy();
        }));
    });

    describe('on mouse leave', function() {
        it('should hide tooltip after a 300ms delay', inject(function(DatagridTooltipService, $timeout) {
            //given
            DatagridTooltipService.init(gridMock);
            DatagridTooltipService.showTooltip = true;

            //when
            var mouseLeaveHandler = gridMock.onMouseLeave.subscribe.calls.argsFor(0)[0];
            mouseLeaveHandler();

            //then
            expect(DatagridTooltipService.showTooltip).toBe(true);

            //when
            $timeout.flush(300);

            //then
            expect(DatagridTooltipService.showTooltip).toBe(false);
        }));

        it('should cancel existing update promise', inject(function(DatagridTooltipService, $timeout) {
            //given
            DatagridTooltipService.init(gridMock);

            var box = {left: 400, right: 500, top: 10, bottom: 40}; //width: 100
            var cell = {row: 1, cell: 2};                           // contains 'titititititititititititititititititi'
            gridMock.initCellMock(cell, box);

            //when : trigger a tooltip and flush 299ms (1ms before actual tooltip show)
            var mouseEnterHandler = gridMock.onMouseEnter.subscribe.calls.argsFor(0)[0];
            var event = {clientX: 500, clientY: 300};
            mouseEnterHandler(event);
            jasmine.clock().tick(300);

            //then
            expect(DatagridTooltipService.showTooltip).toBe(false);

            //when : tooltip hide
            var mouseLeaveHandler = gridMock.onMouseLeave.subscribe.calls.argsFor(0)[0];
            mouseLeaveHandler();
            try{
                $timeout.flush();
                throw Error('Should have thrown exception, because no timeout is waiting');
            }

            //then
            catch(error) {
                expect(DatagridTooltipService.tooltip).toEqual({});
                expect(DatagridTooltipService.showTooltip).toBeFalsy();
            }
        }));
    });

});
