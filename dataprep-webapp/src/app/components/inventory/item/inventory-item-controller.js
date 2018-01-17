/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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
     * @name itemType
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @params item the inventory item
     * @description return the item type (MIME type) or the tag (if present).
     */
	itemType(item) {
		return item.tag ? item.tag : item.type;
	}
}
