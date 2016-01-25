describe('Folder Rest Service', function () {
    'use strict';

    var $httpBackend;

    beforeEach(module('data-prep.services.folder'));

    beforeEach(inject(function ($rootScope, $injector, RestURLs) {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('children', function () {
        it('should call get root\'s children when there is no provided path', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl)
                .respond(200);

            //when
            FolderRestService.children();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should call get folder\'s children', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            var path = '/foo/bar';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '?path=' + encodeURIComponent(path))
                .respond(200);

            //when
            FolderRestService.children(path);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('create', function () {
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
    });

    describe('content', function () {
        it('should get root folder content when there is no provided path', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=%2F')
                .respond(200);

            //when
            FolderRestService.getContent();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should get folder content', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            var folderPath = 'toto';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=toto')
                .respond(200);

            //when
            FolderRestService.getContent(folderPath);
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should get folder content with sort and order', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            var folderPath = 'toto';
            var sort = 'name';
            var order = 'asc';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '/datasets' + '?folder=toto&sort=name&order=asc')
                .respond(200);

            //when
            FolderRestService.getContent(folderPath, sort, order);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('remove', function () {
        it('should call remove', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            var folderPath = 'the beer';
            $httpBackend
                .expectDELETE(RestURLs.folderUrl + '?path=' + encodeURIComponent(folderPath))
                .respond(200);

            //when
            FolderRestService.remove(folderPath);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('rename', function () {
        it('should call rename', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            $httpBackend
                .expectPUT(RestURLs.folderUrl + '/rename?path=foo&newPath=beer')
                .respond(200);

            //when
            FolderRestService.rename('foo', 'beer');
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('search', function () {
        it('should search with no folder name', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl + '/search')
                .respond(200);

            //when
            FolderRestService.search();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should call search with the provided folder path', inject(function ($rootScope, FolderRestService, RestURLs) {
            //given
            var path = '/foo/bar';
            $httpBackend
                .expectGET(RestURLs.folderUrl + '/search?pathName=' + encodeURIComponent(path))
                .respond(200);

            //when
            FolderRestService.search(path);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });
});