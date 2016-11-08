/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationBreadcrumbCtrl {
	constructor($timeout, state, appSettings, SettingsActionsService) {
		'ngInject';
		this.$timeout = $timeout;
		this.state = state;

		this.appSettings = appSettings;
		this.SettingsActionsService = SettingsActionsService;
		this.init();
	}

	init() {
		this.maxItems = this.appSettings.views.breadcrumb.maxItems;
		this.items = this.adaptItems();
	}

	adaptItems() {
		const onItemClick = this.appSettings.views.breadcrumb.onItemClick;
		return this.state.inventory.breadcrumb
			.map(item => ({
				id: item.id,
				text: item.name,
				title: item.name,
				onClick: this.SettingsActionsService.createDispatcher(this.appSettings.actions[onItemClick]),
			}));
	}
}
