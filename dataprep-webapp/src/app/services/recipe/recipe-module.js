/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import RecipeBulletService from './bullet/recipe-bullet-service';
import RecipeService from './recipe-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.recipe
     * @description This module contains the services to manipulate the recipe
     * @requires data-prep.services.preparation
     * @requires data-prep.services.transformation
     * @requires data-prep.services.playground
     * @requires data-prep.services.state
     */
    angular.module('data-prep.services.recipe',
        [
            'data-prep.services.preparation',
            'data-prep.services.transformation',
            'data-prep.services.playground',
            'data-prep.services.state',
            'data-prep.services.filter'
        ])
        .service('RecipeBulletService', RecipeBulletService)
        .service('RecipeService', RecipeService);
})();