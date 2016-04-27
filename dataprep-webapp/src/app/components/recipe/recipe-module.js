/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import RecipeCtrl from './recipe-controller';
import Recipe from './recipe-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.recipe
     * @description This module contains the controller and directives to manage the recipe
     * @requires talend.widget
     * @requires data-prep.recipe-bullet
     * @requires data-prep.services.playground
     * @requires data-prep.services.recipe
     * @requires data-prep.services.preparation
     * @requires data-prep.services.state
     * @requires data-prep.step-description
     * @requires data-prep.transformation-form
     */
    angular.module('data-prep.recipe',
        [
            'pascalprecht.translate',
            'talend.sunchoke',
            'talend.widget',
            'data-prep.recipe-bullet',
            'data-prep.services.playground',
            'data-prep.services.recipe',
            'data-prep.services.preparation',
            'data-prep.services.state',
            'data-prep.step-description',
            'data-prep.transformation-form',
            'data-prep.services.filter',
            'data-prep.services.utils',
        ])
        .controller('RecipeCtrl', RecipeCtrl)
        .directive('recipe', Recipe);
})();
