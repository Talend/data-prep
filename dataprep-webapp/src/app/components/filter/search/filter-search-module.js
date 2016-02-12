/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FilterSearchCtrl from './filter-search-controller';
import FilterSearch from './filter-search-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-search
     * @description This module contains the controller and directives to manage the filter input with suggestions
     * as the user type in the input.
     * @requires data-prep.services.filter
     */
    angular.module('data-prep.filter-search',
        [
            'MassAutoComplete',
            'pascalprecht.translate',
            'data-prep.services.filter'
        ])
        .controller('FilterSearchCtrl', FilterSearchCtrl)
        .directive('filterSearch', FilterSearch);
})();