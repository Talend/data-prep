/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const EASTER_EGGS_VALUE = 'star wars';

/**
 * @ngdoc service
 * @name data-prep.services.recipe.service:SearchService
 * @description Search service. This service provide the entry point to search
 */
export default function SearchService($q, SearchDocumentationService, EasterEggsService, SearchInventoryService) {
	'ngInject';

	return {
		searchAll,
		searchDocumentation,
		searchDocumentationAndHighlight,
		searchInventory,
	};

	function searchInventory(searchInput) {
		return SearchInventoryService.search(searchInput);
	}

	function searchDocumentation(searchInput) {
		return SearchDocumentationService.search(searchInput);
	}

	function searchDocumentationAndHighlight(searchInput) {
		return SearchDocumentationService.searchAndHighlight(searchInput);
	}

	function searchAll(searchInput) {
		if (searchInput && searchInput.toLowerCase() === EASTER_EGGS_VALUE) {
			EasterEggsService.enableEasterEgg(searchInput);
		}

		const inventoryPromise = searchInventory(searchInput);
		const documentationPromise = searchDocumentation(searchInput);

		return $q
			.all([inventoryPromise, documentationPromise])
			.then(([inventory, documentation]) => inventory.concat(documentation));
	}
}
