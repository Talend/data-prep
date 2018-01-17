/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class BreadcrumbCtrl {
	constructor($element, appSettings, SettingsActionsService) {
		'ngInject';
		this.$element = $element;
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
	}

	$onInit() {
		if (!this.appSettings.views.breadcrumb) {
			return;
		}
		this.maxItems = this.appSettings.views.breadcrumb.maxItems;
	}

	$postLink() {
		this.$element[0].addEventListener('click', (e) => {
			// block the native click action to avoid home redirection on empty href
			e.preventDefault();
		});
	}

	$onChanges() {
		if (!this.appSettings.views.breadcrumb) {
			return;
		}
		this.breadcrumbItems = this.adaptItems();
	}

	adaptItems() {
		const onItemClick = this.appSettings.views.breadcrumb.onItemClick;
		return this.items
			.map(item => ({
				id: item.id,
				text: item.name,
				title: item.name,
				onClick: this.SettingsActionsService.createDispatcher(this.appSettings.actions[onItemClick]),
			}));
	}
}
