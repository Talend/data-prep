describe('Folder Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.folder'));

    beforeEach(inject(function ($rootScope, $injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');

        spyOn($rootScope, '$emit').and.returnValue();
    }));


    it('should call delete folder', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var path = '/foo/bar';

        $httpBackend
            .expectDELETE(RestURLs.datasetUrl + '/folders?path=' + encodeURIComponent(path))
            .respond(200);

        //when
        FolderRestService.delete(path);
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call create folder', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var path = '/foo/bar';

        $httpBackend
            .expectPUT(RestURLs.datasetUrl + '/folders/add?path=' + encodeURIComponent(path))
            .respond(200);

        //when
        FolderRestService.create(path);
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call get folders', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var path = '/foo';

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/folders?path=' + encodeURIComponent(path))
            .respond(200);

        //when
        FolderRestService.folders(path);
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call get folders root', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/folders')
            .respond(200);

        //when
        FolderRestService.folders();
        $httpBackend.flush();
        $rootScope.$digest();

    }));


    it('should call create folder entry', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectPUT(RestURLs.datasetUrl + '/folders/entries')
            .respond(200);

        //when
        FolderRestService.createFolderEntry('contentType', 'contentId', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call delete folder entry', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectDELETE(RestURLs.datasetUrl + '/folders/entries/contentType/contentId?path=thepath')
            .respond(200);

        //when
        FolderRestService.deleteFolderEntry('contentType', 'contentId', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call list folder entries', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/folders/entries?path=thepath&contentType=contentType')
            .respond(200);

        //when
        FolderRestService.listFolderEntries('contentType', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call list folder entries w/o path', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/folders/entries?contentType=contentType')
            .respond(200);

        //when
        FolderRestService.listFolderEntries('contentType');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

});