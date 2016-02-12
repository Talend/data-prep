/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import RestErrorMessageHandler from './rest-error-message-interceptor-factory';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.rest
     * @description This module contains the REST interceptor
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.rest', ['data-prep.services.utils'])
        .factory('RestErrorMessageHandler', RestErrorMessageHandler)
        .config(function ($httpProvider) {
            'ngInject';
            $httpProvider.interceptors.push('RestErrorMessageHandler');
        });
})();