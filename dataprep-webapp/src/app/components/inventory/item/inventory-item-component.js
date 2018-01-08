/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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
 *      details="INVENTORY_DETAILS"
 *      item="dataset"
 *      type="dataset">
 * </inventory-item>
 *
 * @param {string}      details of the inventory item to be translated (author, lines number)
 * @param {object}      item the inventory item
 * @param {string}      type of the inventory item
 *
 */
const InventoryItemcomponent = {
	templateUrl: template,
	controller: 'InventoryItemCtrl',
	bindings: {
		details: '@',
		item: '=',
		type: '@',
	},
};

export default InventoryItemcomponent;
