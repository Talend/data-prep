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
	constructor($element, $translate, appSettings, SettingsActionsService, SearchService) {
		'ngInject';
		this.$element = $element;
		this.$translate = $translate;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.searchService = SearchService;
		this.init();
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			// block the native click action to avoid home redirection on empty href
			e.preventDefault();
		});
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
			onClick: this.SettingsActionsService.createDispatcher(clickAction),
		};
	}

	adaptSearch() {
		const searchSettings = this.appSettings.views.appheaderbar.search;
		const searchActionId = searchSettings.itemProps && searchSettings.itemProps.onClick;
		const searchAction = this.appSettings.actions[searchActionId];
		const searchProps = {
			...searchSettings,
			config: {
				...searchSettings.config,
				title: this.$translate.instant('TOGGLE_SEARCH'),
			},
			inputProps: {
				...searchSettings.inputProps,
				placeholder: this.$translate.instant('SEARCH'),
			},
			itemProps: {
				...searchSettings.itemProps,
				onClick: this.SettingsActionsService.createDispatcher(searchAction),
			},
		};
		const inputValue = searchSettings.inputProps && searchSettings.inputProps.value;
		if (inputValue) {
			this.searchService.searchAll()
				.then((results) => {
					const adaptedResults = results.map((result) => {
						return {
							title: '', description: result,
						};
					});
					return {
						...searchProps,
						items: [
							{
								title: 'Common category',
								icon: {
									name: 'fa fa-asterisk',
									title: 'icon',
								},
								suggestions: adaptedResults,
							},
						],
					};
				});
		}
		else {
			return searchProps;
		}
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
					onClick: this.SettingsActionsService.createDispatcher(action),
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
						onClick: this.SettingsActionsService.createDispatcher(action),
					})),
			},
		};
	}
}
