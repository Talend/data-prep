/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationHeaderComponent from './preparation-header-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.preparation-header
     * @description This module contains the entities to manage the preparation list header
     * @requires talend.widget
     * @requires data-prep.inventory-header
     * @requires data-prep.services.folder
     * @requires data-prep.services.state
     */
    angular.module('data-prep.preparation-header',
        [
            'pascalprecht.translate',
            'talend.widget',
            'data-prep.inventory-header',
            'data-prep.services.folder',
            'data-prep.services.state',
        ])
        .component('preparationHeader', PreparationHeaderComponent);
})();
