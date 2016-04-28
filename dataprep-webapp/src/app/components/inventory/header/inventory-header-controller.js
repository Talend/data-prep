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
 * @name data-prep.inventory-header.controller:InventoryHeaderCtrl
 * @description Inventory list header controller.
 */
export default class InventoryHeaderCtrl {
    constructor() {}

    /**
     * @ngdoc method
     * @name openFolderModal
     * @methodOf data-prep.inventory-header.controller:InventoryHeaderCtrl
     * @description Reset folder name and show folder modal
     */
    openFolderModal() {
        this.folderName = '';
        this.folderNameModal = true;
    }

    /**
     * @ngdoc method
     * @name addFolder
     * @methodOf data-prep.inventory-header.controller:InventoryHeaderCtrl
     * @description Create a new folder
     */
    addFolder() {
        this.folderNameForm.$commitViewValue();
        this.onFolderCreation({
                name: this.folderName
            })
            .then(() => {
                this.folderNameModal = false
            });
    }
}