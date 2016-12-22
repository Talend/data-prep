/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const DROPDOWN_ACTION = 'dropdown';

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
				items: this.adaptItemActions(allItems),
			};
		}
		if (changes.sortBy) {
			this.toolbarProps = {
				...this.toolbarProps,
				sortBy: changes.sortBy.currentValue,
			};
		}
		if (changes.sortDesc) {
			this.toolbarProps = {
				...this.toolbarProps,
				sortDesc: changes.sortDesc.currentValue,
			};
		}
	}

	initToolbarProps() {
		const toolbarSettings = this.appSettings.views[this.viewKey].toolbar;

		const displayModeAction = this.appSettings.actions[toolbarSettings.onSelectDisplayMode];
		const sortByAction = this.appSettings.actions[toolbarSettings.onSelectSortBy];

		const onSelectSortBy = sortByAction && this.SettingsActionsService.createDispatcher(sortByAction);
		const dispatchDisplayMode = displayModeAction && this.SettingsActionsService.createDispatcher(displayModeAction);
		const onSelectDisplayMode = dispatchDisplayMode && ((event, mode) => dispatchDisplayMode(event, { mode }));

		const actions = toolbarSettings.actions &&
			{
				left: this.adaptActions(toolbarSettings.actions.left),
				right: this.adaptActions(toolbarSettings.actions.right),
			};

		this.toolbarProps = {
			...toolbarSettings,
			actions,
			onSelectDisplayMode,
			onSelectSortBy,
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
			itemAction.label = item.name;
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
					const staticActions = actionSettings.static.map(
						staticAction => this.createDropdownItemAction(hostModel, staticAction)
					);
					// dropdown dynamic action is the unique action on each item click
					// ex: dataset > "open preparation x" is applied to "preparation x"
					const dynamicActions = this.createDropdownActions(modelItems, actionSettings.dynamic);
					adaptedAction.items = staticActions.concat(dynamicActions);
				}
				else {
					const dispatch = this.getActionDispatcher(actionName);
					adaptedAction.model = hostModel;
					adaptedAction.onClick = (event, payload) => dispatch(event, payload && payload.model);
				}

				return adaptedAction;
			});
	}

	adaptItemActions(items) {
		return items.map((item, index) => {
			const actions = this.adaptActions(item.actions, item);
			actions.forEach((action) => {
				action.id = `${this.id}-${index}-${action.id}`;
			});
			return {
				...item,
				actions,
			};
		});
	}
}
