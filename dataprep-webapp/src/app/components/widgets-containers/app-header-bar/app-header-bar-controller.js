/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const NAV_ITEM = 'navItem';
const DROPDOWN = 'dropdown';

export default class AppHeaderBarCtrl {
	constructor($element, $translate, appSettings, state, SettingsActionsService, SearchService) {
		'ngInject';

		this.$element = $element;
		this.$translate = $translate;
		this.appSettings = appSettings;
		this.settingsActionsService = SettingsActionsService;
		this.searchService = SearchService;
		this.state = state;
		this.init();
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			// block the native click action to avoid home redirection on empty href
			e.preventDefault();
		});
	}

	$onChanges(changes) {
		const updatedContent = this.content.slice();
		const searchConfiguration = updatedContent[1].search;
		if (changes.searchToggle) {
			const searchToggle = changes.searchToggle.currentValue;
			if (searchToggle) {
				searchConfiguration.onToggle = this.searchOnToggle;
				searchConfiguration.items = [];
			}
			else {
				delete searchConfiguration.onToggle;
			}
		}
		else if (changes.searchInput) {
			const searchInput = changes.searchInput.currentValue;
			if (!searchInput.length) {
				searchConfiguration.items = [];
			}
		}
		else if (changes.searchResults) {
			const searchResults = changes.searchResults.currentValue;
			this.adaptedSearchResults = this._adaptSearchResults(searchResults);
			searchConfiguration.items = this.adaptedSearchResults;
		}
		this.content = updatedContent;
	}

	init() {
		this.initApp();
		this.adaptBrandLink();
		this.adaptContent();
	}

	initApp() {
		this.app = this.appSettings.views.appheaderbar.app;
	}

	adaptBrandLink() {
		const settingsBrandLink = this.appSettings.views.appheaderbar.brandLink;
		const clickAction = this.appSettings.actions[settingsBrandLink.onClick];
		this.brandLink = {
			...settingsBrandLink,
			onClick: this.settingsActionsService.createDispatcher(clickAction),
		};
	}

	adaptSearch() {
		const searchSettings = this.appSettings.views.appheaderbar.search;

		// onToggle
		const onToggleAction = this.appSettings.actions[searchSettings.onToggle];
		this.searchOnToggle = this.settingsActionsService.createDispatcher(onToggleAction);

		// onBlur
		// FIXME onBlur should not be triggered while clicking on result item
		// const onBlurAction = this.appSettings.actions[searchSettings.onBlur];
		// const onBlurActionDispatcher = this.settingsActionsService.createDispatcher(onBlurAction);
		this.searchOnBlur = () => {
		};

		// onChange
		const onChangeAction = this.appSettings.actions[searchSettings.onChange];
		const onChangeActionDispatcher = this.settingsActionsService.createDispatcher(onChangeAction);
		this.searchOnChange = (event) => {
			const searchInput = event.target && event.target.value;
			return onChangeActionDispatcher(event, { searchInput });
		};

		// onSelect
		this.searchAvailableInventoryTypes = [];
		const onSelectActionBy = searchSettings.onSelect;
		const onSelectDispatcherByType = [];
		Object.keys(onSelectActionBy).forEach((type) => {
			const onSelectAction = this.appSettings.actions[onSelectActionBy[type]];
			this.searchAvailableInventoryTypes.push({
				title: type,
				iconName: onSelectAction.icon,
				iconTitle: onSelectAction.name,
			});
			onSelectDispatcherByType[type] = this.settingsActionsService.createDispatcher(onSelectAction);
		});
		this.searchOnSelect = (event, { sectionIndex, itemIndex }) => {
			const selectedCategory = this.adaptedSearchResults[sectionIndex];
			const selectedItem = selectedCategory && selectedCategory.suggestions[itemIndex];
			return onSelectDispatcherByType[selectedItem.inventoryType](event, selectedItem);
		};

		return {
			...searchSettings,
			icon: {
				name: onToggleAction && onToggleAction.icon,
				title: this.$translate.instant('TOGGLE_SEARCH'),
				bsStyle: 'link',
			},
			placeholder: this.$translate.instant('SEARCH'),
			onToggle: this.searchOnToggle,
			onBlur: this.searchOnBlur,
			onChange: this.searchOnChange,
			onSelect: this.searchOnSelect,
		};
	}

	_adaptSearchResults(searchResults) {
		return this.searchAvailableInventoryTypes
			.filter((inventoryType) => {
				return searchResults.some((result) => {
					return result.inventoryType === inventoryType.title;
				});
			})
			.map((inventoryType) => {
				const suggestions = searchResults.filter((result) => {
					return result.inventoryType === inventoryType.title;
				});
				return {
					title: inventoryType.title,
					icon: {
						name: inventoryType.iconName,
						title: inventoryType.title,
					},
					suggestions: suggestions.map((result) => {
						return {
							...result,
							title: result.name,
							description: result.description,
						};
					}),
				};
			});
	}

	adaptContent() {
		const search = this.appSettings.views.appheaderbar.search ?
			this.adaptSearch() :
			{};
		const navItems = this.appSettings.views.appheaderbar.actions ?
			this.adaptActions() :
			[];
		const userMenu = this.appSettings.views.appheaderbar.userMenuActions ?
			this.adaptUserMenu() :
			[];

		this.content = [
			{
				navs: [{
					nav: { pullRight: true },
					navItems: navItems.concat(userMenu),
				}],
			},
			{
				search,
			},
		];
	}

	adaptActions() {
		return this.appSettings
			.views
			.appheaderbar
			.actions
			.map(actionName => this.appSettings.actions[actionName])
			.map(action => ({
				type: NAV_ITEM,
				item: {
					id: action.id,
					name: this.$translate.instant(action.name),
					icon: action.icon,
					onClick: this.settingsActionsService.createDispatcher(action),
				},
			}));
	}

	adaptUserMenu() {
		const { id, name, icon, menu } = this.appSettings
			.views
			.appheaderbar
			.userMenuActions;

		return {
			type: DROPDOWN,
			item: {
				dropdown: {
					id,
					icon,
					title: name,
				},
				items: menu
					.map(actionName => this.appSettings.actions[actionName])
					.map(action => ({
						id: action.id,
						icon: action.icon,
						name: action.name,
						onClick: this.settingsActionsService.createDispatcher(action),
					})),
			},
		};
	}
}
