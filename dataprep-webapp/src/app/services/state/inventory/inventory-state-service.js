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
    {id: 'name', name: 'NAME_SORT', property: 'name'},
    {id: 'date', name: 'DATE_SORT', property: 'created'}
];

const orderList = [
    {id: 'asc', name: 'ASC_ORDER'},
    {id: 'desc', name: 'DESC_ORDER'}
];

export const inventoryState = {
    preparations: null,
    datasets: null,
    sortList: sortList,
    orderList: orderList,
    sort: sortList[1],
    order: orderList[1]
};


export function InventoryStateService() {
    return {
        setPreparations: setPreparations,
        removePreparation: removePreparation,
        setDatasets: setDatasets,
        removeDataset: removeDataset,
        setSort: setSort,
        setOrder: setOrder
    };

    function setPreparations(preparations) {
        inventoryState.preparations = preparations;
    }

    function removePreparation(preparation) {
        inventoryState.preparations = _.reject(inventoryState.preparations, {id: preparation.id});
    }

    function setDatasets(datasets) {
        inventoryState.datasets = datasets;
    }

    function removeDataset(dataset) {
        inventoryState.datasets = _.reject(inventoryState.datasets, {id: dataset.id});
    }

    function setSort(sort) {
        inventoryState.sort = sort;
    }

    function setOrder(order) {
        inventoryState.order = order;
    }

}