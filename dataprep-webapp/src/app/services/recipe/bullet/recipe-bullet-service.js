/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.recipe.service:RecipeBulletService
 * @description Recipe Bullet service. This service provides the action services triggered by bullets
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.playground.service:PlaygroundService
 */
export default function RecipeBulletService($timeout, state, RecipeService, PreviewService, PlaygroundService) {
    'ngInject';

    var previewTimeout;

    var service = {
        toggleStep: toggleStep,
        toggleRecipe: toggleRecipe,

        stepHoverStart: stepHoverStart,
        stepHoverEnd: stepHoverEnd
    };
    return service;

    //---------------------------------------------------------------------------------------------
    //------------------------------------------Mouse Actions--------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggleStep
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @param {object} step The step to toggle
     * @description Toggle selected step and load the last active step content
     * <ul>
     *     <li>step is inactive : activate it with all the previous steps</li>
     *     <li>step is active : deactivate it with all the following steps</li>
     * </ul>
     */
    function toggleStep(step) {
        var stepToLoad = step.inactive ? step : RecipeService.getPreviousStep(step);
        PlaygroundService.loadStep(stepToLoad);
    }

    /**
     * @ngdoc method
     * @name toggleRecipe
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @description Enable/disable the recipe.
     * When it is enabled, the last active step before disabling action is loaded
     */
    function toggleRecipe() {
        var recipe = RecipeService.getRecipe();
        var firstStep = recipe[0];
        var stepToLoad;

        if (!firstStep.inactive) {
            service.lastToggled = RecipeService.getLastActiveStep();
            stepToLoad = firstStep;
        }
        else {
            stepToLoad = service.lastToggled || recipe[recipe.length - 1];
        }
        toggleStep(stepToLoad);
    }

    /**
     * @ngdoc method
     * @name cancelPendingPreview
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @description Cancel the pending preview. If the REST call is pending, this call is canceled too.
     */
    function cancelPendingPreview() {
        $timeout.cancel(previewTimeout);
        PreviewService.stopPendingPreview();
    }

    /**
     * @ngdoc method
     * @name stepHoverStart
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @param {object} step The hovered step
     * @description Cancel pending preview and trigger a new one with a 200ms delay
     */
    function stepHoverStart(step) {
        cancelPendingPreview();
        previewTimeout = $timeout(function () {
            var previewFn = step.inactive ? previewAppend : previewDisable;
            previewFn(step);
        }, 300, false);
    }

    /**
     * @ngdoc method
     * @name stepHoverEnd
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @param {object} step The hovered end step
     * @description Cancel any pending preview and cancel the current preview with a 100ms delay
     */
    function stepHoverEnd(step) {
        cancelPendingPreview();
        previewTimeout = $timeout(PreviewService.cancelPreview.bind(null, false, step.column.id), 100, false);
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------------------------Preview-----------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name previewAppend
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @param {object} previewStep The step to preview
     * @description Call the preview service to display the diff between the current active step and the preview step to activate
     */
    function previewAppend(previewStep) {
        var currentStep = RecipeService.getLastActiveStep();
        var preparationId = state.playground.preparation.id;
        var columnToFocusId = previewStep.column.id;
        PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, columnToFocusId);
    }

    /**
     * @ngdoc method
     * @name previewDisable
     * @methodOf data-prep.services.recipe.service:RecipeBulletService
     * @param {object} disabledStep The step to disable for the preview
     * @description Call the preview service to display the diff between the current active step and the step before the one to deactivate
     */
    function previewDisable(disabledStep) {
        var previewStep = RecipeService.getPreviousStep(disabledStep);
        var currentStep = RecipeService.getLastActiveStep();
        var preparationId = state.playground.preparation.id;
        var columnToFocusId = disabledStep.column.id;
        PreviewService.getPreviewDiffRecords(preparationId, currentStep, previewStep, columnToFocusId);
    }
}