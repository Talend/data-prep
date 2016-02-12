/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import RecipeBulletCtrl from './recipe-bullet-controller';
import RecipeBullet from './recipe-bullet-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.recipe-bullet
     * @description This module contains the controller and directives to manage the recipe bullets
     * @requires data-prep.services.recipe
     * @requires data-prep.services.playground
     */
    angular.module('data-prep.recipe-bullet',
        [
            'data-prep.services.recipe',
            'data-prep.services.playground'
        ])
        .controller('RecipeBulletCtrl', RecipeBulletCtrl)
        .directive('recipeBullet', RecipeBullet);
})();