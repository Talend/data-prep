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
	uris: [],
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
			.then(settings => this.setSettings(settings));
	}

	function setSettings(settings) {
		this.clearSettings();

		// FIXME [NC]: MOCK !
		// settings.uris = {
		// 	logout: '/logout',
		// 	apiUser: '/api/user',
		// 	apiShare: '/api/share',
		// 	groups: '/groups',
		// 	users: '/users',
		// 	apiExportAsync: '/api/export/async',
		// 	api: '/api',
		// 	apiAggregate: '/api/aggregate',
		// 	apiDatasets: '/api/datasets',
		// 	apiExport: '/api/export',
		// 	apiFolders: '/api/folders',
		// 	apiMail: '/api/mail',
		// 	apiPreparations: '/api/preparations',
		// 	apiPreparationsPreview: '/api/preparations/preview',
		// 	apiSearch: '/api/search',
		// 	apiSettings: '/api/settings',
		// 	apiTcomp: '/api/tcomp',
		// 	apiTransform: '/api/transform',
		// 	apiTypes: '/api/types',
		// 	apiUpgradeCheck: '/api/upgrade/check',
		// 	apiVersion: '/api/version',
		// 	logoutRedirect: 'http://localhost:9080/oidc/idp/logout?client_id=J3Wqyvlzgw0ySg&post_logout_redirect_uri=http://localhost:3000/',
		// };

		Object.assign(appSettings, settings);
	}

	function clearSettings() {
		appSettings.views = [];
		appSettings.actions = [];
		appSettings.uris = [];
	}
}
