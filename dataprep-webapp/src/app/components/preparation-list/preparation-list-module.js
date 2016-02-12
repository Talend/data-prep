/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import PreparationListCtrl from './preparation-list-controller';
import PreparationList from './preparation-list-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.preparation-list
     * @description This module contains the controller and directives to manage the preparation list
     * @requires ui.router
     * @requires talend.widget
     * @requires data-prep.services.preparation
     * @requires data-prep.services.playground
     * @requires data-prep.services.state
     */
    angular.module('data-prep.preparation-list',
        [
            'ui.router',
            'talend.widget',
            'data-prep.services.preparation',
            'data-prep.services.playground',
            'data-prep.services.state'
        ])
        .controller('PreparationListCtrl', PreparationListCtrl)
        .directive('preparationList', PreparationList);
})();