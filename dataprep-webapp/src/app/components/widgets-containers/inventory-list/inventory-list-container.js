/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import InventoryListCtrl from './inventory-list-controller';
import './inventory-list-container.scss';

/**
 * Inventory list container
 * @restrict E
 */
const InventoryListContainer = {
	template: `
		<pure-list
			id="$ctrl.id"
			display-mode="$ctrl.displayMode"
			list="$ctrl.listProps"
			toolbar="$ctrl.toolbarProps"
		/>
	`,
	bindings: {
		id: '<',
		displayMode: '<',
		folders: '<',
		items: '<',
		sortBy: '<',
		sortDesc: '<',
		viewKey: '<',
		folderViewKey: '<',
	},
	controller: InventoryListCtrl,
};

export default InventoryListContainer;
