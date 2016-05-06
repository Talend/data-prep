/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import FilterValueCtrl from './filter-value-controller';
import FilterValueComponent from './filter-value-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-item-value
     * @description This module contains the component to display filter item value
     */
    angular
        .module('data-prep.filter-item-value', [])
        .controller('FilterValueCtrl', FilterValueCtrl)
        .component('filterValue', FilterValueComponent);
})();
