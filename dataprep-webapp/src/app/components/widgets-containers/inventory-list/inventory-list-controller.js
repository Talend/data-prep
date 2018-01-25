/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const NO_OP = () => {
};
const DROPDOWN_ACTION = 'dropdown';
const SPLITDROPDOWN_ACTION = 'splitDropdown';
const ACTION_TYPE = 'actions';
const LOADING_TIMEOUT_VALUE = 400;

export default class InventoryListCtrl {
	constructor($element, $translate, $timeout, appSettings, SettingsActionsService, state, StateService) {
		'ngInject';

		this.$element = $element;
		this.$translate = $translate;
		this.$timeout = $timeout;
		this.appSettings = appSettings;
		this.stateService = StateService;
		this.SettingsActionsService = SettingsActionsService;
		this.state = state;

		this.actionsDispatchers = [];
		this.initToolbarProps();
		this.initListProps();
	}

	$onInit() {
		const didMountActionCreator = this.appSettings
			.views[this.viewKey]
			.didMountActionCreator;
		if (didMountActionCreator) {
			const action = this.appSettings.actions[didMountActionCreator];
			this.SettingsActionsService.dispatch(action);
		}
		this._setLoading(true);
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			// block the native click action to avoid home redirection on empty href
			e.preventDefault();
		});
	}

	$onChanges(changes) {
		if (changes.folders || changes.items) {
			const allItems = (this.folders || []).concat(this.items || []);
			this.listProps = {
				...this.listProps,
				items: this.adaptItemsActions(allItems),
			};
		}
		if (changes.sortBy || changes.sortDesc) {
			const field = this.sortBy;
			const isDescending = this.sortDesc;
			this.toolbarProps = this.changeSort(this.toolbarProps, field, isDescending);
			this.listProps = this.changeSort(this.listProps, field, isDescending);
		}
		if (changes.isLoading) {
			if (this.isLoading) {
				this.loadingTimeout = this.$timeout(() => {
					this._setLoading(true);
				}, LOADING_TIMEOUT_VALUE);
			}
			else {
				this.$timeout.cancel(this.loadingTimeout);
				this._setLoading(false);
			}
		}
	}

	_setLoading(bool) {
		this.listProps = {
			...this.listProps,
			inProgress: bool,
		};
	}

	changeSort(subProps, field, isDescending) {
		if (!subProps.sort) {
			return subProps;
		}

		return {
			...subProps,
			sort: {
				...subProps.sort,
				field,
				isDescending,
			},
		};
	}

	initToolbarProps() {
		const toolbarSettings = this.appSettings.views[this.viewKey].toolbar;

		this.toolbarProps = {
			...toolbarSettings,
			actionBar: this.getActionBarProps(toolbarSettings.actionBar),
			display: this.getDisplayModeProps(toolbarSettings.display),
			sort: this.getSortProps(toolbarSettings.sort),
		};
	}

	initListProps() {
		const listSettings = this.appSettings.views[this.viewKey].list;

		this.listProps = {
			...listSettings,
			titleProps: this.getListTitleProps(listSettings.titleProps),
			sort: this.getSortProps(listSettings.sort),
			inProgress: this.isLoading,
		};
	}

	getActionBarProps(actionBarSettings) {
		return actionBarSettings &&
			actionBarSettings.actions &&
			{
				...actionBarSettings,
				actions: {
					left: this.adaptActions(actionBarSettings.actions.left),
					right: this.adaptActions(actionBarSettings.actions.right),
				},
			};
	}

	getDisplayModeProps(displayModeSettings) {
		if (!displayModeSettings) {
			return null;
		}

		const displayModeAction = displayModeSettings &&
			displayModeSettings.onChange &&
			this.appSettings.actions[displayModeSettings.onChange];
		const dispatchDisplayMode = displayModeAction && this.SettingsActionsService.createDispatcher(displayModeAction);
		const onDisplayModeChange = dispatchDisplayMode ? ((event, mode) => dispatchDisplayMode(event, { mode })) : NO_OP;

		return {
			...displayModeSettings,
			onChange: onDisplayModeChange,
		};
	}

	getSortProps(sortSettings) {
		if (!sortSettings) {
			return null;
		}

		const sortByAction = sortSettings &&
			sortSettings.onChange &&
			this.appSettings.actions[sortSettings.onChange];
		const onSortByChange = sortByAction ?
			this.SettingsActionsService.createDispatcher(sortByAction) :
			NO_OP;
		return {
			...sortSettings,
			onChange: onSortByChange,
		};
	}

	getListTitleProps(titleSettings) {
		const onItemClick = this.getTitleActionDispatcher(this.viewKey, 'onClick');
		let onClick = onItemClick;
		if (this.folderViewKey) {
			const onFolderClick = this.getTitleActionDispatcher(this.folderViewKey, 'onClick');
			onClick = (event, payload) => {
				return payload.type === 'folder' ?
					onFolderClick(event, payload) :
					onItemClick(event, payload);
			};
		}

		return {
			...titleSettings,
			onClick,
			onEditCancel: this.getTitleActionDispatcher(this.viewKey, 'onEditCancel'),
			onEditSubmit: this.getTitleActionDispatcher(this.viewKey, 'onEditSubmit'),
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

	getTitleActionDispatcher(viewKey, actionKey) {
		const listSettings = this.appSettings.views[viewKey].list;
		const action = this.appSettings.actions[listSettings.titleProps[actionKey]];
		return this.SettingsActionsService.createDispatcher(action);
	}

	createBaseAction(actionName, isDropdownItem) {
		const actionSettings = this.appSettings.actions[actionName];
		const baseAction = {
			id: actionSettings.id,
			label: actionSettings.name,
			bsStyle: actionSettings.bsStyle,
			tooltipLabel: actionSettings.toolTip || actionSettings.name,
		};
		if (!isDropdownItem) {
			baseAction.icon = actionSettings.icon;
		}
		if (actionSettings.displayMode) {
			baseAction.displayMode = actionSettings.displayMode;
		}
		return baseAction;
	}

	createDropdownItemAction(item, actionName) {
		const itemOnClick = this.getActionDispatcher(actionName);
		const itemAction = this.createBaseAction(actionName, true);
		itemAction.onClick = event => itemOnClick(event, item);
		return itemAction;
	}

	createDropdownActions(items, actionName) {
		return items.map((item) => {
			const itemAction = this.createDropdownItemAction(item, actionName);
			itemAction.label = item.label || item.name;
			return itemAction;
		});
	}

	adaptActions(actions, hostModel) {
		return actions &&
			actions.map((actionName) => {
				const adaptedAction = this.createBaseAction(actionName);

				if (adaptedAction.displayMode === DROPDOWN_ACTION) {
					const actionSettings = this.appSettings.actions[actionName];
					// conf.items is the key where the dropdown items are stored
					// ex: dataset > preparations is hosted in dataset, with "preparations" key
					const modelItems = hostModel[actionSettings.items];
					// dropdown static actions are applied to the host model
					// ex: dataset > "create new preparation action" is applied to the dataset
					const staticActions = actionSettings.staticActions.map(
						staticAction => this.createDropdownItemAction(hostModel, staticAction)
					);
					// dropdown dynamic action is the unique action on each item click
					// ex: dataset > "open preparation x" is applied to "preparation x"
					const dynamicActions = this.createDropdownActions(modelItems, actionSettings.dynamicAction);
					adaptedAction.items = staticActions.concat(dynamicActions);
				}
				else if (adaptedAction.displayMode === SPLITDROPDOWN_ACTION) {
					const dispatch = this.getActionDispatcher(actionName);
					const splitDropdownAction = this.appSettings.actions[actionName];
					adaptedAction.items = this.createDropdownActions(splitDropdownAction.items, actionName);
					adaptedAction.onClick = event => dispatch(event, { items: splitDropdownAction.items });
					return adaptedAction;
				}
				else {
					const dispatch = this.getActionDispatcher(actionName);
					adaptedAction.model = hostModel;
					adaptedAction.onClick = (event, payload) => dispatch(event, payload && payload.model);
				}

				return adaptedAction;
			});
	}

	adaptItemActions(item, actions, index) {
		const adaptedActions = this.adaptActions(actions, item);
		if (adaptedActions) {
			adaptedActions.forEach((action) => {
				action.id = `${this.id}-${index}-${action.id}`;
			});
		}
		return adaptedActions;
	}

	adaptItemsActions(items) {
		const actionsColumns = this.listProps.columns.filter(column => column.type === ACTION_TYPE);
		const persistentActionsKey = this.listProps.titleProps.persistentActionsKey;
		return items.map((item, index) => {
			const adaptedItem = {
				...item,
				actions: this.adaptItemActions(item, item.actions, index),
			};

			if (persistentActionsKey) {
				adaptedItem[persistentActionsKey] = this.adaptItemActions(item, item[persistentActionsKey], index);
			}

			actionsColumns.forEach(({ key }) => {
				adaptedItem[key] = this.adaptItemActions(item, item[key], index);
			});

			return adaptedItem;
		});
	}
}
