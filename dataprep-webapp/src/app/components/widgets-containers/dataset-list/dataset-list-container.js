/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetListCtrl from './dataset-list-controller';

/**
 * Dataset list container
 * @restrict E
 */
const DatasetListContainer = {
	template: `
		<pure-list
			display-mode="$ctrl.displayMode"
			list="$ctrl.listProps"
			toolbar="$ctrl.toolbarProps"
		/>
	`,
	bindings: {
		displayMode: '<',
		items: '<',
		sortBy: '<',
		sortDesc: '<',
	},
	controller: DatasetListCtrl,
};

export default DatasetListContainer;
