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
const SEARCH_INVENTORY_TYPES = [
	{
		title: 'preparations',
		iconName: 'talend-dataprep',
		iconTitle: 'preparations',
	}, {
		title: 'datasets',
		iconName: 'talend-files-o',
		iconTitle: 'datasets',
	}, {
		title: 'folders',
		iconName: 'talend-folder',
		iconTitle: 'folders',
	}, {
		title: 'documentation',
		iconName: 'talend-question-circle',
		iconTitle: 'documentation',
	},
];

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
		if (changes.searchToggle) {
			const searchToggle = changes.searchToggle.currentValue;
			if (searchToggle) {
				const searchSettings = this.appSettings.views.appheaderbar.search;
				const searchToggleAction = this.appSettings.actions[searchSettings.onToggle];
				updatedContent[1].search = {
					...updatedContent[1].search,
					onToggle: this.settingsActionsService.createDispatcher(searchToggleAction),
					items: [],
				};
			}
			else {
				delete updatedContent[1].search.onToggle;
			}
		}
		else if (changes.searchInput) {
			const searchInput = changes.searchInput.currentValue;
			if (!searchInput.length) {
				updatedContent[1].search = {
					...updatedContent[1].search,
					items: [],
				};
			}
		}
		else if (changes.searchResults) {
			const searchResults = changes.searchResults.currentValue;
			updatedContent[1].search = {
				...updatedContent[1].search,
				items: SEARCH_INVENTORY_TYPES
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
		const searchToggleAction = this.appSettings.actions[searchSettings.onToggle];
		const searchBlurAction = this.appSettings.actions[searchSettings.onBlur];
		const searchAllAction = this.appSettings.actions[searchSettings.onChange];
		const searchOpenAction = this.appSettings.actions[searchSettings.onSelect];
		return {
			...searchSettings,
			icon: {
				name: searchToggleAction && searchToggleAction.icon,
				title: this.$translate.instant('TOGGLE_SEARCH'),
				bsStyle: 'link',
			},
			placeholder: this.$translate.instant('SEARCH'),
			onToggle: this.settingsActionsService.createDispatcher(searchToggleAction),
			onBlur: this.settingsActionsService.createDispatcher(searchBlurAction),
			onChange: (event) => {
				const searchInput = event.target && event.target.value;
				return this.settingsActionsService.createDispatcher(searchAllAction)(event, { searchInput });
			},
			onSelect: this.settingsActionsService.createDispatcher(searchOpenAction),
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
