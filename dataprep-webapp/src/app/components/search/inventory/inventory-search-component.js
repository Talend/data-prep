/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


/**
 * @ngdoc directive
 * @name data-prep.data-prep.inventory-search
 * @description This directive display an inventory search
 * @restrict E
 * @usage <inventory-search></inventory-search>
 */

import InventorySearchCtrl from './inventory-search-controller';

const InventorySearch = {
    templateUrl: 'app/components/search/inventory/inventory-search.html',
    controller: InventorySearchCtrl
};

export default InventorySearch;