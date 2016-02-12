/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import ActionsListCtrl from './actions-list-controller';
import ActionsList from './actions-list-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.actions-list
     * @description This module display a transformation actions list
     * @requires talend.widget
     * @requires data-prep.services.transformation
     * @requires data-prep.services.state
     */
    angular.module('data-prep.actions-list',
        [
            'talend.widget',
            'data-prep.services.transformation',
            'data-prep.services.state'
        ])
        .controller('ActionsListCtrl', ActionsListCtrl)
        .directive('actionsList', ActionsList);
})();