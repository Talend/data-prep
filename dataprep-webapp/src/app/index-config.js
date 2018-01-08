/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

const configPath = '/assets/config/config.json';
const settingsPath = '/api/settings';

function get(url) {
	const initInjector = angular.injector(['ng']);
	const $http = initInjector.get('$http');
	return $http.get(url).then(response => response.data);
}

function getAppConfig() {
	return get(configPath);
}

function getAppSettings({ serverUrl }) {
	return get(serverUrl + settingsPath);
}

export default function getAppConfiguration() {
	let config;
	return getAppConfig()
		.then((appConfig) => {
			config = appConfig;
			return getAppSettings(config);
		})
		.then(appSettings => ({ config, appSettings }));
}
