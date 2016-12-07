/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationListCtrl {
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
			.views['listview:preparations']
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
		const toolbarSettings = this.appSettings.views['listview:preparations'].toolbar;
		const clickAddAction = this.appSettings.actions[toolbarSettings.onClickAdd];
		const displayModeAction = this.appSettings.actions[toolbarSettings.onSelectDisplayMode];
		const sortAction = this.appSettings.actions[toolbarSettings.onSelectSortBy];
		const dispatchDisplayMode = this.SettingsActionsService.createDispatcher(displayModeAction);

		this.toolbarProps = {
			...toolbarSettings,
			actions: toolbarSettings.actions
				.map(actionName => this.appSettings.actions[actionName])
				.map(action => this.SettingsActionsService.createDispatcher(action)),
			onClickAdd: this.SettingsActionsService.createDispatcher(clickAddAction),
			onSelectDisplayMode: (event, mode) => dispatchDisplayMode(event, { mode }),
			onSelectSortBy: this.SettingsActionsService.createDispatcher(sortAction),
		};
	}

	getTitleActionDispatcher(listViewKey, actionKey) {
		const listSettings = this.appSettings.views[listViewKey].list;
		const action = this.appSettings.actions[listSettings.titleProps[actionKey]];
		return this.SettingsActionsService.createDispatcher(action);
	}

	getItemTitleActionDispatcher(folderDispatcher, prepDispatcher) {
		return (event, payload) => {
			if (payload.type === 'folder' || payload.model.type === 'folder') {
				return folderDispatcher(event, payload);
			}
			return prepDispatcher(event, payload);
		};
	}

	getOnTitleDispatcher(action) {
		const folderDispatcher = this.getTitleActionDispatcher('listview:folders', action);
		const prepDispatcher = this.getTitleActionDispatcher('listview:preparations', action);

		return this.getItemTitleActionDispatcher(folderDispatcher, prepDispatcher);
	}

	initListProps() {
		const listSettings = this.appSettings.views['listview:preparations'].list;
		const onClick = this.getOnTitleDispatcher('onClick');
		const onEditCancel = this.getOnTitleDispatcher('onEditCancel');
		const onEditSubmit = this.getOnTitleDispatcher('onEditSubmit');
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
		return items.map((item) => {
			const actions = item.actions.map((actionName) => {
				const settingAction = this.appSettings.actions[actionName];
				return {
					icon: settingAction.icon,
					label: settingAction.name,
					model: item.model,
					onClick: this.getActionDispatcher(actionName),
				};
			});
			return {
				...item,
				actions,
			};
		});
	}
}
