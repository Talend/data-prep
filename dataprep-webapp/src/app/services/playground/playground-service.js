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
 * @name data-prep.services.playground.service:PlaygroundService
 * @description Playground service. This service provides the entry point to load properly the playground
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.transformation.service:TransformationCacheService
 * @requires data-prep.services.statistics.service:StatisticsService
 * @requires data-prep.services.history.service:HistoryService
 * @requires data-prep.services.onboarding.service:OnboardingService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.utils.service:StepUtilsService
 * @requires data-prep.services.utils.service:StorageService
 */

import { map } from 'lodash';
import {
	PLAYGROUND_PREPARATION_ROUTE,
	HOME_DATASETS_ROUTE,
	HOME_PREPARATIONS_ROUTE,
} from '../../index-route';
// actions scopes
const LINE = 'line';
const DATASET = 'dataset';
// events
export const EVENT_LOADING_START = 'talend.loading.start';
export const EVENT_LOADING_STOP = 'talend.loading.stop';

export default function PlaygroundService(
	$state,
	$rootScope,
	$q,
	$translate,
	$timeout,
	$stateParams,
	state,
	StateService,
	StepUtilsService,
	DatasetService,
	DatagridService,
	StorageService,
	FilterService,
	FilterAdapterService,
	PreparationService,
	PreviewService,
	RecipeService,
	TransformationCacheService,
	ExportService,
	StatisticsService,
	HistoryService,
	OnboardingService,
	MessageService,
	TitleService
) {
	'ngInject';

	const INVENTORY_SUFFIX = ' ' + $translate.instant('PREPARATION');
	let fetchStatsTimeout;
	let currentLoadingItems = 0;

	function wrapInventoryName(invName) {
		return invName + INVENTORY_SUFFIX;
	}

	const service = {
		// events helpers
		startLoader,
		stopLoader,

		// init/load
		loadDataset,        // load dataset
		loadPreparation,    // load preparation
		initDataset,		// init dataset
		initPreparation,	// init preparation
		loadStep,           // load preparation step
		updateStatistics,   // load column statistics and trigger statistics update
		close,
		errorGoBack,

		updateDatasetDatagrid,
		updateDatagrid,

		// preparation
		createOrUpdatePreparation,
		updatePreparationDetails,
		getCurrentPreparation,
		updatePreparationDatagrid,

		// steps
		appendStep,
		updateStep,
		updateStepOrder,
		removeStep,
		copySteps,
		editCell,
		createAppendStepClosure,
		completeParamsAndAppend,
		toggleStep,

		// parameters
		changeDatasetParameters,
	};
	return service;

	/**
	 * Helper to emit start loader event
	 */
	function startLoader() {
		if (currentLoadingItems++ <= 0) {
			$rootScope.$emit(EVENT_LOADING_START);
		}
	}

	/**
	 * Helper to emit stop loader event
	 */
	function stopLoader() {
		if (--currentLoadingItems === 0) {
			$rootScope.$emit(EVENT_LOADING_STOP);
		}
	}

	// --------------------------------------------------------------------------------------------
	// -------------------------------------------INIT/LOAD----------------------------------------
	// --------------------------------------------------------------------------------------------
	function reset(dataset, data, preparation, sampleType = 'HEAD') {
		// reset
		StateService.resetPlayground();
		TransformationCacheService.invalidateCache();
		PreviewService.reset(false);
		HistoryService.clear();

		// init
		StateService.setPreparationName(
			preparation ? preparation.name : dataset.name
		);
		StateService.setCurrentDataset(dataset);
		StateService.setCurrentData(data);
		StateService.setCurrentPreparation(preparation);
		StateService.setCurrentSampleType(sampleType);
		FilterService.initFilters(dataset, preparation);
		updateDatagrid();
		updateGridSelection(dataset, preparation);
		this.updatePreparationDetails().then(() => {
			if (state.playground.recipe.current.steps.length) {
				StateService.showRecipe();
			}
		});

		// preparation specific init
		if (preparation) {
			ExportService.refreshTypes('preparations', preparation.id);
			TitleService.setStrict(preparation.name);
		}

		// dataset specific init
		else {
			StateService.setNameEditionMode(false);
			ExportService.refreshTypes('datasets', dataset.id);
			TitleService.setStrict(dataset.name);
		}
	}

	/**
	 * @ngdoc method
	 * @name updateGridSelection
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} dataset The dataset to update
	 * @param {object} preparation The preparation to update
	 * @description Update grid selection by using localstorage
	 */
	function updateGridSelection(dataset, preparation) {
		const selectedCols = StorageService.getSelectedColumns(
			preparation ? preparation.id : dataset.id
		);
		if (selectedCols.length) {
			StateService.setGridSelection(
				state.playground.grid.columns.filter(
					col => selectedCols.indexOf(col.id) > -1
				)
			);
		}
	}

	/**
	 * @ngdoc method
	 * @name loadDataset
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {string} datasetid The dataset id to load
	 * @description Initiate a new preparation from dataset.
	 * @returns {Promise} The process promise
	 */
	function loadDataset(datasetid) {
		startLoader();
		return DatasetService.getContent(datasetid, true)
			.then(data => checkRecords(data))
			.then(data => reset.call(this, data.metadata, data))
			.then(() => {
				if (OnboardingService.shouldStartTour('playground')) {
					OnboardingService.startTour('playground', 1500);
				}
			})
			.then(() => {
				if (shouldFetchStatistics()) {
					fetchStatistics.call(this);
				}
			})
			.catch(errorGoBack)
			.finally(stopLoader);
	}

	/**
	 * @ngdoc method
	 * @name loadPreparation
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} preparation - the preparation to load
	 * @param {string} sampleType - the sample type
	 * @description Load an existing preparation in the playground :
	 <ul>
	 <li>set name</li>
	 <li>set current preparation before any preparation request</li>
	 <li>load grid with 'head' version content</li>
	 <li>reinit recipe panel with preparation steps</li>
	 </ul>
	 * @returns {Promise} The process promise
	 */
	function loadPreparation(preparation, sampleType = 'HEAD') {
		startLoader();
		return PreparationService.getContent(preparation.id, 'head', sampleType)
			.then(data => reset.call(
				this,
				state.playground.dataset ? state.playground.dataset : { id: preparation.dataSetId },
				data,
				preparation,
				sampleType
			))
			.then(() => {
				if (OnboardingService.shouldStartTour('playground')) {
					OnboardingService.startTour('playground', 1500);
				}
			})
			.then(() => {
				if (shouldFetchStatistics()) {
					fetchStatistics.call(this);
				}
			})
			.catch(errorGoBack)
			.finally(stopLoader);
	}

	/**
	 * @ngdoc method
	 * @name loadStep
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} step The preparation step to load
	 * @description Load a specific step content in the current preparation, and update the recipe
	 * @returns {Promise} The process promise
	 */
	function loadStep(step) {
		startLoader();
		return PreparationService.getContent(
			state.playground.preparation.id,
			step.transformation.stepId,
			state.playground.sampleType
		)
			.then((response) => {
				DatagridService.updateData(response);
				StateService.disableRecipeStepsAfter(step);
				PreviewService.reset(false);
			})
			.finally(stopLoader);
	}

	/**
	 * @ngdoc method
	 * @name getMetadata
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Get the metadata of the current preparation/dataset
	 * and update the statistics in state
	 * @returns {Promise} The process promise
	 */
	function getMetadata() {
		if (state.playground.preparation) {
			return PreparationService.getMetadata(state.playground.preparation.id, 'head')
				.then((response) => {
					if (!response.columns[0].statistics.frequencyTable.length) {
						return $q.reject();
					}
					return response;
				});
		}
		else {
			return DatasetService.getMetadata(state.playground.dataset.id)
				.then((response) => {
					if (!response.columns[0].statistics.frequencyTable.length) {
						return $q.reject();
					}

					StateService.updateDatasetRecord(response.records);
					return response;
				});
		}
	}

	/**
	 * @ngdoc method
	 * @name updateStatistics
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Get fresh statistics, set them in current columns metadata, then trigger a new statistics
	 *     computation
	 * @returns {Promise} The process promise
	 */
	function updateStatistics() {
		return getMetadata()
			.then(StateService.updateDatasetStatistics)
			.then(StatisticsService.updateStatistics);
	}

	// --------------------------------------------------------------------------------------------
	// -------------------------------------------PREPARATION--------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name getCurrentPreparation
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Return the current preparation, wrapped in a promise.
	 * If there is no preparation yet, a new one is created, tagged as draft
	 * @returns {Promise} The process promise
	 */
	function getCurrentPreparation() {
		// create the preparation and taf it draft if it does not exist
		return state.playground.preparation ?
			$q.when(state.playground.preparation) :
			createOrUpdatePreparation(wrapInventoryName(state.playground.dataset.name))
				.then((preparation) => {
					preparation.draft = true;
					return preparation;
				});
	}

	/**
	 * @ngdoc method
	 * @name updatePreparationDetails
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Get preparation details and update recipe
	 */
	function updatePreparationDetails() {
		if (!state.playground.preparation) {
			return $q.when();
		}

		return PreparationService.getDetails(state.playground.preparation.id)
			.then((preparation) => {
				RecipeService.refresh(preparation);

				if (!state.playground.isReadOnly &&
					OnboardingService.shouldStartTour('recipe') &&
					state.playground.recipe.current.steps.length >= 3) {
					StateService.showRecipe();
					$timeout(OnboardingService.startTour('recipe'), 300, false);
				}
				return preparation;
			});
	}

	/**
	 * @ngdoc method
	 * @name createOrUpdatePreparation
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {string} name The preparation name to create or update
	 * @description Create a new preparation or change its name if it already exists. It adds a new entry in history
	 * @returns {Promise} The process promise
	 */
	function createOrUpdatePreparation(name) {
		const oldPreparation = state.playground.preparation;
		let promise = performCreateOrUpdatePreparation(name);

		if (oldPreparation) {
			const oldName = oldPreparation.name;
			promise = promise.then((preparation) => {
				const undo = performCreateOrUpdatePreparation.bind(
					service,
					oldName
				);
				const redo = performCreateOrUpdatePreparation.bind(
					service,
					name
				);
				HistoryService.addAction(undo, redo);
				return preparation;
			});
		}

		return promise;
	}

	/**
	 * @ngdoc method
	 * @name performCreateOrUpdatePreparation
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {string} name The preparation name to create or update
	 * @description Create a new preparation or change its name if it already exists.
	 * @returns {Promise} The process promise
	 */
	function performCreateOrUpdatePreparation(name) {
		let promise;
		if (state.playground.preparation) {
			promise = PreparationService.setName(state.playground.preparation.id, name);
		}
		else {
			promise = PreparationService.create(state.playground.dataset.id, name, state.inventory.homeFolder.id)
				.then((prepid) => {
					$state.go(PLAYGROUND_PREPARATION_ROUTE, { prepid });
					return PreparationService.getDetails(prepid);
				});
		}
		promise.then((preparation) => {
			StateService.setCurrentPreparation(preparation);
			StateService.setPreparationName(preparation.name);
			TitleService.setStrict(preparation.name);
			return preparation;
		});
		return promise;
	}

	/**
	 * @ngdoc method
	 * @name setPreparationHead
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {string} preparationId The preparation id
	 * @param {string} headId The head id to set
	 * @param {string} columnToFocus The column id to focus
	 * @description Move the preparation head to the specified step
	 * @returns {promise} The process promise
	 */
	function setPreparationHead(preparationId, headId, columnToFocus) {
		startLoader();

		let promise = PreparationService.setHead(preparationId, headId);

		// load a specific step, we must load recipe first to get the step id to load. Then we load grid at this step.
		if (StepUtilsService.getLastActiveStep(state.playground.recipe) !== StepUtilsService.getLastStep(state.playground.recipe)) {
			const lastActiveStepIndex = StepUtilsService.getActiveThresholdStepIndex(state.playground.recipe);
			promise = promise
				.then(() => this.updatePreparationDetails())
				// The grid update cannot be done in parallel because the update change the steps ids
				// We have to wait for the recipe update to complete
				.then(() => {
					const activeStep = StepUtilsService.getStep(
						state.playground.recipe,
						lastActiveStepIndex,
						true
					);
					return loadStep(activeStep);
				});
		}
		// load the recipe and grid head in parallel
		else {
			promise = promise.then(() => {
				return $q.all([this.updatePreparationDetails(), updatePreparationDatagrid(columnToFocus)]);
			});
		}

		return promise.finally(stopLoader);
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------STEPS------------------------------------------
	// --------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name appendStep
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {array} actions The actions
	 * @description Call an execution of a transformation on the columns in the current preparation and add an entry
	 * in actions history. It there is no preparation yet, it is created first and tagged as draft.
	 */
	function appendStep(actions) {
		startLoader();
		const actualSteps = state.playground.recipe.current.steps.slice();
		const previousHead = StepUtilsService.getLastStep(state.playground.recipe);

		return getCurrentPreparation()
		// append step
			.then((preparation) => {
				return PreparationService.appendStep(preparation.id, actions);
			})
			// update recipe and datagrid
			.then(() => {
				return $q.all([this.updatePreparationDetails(), updatePreparationDatagrid()]);
			})
			// add entry in history for undo/redo
			.then(() => {
				const actualHead = StepUtilsService.getLastStep(state.playground.recipe);
				const previousStepId = previousHead && previousHead.transformation ? previousHead.transformation.stepId : state.playground.recipe.initialStep.transformation.stepId;
				const undo = setPreparationHead.bind(service, state.playground.preparation.id, previousStepId);
				const redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead.transformation.stepId, actions[0] && actions[0].parameters.column_id);
				HistoryService.addAction(undo, redo);
			})
			// hide loading screen
			.finally(() => {
				stopLoader();
				const actualStepsLength = actualSteps.length;
				if (!actualStepsLength || (actualStepsLength === 1 && actualSteps[0].preview)) {
					StateService.showRecipe();
				}
			});
	}

	/**
	 * @ngdoc method
	 * @name updateStep
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} step The step to update
	 * @param {object} newParams The new parameters
	 * @description Call an execution of a transformation update on the provided step and add an entry in the
	 * actions history
	 */
	function updateStep(step, newParams) {
		PreviewService.cancelPreview();
		PreparationService.copyImplicitParameters(
			newParams,
			step.actionParameters.parameters
		);

		if (!PreparationService.paramsHasChanged(step, newParams)) {
			return $q.when();
		}

		startLoader();

		// save the head before transformation for undo
		const previousHead = StepUtilsService.getLastStep(
			state.playground.recipe
		).transformation.stepId;
		// save the last active step index to load this step after update
		const lastActiveStepIndex = StepUtilsService.getActiveThresholdStepIndex(
			state.playground.recipe
		);

		return (
			PreparationService.updateStep(
				state.playground.preparation.id,
				step,
				newParams
			)
				.then(() => this.updatePreparationDetails())
				// get step id to load and update datagrid with it
				.then(() => {
					const activeStep = StepUtilsService.getStep(
						state.playground.recipe,
						lastActiveStepIndex,
						true
					);
					return loadStep(activeStep);
				})
				// add entry in history for undo/redo
				.then(() => {
					const actualHead = StepUtilsService.getLastStep(
						state.playground.recipe
					).transformation.stepId;
					const undo = setPreparationHead.bind(
						service,
						state.playground.preparation.id,
						previousHead
					);
					const redo = setPreparationHead.bind(
						service,
						state.playground.preparation.id,
						actualHead,
						newParams.column_id
					);
					HistoryService.addAction(undo, redo);
				})
				// hide loading screen
				.finally(stopLoader)
		);
	}

	/**
	 * @ngdoc method
	 * @name updateStepOrder
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {number} previousPosition Previous recipe step position
	 * @param {number} nextPosition New recipe step position
	 * @description Update step order
	 */
	function updateStepOrder(previousPosition, nextPosition) {
		if (previousPosition === nextPosition) {
			return;
		}

		const recipe = state.playground.recipe;

		if (nextPosition < 0 || nextPosition === StepUtilsService.getCurrentSteps(recipe).steps.length) {
			return;
		}

		startLoader();

		// If move up or move down buttons, list is not yet updated
		const currentStep = StepUtilsService.getStep(recipe, previousPosition);

		// Step list has not yet change in fact so
		let nextParentStep;
		// if we want to move step up the next parent is the step at the next position - 1
		if (previousPosition > nextPosition) {
			const parentStepIndex = nextPosition - 1;
			nextParentStep = StepUtilsService.getStep(recipe, parentStepIndex);
		}
		// if we want to move step down the next parent is the step at the next position
		else {
			nextParentStep = StepUtilsService.getStep(recipe, nextPosition);
		}

		const preparationId = state.playground.preparation.id;
		const currentStepId = currentStep.transformation.stepId;
		const nextParentStepId = nextParentStep.transformation.stepId;

		// Save the head before transformation for undo
		// If list is not updated yet or moved step is not the last one
		const previousHead = StepUtilsService.getLastStep(recipe).transformation
			.stepId;

		return (
			PreparationService.moveStep(
				preparationId,
				currentStepId,
				nextParentStepId
			)
				// update recipe and datagrid
				.then(() => {
					return $q.all([
						this.updatePreparationDetails(),
						updatePreparationDatagrid(),
					]);
				})
				// add entry in history for undo/redo
				.then(() => {
					const actualHead = StepUtilsService.getLastStep(recipe)
						.transformation.stepId;
					const undo = setPreparationHead.bind(
						service,
						preparationId,
						previousHead,
						currentStep.actionParameters.parameters.column_id
					);
					const redo = setPreparationHead.bind(
						service,
						preparationId,
						actualHead
					);
					HistoryService.addAction(undo, redo);
				})
				// hide loading screen
				.finally(stopLoader)
		);
	}

	/**
	 * @ngdoc method
	 * @name removeStep
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} step The step to delete
	 * @description Call an execution of a transformation on the column in the current preparation and add an entry
	 * in actions history
	 */
	function removeStep(step) {
		startLoader();

		// save the head before transformation for undo
		const previousHead = StepUtilsService.getLastStep(
			state.playground.recipe
		).transformation.stepId;

		return (
			PreparationService.removeStep(
				state.playground.preparation.id,
				step.transformation.stepId
			)
				// update recipe and datagrid
				.then(() => {
					return $q.all([
						this.updatePreparationDetails(),
						updatePreparationDatagrid(),
					]);
				})
				// add entry in history for undo/redo
				.then(() => {
					const actualHead = StepUtilsService.getLastStep(
						state.playground.recipe
					).transformation.stepId;
					const undo = setPreparationHead.bind(
						service,
						state.playground.preparation.id,
						previousHead,
						step.actionParameters.parameters.column_id
					);
					const redo = setPreparationHead.bind(
						service,
						state.playground.preparation.id,
						actualHead
					);
					HistoryService.addAction(undo, redo);
				})
				// hide loading screen
				.finally(stopLoader)
		);
	}

	/**
	 * @ngdoc method
	 * @name copySteps
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {String} referenceId Preparation Id containing steps to copy
	 * @description Copy preparation steps and apply them on the current preparation
	 * @returns {Promise} The process promise
	 */
	function copySteps(referenceId) {
		startLoader();

		return getCurrentPreparation()
			.then(preparation =>
				PreparationService.copySteps(preparation.id, referenceId)
			)
			.then(() =>
				$q.all([
					this.updatePreparationDetails(),
					updatePreparationDatagrid(),
				])
			)
			.finally(stopLoader);
	}

	/**
	 * @ngdoc method
	 * @name appendClosure
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Transformation application closure.
	 * It take the transformation to build the closure.
	 * The closure then takes the parameters and append the new step in the current preparation
	 */
	function createAppendStepClosure(action, scope) {
		return (params = params || {}) => {
			let actions = [];
			const line = state.playground.grid.selectedLine;
			let stepParameters = { ...params };
			switch (scope) {
			case DATASET:
				stepParameters.scope = scope;
				if (state.playground.filter.applyTransformationOnFilters) {
					const stepFilters = FilterAdapterService.toTree(
						state.playground.filter.gridFilters
					);
					stepParameters = { ...stepParameters, ...stepFilters };
				}
				actions = [
					{ action: action.name, parameters: stepParameters },
				];
				break;
			case LINE:
				stepParameters.scope = scope;
				stepParameters.row_id = line && line.tdpId;

				if (state.playground.filter.applyTransformationOnFilters) {
					const stepFilters = FilterAdapterService.toTree(
							state.playground.filter.gridFilters
						);
					stepParameters = { ...stepParameters, ...stepFilters };
				}
				actions = [
						{ action: action.name, parameters: stepParameters },
				];
				break;
			default:
				actions = map(
						state.playground.grid.selectedColumns,
						(column) => {
							let parameters = { ...params };
							parameters.scope = scope;
							parameters.column_id = column && column.id;
							parameters.column_name = column && column.name;
							parameters.row_id = line && line.tdpId;

							if (
								state.playground.filter
									.applyTransformationOnFilters
							) {
								const stepFilters = FilterAdapterService.toTree(
									state.playground.filter.gridFilters
								);
								parameters = { ...parameters, ...stepFilters };
							}
							return { action: action.name, parameters };
						}
					);
				break;
			}
			return service.appendStep(actions);
		};
	}

	/**
	 * @ngdoc method
	 * @name completeParamsAndAppend
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Transformation application.
	 * It take the transformation to build the closure.
	 * The closure then takes the parameters and append the new step in the current preparation
	 */
	function completeParamsAndAppend(action, scope, params) {
		return service.createAppendStepClosure(action, scope)(params);
	}

	/**
	 * @ngdoc method
	 * @name editCell
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {Object} rowItem The row
	 * @param {object} column The column where to execute the transformation
	 * @param {string} newValue The new value to put on th target
	 * @param {boolean} updateAllCellWithValue Indicates the scope (cell or column)
	 * of the transformation
	 * @description Perform a cell or a column edition
	 */
	function editCell(rowItem, column, newValue, updateAllCellWithValue) {
		let action;
		let scope;
		let params;

		if (updateAllCellWithValue) {
			action = { name: 'replace_on_value' };
			scope = 'column';
			params = {
				cell_value: {
					token: rowItem[column.id],
					operator: 'equals',
				},
				replace_value: newValue,
			};
		}
		else {
			action = { name: 'replace_cell_value' };
			scope = 'cell';
			params = {
				original_value: rowItem[column.id],
				new_value: newValue,
			};
		}

		return service.completeParamsAndAppend(action, scope, params);
	}

	/**
	 * @ngdoc method
	 * @name toggleStep
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} step The step to toggle
	 * @description Toggle selected step and load the last active step content
	 * <ul>
	 *     <li>step is inactive : activate it with all the previous steps</li>
	 *     <li>step is active : deactivate it with all the following steps</li>
	 * </ul>
	 */
	function toggleStep(step) {
		const stepToLoad = step.inactive
			? step
			: StepUtilsService.getPreviousStep(state.playground.recipe, step);
		service.loadStep(stepToLoad);
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------PARAMETERS-------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name changeDatasetParameters
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @param {object} params The new dataset parameters
	 * @description Update the parameters of the dataset and reload
	 */
	function changeDatasetParameters(params) {
		const dataset = state.playground.dataset;
		const isPreparation = state.playground.preparation;
		const lastActiveStepIndex = isPreparation
			? StepUtilsService.getActiveThresholdStepIndex(
					state.playground.recipe
				)
			: null;
		return DatasetService.updateParameters(dataset, params).then(() => {
			if (isPreparation) {
				const activeStep = StepUtilsService.getStep(
					state.playground.recipe,
					lastActiveStepIndex,
					true
				);
				return loadStep(activeStep);
			}
			else {
				this.loadDataset.call(this, dataset.id);
			}
		});
	}

	// --------------------------------------------------------------------------------------------
	// ------------------------------------------UTILS---------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name updatePreparationDatagrid
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Perform an datagrid refresh with the preparation head
	 */
	function updatePreparationDatagrid() {
		return PreparationService.getContent(
			state.playground.preparation.id,
			'head',
			state.playground.sampleType
		).then((response) => {
			DatagridService.updateData(response);
			PreviewService.reset(false);
		});
	}

	function updateDatasetDatagrid(tql) {
		const { dataset } = state.playground;
		if (!dataset) {
			return;
		}
		return DatasetService.getContent(
			dataset.id,
			true,
			tql
		).then((response) => {
			DatagridService.updateData(response);
			PreviewService.reset(false);
		});
	}

	function updateDatagrid() {
		const { filter, preparation } = state.playground;
		if (preparation && preparation.id) {
			return updatePreparationDatagrid();
		}
		const tql =
			filter.enabled &&
			filter.isTQL &&
			FilterService.stringify(filter.gridFilters);
		return updateDatasetDatagrid(tql);
	}

	// TODO : temporary fix because asked to.
	// TODO : when error status during import and get dataset content is managed by backend,
	// TODO : remove this controle and the 'data-prep.services.utils'/MessageService dependency
	function checkRecords(data) {
		if (!data || !data.records) {
			MessageService.error('INVALID_DATASET_TITLE', 'INVALID_DATASET');
			throw Error('Empty data');
		}

		return data;
	}

	//------------------------------------------------------------------------------------------------------
	// ----------------------------------------------STATS REFRESH-------------------------------------------
	//------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name shouldFetchStatistics
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Check if we have the statistics or we have to fetch them
	 */
	function shouldFetchStatistics() {
		const columns = state.playground.data.metadata.columns;

		return (
			!columns ||
			!columns.length || // no columns
			!columns[0].statistics.frequencyTable.length
		); // no frequency table implies no async stats computed
	}

	/**
	 * @ngdoc method
	 * @name fetchStatistics
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Fetch the statistics. If the update fails (no statistics yet) a retry is triggered after 1s
	 */
	function fetchStatistics() {
		StateService.setIsFetchingStats(true);
		this.updateStatistics()
			.then(() => StateService.setIsFetchingStats(false))
			.catch(() => {
				fetchStatsTimeout = $timeout(
					() => fetchStatistics.call(this),
					1500,
					false
				);
			});
	}

	//--------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------INIT----------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name errorGoBack
	 * @description go back to homePage when errors occur
	 */
	function errorGoBack(err) {
		const { status } = err;
		if ([403, 404].includes(status)) {
			return;
		}
		$state.go(state.route.previous, state.route.previousOptions);
	}

	/**
	 * @ngdoc method
	 * @name initPreparation
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description open a preparation
	 */
	function initPreparation(prepid) {
		StateService.setPreviousRoute(HOME_PREPARATIONS_ROUTE, {
			folderId: state.inventory.folder.metadata.id,
		});
		if (!shouldReloadPreparation()) {
			return;
		}

		StateService.setIsLoadingPlayground(true);
		startLoader();
		PreparationService.getDetails(prepid)
			.then((preparation) => {
				this.loadPreparation.call(this, preparation);
				return preparation;
			})
			.then(preparation =>
				DatasetService.getMetadata(preparation.dataSetId)
			)
			.then(dataset => StateService.setCurrentDataset(dataset))
			.catch(errorGoBack)
			.finally(stopLoader);
	}

	/**
	 * @ngdoc method
	 * @name initDataset
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description open a dataset
	 */
	function initDataset(datasetid) {
		StateService.setPreviousRoute(HOME_DATASETS_ROUTE);
		StateService.setIsLoadingPlayground(true);
		this.loadDataset.call(this, datasetid);
	}

	/**
	 * @ngdoc method
	 * @name shouldReloadPreparation
	 * @description Check if the preparation should be reloaded.
	 * The preparation is not reloaded if (and) :
	 * - the current playground preparation is the one we want
	 * - the route param "reload" is not set explicitly to false
	 */
	function shouldReloadPreparation() {
		const currentPrep = state.playground.preparation;
		if (!currentPrep || $stateParams.prepid !== currentPrep.id) {
			return true;
		}

		return $stateParams.reload !== false;
	}

	/**
	 * @ngdoc method
	 * @name close
	 * @methodOf data-prep.services.playground.service:PlaygroundService
	 * @description Playground close callback. It reset the playground and redirect to the previous page
	 */
	function close() {
		$timeout.cancel(fetchStatsTimeout);
		$timeout(StateService.resetPlayground, 500, false);
		$state.go(state.route.previous, state.route.previousOptions);
	}
}
