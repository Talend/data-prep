/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import PreparationRestService from './rest/preparation-rest-service';
import PreparationListService from './list/preparation-list-service';
import PreparationService from './preparation-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.preparation
     * @description This module contains the services to manipulate preparations
     * @requires data-prep.services.dataset
     * @requires data-prep.services.preparation
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.preparation',
        [
            'data-prep.services.dataset',
            'data-prep.services.preparation',
            'data-prep.services.utils'
        ])
        .service('PreparationRestService', PreparationRestService)
        .service('PreparationListService', PreparationListService)
        .service('PreparationService', PreparationService);
})();