describe('Folder services', function () {
    'use strict';


    beforeEach(module('data-prep.services.folder'));

    beforeEach(inject(function($q, FolderRestService) {
        spyOn(FolderRestService, 'delete').and.returnValue($q.when());
        spyOn(FolderRestService, 'create').and.returnValue($q.when());
        spyOn(FolderRestService, 'folders').and.returnValue($q.when());
        spyOn(FolderRestService, 'deleteFolderEntry').and.returnValue($q.when());
        spyOn(FolderRestService, 'createFolderEntry').and.returnValue($q.when());
        spyOn(FolderRestService, 'listFolderEntries').and.returnValue($q.when());

    }));

    it('should call rest delete', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.delete('/foo');
        $rootScope.$digest();

        //then
        expect(FolderRestService.delete).toHaveBeenCalledWith('/foo');
    }));

    it('should call rest create', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.create('/foo');
        $rootScope.$digest();

        //then
        expect(FolderRestService.create).toHaveBeenCalledWith('/foo');
    }));

    it('should call rest folders with a path', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.folders('/foo');
        $rootScope.$digest();

        //then
        expect(FolderRestService.folders).toHaveBeenCalledWith('/foo');
    }));

    it('should call rest folders w/o path', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.folders('');
        $rootScope.$digest();

        //then
        expect(FolderRestService.folders).toHaveBeenCalledWith('');
    }));

    it('should call rest deleteFolderEntry', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.deleteFolderEntry('contentType', 'contentId', 'path');
        $rootScope.$digest();

        //then
        expect(FolderRestService.deleteFolderEntry).toHaveBeenCalledWith('contentType', 'contentId', 'path');
    }));

    it('should call rest createFolderEntry', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.createFolderEntry('contentType', 'contentId', {id:'/beer'});
        $rootScope.$digest();

        //then
        expect(FolderRestService.createFolderEntry).toHaveBeenCalledWith('contentType', 'contentId', '/beer');
    }));

    it('should call rest listFolderEntries', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.listFolderEntries('contentType', {id:'/beer'});
        $rootScope.$digest();

        //then
        expect(FolderRestService.listFolderEntries).toHaveBeenCalledWith('contentType', '/beer');
    }));

});
