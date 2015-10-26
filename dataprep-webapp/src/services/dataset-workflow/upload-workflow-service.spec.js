describe('UploadWorkflow Service', function () {
    'use strict';

    beforeEach(module('data-prep.services.datasetWorkflowService'));
    beforeEach(inject(function ($state, $q, DatasetSheetPreviewService, MessageService, DatasetService) {
        spyOn($state, 'go').and.returnValue(null);
        spyOn(DatasetSheetPreviewService, 'loadPreview').and.returnValue($q.when(true));
        spyOn(DatasetSheetPreviewService, 'display').and.returnValue();
        spyOn(MessageService, 'error').and.returnValue(null);
        spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when(true));
    }));

    it('should redirect to dataset playground when dataset is not a draft', inject(function (UploadWorkflowService, $state) {
        //given
        var dataset = {name: 'Customers (50 lines)', id: 'aA2bc348e933bc2'};

        //when
        UploadWorkflowService.openDataset(dataset);

        //then
        expect($state.go).toHaveBeenCalledWith('nav.home.datasets', {datasetid: dataset.id});
    }));

    it('should redirect load sheet preview when dataset is a draft', inject(function ($q, $rootScope, UploadWorkflowService, DatasetSheetPreviewService) {
        //given
        var dataset = {
            name: 'Customers (50 lines)',
            id: 'aA2bc348e933bc2',
            type: 'application/vnd.ms-excel',
            draft: true
        };

        //when
        UploadWorkflowService.openDataset(dataset);
        $rootScope.$digest();

        //then
        expect(DatasetSheetPreviewService.loadPreview).toHaveBeenCalledWith(dataset);
        expect(DatasetSheetPreviewService.display).toHaveBeenCalled();
    }));


    it('should load excel draft preview and display it', inject(function ($rootScope, $q, UploadWorkflowService, DatasetSheetPreviewService) {
        //given
        var draft = {type: 'application/vnd.ms-excel'};

        //when
        UploadWorkflowService.openDraft(draft);
        $rootScope.$digest();

        //then
        expect(DatasetSheetPreviewService.loadPreview).toHaveBeenCalledWith(draft);
        expect(DatasetSheetPreviewService.display).toHaveBeenCalled();
    }));

    it('should display error message with unknown draft type', inject(function (DatasetSheetPreviewService, $q, UploadWorkflowService, MessageService) {
        //given
        var draft = {type: 'application/myCustomType'};

        //when
        UploadWorkflowService.openDraft(draft);

        //then
        expect(DatasetSheetPreviewService.loadPreview).not.toHaveBeenCalled();
        expect(DatasetSheetPreviewService.display).not.toHaveBeenCalled();
        expect(MessageService.error).toHaveBeenCalledWith('PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE', 'PREVIEW_NOT_IMPLEMENTED_FOR_TYPE_TITLE');
    }));

    it('should refresh dataset list and display error when draft has no type yet', inject(function (UploadWorkflowService, $q, DatasetSheetPreviewService, DatasetService, MessageService) {
        //given
        var draft = {};


        //when
        UploadWorkflowService.openDraft(draft);

        //then
        expect(DatasetSheetPreviewService.loadPreview).not.toHaveBeenCalled();
        expect(DatasetSheetPreviewService.display).not.toHaveBeenCalled();
        expect(MessageService.error).toHaveBeenCalledWith('FILE_FORMAT_ANALYSIS_NOT_READY_TITLE', 'FILE_FORMAT_ANALYSIS_NOT_READY_CONTENT');
        expect(DatasetService.refreshDatasets).toHaveBeenCalled();
    }));
});