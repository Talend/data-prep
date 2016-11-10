/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class BreadcrumbCtrl {
	constructor(appSettings, SettingsActionsService) {
		'ngInject';
		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
	}

	$onInit() {
		this.maxItems = this.appSettings.views.breadcrumb.maxItems;
	}

	$onChanges() {
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
