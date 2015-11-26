describe('Folder Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.folder'));

    beforeEach(inject(function ($rootScope, $injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');

        spyOn($rootScope, '$emit').and.returnValue();
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

    it('should get root folder content', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var folder = {id : '', path: '', name: 'Home'};
        var sort = 'name';
        var order = 'asc';
        var result='';

        $httpBackend
            .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=%2F&sort=name&order=asc')
            .respond(200, 'content');

        //when
        FolderRestService.getFolderContent(folder, sort, order).then(function (res) {
            result = res.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        expect(result).toBe('content');


    }));

    it('should get folder content', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var folder = {id : 'toto', path: 'toto', name: 'toto'};
        var sort = 'name';
        var order = 'asc';
        var result='';

        $httpBackend
            .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=toto&sort=name&order=asc')
            .respond(200, 'content');

        //when
        FolderRestService.getFolderContent(folder, sort, order).then(function (res) {
            result = res.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        expect(result).toBe('content');

    }));


    it('should get folder content without sort&order', inject(function ($rootScope, FolderRestService, RestURLs) {
        //given
        var folder = {id : 'toto', path: 'toto', name: 'toto'};
        var result='';

        $httpBackend
            .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=toto')
            .respond(200, 'content');

        //when
        FolderRestService.getFolderContent(folder).then(function (res) {
            result = res.data;
        });
        $httpBackend.flush();
        $rootScope.$digest();

        expect(result).toBe('content');

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