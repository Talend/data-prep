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
 * @name data-prep.dataset-header.controller:DatasetHeaderCtrl
 * @description Dataset list header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.folder.service:DatasetService
 */
export default class DatasetHeaderCtrl {
    constructor(state, StateService, StorageService, DatasetService) {
        'ngInject';
        this.state = state;
        this.StateService = StateService;
        this.StorageService = StorageService;
        this.DatasetService = DatasetService;
    }

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.dataset-header.controller:DatasetHeaderCtrl
     * @description sort dataset by sortType by calling refreshDatasets from DatasetService
     * @param {object} sortType Criteria to sort
     */
    updateSortBy(sortType) {
        const oldSort = this.state.inventory.datasetsSort;

        this.StateService.setDatasetsSort(sortType);
        this.StorageService.setDatasetsSort(sortType.id);

        return this.DatasetService.refreshDatasets()
            .catch(() => {
                this.StateService.setDatasetsSort(oldSort);
                this.StorageService.setDatasetsSort(oldSort.id);
            });
    }

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.dataset-header.controller:DatasetHeaderCtrl
     * @description sort dataset in order (ASC or DESC) by calling refreshDatasets from DatasetService
     * @param {object} order Sort order ASC(ascending) or DESC(descending)
     */
    updateSortOrder(order) {
        const oldOrder = this.state.inventory.datasetsOrder;

        this.StateService.setDatasetsOrder(order);
        this.StorageService.setDatasetsOrder(order.id);

        return this.DatasetService.refreshDatasets()
            .catch(() => {
                this.StateService.setDatasetsOrder(oldOrder);
                this.StorageService.setDatasetsOrder(oldOrder.id);
            });
    }
}