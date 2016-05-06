/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import FilterItemCtrl from './filter-item-controller';
import FilterItemComponent from './filter-item-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.filter-item
     * @description This module contains the component to display filter item
     * @requires pascalprecht.translate
     * @requires data-prep.services.filter
     * @requires talend.widget
     */
    angular
        .module('data-prep.filter-item', [
            'pascalprecht.translate',
            'data-prep.filter-item-value',
            'talend.widget'
        ])
        .controller('FilterItemCtrl', FilterItemCtrl)
        .component('filterItem', FilterItemComponent);
})();
