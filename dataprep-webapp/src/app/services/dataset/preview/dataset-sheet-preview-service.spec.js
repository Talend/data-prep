/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import SlickGridMock from '../../../../mocks/SlickGrid.mock';

describe('Dataset Sheet Preview Service', function () {
    'use strict';

    var slickGridMock;
    var gridMockElement;
    var previewResponse = {
        metadata: {
            sheetName: 'my sheet',
            columns: [
                { id: '1', name: 'col1' },
                { id: '2', name: 'col2' },
                { id: '3', name: 'col3' },
            ],
        },
        records: [{ 1: 'value1', 2: 'value2', 3: 'value3' }],
    };

    beforeEach(angular.mock.module('data-prep.services.dataset'));

    beforeEach(inject(function ($q, DatasetService) {
        gridMockElement = angular.element('<div id="gridMock"></div>');
        angular.element('body').append(gridMockElement);
        slickGridMock = new Slick.Grid('#gridMock', [], [], {});

        spyOn(slickGridMock, 'setColumns').and.returnValue();
        spyOn(slickGridMock, 'setData').and.returnValue();
        spyOn(slickGridMock, 'autosizeColumns').and.returnValue();
        spyOn(slickGridMock, 'render').and.returnValue();
        spyOn(DatasetService, 'getSheetPreview').and.returnValue($q.when(previewResponse));
        spyOn(DatasetService, 'setDatasetSheet').and.returnValue($q.when(true));
    }));

    afterEach(function () {
        gridMockElement.remove();
    });

    it('should init vars and flags', inject(function (DatasetSheetPreviewService) {
        //then
        expect(DatasetSheetPreviewService.currentMetadata).toBe(null);
        expect(DatasetSheetPreviewService.selectedSheetName).toBe(null);
        expect(DatasetSheetPreviewService.grid).toBe(null);
        expect(DatasetSheetPreviewService.showModal).toBe(false);
    }));

    it('should set modal flag to true', inject(function (DatasetSheetPreviewService) {
        //given
        expect(DatasetSheetPreviewService.showModal).toBe(false);

        //when
        DatasetSheetPreviewService.display();

        //then
        expect(DatasetSheetPreviewService.showModal).toBe(true);
    }));

    describe('load preview', function () {
        beforeEach(inject(function (DatasetSheetPreviewService) {
            DatasetSheetPreviewService.grid = slickGridMock;
        }));

        it('should reset grid content', inject(function ($timeout, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };

            //when
            DatasetSheetPreviewService.loadPreview(metadata);
            $timeout.flush();

            //then
            expect(slickGridMock.setColumns).toHaveBeenCalledWith([]);
            expect(slickGridMock.setData).toHaveBeenCalledWith([]);
        }));

        it('should set current metadata', inject(function ($timeout, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };

            //when
            DatasetSheetPreviewService.loadPreview(metadata);
            $timeout.flush();

            //then
            expect(slickGridMock.setColumns).toHaveBeenCalledWith([]);
            expect(slickGridMock.setData).toHaveBeenCalledWith([]);
        }));

        it('should reset selectedSheetName', inject(function (DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };
            DatasetSheetPreviewService.selectedSheetName = {};

            //when
            DatasetSheetPreviewService.loadPreview(metadata);

            //then
            expect(DatasetSheetPreviewService.selectedSheetName).toBeFalsy();
        }));

        it('should reset currentMetadata', inject(function (DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };
            DatasetSheetPreviewService.currentMetadata = {};

            //when
            DatasetSheetPreviewService.loadPreview(metadata);

            //then
            expect(DatasetSheetPreviewService.currentMetadata).toBeFalsy();
        }));

        it('should set addPreparation', inject(function ($timeout, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };

            //when
            DatasetSheetPreviewService.loadPreview(metadata, true);
            $timeout.flush();

            //then
            expect(DatasetSheetPreviewService.addPreparation).toBeTruthy();
        }));

        it('should call sheet preview service and set selected sheet name', inject(function ($timeout, $rootScope, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };
            var expectedSheetName = previewResponse.metadata.sheetName;

            //when
            DatasetSheetPreviewService.loadPreview(metadata);
            $rootScope.$digest();
            $timeout.flush();

            //then
            expect(DatasetSheetPreviewService.selectedSheetName).toBe(expectedSheetName);
        }));

        it('should call sheet preview service and set current metadata', inject(function ($timeout, $rootScope, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };
            var expectedMetadata = previewResponse.metadata;

            //when
            DatasetSheetPreviewService.loadPreview(metadata);
            $rootScope.$digest();
            $timeout.flush();

            //then
            expect(DatasetSheetPreviewService.currentMetadata).toBe(expectedMetadata);
        }));

        it('should call sheet preview service and update grid', inject(function ($timeout, $rootScope, DatasetService, DatasetSheetPreviewService) {
            //given
            var metadata = { id: '74a856ef486b', name: 'my dataset' };
            var expectedData = previewResponse.records;

            //when
            DatasetSheetPreviewService.loadPreview(metadata);
            $rootScope.$digest();
            $timeout.flush();

            //then
            expect(DatasetService.getSheetPreview).toHaveBeenCalledWith(metadata);

            expect(slickGridMock.setColumns.calls.count()).toBe(2); //the first call is the reset
            var gridColumns = slickGridMock.setColumns.calls.mostRecent().args[0]; //the first call is the reset
            expect(gridColumns.length).toBe(3);
            expect(gridColumns[0].id).toBe('1');
            expect(gridColumns[0].field).toBe('1');
            expect(gridColumns[0].minWidth).toBe(100);
            expect(gridColumns[0].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col1</div></div>');
            expect(gridColumns[1].id).toBe('2');
            expect(gridColumns[1].field).toBe('2');
            expect(gridColumns[1].minWidth).toBe(100);
            expect(gridColumns[1].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col2</div></div>');
            expect(gridColumns[2].id).toBe('3');
            expect(gridColumns[2].field).toBe('3');
            expect(gridColumns[2].minWidth).toBe(100);
            expect(gridColumns[2].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col3</div></div>');

            expect(slickGridMock.setData).toHaveBeenCalledWith(expectedData);
            expect(slickGridMock.autosizeColumns).toHaveBeenCalled();
            expect(slickGridMock.render).toHaveBeenCalled();
        }));
    });

    describe('load sheet', function () {
        beforeEach(inject(function (DatasetSheetPreviewService) {
            DatasetSheetPreviewService.grid = slickGridMock;
        }));

        it('should reset grid content', inject(function (DatasetSheetPreviewService) {
            //given
            var sheetName = 'my sheet';

            //when
            DatasetSheetPreviewService.loadSheet(sheetName);

            //then
            expect(slickGridMock.setColumns).toHaveBeenCalledWith([]);
            expect(slickGridMock.setData).toHaveBeenCalledWith([]);
        }));

        it('should call sheet preview service and update grid', inject(function ($rootScope, DatasetService, DatasetSheetPreviewService) {
            //given
            var sheetName = 'my sheet';
            var expectedData = previewResponse.records;
            DatasetSheetPreviewService.currentMetadata = { id: '74a856ef486b', name: 'my dataset' };

            //when
            DatasetSheetPreviewService.loadSheet(sheetName);
            $rootScope.$digest();

            //then
            expect(DatasetService.getSheetPreview).toHaveBeenCalledWith(DatasetSheetPreviewService.currentMetadata, sheetName);

            expect(slickGridMock.setColumns.calls.count()).toBe(2); //the first call is the reset
            var gridColumns = slickGridMock.setColumns.calls.mostRecent().args[0]; //the first call is the reset
            expect(gridColumns.length).toBe(3);
            expect(gridColumns[0].id).toBe('1');
            expect(gridColumns[0].field).toBe('1');
            expect(gridColumns[0].minWidth).toBe(100);
            expect(gridColumns[0].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col1</div></div>');
            expect(gridColumns[1].id).toBe('2');
            expect(gridColumns[1].field).toBe('2');
            expect(gridColumns[1].minWidth).toBe(100);
            expect(gridColumns[1].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col2</div></div>');
            expect(gridColumns[2].id).toBe('3');
            expect(gridColumns[2].field).toBe('3');
            expect(gridColumns[2].minWidth).toBe(100);
            expect(gridColumns[2].name).toBe('<div class="grid-header"><div class="grid-header-title dropdown-button ng-binding">col3</div></div>');

            expect(slickGridMock.setData).toHaveBeenCalledWith(expectedData);
            expect(slickGridMock.autosizeColumns).toHaveBeenCalled();
            expect(slickGridMock.render).toHaveBeenCalled();
        }));
    });

    it('should call dataset sheet service', inject(function (DatasetService, DatasetSheetPreviewService) {
        //given
        var sheetName = 'my sheet';
        DatasetSheetPreviewService.currentMetadata = { id: '74a856ef486b', name: 'my dataset' };

        //when
        DatasetSheetPreviewService.setDatasetSheet(sheetName);

        //then
        expect(DatasetService.setDatasetSheet).toHaveBeenCalledWith(DatasetSheetPreviewService.currentMetadata, sheetName);
    }));
});
