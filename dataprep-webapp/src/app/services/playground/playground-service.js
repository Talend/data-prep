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
 */

import { map } from 'lodash';

// actions scopes
const LINE = 'line';

// events
const EVENT_LOADING_START = 'talend.loading.start';
const EVENT_LOADING_STOP = 'talend.loading.stop';

export default function PlaygroundService($state, $rootScope, $q, $translate, $timeout, $stateParams,
                                          state, StateService, StepUtilsService,
                                          DatasetService, DatagridService,
                                          FilterAdapterService, PreparationService, PreviewService,
                                          RecipeService, TransformationCacheService,
                                          StatisticsService, HistoryService,
                                          OnboardingService, MessageService) {
    'ngInject';

    const INVENTORY_SUFFIX = ' ' + $translate.instant('PREPARATION');

    function wrapInventoryName(invName) {
        return invName + INVENTORY_SUFFIX;
    }

    const service = {
        // init/load
        initPlayground,     // load dataset
        load,                         // load preparation
        loadStep,                 // load preparation step
        updateStatistics, // load column statistics and trigger statistics update

        // preparation
        createOrUpdatePreparation,
        updatePreparationDetails,

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
        toggleRecipe,

        // parameters
        changeDatasetParameters,
    };
    return service;

    // --------------------------------------------------------------------------------------------
    // -------------------------------------------INIT/LOAD----------------------------------------
    // --------------------------------------------------------------------------------------------
    function reset(dataset, data, preparation) {
        StateService.resetPlayground();
        StateService.setCurrentDataset(dataset);
        StateService.setCurrentData(data);
        StateService.setCurrentPreparation(preparation);
        this.updatePreparationDetails();
        TransformationCacheService.invalidateCache();
        HistoryService.clear();
        PreviewService.reset(false);
    }

    /**
     * @ngdoc method
     * @name initPlayground
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @param {object} dataset The dataset to load
     * @description Initiate a new preparation from dataset.
     * @returns {Promise} The process promise
     */
    function initPlayground(dataset) {
        $rootScope.$emit(EVENT_LOADING_START);
        return DatasetService.getContent(dataset.id, true)
            .then((data) => checkRecords(data))
            .then((data) => {
                StateService.setPreparationName(dataset.name);
                reset.call(this, dataset, data);
                StateService.hideRecipe();
                StateService.setNameEditionMode(true);
            })
            .then(() => {
                if (OnboardingService.shouldStartTour('playground')) {
                    $timeout(OnboardingService.startTour('playground'), 300, false);
                }
            })
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
    }

    /**
     * @ngdoc method
     * @name load
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @param {object} preparation - the preparation to load
     * @description Load an existing preparation in the playground :
     <ul>
     <li>set name</li>
     <li>set current preparation before any preparation request</li>
     <li>load grid with 'head' version content</li>
     <li>reinit recipe panel with preparation steps</li>
     </ul>
     * @returns {Promise} The process promise
     */
    function load(preparation) {
        $rootScope.$emit(EVENT_LOADING_START);
        return PreparationService.getContent(preparation.id, 'head')
            .then((response) => {
                StateService.setPreparationName(preparation.name);
                reset.call(this, state.playground.dataset ? state.playground.dataset : { id: preparation.dataSetId }, response, preparation);
                StateService.showRecipe();
                StateService.setNameEditionMode(false);
                return response;
            })
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
        $rootScope.$emit(EVENT_LOADING_START);
        return PreparationService.getContent(state.playground.preparation.id, step.transformation.stepId)
            .then((response) => {
                DatagridService.updateData(response);
                StateService.disableRecipeStepsAfter(step);
                PreviewService.reset(false);
            })
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
            return PreparationService.getContent(state.playground.preparation.id, 'head')
                .then((response) => {
                    if (!response.metadata.columns[0].statistics.frequencyTable.length) {
                        return $q.reject();
                    }

                    StateService.updateDatasetRecord(response.records.length);
                    return response.metadata;
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
            .then((resp) => {
                const isRecipeEmpty = !state.playground.recipe.current.steps.length;
                RecipeService.refresh(resp);
                if (isRecipeEmpty && state.playground.recipe.current.steps.length) {
                    StateService.showRecipe();
                    $state.go('playground.preparation', { prepid: state.playground.preparation.id });
                }

                if (OnboardingService.shouldStartTour('recipe') &&
                    state.playground.recipe.current.steps.length >= 3) {
                    StateService.showRecipe();
                    $timeout(OnboardingService.startTour('recipe'), 300, false);
                }
                return resp;
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
                const undo = performCreateOrUpdatePreparation.bind(service, oldName);
                const redo = performCreateOrUpdatePreparation.bind(service, name);
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
        const promise = state.playground.preparation ?
            PreparationService.setName(state.playground.preparation.id, name) :
            PreparationService.create(state.playground.dataset.id, name, state.inventory.homeFolderId);

        return promise
            .then((preparation) => {
                StateService.setCurrentPreparation(preparation);
                StateService.setPreparationName(preparation.name);
                return preparation;
            })
            .then((preparation) => {
                $state.go('playground.preparation', { prepid: state.playground.preparation.id });
                return preparation;
            });
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
        $rootScope.$emit(EVENT_LOADING_START);

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

        return promise.finally(() => {
            $rootScope.$emit(EVENT_LOADING_STOP);
        });
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
        /* jshint camelcase: false */
        $rootScope.$emit(EVENT_LOADING_START);
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
                $rootScope.$emit(EVENT_LOADING_STOP);
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
        PreparationService.copyImplicitParameters(newParams, step.actionParameters.parameters);

        if (!PreparationService.paramsHasChanged(step, newParams)) {
            return $q.when();
        }

        /* jshint camelcase: false */
        $rootScope.$emit(EVENT_LOADING_START);

        // save the head before transformation for undo
        const previousHead = StepUtilsService.getLastStep(state.playground.recipe).transformation.stepId;
        // save the last active step index to load this step after update
        const lastActiveStepIndex = StepUtilsService.getActiveThresholdStepIndex(state.playground.recipe);

        return PreparationService.updateStep(state.playground.preparation.id, step, newParams)
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
                const actualHead = StepUtilsService.getLastStep(state.playground.recipe).transformation.stepId;
                const undo = setPreparationHead.bind(service, state.playground.preparation.id, previousHead);
                const redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead, newParams.column_id);
                HistoryService.addAction(undo, redo);
            })
            // hide loading screen
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
        /* jshint camelcase: false */
        $rootScope.$emit(EVENT_LOADING_START);

        const recipe = state.playground.recipe;
        const preparationId = state.playground.preparation.id;
        const steps = recipe.current.steps;

        // save the head before transformation for undo
        const previousHead = StepUtilsService.getLastStep(recipe).transformation.stepId;

        const initialStep = recipe.initialStep;
        const currentStep = steps[previousPosition];
        const stepId = currentStep.transformation.stepId;

        let parentStep;
        if (previousPosition > nextPosition) {
            const parentStepIndex = nextPosition - 1;
            parentStep = steps[parentStepIndex];
        }
        else {
            parentStep = steps[nextPosition];
        }

        let nextParentId;
        if (parentStep) {
            nextParentId = parentStep.transformation.stepId;
        }
        else {
            nextParentId = initialStep.transformation.stepId;
        }

        return PreparationService.moveStep(preparationId, stepId, nextParentId)
            // update recipe and datagrid
            .then(() => {
                return $q.all([this.updatePreparationDetails(), updatePreparationDatagrid()]);
            })
            // add entry in history for undo/redo
            .then(() => {
                const actualHead = StepUtilsService.getLastStep(state.playground.recipe).transformation.stepId;
                const undo = setPreparationHead.bind(service, preparationId, previousHead, currentStep.actionParameters.parameters.column_id);
                const redo = setPreparationHead.bind(service, preparationId, actualHead);
                HistoryService.addAction(undo, redo);
            })
            // hide loading screen
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
        /* jshint camelcase: false */
        $rootScope.$emit(EVENT_LOADING_START);

        // save the head before transformation for undo
        const previousHead = StepUtilsService.getLastStep(state.playground.recipe).transformation.stepId;

        return PreparationService.removeStep(state.playground.preparation.id, step.transformation.stepId)
            // update recipe and datagrid
            .then(() => {
                return $q.all([this.updatePreparationDetails(), updatePreparationDatagrid()]);
            })
            // add entry in history for undo/redo
            .then(() => {
                const actualHead = StepUtilsService.getLastStep(state.playground.recipe).transformation.stepId;
                const undo = setPreparationHead.bind(service, state.playground.preparation.id, previousHead, step.actionParameters.parameters.column_id);
                const redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead);
                HistoryService.addAction(undo, redo);
            })
            // hide loading screen
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
        $rootScope.$emit(EVENT_LOADING_START);

        return getCurrentPreparation()
            .then((preparation) => PreparationService.copySteps(preparation.id, referenceId))
            .then(() => $q.all([this.updatePreparationDetails(), updatePreparationDatagrid()]))
            .finally(() => {
                $rootScope.$emit(EVENT_LOADING_STOP);
            });
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
            let lineParameters = { ...params };
            switch (scope) {
            case LINE:
                lineParameters.scope = scope;
                lineParameters.row_id = line && line.tdpId;

                if (state.playground.filter.applyTransformationOnFilters) {
                    const stepFilters = FilterAdapterService.toTree(state.playground.filter.gridFilters);
                    lineParameters = { ...lineParameters, ...stepFilters };
                }
                actions = [{ action: action.name, parameters: lineParameters }];
                break;
            default:
                actions = map(state.playground.grid.selectedColumns, (column) => {
                    let parameters = { ...params };
                    parameters.scope = scope;
                    parameters.column_id = column && column.id;
                    parameters.column_name = column && column.name;
                    parameters.row_id = line && line.tdpId;

                    if (state.playground.filter.applyTransformationOnFilters) {
                        const stepFilters = FilterAdapterService.toTree(state.playground.filter.gridFilters);
                        parameters = { ...parameters, ...stepFilters };
                    }
                    return { action: action.name, parameters };
                });
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
        const action = { name: 'replace_on_value' };
        const scope = updateAllCellWithValue ? 'column' : 'cell';
        const params = {
            cell_value: {
                token: rowItem[column.id],
                operator: 'equals',
            },
            replace_value: newValue,
        };

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
        const stepToLoad = step.inactive ?
            step :
            StepUtilsService.getPreviousStep(state.playground.recipe, step);
        service.loadStep(stepToLoad);
    }

    /**
     * @ngdoc method
     * @name toggleRecipe
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @description Enable/disable the recipe.
     * When it is enabled, the last active step before disabling action is loaded
     */
    function toggleRecipe() {
        const steps = state.playground.recipe.current.steps;
        const firstStep = steps[0];
        let stepToLoad;

        if (!firstStep.inactive) {
            service.lastToggled = StepUtilsService.getLastActiveStep(state.playground.recipe);
            stepToLoad = firstStep;
        }
        else {
            stepToLoad = service.lastToggled || steps[steps.length - 1];
        }

        service.toggleStep(stepToLoad);
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
        const lastActiveStepIndex = isPreparation ?
            StepUtilsService.getActiveThresholdStepIndex(state.playground.recipe) :
            null;
        return DatasetService.updateParameters(dataset, params)
            .then(() => {
                if (isPreparation) {
                    const activeStep = StepUtilsService.getStep(
                        state.playground.recipe,
                        lastActiveStepIndex,
                        true
                    );
                    return loadStep(activeStep);
                }
                else {
                    initPlayground.call(this, dataset);
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
        return PreparationService.getContent(state.playground.preparation.id, 'head')
            .then((response) => {
                DatagridService.updateData(response);
                PreviewService.reset(false);
            });
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
}
