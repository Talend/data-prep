/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './inventory-header.html';

export default function InventoryHeaderDirective() {
	return {
		templateUrl: template,
		scope: true,
		bindToController: {
			sort: '<',
			order: '<',
			folderList: '<',
			sortList: '<',
			orderList: '<',
			onSortChange: '&',
			onOrderChange: '&',
			onFolderCreation: '&',
			onAddPreparation: '&',
		},
		controllerAs: '$ctrl',
		controller: 'InventoryHeaderCtrl',
		link(scope, iElement, iAttrs, ctrl) {
			ctrl.canCreateFolder = iAttrs.onFolderCreation;
		},
	};
}
