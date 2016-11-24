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
		this.initState();
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
		if (changes.items) {
			this.listProps = {
				...this.listProps,
				items: this.adaptActions(changes.items.currentValue || []),
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

	initState() {
		const listViewSettings = this.appSettings.views['listview:preparations'];

		// list content props
		const listSettings = listViewSettings.list;
		const titleClickAction = this.appSettings.actions[listSettings.onTitleClick];
		this.listProps = {
			...listSettings,
			onTitleClick: this.SettingsActionsService.createDispatcher(titleClickAction),
		};

		// toolbar props
		const toolbarSettings = listViewSettings.toolbar;
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

	adaptActions(preparations) {
		const dispatchers = {};
		return preparations.map((prep) => {
			const actions = prep.actions.map((actionName) => {
				const settingAction = this.appSettings.actions[actionName];
				let dispatcher = dispatchers[actionName];
				if (!dispatcher) {
					dispatcher = this.SettingsActionsService.createDispatcher(settingAction);
					dispatchers[actionName] = dispatcher;
				}

				return {
					icon: settingAction.icon,
					label: settingAction.name,
					model: prep.model,
					onClick: dispatcher,
				};
			});
			return {
				...prep,
				actions,
			};
		});
	}
}
