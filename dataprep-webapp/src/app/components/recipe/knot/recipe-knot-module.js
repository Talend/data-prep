/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import angular from 'angular';

import RecipeKnot from './recipe-knot-component';

const MODULE_NAME = 'data-prep.recipe-knot';

/**
 * @ngdoc object
 * @name data-prep.recipe-knot
 * @description This module contains the controller and component to manage the recipe knots
 */

angular.module(MODULE_NAME, [])
    .component('recipeKnot', RecipeKnot);

export default MODULE_NAME;
