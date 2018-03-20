/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

export const HOME_FOLDER = {
	id: 'Lw==',
	path: '/',
};

const FALLBACK_DISPLAY_MODE = 'table';

export const inventoryState = {
	datasetToUpdate: null,

	sortList,
	orderList,

	datasetsDisplayMode: FALLBACK_DISPLAY_MODE,
	preparationsDisplayMode: FALLBACK_DISPLAY_MODE,

	datasets: {
		sort: {
			field: 'name',
			isDescending: false,
		},
		content: null,
	},

	homeFolder: HOME_FOLDER,
	folder: {
		sort: {
			field: 'name',
			isDescending: false,
		},
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

export function InventoryStateService($translate, StorageService) {
	'ngInject';

	return {
		init,

		enableEdit,
		disableEdit,

		setDatasets,
		removeDataset,
		setDatasetName,
		setDatasetToUpdate,

		setHomeFolderId,
		setFolder,
		setBreadcrumb,
		setBreadcrumbChildren,

		setDatasetsSort,
		setDatasetsDisplayMode,

		setPreparationsSort,
		setPreparationsDisplayMode,

		setFetchingDatasets,
		setFetchingPreparations,
	};

	function init() {
		const { datasets, preparations } = StorageService.getListsDisplayModes();
		inventoryState.datasetsDisplayMode = datasets || FALLBACK_DISPLAY_MODE;
		inventoryState.preparationsDisplayMode = preparations || FALLBACK_DISPLAY_MODE;
	}

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
		case 'dataset': {
			const datasetList = inventoryState.datasets.content;
			inventoryState.datasets.content = fn(datasetList);
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
	 * @name setDatasetToUpdate
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {dataset} dataset dataset to update
	 * @description Set datasetToUpdate in Inventory
	 */
	function setDatasetToUpdate(dataset) {
		inventoryState.datasetToUpdate = dataset;
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
		inventoryState.datasets.content = datasets;
	}

	/**
	 * @ngdoc method
	 * @name removeDataset
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {object} dataset The dataset
	 * @description Remove a dataset
	 */
	function removeDataset(dataset) {
		inventoryState.datasets.content = reject(inventoryState.datasets.content, { id: dataset.id });
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
		const dataset = find(inventoryState.datasets.content, { id: datasetId });
		dataset.name = name;
	}

	/**
	 * @ngdoc method
	 * @name setHomeFolderId
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} homeFolderId The home folder id
	 */
	function setHomeFolderId(homeFolderId) {
		const currentIsHome = inventoryState.folder.metadata.id === inventoryState.homeFolder.id;
		inventoryState.homeFolder.id = homeFolderId;
		if (currentIsHome) {
			inventoryState.folder.metadata.id = homeFolderId;
		}
	}

	/**
	 * @ngdoc method
	 * @name setFolder
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} metadata The folder metadata
	 * @param {object} content The folder content
	 */
	function setFolder(metadata, content) {
		inventoryState.folder.metadata = metadata;
		inventoryState.folder.content = content;
	}

	/**
	 * @ngdoc method
	 * @name setBreadcrumb
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {array} folders The folders in breadcrumb
	 */
	function setBreadcrumb(folders) {
		inventoryState.breadcrumb = folders.map((folder) => {
			const translatedFolder = folder;
			if (folder.path === HOME_FOLDER.path) {
				translatedFolder.name = $translate.instant('HOME_FOLDER');
			}
			return translatedFolder;
		});
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
	 * @param {string} field The sort field
	 * @param {boolean} isDescending True if the sort is descending
	 * @description Set the sort type
	 */
	function setDatasetsSort(field, isDescending) {
		inventoryState.datasets.sort = { field, isDescending };
	}

	/**
	 * @ngdoc method
	 * @name setDatasetsDisplayMode
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} displayMode.mode The display mode
	 * @description Set the dataset display mode
	 */
	function setDatasetsDisplayMode({ mode }) {
		if (mode !== inventoryState.datasetsDisplayMode) {
			inventoryState.datasetsDisplayMode = mode;
			StorageService.setListsDisplayModes({
				preparations: inventoryState.preparationsDisplayMode,
				datasets: inventoryState.datasetsDisplayMode,
			});
		}
	}

	/**
	 * @ngdoc method
	 * @name setPreparationSort
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} field The sort field
	 * @param {boolean} isDescending True if the sort is in descending order
	 * @description Set the sort
	 */
	function setPreparationsSort(field, isDescending) {
		inventoryState.folder.sort = {
			field,
			isDescending,
		};
	}

	/**
	 * @ngdoc method
	 * @name setPreparationsDisplayMode
	 * @methodOf data-prep.services.state.service:InventoryStateService
	 * @param {string} displayMode.mode The display mode
	 * @description Set the preparation display mode
	 */
	function setPreparationsDisplayMode({ mode }) {
		if (mode !== inventoryState.preparationsDisplayMode) {
			inventoryState.preparationsDisplayMode = mode;
			StorageService.setListsDisplayModes({
				preparations: inventoryState.preparationsDisplayMode,
				datasets: inventoryState.datasetsDisplayMode,
			});
		}
	}
}
