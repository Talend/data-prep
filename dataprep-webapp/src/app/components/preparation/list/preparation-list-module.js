/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationListComponent from './preparation-list-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.preparation-list
     * @description This module contains the entities to manage the preparation list
     * @requires ui.router
     * @requires talend.widget
     * @requires data-prep.inventory-tile
     * @requires data-prep.services.folder
     * @requires data-prep.services.preparation
     * @requires data-prep.services.playground
     * @requires data-prep.services.state
     */
    angular.module('data-prep.preparation-list',
        [
            'ui.router',
            'talend.widget',
            'data-prep.inventory-tile',
            'data-prep.services.folder',
            'data-prep.services.preparation',
            'data-prep.services.playground',
            'data-prep.services.state',
            'data-prep.inventory-item',
        ])
        .component('preparationList', PreparationListComponent);
})();
