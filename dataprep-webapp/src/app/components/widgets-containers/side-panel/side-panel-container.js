 /*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import SidePanelCtrl from './side-panel-controller';

const SidePanelContainer = {
	template: `<pure-side-panel
			id="$ctrl.id"
			ng-if="$ctrl.appSettings.views.sidepanel.actions.length"
		 	actions="$ctrl.actions"
		 	toggle-icon="$ctrl.toggleIcon"
			on-toggle-dock="$ctrl.toggle"
			docked="$ctrl.state.home.sidePanelDocked"
			watch-depth="reference"
		/>`,
	bindings: {
		id: '<',
		active: '<',
	},
	controller: SidePanelCtrl,
};
export default SidePanelContainer;
