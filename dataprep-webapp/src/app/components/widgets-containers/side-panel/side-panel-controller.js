/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class SidePanelCtrl {
	constructor($state, state, appSettings, SettingsActionsService) {
		'ngInject';

		this.$state = $state;
		this.state = state;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.init();
	}

	$onChanges(changes) {
		if (changes.active) {
			this.actions = this.actions.map(action => ({
				...action,
				active: changes.active.currentValue === action.payload.args[0],
			}));
		}
	}

	init() {
		this.adaptActions();
		this.adaptToggle();
	}

	adaptActions() {
		this.actions = this.appSettings.views.sidepanel.actions
			.map(actionName => this.appSettings.actions[actionName])
			.map(action => ({
				...action,
				label: action.name,
				id: action.id.replace(/:/g, '-'),
				onClick: this.SettingsActionsService.createDispatcher(action),
			}));
	}

	adaptToggle() {
		const action = this.appSettings.actions[
			this.appSettings.views.sidepanel.onToggleDock
		];
		this.toggle = this.SettingsActionsService.createDispatcher(action);
	}
}
