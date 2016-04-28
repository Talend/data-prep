/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder Rest Service', () => {
    'use strict';

    let $httpBackend;

    beforeEach(angular.mock.module('data-prep.services.folder'));

    beforeEach(inject(($rootScope, $injector, RestURLs) => {
        RestURLs.setServerUrl('');
        $httpBackend = $injector.get('$httpBackend');
    }));

    describe('children', () => {
        it('should call get root\'s children when there is no provided path', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl)
                .respond(200);

            //when
            FolderRestService.children();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should call get folder\'s children', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const path = '/foo/bar';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '?path=' + encodeURIComponent(path))
                .respond(200);

            //when
            FolderRestService.children(path);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('create', () => {
        it('should call create folder', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const path = '/foo/bar';

            $httpBackend
                .expectPUT(RestURLs.folderUrl + '?path=' + encodeURIComponent(path))
                .respond(200);

            //when
            FolderRestService.create(path);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('content', () => {
        it('should get root folder content when there is no provided path', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl + '/preparations?folder=%2F')
                .respond(200);

            //when
            FolderRestService.getContent();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should get folder content', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const folderPath = 'toto';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '/preparations?folder=toto')
                .respond(200);

            //when
            FolderRestService.getContent(folderPath);
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should get folder content with sort and order', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const folderPath = 'toto';
            const sort = 'name';
            const order = 'asc';

            $httpBackend
                .expectGET(RestURLs.folderUrl + '/preparations?folder=toto&sort=name&order=asc')
                .respond(200);

            //when
            FolderRestService.getContent(folderPath, sort, order);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('remove', () => {
        it('should call remove', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const folderPath = 'the beer';
            $httpBackend
                .expectDELETE(RestURLs.folderUrl + '?path=' + encodeURIComponent(folderPath))
                .respond(200);

            //when
            FolderRestService.remove(folderPath);
            $httpBackend.flush();
            $rootScope.$digest();
        }));
    });

    describe('rename', () => {
        it('should call rename', inject(($rootScope, FolderRestService, RestURLs) => {
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

    describe('search', () => {
        it('should search with no folder name', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            $httpBackend
                .expectGET(RestURLs.folderUrl + '/search')
                .respond(200);

            //when
            FolderRestService.search();
            $httpBackend.flush();
            $rootScope.$digest();
        }));

        it('should call search with the provided folder path', inject(($rootScope, FolderRestService, RestURLs) => {
            //given
            const path = '/foo/bar';
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