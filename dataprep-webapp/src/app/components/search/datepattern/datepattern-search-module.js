/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatepatternSearch from './datepattern-search-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.data-prep.datepattern-search
     * @description This module contains the component to manage an datepattern search
     * @requires talend.widget
     */
    angular.module('data-prep.datepattern-search',
        [
            'talend.widget',  
            'data-prep.search-bar',
            'data-prep.services.datepattern'
        ])
        .component('datepatternSearch', DatepatternSearch);
})();

