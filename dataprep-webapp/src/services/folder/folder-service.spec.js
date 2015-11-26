describe('Folder services', function () {
    'use strict';


    beforeEach(module('data-prep.services.folder'));

    beforeEach(inject(function($q, FolderRestService, StateService) {
        spyOn(FolderRestService, 'create').and.returnValue($q.when());
        spyOn(FolderRestService, 'renameFolder').and.returnValue($q.when());
        spyOn(StateService, 'setFoldersStack').and.returnValue();

    }));

    it('should call rest create', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.create('/foo');
        $rootScope.$digest();

        //then
        expect(FolderRestService.create).toHaveBeenCalledWith('/foo');
    }));

    it('should call rest renameFolder', inject(function ($rootScope, FolderService, FolderRestService) {
        //when
        FolderService.renameFolder('foo', 'beer');
        $rootScope.$digest();

        //then
        expect(FolderRestService.renameFolder).toHaveBeenCalledWith('foo', 'beer');
    }));


    it('should build stack from folder id', inject(function ($rootScope, FolderService, StateService) {
        //when
        FolderService.buidStackFromId('1/2');

        //then
        expect(StateService.setFoldersStack).toHaveBeenCalledWith([{id:'', path:'', name: 'HOME_FOLDER'},{id : '1', path: '1', name: '1'},{id : '1/2', path: '1/2', name: '2'}]);
    }));

    it('should build stack from root folder id', inject(function ($rootScope, FolderService, StateService) {
        //when
        FolderService.buidStackFromId('');

        //then
        expect(StateService.setFoldersStack).toHaveBeenCalledWith([{id:'', path:'', name: 'HOME_FOLDER'}]);
    }));


    it('should populateMenuChilds', inject(function ($q, $rootScope, FolderService, StateService, FolderRestService) {
        //Given
        var content ={data: {
            folders: [{id : 'toto', path: 'toto', name: 'toto'}]
        }};
        spyOn(FolderRestService, 'getFolderContent').and.returnValue($q.when(content));
        spyOn(StateService, 'setMenuChilds').and.returnValue();

        //when
        FolderService.populateMenuChilds({id : 'toto', path: 'toto', name: 'toto'});
        $rootScope.$digest();

        //then
        expect(FolderRestService.getFolderContent).toHaveBeenCalledWith({id : 'toto', path: 'toto', name: 'toto'});
        expect(StateService.setMenuChilds).toHaveBeenCalledWith([{id : 'toto', path: 'toto', name: 'toto'}]);
    }));

    it('should getFolderContent', inject(function ($q, $rootScope, FolderService, StateService, FolderRestService, DatasetListSortService) {
        //Given
        var content ={data: {
            folders: [{id : 'toto', path: 'toto', name: 'toto'}]
        }};

        spyOn(DatasetListSortService, 'getSort').and.returnValue('name');
        spyOn(DatasetListSortService, 'getOrder').and.returnValue('asc');

        spyOn(FolderRestService, 'getFolderContent').and.returnValue($q.when(content));
        spyOn(StateService, 'setCurrentFolder').and.returnValue();
        spyOn(StateService, 'setCurrentFolderContent').and.returnValue();

        //when
        FolderService.getFolderContent({id : 'toto', path: 'toto', name: 'toto'});
        $rootScope.$digest();

        //then
        expect(FolderRestService.getFolderContent).toHaveBeenCalledWith({id : 'toto', path: 'toto', name: 'toto'}, 'name', 'asc');
        expect(StateService.setCurrentFolder).toHaveBeenCalledWith({id : 'toto', path: 'toto', name: 'toto'});
        expect(StateService.setCurrentFolderContent).toHaveBeenCalledWith({
            folders: [{id : 'toto', path: 'toto', name: 'toto'}]
        });
        expect(StateService.setFoldersStack).toHaveBeenCalledWith([{id:'', path:'', name: 'HOME_FOLDER'},{id : 'toto', path: 'toto', name: 'toto'}]);
    }));

    it('should getFolderContent for root folder', inject(function ($q, $rootScope, FolderService, StateService, FolderRestService, DatasetListSortService) {
        //Given
        var content ={data: {
            folders: [{id : 'toto', path: 'toto', name: 'toto'}]
        }};

        spyOn(DatasetListSortService, 'getSort').and.returnValue('name');
        spyOn(DatasetListSortService, 'getOrder').and.returnValue('asc');

        spyOn(FolderRestService, 'getFolderContent').and.returnValue($q.when(content));
        spyOn(StateService, 'setCurrentFolder').and.returnValue();
        spyOn(StateService, 'setCurrentFolderContent').and.returnValue();

        //when
        FolderService.getFolderContent();
        $rootScope.$digest();

        //then
        expect(FolderRestService.getFolderContent).toHaveBeenCalledWith(undefined, 'name', 'asc');
        expect(StateService.setCurrentFolder).toHaveBeenCalledWith({id:'', path:'', name: 'HOME_FOLDER'});
        expect(StateService.setCurrentFolderContent).toHaveBeenCalledWith({
            folders: [{id : 'toto', path: 'toto', name: 'toto'}]
        });
        expect(StateService.setFoldersStack).toHaveBeenCalledWith([{id:'', path:'', name: 'HOME_FOLDER'}]);
    }));

});
