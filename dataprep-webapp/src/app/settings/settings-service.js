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

export function SettingsService($http, RestURLs) {
	'ngInject';

	return {
		clearSettings,
		refreshSettings,
		setSettings,
	};

	function refreshSettings() {
		return $http.get(RestURLs.settingsUrl)
			.then(response => response.data)
			.then((settings) => {
				settings.actions['version:toggle'] = {
					id: 'version:toggle',
					name: 'Toggle',
					payload: { method: 'toggleVersion' },
					type: '@@version/VERSION_TOGGLE',
				};
				settings.actions['version:select'] = {
					id: 'version:select',
					name: 'Select',
					payload: { method: 'selectVersion' },
					type: '@@router/GO_VERSION_READ_ONLY',
				};
				settings.actions['export:toggle'] = {
					id: 'export:toggle',
					name: 'Toggle',
					payload: { method: 'toggleExport' },
					type: '@@export/EXPORT_TOGGLE',
				};
				return settings;
			})
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
