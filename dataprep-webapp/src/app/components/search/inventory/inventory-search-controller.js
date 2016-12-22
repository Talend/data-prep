/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.inventory-search.controller:InventorySearchCtrl
 * @description InventorySearchCtrl controller.
 * @requires data-prep.services.search:SearchService
 *
 */
class InventorySearchCtrl {

	constructor(SearchService) {
		'ngInject';
		this.searchService = SearchService;
	}

	/**
	 * @ngdoc method
	 * @name search
	 * @methodOf data-prep.inventory-search.controller:InventorySearchCtrl
	 * @description Search based on searchInput
	 */
	search(searchInput) {
		this.results = null;
		this.currentInput = searchInput;

		return this.searchService.searchAll(searchInput)
			.then(response => (this.currentInput === searchInput) && (this.results = response));
	}
}

export default InventorySearchCtrl;
