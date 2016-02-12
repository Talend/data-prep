/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FilterListCtrl from './filter-list-controller';
import FilterList from './filter-list-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-list
     * @description This module contains the controller and directives to manage the filter list
     * @requires talend.widget
     */
    angular.module('data-prep.filter-list', ['talend.widget'])
        .controller('FilterListCtrl', FilterListCtrl)
        .directive('filterList', FilterList);
})();