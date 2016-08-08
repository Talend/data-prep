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
 * @name data-prep.inventory-item.controller:InventoryItemCtrl
 * @description InventoryItemCtrl controller.
 */
export default class InventoryItemCtrl {

    /**
     * @ngdoc method
     * @name getItemHrefLink
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @description return the href link of the item
     */
    getItemHrefLink() {
        switch (this.type) {
        case 'dataset':
            return '#/playground/dataset?datasetid=' + this.item.id;
        case 'preparation':
            return '#/playground/preparation?prepid=' + this.item.id;
        case 'folder':
            return '#/index/preparations/' + this.item.id;
        }
    }

    /**
     * @ngdoc method
     * @name itemType
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @params item the inventory item
     * @description return the item type (MIME type) or the tag (if present).
     */
    itemType(item) {
        return item.tag ? item.tag : item.type;
    }

    /**
     * @ngdoc method
     * @name openRelatedInventoryItem
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @params {Object} relatedInventory the related inventory item
     * @description opens the inventory related to the current inventory item
     */
    openRelatedInventoryItem(relatedInventory) {
        if (this.openRelatedInventory) {
            this.openRelatedInventory(relatedInventory);
        }
    }

    /**
     * @ngdoc method
     * @name getTooltipContent
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @description creates the object used to construct the tooltip
     * @params {Object} isRelatedInventory if the related inventory item
     * @returns {Object} the object to construct the tooltip with
     */
    getTooltipContent(isRelatedInventory) {
        return isRelatedInventory && this.relatedInventories && this.relatedInventories.length ?
            {
                type: this.relatedInventoriesType,
                name: this.relatedInventories[0].name,
            } :
            {
                type: this.type,
                name: this.item.tooltipName ? this.item.tooltipName : this.item.name,
            };
    }
}
