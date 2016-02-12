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
export default function InventoryItemCtrl() {
    var vm = this;

    /**
     * @ngdoc method
     * @name openRelatedInventoryItem
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @params relatedInventory the related inventory item
     * @description opens the inventory related to the current inventory item
     */
    vm.openRelatedInventoryItem = function openRelatedInventoryItem(relatedInventory) {
        if (vm.openRelatedInv) {
            vm.openRelatedInv(relatedInventory);
        }
    };

    /**
     * @ngdoc method
     * @name getTooltipContent
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @description creates the object used to construct the tooltip
     * @returns {Object} the object to construct the tooltip with
     */
    vm.getTooltipContent = function getTooltipContent() {
        return vm.relatedInventories && vm.relatedInventories.length ?
        {
            type: vm.relatedInventoriesType,
            name: vm.relatedInventories[0].name
        } :
        {
            type: vm.type,
            name: vm.item.name
        };
    };

    /**
     * @ngdoc method
     * @name openInventoryItem
     * @methodOf data-prep.inventory-item:InventoryItemCtrl
     * @description given an inventory Item, it opens it
     */
    vm.openInventoryItem = function openInventoryItem() {
        if (vm.relatedInventories && vm.relatedInventories.length) {
            vm.openRelatedInventoryItem(vm.relatedInventories[0]);
        }
        else {
            if (vm.open) {
                vm.open(vm.item);
            }
        }
    };
}