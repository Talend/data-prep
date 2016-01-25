(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.inventory-item.controller:InventoryItemCtrl
     * @description InventoryItemCtrl controller.
     */
    function InventoryItemCtrl() {
        var vm = this;

        vm.openOnClick = function openOnClick(item) {
            if(vm.actionsEnabled) {
                vm.open(item);
            }
        };
    }
    angular.module('data-prep.inventory-item')
        .controller('InventoryItemCtrl', InventoryItemCtrl);
})();
