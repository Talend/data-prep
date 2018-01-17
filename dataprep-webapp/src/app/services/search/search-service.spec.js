/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search service', () => {
	let stateMock;

	const searchInput = 'lorem ipsum';

	beforeEach(angular.mock.module('data-prep.services.search', ($provide) => {
		stateMock = {
			search: {
				searchInput,
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('searchDocumentation', () => {
		it('should call documentation service', inject(($rootScope, SearchService, SearchDocumentationService) => {
			// given
			spyOn(SearchDocumentationService, 'search');

			// when
			SearchService.searchDocumentation(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchDocumentationService.search).toHaveBeenCalledWith(searchInput);
		}));
	});

	describe('searchInventory', () => {
		it('should call inventory service', inject(($rootScope, SearchService, SearchInventoryService) => {
			// given
			spyOn(SearchInventoryService, 'search');

			// when
			SearchService.searchInventory(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchInventoryService.search).toHaveBeenCalledWith(searchInput);
		}));
	});

	describe('searchAll', () => {
		it('should call all services', inject(($rootScope, $q, SearchService, SearchDocumentationService, SearchInventoryService) => {
			// given
			spyOn(SearchDocumentationService, 'search').and.returnValue($q.when([]));
			spyOn(SearchInventoryService, 'search').and.returnValue($q.when([]));

			// when
			SearchService.searchAll(searchInput);
			$rootScope.$digest();

			// then
			expect(SearchDocumentationService.search).toHaveBeenCalledWith(searchInput);
			expect(SearchInventoryService.search).toHaveBeenCalledWith(searchInput);
		}));

		it('should aggregate results', inject(($rootScope, $q, SearchService, SearchDocumentationService, SearchInventoryService) => {
			let results = null;

			// given
			const documentationResult = 'documentation';
			const inventoryResult = 'inventory';
			spyOn(SearchDocumentationService, 'search').and.returnValue($q.when([documentationResult]));
			spyOn(SearchInventoryService, 'search').and.returnValue($q.when([inventoryResult]));

			// when
			SearchService.searchAll(searchInput).then((response) => {
				results = response;
			});
			$rootScope.$digest();

			// then
			expect(results.length).toBe(2);
			expect(results).toContain(documentationResult);
			expect(results).toContain(inventoryResult);
		}));
	});
});
