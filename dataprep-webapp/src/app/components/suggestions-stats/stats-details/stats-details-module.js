/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import StatsDetailsCtrl from './stats-details-controller';
import StatsDetails from './stats-details-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.stats-details
     * @description This module contains the controller and directives for the statistics tabs
     * @requires talend.widget
     * @requires data-prep.services.state
     * @requires data-prep.services.filter
     */
    angular.module('data-prep.stats-details',
        [
            'talend.widget',
            'data-prep.services.state',
            'data-prep.services.filter'
        ])
        .controller('StatsDetailsCtrl', StatsDetailsCtrl)
        .directive('statsDetails', StatsDetails);
})();