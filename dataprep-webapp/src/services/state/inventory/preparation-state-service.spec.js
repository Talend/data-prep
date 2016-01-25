describe('Inventory state service', function(){
    'use strict';

    var preparations = [{id: 'toto'}, {id: 'titi'}];

    beforeEach(module('data-prep.services.state'));

    it('should update preparations list', inject(function (inventoryState, InventoryStateService) {
        //given
        inventoryState.preparations = null;

        //when
        InventoryStateService.setPreparations(preparations);

        //then
        expect(inventoryState.preparations.length).toBe(2);
    }));

    it('should remove a preparation from preparations list', inject(function (inventoryState, InventoryStateService) {
        //given
        inventoryState.preparations = null;
        InventoryStateService.setPreparations(preparations);

        //when
        InventoryStateService.removePreparation(preparations[0]);

        //then
        expect(inventoryState.preparations.length).toBe(1);
        expect(inventoryState.preparations[0].id).toBe('titi');
    }));
});