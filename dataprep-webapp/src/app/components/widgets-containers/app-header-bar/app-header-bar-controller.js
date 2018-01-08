/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
export default class AppHeaderBarCtrl {
	constructor($element, $translate, state, appSettings, SettingsActionsService) {
		'ngInject';

		this.$element = $element;
		this.$translate = $translate;
		this.appSettings = appSettings;
		this.settingsActionsService = SettingsActionsService;
		this.state = state;
	}

	$onInit() {
		this.viewKey = this.viewKey || 'appheaderbar';
		this.initLogo();
		this.initBrand();
		this.initHelp();
		this.initSearch();
		this.initUserMenu();
		this.initProducts();
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			// block the native click action to avoid home redirection on empty href
			e.preventDefault();
		});
	}

	$onChanges(changes) {
		if (this.search) {
			const searchConfiguration = { ...this.search };
			if (changes.searching) {
				const searching = changes.searching.currentValue;
				searchConfiguration.searching = searching;
			}
			if (changes.searchFocusedSectionIndex) {
				const focusedSectionIndex = changes.searchFocusedSectionIndex.currentValue;
				searchConfiguration.focusedSectionIndex = focusedSectionIndex;
			}
			if (changes.searchFocusedItemIndex) {
				const focusedItemIndex = changes.searchFocusedItemIndex.currentValue;
				searchConfiguration.focusedItemIndex = focusedItemIndex;
			}
			if (changes.searchToggle) {
				const searchToggle = changes.searchToggle.currentValue;
				if (searchToggle) {
					searchConfiguration.onToggle = this.searchOnToggle;
					delete searchConfiguration.value;
					searchConfiguration.items = null;
				}
				else {
					delete searchConfiguration.onToggle;
				}
			}
			if (changes.searchInput) {
				const searchInput = changes.searchInput.currentValue;
				searchConfiguration.value = searchInput;
				if (!searchInput) {
					searchConfiguration.items = null;
				}
			}
			if (changes.searchResults) {
				const searchResults = changes.searchResults.currentValue;
				this.adaptedSearchResults = searchResults && this._adaptSearchResults(searchResults);
				searchConfiguration.items = this.adaptedSearchResults;
			}
			this.search = searchConfiguration;
		}
	}

	initLogo() {
		const settingsLogo = this.appSettings.views[this.viewKey].logo;
		const clickAction = this.appSettings.actions[settingsLogo.onClick];
		this.logo = {
			...settingsLogo,
			onClick: this.settingsActionsService.createDispatcher(clickAction),
		};
	}

	initBrand() {
		const settingsBrand = this.appSettings.views[this.viewKey].brand;
		const clickAction = this.appSettings.actions[settingsBrand.onClick];
		this.brand = {
			...settingsBrand,
			onClick: this.settingsActionsService.createDispatcher(clickAction),
		};
	}

	initHelp() {
		const helpActionSplitDropdown = this.appSettings.actions[this.appSettings.views[this.viewKey].help];
		const items = helpActionSplitDropdown
			.items
			.map(actionName => this.appSettings.actions[actionName])
			.map(action => ({
				id: action.id,
				label: action.name,
				onClick: this.settingsActionsService.createDispatcher(action),
			}));

		this.help = {
			id: helpActionSplitDropdown.id,
			onClick: this.settingsActionsService.createDispatcher(this.appSettings.actions[helpActionSplitDropdown.action]),
			items,
		};
	}

	initSearch() {
		this.search = this.appSettings.views[this.viewKey].search ?
			this.adaptSearch() :
			null;
	}

	initUserMenu() {
		this.user = this.appSettings.views[this.viewKey].userMenu ?
			this.adaptUserMenu() :
			null;
	}

	initProducts() {
		this.products = this.appSettings.views[this.viewKey].products ?
			this.adaptProducts() :
			null;
	}

	adaptSearch() {
		const searchSettings = this.appSettings.views[this.viewKey].search;

		// onToggle
		const onToggleAction = this.appSettings.actions[searchSettings.onToggle];
		this.searchOnToggle = onToggleAction && this.settingsActionsService.createDispatcher(onToggleAction);

		// onBlur
		const onBlurAction = this.appSettings.actions[searchSettings.onBlur];
		const onBlurActionDispatcher = onBlurAction && this.settingsActionsService.createDispatcher(onBlurAction);
		this.searchOnBlur = (event) => {
			if (onBlurActionDispatcher) {
				onBlurActionDispatcher(event);
			}
		};

		// onChange
		const onChangeAction = this.appSettings.actions[searchSettings.onChange];
		const onChangeActionDispatcher = onChangeAction && this.settingsActionsService.createDispatcher(onChangeAction);
		this.searchOnChange = (event) => {
			const searchInput = event.target && event.target.value;
			if (onChangeActionDispatcher) {
				return onChangeActionDispatcher(event, { searchInput });
			}
		};

		// onSelect
		this.searchAvailableInventoryTypes = [];
		const onSelectActionBy = searchSettings.onSelect;
		const onSelectDispatcherByType = [];
		Object.keys(onSelectActionBy).forEach((type) => {
			const onSelectAction = this.appSettings.actions[onSelectActionBy[type]];
			if (onSelectAction) {
				this.searchAvailableInventoryTypes.push({
					type,
					iconName: onSelectAction.icon,
					iconTitle: onSelectAction.name,
				});
				onSelectDispatcherByType[type] = this.settingsActionsService.createDispatcher(onSelectAction);
			}
		});
		this.searchOnSelect = (event, { sectionIndex, itemIndex }) => {
			const selectedCategory = this.adaptedSearchResults[sectionIndex];
			const selectedItem = selectedCategory && selectedCategory.suggestions[itemIndex];
			const onSelectDispatcher = onSelectDispatcherByType[selectedItem.inventoryType];
			if (onSelectDispatcher) {
				return onSelectDispatcher(event, selectedItem);
			}
		};

		// onKeyDown
		const onKeyDownAction = this.appSettings.actions[searchSettings.onKeyDown];
		const onKeyDownActionDispatcher = onKeyDownAction && this.settingsActionsService.createDispatcher(onKeyDownAction);
		this.searchOnKeyDown = (event, { focusedSectionIndex, focusedItemIndex, newFocusedSectionIndex, newFocusedItemIndex }) => {
			switch (event.key) {
			case 'ArrowDown':
			case 'ArrowUp':
				event.preventDefault();
				onKeyDownActionDispatcher(event, {
					focusedSectionIndex: newFocusedSectionIndex,
					focusedItemIndex: newFocusedItemIndex,
				});
				break;
			case 'Enter':
				event.preventDefault();
				if (focusedSectionIndex !== null && focusedItemIndex !== null) {
					this.searchOnSelect(event, {
						sectionIndex: focusedSectionIndex,
						itemIndex: focusedItemIndex,
					});
				}
				break;
			case 'Escape':
				event.preventDefault();
				this.searchOnToggle();
				break;
			}
		};

		return {
			...searchSettings,
			searchingText: this.$translate.instant('HEADERBAR_SEARCH_SEARCHING'),
			noResultText: this.$translate.instant('HEADERBAR_SEARCH_NO_RESULT'),
			icon: onToggleAction && {
				name: onToggleAction.icon,
				title: onToggleAction.name,
				bsStyle: 'link',
				tooltipPlacement: 'bottom',
			},
			onToggle: this.searchOnToggle,
			onBlur: this.searchOnBlur,
			onChange: this.searchOnChange,
			onSelect: this.searchOnSelect,
			onKeyDown: this.searchOnKeyDown,
		};
	}

	_adaptSearchResults(searchResults) {
		return this.searchAvailableInventoryTypes
			.filter(inventoryType => searchResults
				.some(result => result.inventoryType === inventoryType.type))
			.map((inventoryType) => {
				const suggestions = searchResults
					.filter(result => result.inventoryType === inventoryType.type);
				let label = inventoryType.type;
				if (this.state.search.searchCategories) {
					label = this.state
						.search
						.searchCategories
						.find(category => category.type === inventoryType.type).label;
				}

				return {
					title: label,
					icon: {
						name: inventoryType.iconName,
						title: label,
					},
					suggestions: suggestions.map((result) => {
						return {
							...result,
							title: result.name || '',
							description: result.description || '',
						};
					}),
				};
			});
	}

	adaptUserMenu() {
		return this._adaptDropdown(this.appSettings.views[this.viewKey].userMenu);
	}

	adaptProducts() {
		return this._adaptDropdown(this.appSettings.views[this.viewKey].products, true);
	}

	_adaptDropdown(menu, showIcons) {
		const action = this.appSettings.actions[menu];
		if (!action || !action.staticActions) {
			return null;
		}

		const { id, name, staticActions } = action;
		return {
			id,
			name,
			items: staticActions
				.map(actionName => this.appSettings.actions[actionName])
				.map(action => ({
					id: action.id,
					icon: showIcons && action.icon,
					label: action.name,
					onClick: this.settingsActionsService.createDispatcher(action),
				})),
		};
	}
}
