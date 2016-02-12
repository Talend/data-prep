/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FilterMonitor from './filter-monitor-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-monitor
     * @description This module contains the controller and directives to manage the filter list
     * @requires data-prep.services.filter
     * @requires data-prep.services.state
     */
    angular.module('data-prep.filter-monitor',
        [
            'data-prep.services.filter',
            'data-prep.services.state'
        ])
        .directive('filterMonitor', FilterMonitor);
})();