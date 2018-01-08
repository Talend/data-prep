/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import uiRouter from 'angular-ui-router';

import NotFoundInterceptor from './notfound-interceptor';

const MODULE_NAME = 'data-prep.services.errors';

angular.module(MODULE_NAME, [uiRouter])
	.factory('NotFoundInterceptor', NotFoundInterceptor)
	.config(($httpProvider) => {
		'ngInject';

		$httpProvider.interceptors.push('NotFoundInterceptor');
	});

export default MODULE_NAME;
