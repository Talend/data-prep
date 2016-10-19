/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const appSettings = {
	actions: [],
	views: [],
};

export class SettingsService {
	constructor($http) {
		'ngInject';
		this.$http = $http;
	}

	refreshSettings() {
		return this.$http.get('/assets/config/app-settings.json')
			.then(settings => settings.data)
			.then((settings => this.setSettings(settings)));
	}

	setSettings(settings) {
		this.clearSettings();
		Object.assign(appSettings, settings);
	}

	clearSettings() {
		appSettings.views = [];
		appSettings.actions = [];
	}
}
