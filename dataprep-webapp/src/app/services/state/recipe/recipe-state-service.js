/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';

export const recipeState = {
	visible: false,
	initialStep: null,
	current: {
		steps: [],
		reorderedSteps: [],
		lastActiveStep: null,
	},
	beforePreview: null,
	hoveredStep: null,
	allowDistributedRun: true,
};

export function RecipeStateService() {
	return {
		show,
		hide,
		setHoveredStep,
		setSteps,
		setPreviewSteps,
		restoreBeforePreview,
		disableStepsAfter,
		reset,
		setAllowDistributedRun,
	};

	/**
	 * @ngdoc method
	 * @name show
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @description Show the recipe
	 */
	function show() {
		recipeState.visible = true;
	}

	/**
	 * @ngdoc method
	 * @name hide
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @description Hide the recipe
	 */
	function hide() {
		recipeState.visible = false;
	}

	/**
	 * @ngdoc method
	 * @name setHoveredStep
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @param {object} step The hovered step
	 * @description set the currently hovered step
	 */
	function setHoveredStep(step) {
		recipeState.hoveredStep = step;
	}

	/**
	 * @ngdoc method
	 * @name setSteps
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @param {object} initialStep The initialStep (no step)
	 * @param {array} steps The new recipe steps
	 * @description Set the new recipe steps
	 */
	function setSteps(initialStep, steps) {
		recipeState.initialStep = initialStep;
		recipeState.current = {
			steps,
			reorderedSteps: steps.slice(0),
			lastActiveStep: null,
		};
		recipeState.beforePreview = null;
	}

	/**
	 * @ngdoc method
	 * @name setPreviewSteps
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @param {array} previewSteps The preview recipe steps
	 * @description Set the preview recipe steps
	 */
	function setPreviewSteps(previewSteps) {
		// save current steps
		recipeState.beforePreview = recipeState.beforePreview || recipeState.current;

		recipeState.current = {
			steps: previewSteps,
			reorderedSteps: previewSteps.slice(0),
			lastActiveStep: null,
		};
		disableStepsAfter(null); // enable all steps
	}

	/**
	 * @ngdoc method
	 * @name restoreBeforePreview
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @description Set the preview recipe steps
	 */
	function restoreBeforePreview() {
		if (!recipeState.beforePreview) return;

		recipeState.current = recipeState.beforePreview;
		recipeState.beforePreview = null;
		disableStepsAfter(recipeState.current.lastActiveStep);
	}

	/**
	 * @ngdoc method
	 * @name disableStepsAfter
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @param {object} step The starting step
	 * @description Disable all steps after the provided one
	 */
	function disableStepsAfter(step) {
		let stepFound = step === recipeState.initialStep;
		_.forEach(recipeState.current.steps, (nextStep) => {
			nextStep.inactive = stepFound;
			stepFound = stepFound || nextStep === step;
		});
		recipeState.current.lastActiveStep = step;
	}

	/**
	 * @ngdoc method
	 * @name reset
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @description Reset the recipe state
	 */
	function reset() {
		recipeState.visible = false;
		recipeState.initialStep = null;
		recipeState.current = {
			steps: [],
			reorderedSteps: [],
			lastActiveStep: null,
		};
		recipeState.beforePreview = {
			steps: [],
			reorderedSteps: [],
			lastActiveStep: null,
		};
	}

	/**
	 * @ngdoc method
	 * @name setAllowDistributedRun
	 * @methodOf data-prep.services.state.service:RecipeStateService
	 * @description Set if the recipe could be run in distributed environment
	 * @param allowDistributedRun
	 */
	function setAllowDistributedRun(allowDistributedRun) {
		recipeState.allowDistributedRun = allowDistributedRun;
	}
}
