/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

'use strict';

const sortList = [
    { id: 'name', name: 'NAME_SORT', property: 'name' },
    { id: 'date', name: 'DATE_SORT', property: 'created' }
];

const orderList = [
    { id: 'asc', name: 'ASC_ORDER' },
    { id: 'desc', name: 'DESC_ORDER' }
];

const ROOT_FOLDER = {
    path: '',
    name: 'Home'
};

export const inventoryState = {
    preparations: null,
    datasets: null,

    sortList: sortList,
    orderList: orderList,
    datasetsSort: sortList[1],
    datasetsOrder: orderList[1],
    preparationsSort: sortList[1],
    preparationsOrder: orderList[1],

    folder: {
        metadata: ROOT_FOLDER,
        content: {
            folders: [],
            preparations: [],
        },
    },
    folderStack: [],
    menuChildren: [],
};


export function InventoryStateService() {
    return {
        setPreparations: setPreparations,
        removePreparation: removePreparation,

        setDatasets: setDatasets,
        removeDataset: removeDataset,
        setDatasetName: setDatasetName,

        setFolder: setFolder,
        setMenuChildren: setMenuChildren,

        setDatasetsSort: setDatasetsSort,
        setDatasetsOrder: setDatasetsOrder,

        setPreparationsSort: setPreparationsSort,
        setPreparationsOrder: setPreparationsOrder,
    };

    /**
     * @ngdoc method
     * @name _consolidateDatasets
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} datasets The datasets list to consolidate
     * @description Set the preparations list in each dataset to consolidate
     */
    function _consolidateDatasets(datasets) {
        if (!datasets || !inventoryState.preparations) {
            return;
        }

        const preparationsByDataset = _.groupBy(inventoryState.preparations, 'dataSetId');
        _.forEach(datasets, (dataset) => {
            const preparations = preparationsByDataset[dataset.id] || [];
            dataset.preparations = _.sortByOrder(preparations, 'lastModificationDate', false);
        });
    }

    /**
     * @ngdoc method
     * @name _consolidatePreparations
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} preparations The preparations list to consolidate
     * @description Set the dataset in each preparation to consolidate
     */
    function _consolidatePreparations(preparations) {
        if (!preparations || !inventoryState.datasets) {
            return;
        }

        _.forEach(preparations, (prep) => {
            prep.dataset = _.find(inventoryState.datasets, { id: prep.dataSetId });
        });
    }

    /**
     * @ngdoc method
     * @name consolidate
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @description Consolidate preparations, datasets and the current folder datasets
     */
    function consolidate() {
        _consolidatePreparations(inventoryState.preparations);
        _consolidateDatasets(inventoryState.datasets);
    }

    /**
     * @ngdoc method
     * @name setPreparations
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} preparations The preparations list
     * @description Set preparations in Inventory
     */
    function setPreparations(preparations) {
        inventoryState.preparations = preparations;
        consolidate();
    }

    /**
     * @ngdoc method
     * @name removePreparation
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} preparation The preparation
     * @description Remove a preparation
     */
    function removePreparation(preparation) {
        inventoryState.preparations = _.reject(inventoryState.preparations, { id: preparation.id });
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
        consolidate();
    }

    /**
     * @ngdoc method
     * @name removeDataset
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} dataset The dataset
     * @description Remove a dataset
     */
    function removeDataset(dataset) {
        inventoryState.datasets = _.reject(inventoryState.datasets, { id: dataset.id });
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
        const dataset = _.find(inventoryState.datasets, { id: datasetId });
        dataset.name = name;
    }

    /**
     * @ngdoc method
     * @name setFolder
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {string} path The folder path
     * @param {object} content The folder content
     */
    function setFolder(path, content) {
        const pathParts = path
            .split('/')
            .filter((name) => name);
        const metadata = pathParts.length ?
            { name: pathParts[pathParts.length - 1], path: path } :
            ROOT_FOLDER;

        inventoryState.folder = { metadata, content };
        inventoryState.foldersStack = pathParts.reduce((accu, name) => {
            const path = `${accu[accu.length - 1].path}/${name}`;
            return accu.concat([{ path, name }]);
        }, [ROOT_FOLDER]);
    }

    /**
     * @ngdoc method
     * @name setMenuChildren
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {array} children The current children of the current menu entry
     */
    function setMenuChildren(children) {
        inventoryState.menuChildren = children;
    }

    /**
     * @ngdoc method
     * @name setDatasetsSort
     * @methodOf data-prep.services.state.service:InventoryStateService
     * @param {object} sort The sort type
     * @description Set rhe sort type
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
}