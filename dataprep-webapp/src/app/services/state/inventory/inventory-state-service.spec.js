/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Inventory state service', function(){
    'use strict';

    var preparations = [{id: 'toto'}, {id: 'titi'}];

    beforeEach(angular.mock.module('data-prep.services.state'));

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