/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';

/**
 * @ngdoc service
 * @name data-prep.services.recipe.service:RecipeService
 * @description Recipe service. This service provide the entry point to manipulate properly the recipe
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.parameters.service:ParametersService
 * @requires data-prep.services.transformation.service:TransformationService
 * @requires data-prep.services.filters.service:FilterAdapterService
 */
export default function RecipeService(state, StateService, StepUtilsService, PreparationService, ParametersService, TransformationService, FilterAdapterService) {
	'ngInject';

	return {
		// recipe and steps manipulation
		refresh,

		// append step preview
		earlyPreview,
		cancelEarlyPreview,
	};

	//--------------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------STEP PARAMS-----------------------------------------------
	//--------------------------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name initDynamicParams
	 * @methodOf data-prep.services.recipe.service:RecipeService
	 * @param {object} recipes The old recipe (recipes.old) and the new recipe (recipes.new)
	 * @description Initialize the dynamic parameters of the provided dynamic steps.
	 * If a step exists in the old recipe (same stepId == same parent + same content), we reuse them
	 * Otherwise, we call the backend
	 */
	function initDynamicParams(recipes) {
		function getOldStepById(step) {
			return _.find(recipes.old, function (oldStep) {
				return oldStep.transformation.stepId === step.transformation.stepId;
			});
		}

		function initOnStep(step) {
			const oldStep = getOldStepById(step);
			if (oldStep) {
				step.transformation.parameters = oldStep.transformation.parameters;
				step.transformation.cluster = oldStep.transformation.cluster;
			}
			else {
				const infos = {
					columnId: step.column.id,
					preparationId: state.playground.preparation.id,
					stepId: StepUtilsService.getPreviousStep(state.playground.recipe, step)
						.transformation
						.stepId,
				};
				return TransformationService.initDynamicParameters(step.transformation, infos)
					.then(() => {
						return ParametersService.initParamsValues(step.transformation, step.actionParameters.parameters);
					});
			}
		}

		return _.chain(recipes.new)
			.filter(function (step) {
				return step.transformation.dynamic;
			})
			.forEach(initOnStep)
			.value();
	}

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------STEPS--------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name createItem
	 * @methodOf data-prep.services.recipe.service:RecipeService
	 * @param {object[]} actionStep - the action step array containing [id, actions, metadata]
	 * @description [PRIVATE] Create a recipe item from Preparation step
	 * @returns {object} - the adapted recipe item
	 */
	function createItem(actionStep) {
		const stepId = actionStep[0];
		const actionValues = actionStep[1];
		const metadata = actionStep[2];
		const diff = actionStep[3];

		const item = {
			column: {
				id: actionValues.parameters.column_id,
				name: actionValues.parameters.column_name,
			},
			row: {
				id: actionValues.parameters.row_id,
			},
			transformation: {
				stepId,
				name: actionValues.action,
				label: metadata.label,
				description: metadata.description,
				parameters: metadata.parameters,
				dynamic: metadata.dynamic,
			},
			actionParameters: actionValues,
			diff,
			filters: FilterAdapterService.fromTree(
				actionValues.parameters.filter,
				state.playground.data.metadata.columns
			),
		};

		ParametersService.initParamsValues(item.transformation, actionValues.parameters);

		return item;
	}

	/**
	 * @ngdoc method
	 * @name refresh
	 * @methodOf data-prep.services.recipe.service:RecipeService
	 * @description Refresh recipe items with current preparation steps
	 */
	function refresh(details) {
		// steps ids are in reverse order and the last is the 'no-transformation' id
		const steps = details.steps.slice(0);
		const initialStepId = steps.shift();
		const initialStep = { transformation: { stepId: initialStepId } };

		const oldRecipeSteps = state.playground.recipe.current.steps;
		const newRecipeSteps = _.chain(steps)
			.zip(details.actions, details.metadata, details.diff)
			.map(createItem)
			.value();
		StateService.setRecipeSteps(initialStep, newRecipeSteps);
		StateService.setRecipeAllowDistributedRun(details.allowDistributedRun);
		initDynamicParams({
			old: oldRecipeSteps,
			new: newRecipeSteps,
		});

		return details;
	}

	//--------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------PREVIEW--------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name earlyPreview
	 * @methodOf data-prep.services.recipe.service:RecipeService
	 * @param {object} transformation The transformation
	 * @param {array} params The transformation params
	 * @description Add a preview step in the recipe. The state before preview is saved to be able to revert.
	 */
	function earlyPreview(transformation, params) {
		const previewSteps = state.playground.recipe.beforePreview ?
			state.playground.recipe.beforePreview.steps.slice(0) :
			state.playground.recipe.current.steps.slice(0);

		params.forEach((param, index) => {
			const stepFilters = state.playground.filter.applyTransformationOnFilters ?
				state.playground.filter.gridFilters.slice(0) :
				[];

			// create the preview step
			const previewStep = {
				column: {
					id: param.column_id,
					name: param.column_name,
				},
				row: {
					id: param.row_id,
				},
				transformation: {
					stepId: 'early_preview_' + index,
					name: transformation.name,
					label: transformation.label,
					description: transformation.description,
					parameters: _.cloneDeep(transformation.parameters),
					dynamic: transformation.dynamic,
				},
				actionParameters: {
					action: transformation.name,
					parameters: param,
				},
				preview: true,
				filters: stepFilters,
			};
			ParametersService.initParamsValues(previewStep.transformation, param);
			previewSteps.push(previewStep);
		});
		StateService.setRecipePreviewSteps(previewSteps);
	}

	/**
	 * @ngdoc method
	 * @name cancelEarlyPreview
	 * @methodOf data-prep.services.recipe.service:RecipeService
	 * @description Set back the state before preview and reset preview state
	 */
	function cancelEarlyPreview() {
		StateService.restoreRecipeBeforePreview();
	}
}
