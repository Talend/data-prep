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
            .expectGET(RestURLs.datasetUrl + '/folders/add?path=' + encodeURIComponent(path))
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
        FolderRestService.childs(path);
        $httpBackend.flush();
        $rootScope.$digest();


    }));

    it('should call get folders root', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.datasetUrl + '/folders')
            .respond(200);

        //when
        FolderRestService.childs();
        $httpBackend.flush();
        $rootScope.$digest();


    }));

});