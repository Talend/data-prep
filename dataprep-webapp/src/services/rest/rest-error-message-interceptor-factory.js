(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.rest.service:RestErrorMessageHandler
     * @description Error message interceptor
     * @requires data-prep.services.utils.service:MessageService
     */
    function RestErrorMessageHandler($q, MessageService) {

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
                if (rejection.config.timeout && rejection.config.timeout.$$state.value === 'user cancel') {
                    return $q.reject(rejection);
                }

                if (rejection.status <= 0) {
                    MessageService.error('SERVER_ERROR_TITLE', 'SERVICE_UNAVAILABLE');
                }

                else if (rejection.status === 500) {
                    MessageService.error('SERVER_ERROR_TITLE', 'GENERIC_ERROR');
                }
                /*jshint camelcase: false */
                else if (rejection.data.message_title && rejection.data.message) {
                    MessageService.error(rejection.data.message_title, rejection.data.message);
                }

                return $q.reject(rejection);
            }
        };
    }

    angular.module('data-prep.services.rest')
        .factory('RestErrorMessageHandler', RestErrorMessageHandler)
        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('RestErrorMessageHandler');
        }]);
})();