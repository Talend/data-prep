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

export function SettingsService($http, state) {
	'ngInject';

	return {
		adapSettings,
		clearSettings,
		refreshSettings,
		setSettings,
	};

	function adapSettings(settings) {
		const importTypes = state.import.importTypes;
		const datasetCreateAction = settings && settings.actions && settings.actions['dataset:create'];
		if (importTypes.length && datasetCreateAction) {
			datasetCreateAction.items = importTypes;
		}
		return settings;
	}

	function refreshSettings() {
		return $http.get('/assets/config/app-settings.json')
			.then(response => response.data)
			.then(settings => this.adapSettings(settings))
			.then(settings => this.setSettings(settings));
	}

	function setSettings(settings) {
		this.clearSettings();
		Object.assign(appSettings, settings);
	}

	function clearSettings() {
		appSettings.views = [];
		appSettings.actions = [];
	}
}
