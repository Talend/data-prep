/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import CollapsiblePanelCtrl from './collapsible-panel-controller';

const CollapsiblePanelContainer = {
	template: `
		<pure-collapsible-panel
			header="$ctrl.adaptedItem.header"
			content="$ctrl.adaptedItem.content"
			on-select="$ctrl.adaptedItem.onSelect"
			on-toggle="$ctrl.adaptedItem.onToggle"
			selected="$ctrl.adaptedItem.selected"
			expanded="$ctrl.adaptedItem.expanded"
			theme="$ctrl.adaptedItem.theme"
		>
		</pure-collapsible-panel>
	`,
	bindings: {
		item: '<',
	},
	controller: CollapsiblePanelCtrl,
};
export default CollapsiblePanelContainer;
