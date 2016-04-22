/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import ImportRestService from './import-rest-service';
import ImportService from './import-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.import
     * @description This module contains the services for import
     */
    angular.module('data-prep.services.import', [
            'data-prep.services.utils',
            'data-prep.services.state',
        ])
        .service('ImportRestService', ImportRestService)
        .service('ImportService', ImportService)
})();
