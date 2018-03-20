/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import templateUrl from './recipe.html';
import RecipeCtrl from './recipe-controller';

/**
 * @ngdoc component
 * @name data-prep.recipe.component:Recipe
 * @description This component displays the recipe with the step params as accordions.
 * @usage <recipe></recipe>
 */
const Recipe = {
	controller: RecipeCtrl,
	templateUrl,
};

export default Recipe;
