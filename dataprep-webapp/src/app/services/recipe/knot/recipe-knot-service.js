/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.recipe.service:RecipeKnotService
 * @description Recipe knot service. This service provides the action services triggered by knots
 * @requires data-prep.services.utils.service:StepUtilsService
 * @requires data-prep.services.playground.service:PreviewService
 */
export default function RecipeKnotService($timeout, state, StepUtilsService, PreviewService) {
	'ngInject';

	let previewTimeout;

	return {
		stepHoverStart,
		stepHoverEnd,
	};

    //---------------------------------------------------------------------------------------------
    // ------------------------------------------Mouse Actions--------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name cancelPendingPreview
     * @methodOf data-prep.services.recipe.service:RecipeKnotService
     * @description Cancel the pending preview. If the REST call is pending, this call is canceled too.
     */
	function cancelPendingPreview() {
		$timeout.cancel(previewTimeout);
		PreviewService.stopPendingPreview();
	}

    /**
     * @ngdoc method
     * @name stepHoverStart
     * @methodOf data-prep.services.recipe.service:RecipeKnotService
     * @param {object} step The hovered step
     * @description Cancel pending preview and trigger a new one with a 200ms delay
     */
	function stepHoverStart(step) {
		cancelPendingPreview();
		previewTimeout = $timeout(function () {
			const previewFn = step.inactive ? previewAppend : previewDisable;
			previewFn(step);
		}, 300, false);
	}

    /**
     * @ngdoc method
     * @name stepHoverEnd
     * @methodOf data-prep.services.recipe.service:RecipeKnotService
     * @param {object} step The hovered end step
     * @description Cancel any pending preview and cancel the current preview with a 100ms delay
     */
	function stepHoverEnd(step) {
		cancelPendingPreview();
		previewTimeout = $timeout(PreviewService.cancelPreview.bind(null, false, step.column.id), 100);
	}

    //---------------------------------------------------------------------------------------------
    // ---------------------------------------------Preview-----------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name previewAppend
     * @methodOf data-prep.services.recipe.service:RecipeKnotService
     * @param {object} previewStep The step to preview
     * @description Call the preview service to display the diff between the current active step and the preview step to activate
     */
	function previewAppend(previewStep) {
		const currentStep = StepUtilsService.getLastActiveStep(state.playground.recipe);
		const preparationId = state.playground.preparation.id;
		const columnToFocusId = previewStep.column.id;
		PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, columnToFocusId);
	}

    /**
     * @ngdoc method
     * @name previewDisable
     * @methodOf data-prep.services.recipe.service:RecipeKnotService
     * @param {object} disabledStep The step to disable for the preview
     * @description Call the preview service to display the diff between the current active step and the step before the one to deactivate
     */
	function previewDisable(disabledStep) {
		const previewStep = StepUtilsService.getPreviousStep(state.playground.recipe, disabledStep);
		const currentStep = StepUtilsService.getLastActiveStep(state.playground.recipe);
		const preparationId = state.playground.preparation.id;
		const columnToFocusId = disabledStep.column.id;
		PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, columnToFocusId);
	}
}
