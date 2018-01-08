/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
export default function SearchInventoryService($q, SearchInventoryRestService, StateService) {
	'ngInject';

	let deferredCancel = null;

	return {
		adaptSearchResult,
		addHtmlLabelsAndSort,
		search,
	};

	/**
	 * @ngdoc method
	 * @name cancelPendingGetRequest
	 * @methodOf data-prep.services.search.inventory:SearchInventoryService
	 * @description Cancel the pending search GET request
	 */
	function cancelPendingGetRequest() {
		if (deferredCancel) {
			deferredCancel.resolve('user cancel');
			deferredCancel = null;
		}
	}

	/**
	 * @ngdoc method
	 * @name search
	 * @methodOf data-prep.services.search.inventory:SearchInventoryService
	 * @param {String} searchValue string
	 * @description Search inventory items
	 */
	function search(searchValue) {
		cancelPendingGetRequest();

		deferredCancel = $q.defer();

		return SearchInventoryRestService.search(searchValue, deferredCancel)
			.then((response) => {
				StateService.setSearchCategories(response.data.categories ? response.data.categories : null);
				return this.addHtmlLabelsAndSort(response.data);
			})
			.catch(() => [])
			.finally(() => deferredCancel = null);
	}

	/**
	 * @ngdoc method
	 * @name adaptSearchResult
	 * @methodOf data-prep.services.search.inventory:SearchInventoryService
	 * @param {Object} data data to process
	 * @description adapte search result for react components
	 */
	function adaptSearchResult(data) {
		let inventoryItems = [];

		if (data.datasets && data.datasets.length) {
			data.datasets.forEach((item) => {
				const itemToDisplay = {};

				itemToDisplay.id = item.id;
				itemToDisplay.inventoryType = 'dataset';
				itemToDisplay.author = item.author;
				itemToDisplay.created = item.created;
				itemToDisplay.records = item.records;
				itemToDisplay.name = item.name;
				itemToDisplay.path = item.path;
				itemToDisplay.type = item.type;
				itemToDisplay.model = item;
				itemToDisplay.lastModificationDate = item.lastModificationDate;
				itemToDisplay.tooltipName = item.name;
				itemToDisplay.owner = item.owner;

				inventoryItems.push(itemToDisplay);
			});
		}

		if (data.preparations && data.preparations.length) {
			data.preparations.forEach((item) => {
				item.inventoryType = 'preparation';
				item.tooltipName = item.name;
			});

			inventoryItems = inventoryItems.concat(data.preparations);
		}

		if (data.folders && data.folders.length) {
			data.folders.forEach((item) => {
				item.inventoryType = 'folder';
				item.tooltipName = item.name;
			});

			inventoryItems = inventoryItems.concat(data.folders);
		}

		return inventoryItems;
	}

	/**
	 * @ngdoc method
	 * @name addHtmlLabelsAndSort
	 * @methodOf data-prep.services.search.inventory:SearchInventoryService
	 * @param {Object} data data to process
	 * @description add html label to data based on searchValue and sort the results
	 */
	function addHtmlLabelsAndSort(data) {
		return _.chain(this.adaptSearchResult(data))
			.sortBy('lastModificationDate')
			.reverse()
			.value();
	}
}
