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
		const newContent = this.content.slice();
		if (changes.searchInput) {
			const searchInput = changes.searchInput && changes.searchInput.currentValue;
			newContent[1].search = {
				...newContent[1].search,
				inputProps: {
					...newContent[1].search.inputProps,
					value: searchInput,
					items: searchInput === '' ? [] : newContent[1].search.inputProps.items,
				},
			};
		}
		else if (changes.searchResults) {
			const results = changes.searchResults && changes.searchResults.currentValue;
			const inventoryTypes = [
				{
					title: 'preparation',
					iconName: 'talend-preparation',
					iconTitle: 'preparation',
				}, {
					title: 'dataset',
					iconName: 'talend-dataset',
					iconTitle: 'dataset',
				}, {
					title: 'folder',
					iconName: 'talend-folder',
					iconTitle: 'folder',
				}, {
					title: 'documentation',
					iconName: 'talend-documentation',
					iconTitle: 'documentation',
				},
			];
			newContent[1].search = {
				...newContent[1].search,
				items: inventoryTypes
					.filter((inventoryType) => {
						const suggestions = results.filter((result) => {
							return result.inventoryType === inventoryType.title;
						});
						return suggestions && suggestions.length;
					})
					.map((inventoryType) => {
						const suggestions = results.filter((result) => {
							return result.inventoryType === inventoryType.title;
						});
						return {
							title: inventoryType.title,
							icon: {
								name: inventoryType.iconName,
								title: inventoryType.iconTitle,
							},
							suggestions: suggestions.map((result) => {
								return {
									title: result.name,
									description: result.description,
								};
							}),
						};
					}),
			};
		}
		this.content = newContent;
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
		const searchAllActionId = searchSettings.itemProps && searchSettings.inputProps.onKeyDown;
		const searchOpenActionId = searchSettings.itemProps && searchSettings.itemProps.onClick;
		const searchAllAction = this.appSettings.actions[searchAllActionId];
		const searchOpenAction = this.appSettings.actions[searchOpenActionId];
		return {
			...searchSettings,
			config: {
				...searchSettings.config,
				title: this.$translate.instant('TOGGLE_SEARCH'),
			},
			inputProps: {
				...searchSettings.inputProps,
				value: this.state.search.searchInput,
				placeholder: this.$translate.instant('SEARCH'),
				onKeyDown: () => {
				},
				onChange: (event) => {
					const searchInput = event.target && event.target.value;
					return this.settingsActionsService
						.createDispatcher(searchAllAction)(event, { searchInput });
				},
			},
			itemProps: {
				...searchSettings.itemProps,
				onClick: this.settingsActionsService.createDispatcher(searchOpenAction),
			},
		};
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
