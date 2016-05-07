/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatepatternService from './datepattern-service';
import DatepatternRestService from './rest/datepattern-rest-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.datepattern
     * @description This module contains the services to manage the datepattern
     */
    angular.module('data-prep.services.datepattern', ['data-prep.services.utils'])
        .service('DatepatternService', DatepatternService)
        .service('DatepatternRestService', DatepatternRestService);
})();
