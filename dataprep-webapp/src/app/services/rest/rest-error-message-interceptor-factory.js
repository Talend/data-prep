/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const specialClientErrorCodes = [401, 403, 404];

/**
 * @ngdoc service
 * @name data-prep.services.rest.service:RestErrorMessageHandler
 * @description Error message interceptor
 * @requires data-prep.services.utils.service:MessageService
 */
export default function RestErrorMessageHandler($q, MessageService) {
	'ngInject';

	/**
	 * Dedicated pages instead of toast for certain error codes
	 * @param errorCode http error code to test
	 * @returns {boolean}
	 */
	function hasSpecialCase(errorCode) {
		return specialClientErrorCodes.includes(errorCode);
	}

	return {
		/**
		 * @ngdoc method
		 * @name responseError
		 * @methodOf data-prep.services.rest.service:RestErrorMessageHandler
		 * @param {object} rejection - the rejected promise
		 * @description Display the error message depending on the error status and error code
		 */
		responseError(rejection) {
			const { config, status } = rejection;

			// user cancel the request or the request should fail silently : we do not show message
			if (config && (config.failSilently || (config.timeout && config.timeout.$$state.value === 'user cancel'))) { // eslint-disable-line angular/no-private-call
				return $q.reject(rejection);
			}

			if (status <= 0) {
				MessageService.error('SERVER_ERROR_TITLE', 'SERVICE_UNAVAILABLE');
			}
			else if (status === 500) {
				MessageService.error('SERVER_ERROR_TITLE', 'GENERIC_ERROR');
			}
			else if (!hasSpecialCase(status) && rejection.data && rejection.data.messageTitle && rejection.data.message) {
				MessageService.error(rejection.data.messageTitle, rejection.data.message);
			}

			return $q.reject(rejection);
		},
	};
}
