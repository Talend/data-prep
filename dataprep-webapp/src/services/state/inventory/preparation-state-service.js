(function () {
    'use strict';

    var inventoryState = {
        preparations: null
    };

    function InventoryStateService() {
        return {
            setPreparations: setPreparations,
            removePreparation: removePreparation
        };

        function setPreparations(preparations) {
            inventoryState.preparations = preparations;
        }

        function removePreparation(preparation) {
            inventoryState.preparations = _.reject(inventoryState.preparations, {id: preparation.id});
        }
    }

    angular.module('data-prep.services.state')
        .service('InventoryStateService', InventoryStateService)
        .constant('inventoryState', inventoryState);
})();