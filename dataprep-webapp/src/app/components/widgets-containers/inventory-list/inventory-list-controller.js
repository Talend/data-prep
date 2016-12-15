/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

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

	getTitleActionDispatcher(viewKey, actionKey) {
		const listSettings = this.appSettings.views[viewKey].list;
		const action = this.appSettings.actions[listSettings.titleProps[actionKey]];
		return this.SettingsActionsService.createDispatcher(action);
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
			const settingAction = this.appSettings.actions[actionName];
			dispatcher = this.SettingsActionsService.createDispatcher(settingAction);
			this.actionsDispatchers[actionName] = dispatcher;
		}
		return dispatcher;
	}

	adaptItemActions(items) {
		return items.map((item, index) => {
			const actions = this.adaptActions(item.actions).map((action) => {
				// TODO remove that and do something more generic
				if (action.id === 'menu:playground:preparation') {
					const preparations = item.model.preparations.map((preparation) => {
						return {
							label: preparation.name,
							onClick: event => action.onClick(event, preparation),
						};
					});

					const dispatchDataset = this.getActionDispatcher('menu:playground:dataset');
					const items = [
						{
							icon: 'talend-plus',
							label: this.$translate.instant('CREATE_NEW_PREP'),
							onClick: event => dispatchDataset(event, item.model),
						},
					];
					return {
						...action,
						id: 'dropdown_' + item.model.id, // TODO change the id
						displayMode: 'dropdown',
						items: items.concat(preparations),
						onClick: null,
					};
				}

				return {
					...action,
					id: `${this.id}-${index}-${action.id}`,
					model: item,
					onClick: (event, payload) => action.onClick(event, payload.model),
				};
			});
			return {
				...item,
				actions,
			};
		});
	}

	adaptActions(actions) {
		return actions && actions.map((actionName) => {
			const settingAction = this.appSettings.actions[actionName];
			const onClick = this.getActionDispatcher(actionName);

			// TODO remove that and do something more generic
			if (actionName === 'menu:playground:preparation') {
				return {
					id: settingAction.id,
					displayMode: 'dropdown',
					label: settingAction.name,
					icon: settingAction.icon,
					onClick,
				};
			}

			return {
				id: settingAction.id,
				icon: settingAction.icon,
				label: settingAction.name,
				bsStyle: settingAction.bsStyle,
				onClick,
			};
		});
	}
}
