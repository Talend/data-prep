/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import InventoryItemCtrl from './inventory-item-controller';
import InventoryItem from './inventory-item-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.data-prep.inventory-item
     * @description This module contains the controller and directives to manage an inventory item
     * @requires talend.widget
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.inventory-item',
        [
            'ngSanitize',
            'pascalprecht.translate',
            'talend.widget',
            'data-prep.services.utils'
        ])
        .controller('InventoryItemCtrl', InventoryItemCtrl)
        .directive('inventoryItem', InventoryItem);
})();