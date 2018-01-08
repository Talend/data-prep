/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const STATUS_DISPLAY_MODE = 'status';
const ACTION_DISPLAY_MODE = 'action';

export default class CollapsiblePanelCtrl {
	constructor(appSettings, SettingsActionsService) {
		'ngInject';
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.actionsDispatchers = [];
	}

	$onChanges(changes) {
		if (!changes.item) {
			return;
		}
		this.adaptItem();
	}

	adaptItem() {
		this.adaptedItem = { ...this.item };
		this.adaptHeaderActions();
		this.adaptHeader();
	}

	adaptHeaderActions() {
		if (this.adaptedItem.onToggle) {
			const headerOnToggle = this.getActionDispatcher(this.adaptedItem.onToggle);
			this.adaptedItem.onToggle = event => headerOnToggle(event, this.adaptedItem);
		}
		if (this.adaptedItem.onSelect) {
			const headerOnSelect = this.getActionDispatcher(this.adaptedItem.onSelect);
			this.adaptedItem.onSelect = event => headerOnSelect(event, this.adaptedItem);
		}
	}

	adaptHeader() {
		this.adaptedItem.header = this.adaptedItem
			.header
			.map((headerItem) => {
				if (Array.isArray(headerItem)) {
					return headerItem.map(item => this.adaptHeaderItem(item));
				}
				return this.adaptHeaderItem(headerItem);
			});
	}

	adaptHeaderItem(headerItem) {
		switch (headerItem.displayMode) {
		case STATUS_DISPLAY_MODE:
			return this.adaptStatusItem(headerItem);
		case ACTION_DISPLAY_MODE:
			return this.adaptActionItem(headerItem);
		default:
			return headerItem;
		}
	}

	adaptStatusItem(statusItem) {
		const actions = statusItem.actions;
		if (!actions || !actions.length) {
			return statusItem;
		}

		const statusActions = actions.map(
			action => this.createItemAction(this.adaptedItem, action)
		);
		return {
			...statusItem,
			actions: statusActions,
		};
	}

	adaptActionItem(actionItem) {
		const action = actionItem.action;
		if (!action) {
			return actionItem;
		}

		const adaptedAction = this.createItemAction(this.adaptedItem, action);
		adaptedAction.displayMode = ACTION_DISPLAY_MODE;
		return adaptedAction;
	}

	createItemAction(item, actionName) {
		const itemOnClick = this.getActionDispatcher(actionName);
		const itemAction = this.createBaseAction(actionName);
		itemAction.onClick = event => itemOnClick(event, item);
		return itemAction;
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

	createBaseAction(actionName) {
		const actionSettings = this.appSettings.actions[actionName];
		return {
			id: actionSettings.id,
			icon: actionSettings.icon,
			label: actionSettings.name,
			bsStyle: actionSettings.bsStyle,
			bsSize: actionSettings.bsSize,
			link: actionSettings.link,
			hideLabel: actionSettings.hideLabel,
		};
	}
}
