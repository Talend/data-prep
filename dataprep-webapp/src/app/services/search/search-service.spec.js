/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const searchInput = 'lorem ipsum';

describe('Search service', () => {
	beforeEach(angular.mock.module('data-prep.services.search'));

	describe('searchDocumentation', () => {
		it('should call documentation service', inject(($rootScope, SearchService, DocumentationService) => {
			// given
			spyOn(DocumentationService, 'search');

			// when
			SearchService.searchDocumentation(searchInput);
			$rootScope.$digest();

			// then
			expect(DocumentationService.search).toHaveBeenCalledWith(searchInput);
		}));
	});

	describe('searchInventory', () => {
		it('should call inventory service', inject(($rootScope, SearchService, InventoryService) => {
			// given
			spyOn(InventoryService, 'search');

			// when
			SearchService.searchInventory(searchInput);
			$rootScope.$digest();

			// then
			expect(InventoryService.search).toHaveBeenCalledWith(searchInput);
		}));
	});

	describe('searchAll', () => {
		it('should call all services', inject(($rootScope, $q, SearchService, DocumentationService, InventoryService) => {
			// given
			spyOn(DocumentationService, 'search').and.returnValue($q.when([]));
			spyOn(InventoryService, 'search').and.returnValue($q.when([]));

			// when
			SearchService.searchAll(searchInput);
			$rootScope.$digest();

			// then
			expect(DocumentationService.search).toHaveBeenCalledWith(searchInput);
			expect(InventoryService.search).toHaveBeenCalledWith(searchInput);
		}));

		it('should aggregate results', inject(($rootScope, $q, SearchService, DocumentationService, InventoryService) => {
			let results = null;

			// given
			const documentationResult = 'documentation';
			const inventoryResult = 'inventory';
			spyOn(DocumentationService, 'search').and.returnValue($q.when([documentationResult]));
			spyOn(InventoryService, 'search').and.returnValue($q.when([inventoryResult]));

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
