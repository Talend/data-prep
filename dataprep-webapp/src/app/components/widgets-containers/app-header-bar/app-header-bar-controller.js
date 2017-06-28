/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
					searchConfiguration.value = '';
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
		const settingsLogo = this.appSettings.views.appheaderbar.logo;
		const clickAction = this.appSettings.actions[settingsLogo.onClick];
		this.logo = {
			...settingsLogo,
			onClick: this.settingsActionsService.createDispatcher(clickAction),
		};
	}

	initBrand() {
		const settingsBrand = this.appSettings.views.appheaderbar.brand;
		const clickAction = this.appSettings.actions[settingsBrand.onClick];
		this.brand = {
			...settingsBrand,
			onClick: this.settingsActionsService.createDispatcher(clickAction),
		};
	}

	initHelp() {
		const helpActionSplitDropdown = this.appSettings.actions[this.appSettings.views.appheaderbar.help];
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
		this.search = this.appSettings.views.appheaderbar.search ?
			this.adaptSearch() :
			null;
	}

	initUserMenu() {
		this.user = this.appSettings.views.appheaderbar.userMenu ?
			this.adaptUserMenu() :
			null;
	}

	initProducts() {
		this.products = this.adaptProducts();
		// this.products = this.appSettings.views.appheaderbar.products ?
		// 	this.adaptProducts() :
		// 	null;
	}

	adaptSearch() {
		const searchSettings = this.appSettings.views.appheaderbar.search;

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
		const userMenu = this.appSettings
			.views
			.appheaderbar
			.userMenu;
		const { id, name, staticActions } = this.appSettings.actions[userMenu];

		return {
			id,
			name,
			items: staticActions
				.map(actionName => this.appSettings.actions[actionName])
				.map(action => ({
					id: action.id,
					label: action.name,
					onClick: this.settingsActionsService.createDispatcher(action),
				})),
		};
	}

	adaptProducts() {
		// const productsMenu = this.appSettings
		// 	.views
		// 	.appheaderbar
		// 	.productsMenu;
		// const { id, name, staticActions } = this.appSettings.actions[productsMenu];

		const act = {
			displayMode: 'ActionSettings',
			icon: 'talend-question-circle',
			id: 'external:documentation',
			name: 'Documentation',
			payload: {
				method: 'open',
			},
			method: 'open',
			type: '@@external/OPEN_WINDOW',
		};

		const mock = {
			id: 'header-products',
			items: [
				{
					icon: 'talend-logo-dp',
					key: 'tdp',
					label: 'Data Preparation',
				},
				{
					icon: 'talend-logo-ic',
					key: 'tic',
					label: 'Integration Cloud',
				},
				{
					icon: 'talend-logo-mc',
					key: 'tmc',
					label: 'Management Console',
				},
			],
			onSelect: (a, b, c) => {
				console.log('[NC] a: ', a);
				console.log('[NC] b: ', b);
				console.log('[NC] c: ', c);
			},
		};

		return mock;
	}
}
