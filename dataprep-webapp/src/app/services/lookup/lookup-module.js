/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import LookupService from './lookup-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.lookup
     * @description This module contains the services to load dataset lookup
     * @requires data-prep.services.dataset
     * @requires data-prep.services.transformation
     * @requires data-prep.services.state
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.lookup',
        [
            'data-prep.services.dataset',
            'data-prep.services.transformation',
            'data-prep.services.state',
            'data-prep.services.utils'
        ])
        .service('LookupService', LookupService);
})();