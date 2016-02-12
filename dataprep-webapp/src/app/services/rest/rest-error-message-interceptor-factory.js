/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.rest.service:RestErrorMessageHandler
 * @description Error message interceptor
 * @requires data-prep.services.utils.service:MessageService
 */
export default function RestErrorMessageHandler($q, MessageService) {
    'ngInject';

    return {
        /**
         * @ngdoc method
         * @name responseError
         * @methodOf data-prep.services.rest.service:RestErrorMessageHandler
         * @param {object} rejection - the rejected promise
         * @description Display the error message depending on the error status and error code
         */
        responseError: function (rejection) {
            //user cancel the request : we do not show message
            if (rejection.config.timeout && rejection.config.timeout.$$state.value === 'user cancel') { //eslint-disable-line angular/no-private-call
                return $q.reject(rejection);
            }

            if (rejection.status <= 0) {
                MessageService.error('SERVER_ERROR_TITLE', 'SERVICE_UNAVAILABLE');
            }

            else if (rejection.status === 500) {
                MessageService.error('SERVER_ERROR_TITLE', 'GENERIC_ERROR');
            }
            else if (rejection.data.message_title && rejection.data.message) {
                MessageService.error(rejection.data.message_title, rejection.data.message);
            }

            return $q.reject(rejection);
        }
    };
}