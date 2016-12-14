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

		this.adapted = {
			folders: [],
			items: [],
		};
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
			if (changes.folders) {
				this.adapted.folders = this.adaptActions(changes.folders.currentValue || []);
			}
			if (changes.items) {
				this.adapted.items = this.adaptActions(changes.items.currentValue || []);
			}
			this.listProps = {
				...this.listProps,
				items: this.adapted.folders.concat(this.adapted.items),
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

		const clickAddAction = this.appSettings.actions[toolbarSettings.onClickAdd];
		const displayModeAction = this.appSettings.actions[toolbarSettings.onSelectDisplayMode];
		const sortByAction = this.appSettings.actions[toolbarSettings.onSelectSortBy];

		const onClickAdd = clickAddAction && this.SettingsActionsService.createDispatcher(clickAddAction);
		const onSelectSortBy = sortByAction && this.SettingsActionsService.createDispatcher(sortByAction);
		const dispatchDisplayMode = displayModeAction && this.SettingsActionsService.createDispatcher(displayModeAction);
		const onSelectDisplayMode = dispatchDisplayMode && ((event, mode) => dispatchDisplayMode(event, { mode }));

		const actions = toolbarSettings.actions
			.map(actionName => this.appSettings.actions[actionName])
			.map(action => this.SettingsActionsService.createDispatcher(action));

		this.toolbarProps = {
			...toolbarSettings,
			actions,
			onClickAdd,
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

		let onClick;
		if (this.folderViewKey) {
			const onFolderClick = this.getTitleActionDispatcher(this.folderViewKey, 'onClick');
			onClick = (event, payload) => {
				return payload.type === 'folder' ?
					onFolderClick(event, payload) :
					onItemClick(event, payload);
			};
		}
		else {
			onClick = onItemClick;
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

	adaptActions(items) {
		return items.map((item, index) => {
			const actions = item.actions.map((actionName) => {
				const settingAction = this.appSettings.actions[actionName];
				const dispatch = this.getActionDispatcher(actionName);
				return {
					id: `${this.id}-${index}-${settingAction.id}`,
					icon: settingAction.icon,
					label: settingAction.name,
					model: item,
					onClick: (event, payload) => dispatch(event, payload.model),
				};
			});
			return {
				...item,
				actions,
			};
		});
	}
}
