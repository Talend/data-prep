/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './inventory-item.html';

/**
 * @ngdoc component
 * @name data-prep.inventoryItem.component:InventoryItem
 * @description This component display an inventory item
 * @restrict E
 *
 * @usage
 * <inventory-item
 *      copy="datasetListCtrl.openFolderChoice"
 *      details="INVENTORY_DETAILS"
 *      file-model="datasetListCtrl.updateDatasetFile"
 *      item="dataset"
 *      open="datasetListCtrl.uploadWorkflowService.openDataset"
 *      open-related-inventory="datasetListCtrl.openPreparation"
 *      process-certification="datasetListCtrl.processCertification"
 *      remove="datasetListCtrl.remove"
 *      rename="datasetListCtrl.rename"
 *      rename="datasetListCtrl.isItemShared"
 *      related-inventories="dataset.preparations"
 *      related-inventories-type="preparation"
 *      toggle-favorite="datasetListCtrl.datasetService.toggleFavorite"
 *      type="dataset"
 *      update="datasetListCtrl.uploadUpdatedDatasetFile"
 *      copy-enabled="true"
 *      open-enabled="true"
 *      process-certification-enabled="true"
 *      remove-enabled="true"
 *      rename-enabled="true"
 *      toggle-favorite-enabled="true"
 *      process-certification-enabled="true">
 * </inventory-item>
 *
 * @param {function}    copy copy or remove an inventory item
 * @param {string}      details of the inventory item to be translated (author, lines number)
 * @param {array}       fileModel the file which will replace the current item
 * @param {object}      item the inventory item
 * @param {function}    open the playground
 * @param {function}    openRelatedInventory callback function to open the related inventory item
 * @param {function}    processCertification attributes certification to the inventory item
 * @param {function}    remove the inventory item
 * @param {function}    rename the inventory item
 * @param {boolean}     renameEnabled true if rename is enabled
 * @param {array}       relatedInventories related inventory items
 * @param {string}      relatedInventoriesType of the related inventory item
 * @param {function}    toggleFavorite the inventory item
 * @param {string}      type of the inventory item
 * @param {function}    update the inventory item with the given fileModel
 * @param {boolean}     processCertificationEnabled true if certify is enabled
 * @param {boolean}     removeEnabled true if remove is enabled
 * @param {boolean}     toggleFavoriteEnabled true if toogle is enabled
 * @param {boolean}     openEnabled true if open is enabled
 *
 */
const InventoryItemcomponent = {
    templateUrl: template,
    controller: 'InventoryItemCtrl',
    bindings: {
        copy: '=',
        details: '@',
        fileModel: '=',
        item: '=',
        open: '=',
        openRelatedInventory: '=',
        processCertification: '=',
        remove: '=',
        rename: '=',
        relatedInventories: '=',
        relatedInventoriesType: '@',
        toggleFavorite: '=',
        type: '@',
        update: '=',
        processCertificationEnabled: '<',
        removeEnabled: '<',
        toggleFavoriteEnabled: '<',
        renameEnabled: '<',
        openEnabled: '<',
    },
};

export default InventoryItemcomponent;
