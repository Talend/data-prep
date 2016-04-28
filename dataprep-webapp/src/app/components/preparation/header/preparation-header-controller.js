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
 * @name data-prep.preparation-header.controller:PreparationHeaderCtrl
 * @description Dataset list header controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.folder.service:DatasetService
 */
export default class PreparationHeaderCtrl {
    constructor(state, StateService, StorageService, FolderService) {
        'ngInject';
        this.state = state;
        this.StateService = StateService;
        this.StorageService = StorageService;
        this.FolderService = FolderService;
    }

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.preparation-header.controller:PreparationHeaderCtrl
     * @description sort dataset by sortType by calling refreshDatasets from DatasetService
     * @param {object} sortType Criteria to sort
     */
    updateSortBy(sortType) {
        const oldSort = this.state.inventory.preparationsSort;

        this.StateService.setPreparationsSort(sortType);
        this.StorageService.setPreparationsSort(sortType.id);

        return this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            .catch(() => {
                this.StateService.setPreparationsSort(oldSort);
                this.StorageService.setPreparationsSort(oldSort.id);
            });
    }

    /**
     * @ngdoc method
     * @name sort
     * @methodOf data-prep.preparation-header.controller:PreparationHeaderCtrl
     * @description sort dataset in order (ASC or DESC) by calling refreshDatasets from DatasetService
     * @param {object} order Sort order ASC(ascending) or DESC(descending)
     */
    updateSortOrder(order) {
        const oldOrder = this.state.inventory.preparationsOrder;

        this.StateService.setPreparationsOrder(order);
        this.StorageService.setPreparationsOrder(order.id);

        return this.FolderService.refreshContent(this.state.inventory.folder.metadata.path)
            .catch(() => {
                this.StateService.setPreparationsOrder(oldOrder);
                this.StorageService.setPreparationsOrder(oldOrder.id);
            });
    }

    /**
     * @ngdoc method
     * @name createFolder
     * @methodOf data-prep.preparation-header.controller:PreparationHeaderCtrl
     * @description Create a new folder
     * @param {string} folderName The new folder name
     */
    createFolder(folderName) {
        const currentFolderPath = this.state.inventory.folder.metadata.path;
        const pathToCreate = `${currentFolderPath}/${folderName}`;
        return this.FolderService.create(pathToCreate)
            .then(() => { this.FolderService.refreshContent(this.state.inventory.folder.metadata.path) });
    }
}