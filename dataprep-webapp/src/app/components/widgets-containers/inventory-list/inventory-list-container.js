/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './inventory-list.html';
import InventoryListCtrl from './inventory-list-controller';

/**
 * Inventory list container
 * @restrict E
 */
const InventoryListContainer = {
	templateUrl: template,
	bindings: {
		id: '<',
		displayMode: '<',
		folders: '<',
		isLoading: '<',
		items: '<',
		sortBy: '<',
		sortDesc: '<',
		viewKey: '<',
		folderViewKey: '<',
	},
	controller: InventoryListCtrl,
};

export default InventoryListContainer;
