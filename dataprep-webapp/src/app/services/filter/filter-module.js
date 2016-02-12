/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FilterService from './filter-service';
import FilterAdapterService from './filter-adapter-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.filter
     * @description This module contains the services to manage filters in the datagrid. It is responsible for the filter update within the SlickGrid grid
     * @requires data-prep.services.playground
     */
    angular.module('data-prep.services.filter',
        [
            'data-prep.services.playground',
            'data-prep.services.statistics',
            'data-prep.services.utils'
        ])
        .service('FilterService', FilterService)
        .service('FilterAdapterService', FilterAdapterService);
})();