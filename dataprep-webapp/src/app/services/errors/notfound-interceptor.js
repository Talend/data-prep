/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_404_ROUTE } from '../../index-route';

/**
 * @ngdoc service
 * @name data-prep.services.errors:NotFoundInterceptor
 * @description Http Interceptor that manage 404
 */
export default function NotFoundInterceptor($q, $injector) {
	'ngInject';

	return {
		responseError: (rejection) => {
			const $state = $injector.get('$state');
			if (rejection.status === 404) {
				$state.go(HOME_404_ROUTE);
			}
			return $q.reject(rejection);
		},
	};
}
