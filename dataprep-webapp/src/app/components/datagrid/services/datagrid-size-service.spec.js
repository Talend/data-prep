/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Datagrid size service', function () {
    'use strict';

    var gridMock;
    var windowMock;
    var gridColumns;
    var stateMock;

    var storageKey = 'org.talend.dataprep.col_size_00000000';

    beforeEach(angular.mock.module('data-prep.datagrid', function ($provide) {
        $provide.factory('$window', function () {
            windowMock = jasmine.createSpy('$window');
            /*global window:false */
            windowMock.localStorage = window.localStorage; //eslint-disable-line angular/window-service
            windowMock.addEventListener = function () {};

            return windowMock;
        });

        stateMock = { playground: {} };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function (_$window_, $window) {
        gridMock = new SlickGridMock();
        gridColumns = [
            { id: '0000', name: 'col0', width: 10, minWidth: 80 },
            { id: '0001', name: 'col1', width: 20, minWidth: 80 },
            { id: '0002', name: 'col2', width: 30, minWidth: 80 },
            { id: '0003', name: 'col3', width: 40, minWidth: 80 },
            { id: '0004', name: 'col4', width: 50, minWidth: 80 },
        ];
        stateMock.playground.dataset = { id: '00000000' };

        spyOn(gridMock.onColumnsResized, 'subscribe').and.returnValue();
        spyOn(gridMock, 'resizeCanvas').and.returnValue();
        spyOn(gridMock, 'autosizeColumns').and.returnValue();
        spyOn($window, 'addEventListener').and.returnValue();
    }));

    afterEach(inject(function ($window) {
        $window.localStorage.removeItem(storageKey);
    }));

    describe('on creation', function () {
        it('should add grid column resize listener', inject(function (DatagridSizeService) {
            //when
            DatagridSizeService.init(gridMock);

            //then
            expect(gridMock.onColumnsResized.subscribe).toHaveBeenCalled();
        }));

        it('should add window resize listener', inject(function ($window, DatagridSizeService) {
            //when
            DatagridSizeService.init(gridMock);

            //then
            expect($window.addEventListener).toHaveBeenCalled();
            var args = $window.addEventListener.calls.argsFor(0);
            expect(args[0]).toBe('resize');
            expect(args[2]).toBe(true);
        }));
    });

    describe('on event', function () {
        it('should resize grid canvas when window is resized', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);
            var resizeHandler = $window.addEventListener.calls.argsFor(0)[1];

            //when
            resizeHandler();

            //then
            expect(gridMock.resizeCanvas).toHaveBeenCalled();
        }));

        it('should save sizes in localStorage when column is resized', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);
            var resizeHandler = gridMock.onColumnsResized.subscribe.calls.argsFor(0)[0];

            expect($window.localStorage.getItem(storageKey)).toBeFalsy();

            //when
            resizeHandler();

            //then
            expect($window.localStorage.getItem(storageKey)).toBe(JSON.stringify({
                '0000': 10,
                '0001': 20,
                '0002': 30,
                '0003': 40,
                '0004': 50,
            }));
        }));
    });

    describe('auto size columns', function () {
        it('should set column sizes from localStorage', inject(function ($window, DatagridSizeService) {
            //given
            DatagridSizeService.init(gridMock);

            $window.localStorage.setItem(storageKey, JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90,
                '0004': 100,
            }));

            expect(gridColumns[0].width).toBe(10);
            expect(gridColumns[1].width).toBe(20);
            expect(gridColumns[2].width).toBe(30);
            expect(gridColumns[3].width).toBe(40);
            expect(gridColumns[4].width).toBe(50);

            //when
            DatagridSizeService.autosizeColumns(gridColumns);

            //then
            expect(gridMock.getColumns()).toBe(gridColumns);
            expect($window.localStorage.getItem(storageKey)).toBe(JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90,
                '0004': 100,
            }));
            expect(gridMock.getColumns()[0].width).toBe(60);
            expect(gridMock.getColumns()[1].width).toBe(70);
            expect(gridMock.getColumns()[2].width).toBe(80);
            expect(gridMock.getColumns()[3].width).toBe(90);
            expect(gridMock.getColumns()[4].width).toBe(100);
        }));

        it('should set column min width when there is no saved sizes', inject(function ($window, DatagridSizeService) {
            //given
            DatagridSizeService.init(gridMock);

            //when
            DatagridSizeService.autosizeColumns(gridColumns);

            //then
            expect(gridMock.getColumns()).toBe(gridColumns);
            expect(gridMock.getColumns()[0].width).toBe(120);
            expect(gridMock.getColumns()[1].width).toBe(120);
            expect(gridMock.getColumns()[2].width).toBe(120);
            expect(gridMock.getColumns()[3].width).toBe(120);
            expect(gridMock.getColumns()[4].width).toBe(120);
        }));

        it('should set column min width when this column is not in the localStorage entries', inject(function ($window, DatagridSizeService) {
            //given
            DatagridSizeService.init(gridMock);

            $window.localStorage.setItem(storageKey, JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90,
                //missing 0004 entry
            }));

            expect(gridColumns[4].width).toBe(50);

            //when
            DatagridSizeService.autosizeColumns(gridColumns);

            //then
            expect(gridMock.getColumns()).toBe(gridColumns);
            expect(gridMock.getColumns()[4].width).toBe(120);
        }));
    });
});
