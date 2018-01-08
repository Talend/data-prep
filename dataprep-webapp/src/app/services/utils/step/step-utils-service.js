/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { map } from 'lodash';

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:StepUtilsService
 * @description Step utility service
 */
export default class StepUtilsService {

    /**
     * @ngdoc method
     * @name getCurrentSteps
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @description Return real current recipe steps
     * @returns {object} The current recipe steps
     */
	getCurrentSteps(recipeState) {
		return recipeState.beforePreview || recipeState.current;
	}

    /**
     * @ngdoc method
     * @name getStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {number} index The wanted index
     * @param {boolean} defaultLast Return the last step if no step is identified by the index
     * @description Return a recipe step identified by index
     * @returns {object} The recipe step
     */
	getStep(recipeState, index, defaultLast = false) {
		if (index < 0) {
			return recipeState.initialStep;
		}

		if (index >= this.getCurrentSteps(recipeState).steps.length || index < 0) {
			return defaultLast ? this.getCurrentSteps(recipeState).steps[this.getCurrentSteps(recipeState).steps.length - 1] : null;
		}

		return this.getCurrentSteps(recipeState).steps[index];
	}

    /**
     * @ngdoc method
     * @name getStepBefore
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {number} index The targeted step index
     * @description Return the step just before the provided index
     * @returns {object} The recipe step
     */
	getStepBefore(recipeState, index) {
		if (index <= 0) {
			return recipeState.initialStep;
		}
		else if (index >= this.getCurrentSteps(recipeState).steps.length) {
			return this.getCurrentSteps(recipeState).steps[this.getCurrentSteps(recipeState).steps.length - 1];
		}

		return this.getCurrentSteps(recipeState).steps[index - 1];
	}

    /**
     * @ngdoc method
     * @name getPreviousStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {object} step The given step
     * @description Get the step before the given one
     */
	getPreviousStep(recipeState, step) {
		const index = this.getCurrentSteps(recipeState).steps.indexOf(step);
		return this.getStepBefore(recipeState, index);
	}

    /**
     * @ngdoc method
     * @name getActiveThresholdStepIndex
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @description Get the last active step index
     * @returns {number} The last active step index
     */
	getActiveThresholdStepIndex(recipeState) {
		return this.getCurrentSteps(recipeState).lastActiveStep ?
            this.getCurrentSteps(recipeState).steps.indexOf(this.getCurrentSteps(recipeState).lastActiveStep) :
        this.getCurrentSteps(recipeState).steps.length - 1;
	}

    /**
     * @ngdoc method
     * @name getStepIndex
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {object} step The step
     * @description Get the current clicked step index
     * @returns {number} The current step index
     */
	getStepIndex(recipeState, step) {
		return this.getCurrentSteps(recipeState).steps.indexOf(step);
	}

    /**
     * @ngdoc method
     * @name getLastActiveStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @description Get the last active step (last step if activeThresholdStep var is not set)
     * @param {object} recipeState The recipe state
     * @returns {object} The last active step
     */
	getLastActiveStep(recipeState) {
		return this.getCurrentSteps(recipeState).lastActiveStep || this.getLastStep(recipeState);
	}

    /**
     * @ngdoc method
     * @name isFirstStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {object} step The step to test
     * @description Test if the provided step is the first step of the recipe
     * @returns {boolean} True if the step is the first step
     */
	isFirstStep(recipeState, step) {
		return this.getStepIndex(recipeState, step) === 0;
	}

    /**
     * @ngdoc method
     * @name isLastStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @param {object} step The step to test
     * @description Test if the provided step is the last step of the recipe
     * @returns {boolean} True if the step is the last step
     */
	isLastStep(recipeState, step) {
		return step === this.getLastStep(recipeState);
	}

    /**
     * @ngdoc method
     * @name getLastStep
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @param {object} recipeState The recipe state
     * @description Get the last step of the recipe
     * @returns {object} The last step
     */
	getLastStep(recipeState) {
		return this.getCurrentSteps(recipeState).steps.length > 0 ?
            this.getCurrentSteps(recipeState).steps[this.getCurrentSteps(recipeState).steps.length - 1] :
            recipeState.initialStep;
	}

    /**
     * @ngdoc method
     * @name getAllStepsFrom
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @description Get all steps from provided step to the head
     * @param {object} recipeState The recipe state
     * @param {object} step The starting step
     * @returns {object} The sublist from 'step' to head
     */
	getAllStepsFrom(recipeState, step) {
		const index = this.getStepIndex(recipeState, step);
		return this.getCurrentSteps(recipeState).steps.slice(index);
	}

    /**
     * @ngdoc method
     * @name getAllActionsFrom
     * @methodOf data-prep.services.utils.service:StepUtilsService
     * @description Get all actions from provided step to the head
     * @param {object} recipeState The recipe state
     * @param {object} step The starting step
     * @returns {object} The actions array
     */
	getAllActionsFrom(recipeState, step) {
		const steps = this.getAllStepsFrom(recipeState, step);
		return map(steps, 'actionParameters');
	}
}
