/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const STATUS_DISPLAY_MODE = 'status';
const ACTION_DISPLAY_MODE = 'action';

export default class CollapsiblePanelCtrl {
	constructor($state, state, appSettings, SettingsActionsService) {
		'ngInject';
		this.$state = $state;
		this.state = state;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.actionsDispatchers = [];
	}

	$onChanges() {
		this.init();
	}
	init() {
		this.adaptHeader();
		this.content = this.item.content;
	}
	adaptHeader() {
		this.header = this.item
			.header
			.map((headerItem) => {
				if (headerItem.displayMode === STATUS_DISPLAY_MODE) {
					if (headerItem.actions && headerItem.actions.length) {
						const statusActions =  headerItem.actions.map(
							action => this.createItemAction(this.item.model, action)
						);
						return {
							...headerItem,
							actions: statusActions,
						};
					}
				}
				else if (headerItem.displayMode === ACTION_DISPLAY_MODE) {
					if (headerItem.action) {
						const action = this.createItemAction(this.item.model, headerItem.action);
						return {
							displayMode: ACTION_DISPLAY_MODE,
							...action,
						};
					}
				}
				return headerItem;
			});
	}

	createBaseAction(actionName) {
		const actionSettings = this.appSettings.actions[actionName];
		return {
			id: actionSettings.id,
			icon: actionSettings.icon,
			label: actionSettings.name,
			bsStyle: actionSettings.bsStyle,
			bsSize: actionSettings.bsSize,
			hideLabel: actionSettings.hideLabel,
		};
	}

	getActionDispatcher(actionName) {
		let dispatcher = this.actionsDispatchers[actionName];
		if (!dispatcher) {
			const actionSettings = this.appSettings.actions[actionName];
			dispatcher = this.SettingsActionsService.createDispatcher(actionSettings);
			this.actionsDispatchers[actionName] = dispatcher;
		}
		return dispatcher;
	}

	createItemAction(item, actionName) {
		const itemOnClick = this.getActionDispatcher(actionName);
		const itemAction = this.createBaseAction(actionName);
		itemAction.onClick = event => itemOnClick(event, item);
		return itemAction;
	}

}
