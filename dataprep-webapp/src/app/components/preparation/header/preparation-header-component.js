/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationHeaderCtrl from './preparation-header-controller';

const PreparationHeaderComponent = {
	template: `
		<inventory-header
			id="preparation-inventory-header"
			sort="$ctrl.state.inventory.preparationsSort"
			order="$ctrl.state.inventory.preparationsOrder"
			sort-list="$ctrl.state.inventory.sortList"
			order-list="$ctrl.state.inventory.orderList"
			folder-list="$ctrl.state.inventory.folder.content.folders"
			on-sort-change="$ctrl.updateSortBy(sort)"
			on-order-change="$ctrl.updateSortOrder(order)"
			on-folder-creation="$ctrl.createFolder(name)"
			on-add-preparation="$ctrl.StateService.togglePreparationCreator()"></inventory-header>
	`,
	controller: PreparationHeaderCtrl,
};

export default PreparationHeaderComponent;
