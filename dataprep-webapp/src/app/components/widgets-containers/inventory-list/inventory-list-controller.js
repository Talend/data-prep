/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const NO_OP = () => {};
const DROPDOWN_ACTION = 'dropdown';
const SPLITDROPDOWN_ACTION = 'splitDropdown';

export default class InventoryListCtrl {
	constructor($element, $translate, appSettings, SettingsActionsService) {
		'ngInject';

		this.$element = $element;
		this.$translate = $translate;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;

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
		if (changes.sortBy) {
			this.toolbarProps = {
				...this.toolbarProps,
				sort: {
					...this.toolbarProps.sort,
					field: changes.sortBy.currentValue,
				},
			};
		}
		if (changes.sortDesc) {
			this.toolbarProps = {
				...this.toolbarProps,
				sort: {
					...this.toolbarProps.sort,
					isDescending: changes.sortDesc.currentValue,
				},
			};
		}
	}

	initToolbarProps() {
		const toolbarSettings = this.appSettings.views[this.viewKey].toolbar;

		// display mode action
		const displayModeAction = toolbarSettings.display &&
			toolbarSettings.display.onChange &&
			this.appSettings.actions[toolbarSettings.display.onChange];
		const dispatchDisplayMode = displayModeAction && this.SettingsActionsService.createDispatcher(displayModeAction);
		const onDisplayModeChange = dispatchDisplayMode ? ((event, mode) => dispatchDisplayMode(event, { mode })) : NO_OP;

		// sort by action
		const sortByAction = toolbarSettings.sort &&
			toolbarSettings.sort.onChange &&
			this.appSettings.actions[toolbarSettings.sort.onChange];
		const onSortByChange = sortByAction ? this.SettingsActionsService.createDispatcher(sortByAction) : NO_OP;

		// toolbar actions
		const actions = toolbarSettings.actionBar && toolbarSettings.actionBar.actions &&
			{
				left: this.adaptActions(toolbarSettings.actionBar.actions.left),
				right: this.adaptActions(toolbarSettings.actionBar.actions.right),
			};

		this.toolbarProps = {
			...toolbarSettings,
			actionBar: toolbarSettings.actionBar && {
				...toolbarSettings.actionBar,
				actions,
			},
			display: toolbarSettings.display && {
				...toolbarSettings.display,
				onChange: onDisplayModeChange,
			},
			sort: toolbarSettings.sort && {
				...toolbarSettings.sort,
				onChange: onSortByChange,
			},
		};
	}

	initListProps() {
		const listSettings = this.appSettings.views[this.viewKey].list;
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

		const onEditCancel = this.getTitleActionDispatcher(this.viewKey, 'onEditCancel');
		const onEditSubmit = this.getTitleActionDispatcher(this.viewKey, 'onEditSubmit');
		this.listProps = {
			...listSettings,
			titleProps: {
				...listSettings.titleProps,
				onClick,
				onEditCancel,
				onEditSubmit,
			},
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

	createBaseAction(actionName) {
		const actionSettings = this.appSettings.actions[actionName];
		const baseAction = {
			id: actionSettings.id,
			icon: actionSettings.icon,
			label: actionSettings.name,
			bsStyle: actionSettings.bsStyle,
		};
		if (actionSettings.displayMode) {
			baseAction.displayMode = actionSettings.displayMode;
		}
		return baseAction;
	}

	createDropdownItemAction(item, actionName) {
		const itemOnClick = this.getActionDispatcher(actionName);
		const itemAction = this.createBaseAction(actionName);
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
		adaptedActions.forEach((action) => {
			action.id = `${this.id}-${index}-${action.id}`;
		});
		return adaptedActions;
	}

	adaptItemsActions(items) {
		const actionsColumns = this.listProps.columns.filter(column => column.type === 'actions');
		return items.map((item, index) => {
			const actions = this.adaptItemActions(item, item.actions, index);
			const adaptedItem = {
				...item,
				actions,
			};
			actionsColumns.forEach(({ key }) => {
				adaptedItem[key] = this.adaptItemActions(item, item[key], index);
			});
			return adaptedItem;
		});
	}
}
