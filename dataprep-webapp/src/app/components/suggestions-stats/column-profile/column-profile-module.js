/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import ColumnProfileCtrl from './column-profile-controller';
import ColumnProfile from './column-profile-directive';

(() => {
    'use strict';

    angular.module('data-prep.column-profile',
        [
            'talend.widget',
            'data-prep.services.dataset',
            'data-prep.services.filter',
            'data-prep.services.playground',
            'data-prep.services.statistics',
            'data-prep.services.state'
        ])
        .controller('ColumnProfileCtrl', ColumnProfileCtrl)
        .directive('columnProfile', ColumnProfile);
})();