/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class SidePanelCtrl {
	constructor(state, appSettings, SettingsActionsService) {
		'ngInject';

		this.state = state;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.init();
	}

	init() {
		this.adaptActions();
		this.adaptToggle();
	}

	adaptActions() {
		this.actions = this.appSettings
			.views
			.sidepanel
			.actions
			.map(action => ({
				label: action.label,
				icon: action.icon,
				onClick: this.SettingsActionsService.createDispatcher(this.appSettings.actions[action.onClick]),
			}));
	}

	adaptToggle() {
		const action = this.appSettings.actions[this.appSettings.views.sidepanel.onToggleDock];
		this.toggle = this.SettingsActionsService.createDispatcher(action);
		this.toggleIcon = action.icon;
	}
}
