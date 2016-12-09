/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { find, reject } from 'lodash';

const sortList = [
    { id: 'name', name: 'NAME_SORT', property: 'name' },
    { id: 'date', name: 'DATE_SORT', property: 'created' },
];

const orderList = [
    { id: 'asc', name: 'ASC_ORDER' },
    { id: 'desc', name: 'DESC_ORDER' },
];

const HOME_FOLDER = {
	id: 'Lw==',
	path: '/',
	name: 'Home',
};

export const inventoryState = {
	datasets: null,

	sortList,
	orderList,
	datasetsSort: sortList[1],
	datasetsOrder: orderList[1],
	preparationsSort: sortList[1],
	preparationsOrder: orderList[1],

	datasetsDisplayMode: 'table',
	preparationsDisplayMode: 'table',

	homeFolderId: HOME_FOLDER.id,
	folder: {
		metadata: HOME_FOLDER,
		content: {
			folders: [],
			preparations: [],
		},
	},
	breadcrumb: [],
	breadcrumbChildren: {},

	isFetchingDatasets: false,
	isFetchingPreparations: false,
};

export function InventoryStateService() {
	return {
		enableEdit,
		disableEdit,

		setDatasets,
		removeDataset,
		setDatasetName,

		setHomeFolderId,
		setFolder,
		setBreadcrumb,
		setBreadcrumbChildren,

		setDatasetsSort,
		setDatasetsOrder,
		setDatasetsSortFromIds,
		setDatasetsDisplayMode,

		setPreparationsSort,
		setPreparationsOrder,
		setPreparationsSortFromIds,
		setPreparationsDisplayMode,

		setFetchingDatasets,
		setFetchingPreparations,
	};

	function createNextEntities(type, fn) {
		switch (type) {
		case 'folder': {
			const folders = inventoryState.folder.content.folders;
			inventoryState.folder.content.folders = fn(folders);
			break;
		}
		case 'preparation': {
			const preparations = inventoryState.folder.content.preparations;
			inventoryState.folder.content.preparations = fn(preparations);
			break;
		}
		}
	}

	/**
	 * @ngdoc method
	 * @name enableEdit
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {object} entity The entity to switch display mode
	 * @description Switch the edit mode of the provided entity
	 */
	function enableEdit(entity) {
		const nextEntities = entities => entities.map((item) => {
			if (item.id === entity.id) {
				return {
					...item,
					displayMode: 'input',
				};
			}
			return item;
		});
		createNextEntities(entity.type, nextEntities);
	}

	/**
	 * @ngdoc method
	 * @name cancelEdit
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {object} entity The entity to switch display mode
	 * @description Switch the edit mode of the provided entity
	 */
	function disableEdit(entity) {
		const nextEntities = entities => entities.map((item) => {
			if (item.id === entity.id) {
				return {
					...item,
					displayMode: undefined,
				};
			}
			return item;
		});
		createNextEntities(entity.type, nextEntities);
	}

    /**
     * @ngdoc method
     * @name setFetchingDatasets
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {boolean} bool isFetchingDatasets flag
     * @description Set isFetchingDatasets in Inventory
     */
	function setFetchingDatasets(bool) {
		inventoryState.isFetchingDatasets = bool;
	}

    /**
     * @ngdoc method
     * @name setFetchingPreparations
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {boolean} bool setFetchingPreparations flag
     * @description Set isFetchingPreparations in Inventory
     */
	function setFetchingPreparations(bool) {
		inventoryState.isFetchingPreparations = bool;
	}

    /**
     * @ngdoc method
     * @name setDatasets
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} datasets The datasets list
     * @description Set datasets in Inventory
     */
	function setDatasets(datasets) {
		inventoryState.datasets = datasets;
	}

    /**
     * @ngdoc method
     * @name removeDataset
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} dataset The dataset
     * @description Remove a dataset
     */
	function removeDataset(dataset) {
		inventoryState.datasets = reject(inventoryState.datasets, { id: dataset.id });
	}

    /**
     * @ngdoc method
     * @name setDatasetName
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {string} datasetId The dataset id
     * @param {string} name The dataset name
     * @description Change the dataset name in folder and datasets list
     */
	function setDatasetName(datasetId, name) {
		const dataset = find(inventoryState.datasets, { id: datasetId });
		dataset.name = name;
	}

    /**
     * @ngdoc method
     * @name setHomeFolderId
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {string} homeFolderId The home folder id
     */
	function setHomeFolderId(homeFolderId) {
		inventoryState.homeFolderId = homeFolderId;
	}

    /**
     * @ngdoc method
     * @name setFolder
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {string} metadata The folder metadata
     * @param {object} content The folder content
     */
	function setFolder(metadata, content) {
		inventoryState.folder = { metadata, content };
	}

    /**
     * @ngdoc method
     * @name setBreadcrumb
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} folders The folders in breadcrumb
     */
	function setBreadcrumb(folders) {
		inventoryState.breadcrumb = folders;
		inventoryState.breadcrumbChildren = {};
	}

    /**
     * @ngdoc method
     * @name setBreadcrumbChildren
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} parentId The parent folder id
     * @param {array} children The children folders
     */
	function setBreadcrumbChildren(parentId, children) {
		inventoryState.breadcrumbChildren[parentId] = children;
	}

    /**
     * @ngdoc method
     * @name setDatasetsSort
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} sort The sort type
     * @description Set the sort type
     */
	function setDatasetsSort(sort) {
		inventoryState.datasetsSort = sort;
	}

    /**
     * @ngdoc method
     * @name setDatasetsOrder
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} order The order
     * @description Set the order
     */
	function setDatasetsOrder(order) {
		inventoryState.datasetsOrder = order;
	}

	/**
	 * @ngdoc method
	 * @name setDatasetsSortFromIds
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} sortId The sort id
	 * @param {string} orderId The order id
	 * @description Set the order by its id
	 */
	function setDatasetsSortFromIds(sortId, orderId) {
		inventoryState.datasetsSort = find(sortList, { id: sortId });
		inventoryState.datasetsOrder = find(orderList, { id: orderId });
	}

	/**
	 * @ngdoc method
	 * @name setDatasetsDisplayMode
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} displayMode.mode The display mode
	 * @description Set the dataset display mode
	 */
	function setDatasetsDisplayMode(displayMode) {
		inventoryState.datasetsDisplayMode = displayMode.mode;
	}

    /**
     * @ngdoc method
     * @name setPreparationsSort
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} sort The sort type
     * @description Set rhe sort type
     */
	function setPreparationsSort(sort) {
		inventoryState.preparationsSort = sort;
	}

    /**
     * @ngdoc method
     * @name setPreparationsOrder
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} order The order
     * @description Set the order
     */
	function setPreparationsOrder(order) {
		inventoryState.preparationsOrder = order;
	}

	/**
	 * @ngdoc method
	 * @name setPreparationsOrderFromId
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} sortId The sort id
	 * @param {string} orderId The order id
	 * @description Set the order by its id
	 */
	function setPreparationsSortFromIds(sortId, orderId) {
		inventoryState.preparationsSort = find(sortList, { id: sortId });
		inventoryState.preparationsOrder = find(orderList, { id: orderId });
	}

    /**
     * @ngdoc method
     * @name setPreparationsDisplayMode
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {string} displayMode.mode The display mode
     * @description Set the preparation display mode
     */
	function setPreparationsDisplayMode(displayMode) {
		inventoryState.preparationsDisplayMode = displayMode.mode;
	}
}
