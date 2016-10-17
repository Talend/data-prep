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
	constructor($element, $translate, AppSettings, SettingsActionsService) {
		'ngInject';
		this.$element = $element;
		this.$translate = $translate;
		this.AppSettings = AppSettings;
		this.SettingsActionsService = SettingsActionsService;

		this.init();
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			if (e.target.tagName === 'A') {
				e.preventDefault();
			}
		});
	}

	createDispatcher(type) {
		return this.SettingsActionsService.createDispatcher(type);
	}

	init() {
		this.initApp();
		this.adaptBrandLink();
		this.adaptContent();
	}

	initApp() {
		this.app = this.AppSettings.views.appheaderbar.app;
	}

	adaptBrandLink() {
		const settingsBrandLink = this.AppSettings.views.appheaderbar.brandLink;
		this.brandLink = {
			...settingsBrandLink,
			onClick: this.createDispatcher(settingsBrandLink.onClick),
		};
	}

	adaptContent() {
		const settingsContent = this.AppSettings.views.appheaderbar.content;
		this.content = settingsContent.map(nextContent => ({
			...nextContent,
			navs: nextContent.navs.map(nav => this.adaptNav(nav)),
		}));
	}

	adaptNav(nav) {
		const adaptedNavItems = nav.navItems.map((navItem) => {
			switch (navItem.type) {
			case NAV_ITEM:
				return this.adaptNavItem(navItem);
			case DROPDOWN:
				return this.adaptDropdown(navItem);
			}
			return navItem;
		});

		return {
			...nav,
			navItems: adaptedNavItems,
		};
	}

	adaptNavItem(navItem) {
		return {
			...navItem,
			item: {
				...navItem.item,
				name: this.$translate.instant(navItem.item.name),
				onClick: this.createDispatcher(navItem.item.onClick),
			},
		};
	}

	adaptDropdown(navItem) {
		return {
			...navItem,
			item: {
				...navItem.item,
				dropdown: {
					...navItem.item.dropdown,
					onSelect: this.createDispatcher(navItem.item.dropdown.onSelect),
				},
				items: navItem.item.items.map((dropItem) => {
					return {
						...dropItem,
						name: this.$translate.instant(dropItem.name),
						onClick: this.createDispatcher(dropItem.onClick),
					};
				}),
			},
		};
	}
}
