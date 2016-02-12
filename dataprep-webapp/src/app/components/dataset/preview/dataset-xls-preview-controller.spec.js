/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset xls preview controller', function () {
    'use strict';

    var createController, scope, gridElement, stateMock;
    var content = {column: [], records: []};

    beforeEach(angular.mock.module('data-prep.dataset-xls-preview', function ($provide) {
        stateMock = {folder: {currentFolder: {path :'HOME'}}};
        $provide.constant('state', stateMock);
    }));


    beforeEach(inject(function ($rootScope, $controller, $q, DatasetSheetPreviewService, DatasetService, DatasetRestService, PlaygroundService, StateService, FolderService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('DatasetXlsPreviewCtrl', {
                $scope: scope
            });
        };

        //insert grid entry point
        gridElement = angular.element('<div id="datasetSheetPreviewGrid"></div>');
        angular.element('body').append(gridElement);

        spyOn(DatasetSheetPreviewService, 'loadSheet').and.returnValue();
        spyOn(DatasetSheetPreviewService, 'setDatasetSheet').and.returnValue($q.when(true));
        spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when(true));
        spyOn(DatasetService, 'getContent').and.returnValue($q.when(content));
        spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when());
        spyOn(StateService, 'showPlayground').and.returnValue();
        spyOn(FolderService, 'getContent').and.returnValue();
    }));

    afterEach(function() {
        gridElement.remove();
    });

    it('should bind modal state getter to DatasetSheetPreviewService.showModal', inject(function (DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        expect(ctrl.state).toBeFalsy();

        //when
        DatasetSheetPreviewService.showModal = true;

        //then
        expect(ctrl.state).toBe(true);
    }));

    it('should bind modal state setter to DatasetSheetPreviewService.showModal', inject(function (DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        DatasetSheetPreviewService.showModal = true;

        //when
        ctrl.state = false;

        //then
        expect(DatasetSheetPreviewService.showModal).toBe(false);
    }));

    it('should bind metadata getter to DatasetSheetPreviewService.currentMetadata', inject(function (DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        expect(ctrl.metadata).toBeFalsy();

        //when
        DatasetSheetPreviewService.currentMetadata = true;

        //then
        expect(ctrl.metadata).toBe(true);
    }));

    it('should bind selectedSheetName getter to DatasetSheetPreviewService.selectedSheetName', inject(function (DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        expect(ctrl.selectedSheetName).toBeFalsy();

        //when
        DatasetSheetPreviewService.selectedSheetName = 'my sheet';

        //then
        expect(ctrl.selectedSheetName).toBe('my sheet');
    }));

    it('should bind selectedSheetName setter to DatasetSheetPreviewService.selectedSheetName', inject(function (DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        DatasetSheetPreviewService.selectedSheetName = 'my sheet';

        //when
        ctrl.selectedSheetName = 'my new sheet';

        //then
        expect(DatasetSheetPreviewService.selectedSheetName).toBe('my new sheet');
    }));

    it('should set grid in service', inject(function ($timeout, DatasetSheetPreviewService) {
        //given
        expect(DatasetSheetPreviewService.grid).toBeFalsy();

        //when
        createController();
        $timeout.flush();

        //then
        expect(DatasetSheetPreviewService.grid).toBeTruthy();
    }));

    it('should call service to load the selected sheet', inject(function ($timeout, DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        $timeout.flush();
        ctrl.selectedSheetName = 'my sheet';

        //when
        ctrl.selectSheet();

        //then
        expect(DatasetSheetPreviewService.loadSheet).toHaveBeenCalledWith('my sheet');
    }));

    it('should set the selected sheet to the dataset', inject(function ($timeout, DatasetSheetPreviewService) {
        //given
        var ctrl = createController();
        $timeout.flush();
        ctrl.selectedSheetName = 'my sheet';

        //when
        ctrl.setDatasetSheet();

        //then
        expect(DatasetSheetPreviewService.setDatasetSheet).toHaveBeenCalledWith('my sheet');
    }));

    it('should refresh datasets list on selected sheet set', inject(function ($timeout, DatasetService, FolderService) {
        //given
        var ctrl = createController();
        $timeout.flush();

        //when
        ctrl.setDatasetSheet();
        scope.$digest();

        //then
        expect(DatasetService.refreshDatasets).toHaveBeenCalled();
        expect(FolderService.getContent).toHaveBeenCalledWith({path :'HOME'});
    }));

    it('should open dataset on selected sheet set', inject(function ($timeout, DatasetSheetPreviewService, PlaygroundService, StateService) {
        //given
        var ctrl = createController();
        $timeout.flush();
        ctrl.selectedSheetName = 'my sheet';
        DatasetSheetPreviewService.currentMetadata = {id : 'mqfdg684qfg6q84g8q'};

        //when
        ctrl.setDatasetSheet();
        scope.$digest();

        //then
        expect(PlaygroundService.initPlayground).toHaveBeenCalledWith(DatasetSheetPreviewService.currentMetadata, undefined);
        expect(StateService.showPlayground).toHaveBeenCalled();
    }));

    it('should hide modal on selected sheet set', inject(function ($timeout) {
        //given
        var ctrl = createController();
        $timeout.flush();
        ctrl.state = true;

        //when
        ctrl.setDatasetSheet();
        scope.$digest();

        //then
        expect(ctrl.state).toBe(false);
    }));
});
