/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

 import settings from '../../../../mocks/Settings.mock';

describe('Folder Rest Service', () => {
	'use strict';

	let $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.folder'));

	beforeEach(inject(($rootScope, $injector, RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');
	}));

	describe('children', () => {
		it('should call get folder\'s children', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const folderId = 'L215L2ZvbGRlcg==';

			$httpBackend
				.expectGET(`${RestURLs.folderUrl}?parentId=${encodeURIComponent(folderId)}`)
				.respond(200);

			//when
			FolderRestService.children(folderId);
			$httpBackend.flush();
			$rootScope.$digest();
		}));
	});

	describe('create', () => {
		it('should call create folder', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const parentId = 'L215L2ZvbGRlcg==';
			const path = 'toto';

			$httpBackend
				.expectPUT(`${RestURLs.folderUrl}?parentId=${encodeURIComponent(parentId)}&path=${path}`)
				.respond(200);

			//when
			FolderRestService.create(parentId, path);
			$httpBackend.flush();
			$rootScope.$digest();
		}));
	});

	describe('content', () => {
		it('should get folder content', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const folderId = 'L215L2ZvbGRlcg==';

			$httpBackend
				.expectGET(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}/preparations`)
				.respond(200);

			//when
			FolderRestService.getContent(folderId);
			$httpBackend.flush();
			$rootScope.$digest();
		}));

		it('should get folder content with sort and order', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const folderId = 'L215L2ZvbGRlcg==';
			const sort = 'name';
			const order = 'asc';

			$httpBackend
				.expectGET(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}/preparations?sort=name&order=asc`)
				.respond(200);

			//when
			FolderRestService.getContent(folderId, sort, order);
			$httpBackend.flush();
			$rootScope.$digest();
		}));
	});

	describe('remove', () => {
		it('should call remove', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const folderId = 'L215L2ZvbGRlcg==';
			$httpBackend
				.expectDELETE(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}`)
				.respond(200);

			//when
			FolderRestService.remove(folderId);
			$httpBackend.flush();
			$rootScope.$digest();
		}));
	});

	describe('rename', () => {
		it('should call rename', inject(($rootScope, FolderRestService, RestURLs) => {
			//given
			const folderId = 'L215L2ZvbGRlcg==';
			$httpBackend
				.expectPUT(`${RestURLs.folderUrl}/${encodeURIComponent(folderId)}/name`, 'beer')
				.respond(200);

			//when
			FolderRestService.rename(folderId, 'beer');
			$httpBackend.flush();
			$rootScope.$digest();
		}));
	});
});
