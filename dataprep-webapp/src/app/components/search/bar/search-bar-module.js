/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import SearchBarComponent from './search-bar-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.data-prep.search-bar
     * @description This module contains the component to manage search
     * @requires talend.widget
     */
    angular.module('data-prep.search-bar',
        [
            'talend.widget',
            'data-prep.services.datasetWorkflowService',
            'data-prep.services.preparation'
        ])
        .component('searchBar', SearchBarComponent);
})();

