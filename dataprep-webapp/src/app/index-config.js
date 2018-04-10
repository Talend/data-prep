/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

function get(url) {
	const initInjector = angular.injector(['ng']);
	const $http = initInjector.get('$http');
	return $http.get(url, {
		headers: { 'Accept-Language': '' },
	}).then(response => response.data);
}

export default function getAppSettings() {
	return get('/api/settings');
}
