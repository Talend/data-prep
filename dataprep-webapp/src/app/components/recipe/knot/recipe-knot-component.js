/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './recipe-knot.html';

/**
 * @ngdoc component
 * @name data-prep.recipe-knot.component:recipeKnot
 * @description This component displays the recipe with the step params as accordions.
 *   <recipe-knot class="step-trigger"
 *       inactive="inactive"
 *       on-hover-start="stepHoverStart()"
 *       on-hover-end="stepHoverEnd()"
 *       show-bottom-line="showBottomLine()"
 *       show-top-line="showTopLine()"
 *       to-be-switched="toBeSwitched()"
 *       toggle-step="toggleStep()">
 *   </recipe-knot>
 *
 * @param {boolean}     inactive status of the step
 * @param {function}    onHoverStart callback triggered on knot hover
 * @param {function}    onHoverEnd callback triggered on knot mouseleave
 * @param {boolean}     showBottomLine to show to bottom line
 * @param {boolean}     showTopLine to hos the top line
 * @param {boolean}     toBeSwitched when the knot will change its status
 * @param {function}    toggleStep callback triggered on knot click
 */

const RecipeKnot = {
	bindings: {
		inactive: '<',
		onHoverEnd: '&',
		onHoverStart: '&',
		showBottomLine: '<',
		showTopLine: '<',
		toBeSwitched: '<',
		toggleStep: '&',
	},
	templateUrl: template,
};

export default RecipeKnot;
