/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

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
	template: `
    <div class="knot"
        role="checkbox"
        aria-checked="{{ $ctrl.inactive ? $ctrl.toBeSwitched : !$ctrl.toBeSwitched }}"
        aria-disabled="{{ $ctrl.inactive }}"
        aria-labelledby="{{ 'ENABLE_DISABLE_STEP' | translate }}"
        ng-click="$ctrl.toggleStep()"
        ng-mouseover="$ctrl.onHoverStart()"
        ng-mouseleave="$ctrl.onHoverEnd()"
        title="{{ $ctrl.inactive ? 'ENABLE_STEP' : 'DISABLE_STEP' | translate }}">
            <div class="line" ng-class="{'no-line': !$ctrl.showTopLine}"></div>
            <div ng-if="$ctrl.inactive" class="circle inactive-knot" ng-class="{'to-be-activated': $ctrl.toBeSwitched}"></div>
            <div ng-if="!$ctrl.inactive" class="circle" ng-class="{'to-be-deactivated': $ctrl.toBeSwitched}"></div>
            <div class="line bottom-line" ng-class="{'no-line': !$ctrl.showBottomLine}"></div>
    <div>`,
};

export default RecipeKnot;
