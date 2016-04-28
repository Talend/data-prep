/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import InventoryCopyMoveComponent from './inventory-copy-move-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.inventory-copy-move
     * @description This module contains the entities to manage the inventory copy/move wiazerd
     * @requires data-prep.folder-selection
     */
    angular.module('data-prep.inventory-copy-move',
        [
            'talend.widget',
            'data-prep.folder-selection',
        ])
        .component('inventoryCopyMove', InventoryCopyMoveComponent);
})();
