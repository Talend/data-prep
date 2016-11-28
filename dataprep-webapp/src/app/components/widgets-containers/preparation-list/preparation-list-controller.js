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
		this.initToolbarProps();
		this.initListProps();
	}

	$onInit() {
		this.didMountAction();
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
			const currentValue = changes.sortBy.currentValue;
			const sortBy = this.toolbarProps.sortBy.map((sort) => {
				const isSelected = sort.selected;
				const shouldBeSelected = sort.id === currentValue;
				if (isSelected === shouldBeSelected) {
					return sort;
				}
				return {
					...sort,
					selected: shouldBeSelected,
				};
			});
			this.toolbarProps = {
				...this.toolbarProps,
				sortBy,
			};
		}
		if (changes.sortDesc) {
			this.toolbarProps = {
				...this.toolbarProps,
				sortDesc: changes.sortDesc.currentValue,
			};
		}
	}

	didMountAction() {
		const didMountActionCreator = this.appSettings
			.views['listview:preparations']
			.didMountActionCreator;
		if (didMountActionCreator) {
			const action = this.appSettings.actions[didMountActionCreator];
			this.SettingsActionsService.dispatch(action);
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

	initListProps() {
		// list title click
		const listSettings = this.appSettings.views['listview:preparations'].list;
		const titleClickAction = this.appSettings.actions[listSettings.titleProps.onClick];
		const prepDispatcher = this.SettingsActionsService.createDispatcher(titleClickAction);

		// folder title click
		const folderListSettings = this.appSettings.views['listview:folders'].list;
		const folderClickAction = this.appSettings.actions[folderListSettings.titleProps.onClick];
		const folderDispatcher = this.SettingsActionsService.createDispatcher(folderClickAction);

		// list item title click
		const onClick = (event, entity) => {
			if (this.adapted.folders.indexOf(entity) > -1) {
				return folderDispatcher(event, entity);
			}
			return prepDispatcher(event, entity);
		};

		// lit props
		this.listProps = {
			...listSettings,
			titleProps: {
				...listSettings.titleProps,
				onClick,
			},
		};
	}

	adaptActions(items) {
		const dispatchers = {};
		return items.map((item) => {
			const actions = item.actions.map((actionName) => {
				const settingAction = this.appSettings.actions[actionName];
				let dispatcher = dispatchers[actionName];
				if (!dispatcher) {
					dispatcher = this.SettingsActionsService.createDispatcher(settingAction);
					dispatchers[actionName] = dispatcher;
				}

				return {
					icon: settingAction.icon,
					label: settingAction.name,
					model: item.model,
					onClick: dispatcher,
				};
			});
			return {
				...item,
				actions,
			};
		});
	}
}
