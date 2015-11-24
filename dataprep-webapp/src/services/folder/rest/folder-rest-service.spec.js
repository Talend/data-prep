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
            .expectDELETE(RestURLs.folderUrl + '?path=' + encodeURIComponent(path))
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
            .expectPUT(RestURLs.folderUrl + '?path=' + encodeURIComponent(path))
            .respond(200);

        //when
        FolderRestService.create(path);
        $httpBackend.flush();
        $rootScope.$digest();

    }));


    it('should call create folder entry', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectPUT(RestURLs.folderUrl + '/entries')
            .respond(200);

        //when
        FolderRestService.createFolderEntry('contentType', 'contentId', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call delete folder entry', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectDELETE(RestURLs.folderUrl + '/entries/contentType/contentId?path=thepath')
            .respond(200);

        //when
        FolderRestService.deleteFolderEntry('contentType', 'contentId', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call list folder entries', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.folderUrl + '/entries?path=thepath&contentType=contentType')
            .respond(200);

        //when
        FolderRestService.listFolderEntries('contentType', 'thepath');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call list folder entries w/o path', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectGET(RestURLs.folderUrl + '/entries?path='+encodeURIComponent('/')+'&contentType=contentType')
            .respond(200);

        //when
        FolderRestService.listFolderEntries('contentType');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

    it('should call rename', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given

        $httpBackend
            .expectPUT(RestURLs.folderUrl + '/rename?path=foo&newPath=beer')
            .respond(200);

        //when
        FolderRestService.renameFolder('foo', 'beer');
        $httpBackend.flush();
        $rootScope.$digest();

    }));

});