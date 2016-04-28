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
 * @requires data-prep.services.export.service:ExportService
 */
export default function PlaygroundService($state, $rootScope, $q, $translate, $timeout,
                                          state, StateService,
                                          DatasetService, DatagridService,
                                          PreparationService, PreviewService,
                                          RecipeService, TransformationCacheService,
                                          StatisticsService, HistoryService,
                                          OnboardingService, MessageService, ExportService) {
    'ngInject';

    const INVENTORY_SUFFIX = ' ' + $translate.instant('PREPARATION');

    function _wrapInventoryName(invName) {
        return invName + INVENTORY_SUFFIX;
    }

    var service = {
        //init/load
        initPlayground: initPlayground,     // load dataset
        load: load,                         // load preparation
        loadStep: loadStep,                 // load preparation step
        updateStatistics: updateStatistics, // load column statistics and trigger statistics update

        //preparation
        createOrUpdatePreparation: createOrUpdatePreparation,
        appendStep: appendStep,
        updateStep: updateStep,
        removeStep: removeStep,
        copySteps: copySteps,

        //Preview
        updatePreview: updatePreview,

        //parameters
        changeDatasetParameters: changeDatasetParameters
    };
    return service;

    //------------------------------------------------------------------------------------------------------
    //-------------------------------------------------INIT/LOAD--------------------------------------------
    //------------------------------------------------------------------------------------------------------
    function reset(dataset, data, preparation) {
        StateService.resetPlayground();
        StateService.setCurrentDataset(dataset);
        StateService.setCurrentData(data);
        StateService.setCurrentPreparation(preparation);

        RecipeService.refresh();
        TransformationCacheService.invalidateCache();
        HistoryService.clear();
        PreviewService.reset(false);
        ExportService.reset();
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
        $rootScope.$emit('talend.loading.start');
        return DatasetService.getContent(dataset.id, true)
            .then((data) => checkRecords(data))
            .then(function (data) {
                StateService.setPreparationName(dataset.name);
                reset(dataset, data);
                StateService.hideRecipe();
                StateService.setNameEditionMode(true);
            })
            .then(function () {
                if (OnboardingService.shouldStartTour('playground')) {
                    $timeout(OnboardingService.startTour.bind(null, 'playground'), 300, false);
                }
            })
            .finally(function () {
                $rootScope.$emit('talend.loading.stop');
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
        if (!state.playground.preparation || state.playground.preparation.id !== preparation.id) {

            $rootScope.$emit('talend.loading.start');
            return PreparationService.getContent(preparation.id, 'head')
                .then(function (response) {
                    StateService.setPreparationName(preparation.name);
                    reset(preparation.dataset ? preparation.dataset : {id: preparation.dataSetId}, response, preparation);
                    StateService.showRecipe();
                    StateService.setNameEditionMode(false);
                })
                .finally(function () {
                    $rootScope.$emit('talend.loading.stop');
                });
        }
        else {
            return $q.when(true);
        }
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
        $rootScope.$emit('talend.loading.start');
        return PreparationService.getContent(state.playground.preparation.id, step.transformation.stepId)
            .then(function (response) {
                DatagridService.updateData(response);
                RecipeService.disableStepsAfter(step);
                PreviewService.reset(false);
            })
            .finally(function () {
                $rootScope.$emit('talend.loading.stop');
            });
    }

    function getMetadata() {
        if (state.playground.preparation) {
            return PreparationService.getContent(state.playground.preparation.id, 'head')
                .then(function (response) {
                    if (!response.metadata.columns[0].statistics.frequencyTable.length) {
                        return $q.reject();
                    }
                    StateService.updateDatasetRecord(response.records.length);
                    return response.metadata;
                });
        }
        else {
            return DatasetService.getMetadata(state.playground.dataset.id)
                .then(function (response) {
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

    //------------------------------------------------------------------------------------------------------
    //------------------------------------------------PREPARATION-------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name _getCurrentPreparation
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @description Return the current preparation, wrapped in a promise.
     * If there is no preparation yet, a new one is created, tagged as draft
     * @returns {Promise} The process promise
     */
    function _getCurrentPreparation() {
        //create the preparation and taf it draft if it does not exist
        return state.playground.preparation ?
            $q.when(state.playground.preparation) :
            createOrUpdatePreparation(_wrapInventoryName(state.playground.dataset.name))
                .then((preparation) => {
                    preparation.draft = true;
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
                var undo = performCreateOrUpdatePreparation.bind(service, oldName);
                var redo = performCreateOrUpdatePreparation.bind(service, name);
                HistoryService.addAction(undo, redo);
                return preparation;
            })
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
            PreparationService.create(state.playground.dataset.id, name);

        return promise
            .then((preparation) => {
                StateService.setCurrentPreparation(preparation);
                StateService.setPreparationName(preparation.name);
                return preparation;
            })
            .then((preparation) => {
                $state.go('playground.preparation', {prepid: state.playground.preparation.id});
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
        $rootScope.$emit('talend.loading.start');

        var promise = PreparationService.setHead(preparationId, headId);

        //load a specific step, we must load recipe first to get the step id to load. Then we load grid at this step.
        if (RecipeService.getLastActiveStep() !== RecipeService.getLastStep()) {
            var lastActiveStepIndex = RecipeService.getActiveThresholdStepIndex();
            promise = promise.then(updateRecipe)
                // The grid update cannot be done in parallel because the update change the steps ids
                // We have to wait for the recipe update to complete
                .then(function () {
                    var activeStep = RecipeService.getStep(lastActiveStepIndex, true);
                    return loadStep(activeStep);
                });
        }
        //load the recipe and grid head in parallel
        else {
            promise = promise.then(function () {
                return $q.all([updateRecipe(), updatePreparationDatagrid(columnToFocus)]);
            });
        }

        return promise.finally(function () {
            $rootScope.$emit('talend.loading.stop');
        });
    }

    /**
     * @ngdoc method
     * @name appendStep
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @param {string} action The action name
     * @param {object} parameters The transformation parameters
     * @description Call an execution of a transformation on the column in the current preparation and add an entry
     * in actions history. It there is no preparation yet, it is created first and tagged as draft.
     */
    function appendStep(action, parameters) {
        /*jshint camelcase: false */
        $rootScope.$emit('talend.loading.start');

        return _getCurrentPreparation()
            //append step
            .then(function (preparation) {
                return PreparationService.appendStep(preparation.id, {action: action, parameters: parameters});
            })
            //update recipe and datagrid
            .then(function () {
                var columnToFocus = parameters.column_id;
                return $q.all([updateRecipe(), updatePreparationDatagrid(columnToFocus)]);
            })
            //add entry in history for undo/redo
            .then(function () {
                var actualHead = RecipeService.getLastStep();
                var previousHead = RecipeService.getPreviousStep(actualHead);

                var undo = setPreparationHead.bind(service, state.playground.preparation.id, previousHead.transformation.stepId);
                var redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead.transformation.stepId, parameters.column_id);
                HistoryService.addAction(undo, redo);
            })
            //hide loading screen
            .finally(function () {
                $rootScope.$emit('talend.loading.stop');
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
            return;
        }

        /*jshint camelcase: false */
        $rootScope.$emit('talend.loading.start');

        //save the head before transformation for undo
        var previousHead = RecipeService.getLastStep().transformation.stepId;
        //save the last active step index to load this step after update
        var lastActiveStepIndex = RecipeService.getActiveThresholdStepIndex();

        return PreparationService.updateStep(state.playground.preparation.id, step, newParams)
            .then(updateRecipe)
            //get step id to load and update datagrid with it
            .then(function () {
                var activeStep = RecipeService.getStep(lastActiveStepIndex, true);
                return loadStep(activeStep);
            })
            //add entry in history for undo/redo
            .then(function () {
                var actualHead = RecipeService.getLastStep().transformation.stepId;
                var undo = setPreparationHead.bind(service, state.playground.preparation.id, previousHead);
                var redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead, newParams.column_id);
                HistoryService.addAction(undo, redo);
            })
            //hide loading screen
            .finally(function () {
                $rootScope.$emit('talend.loading.stop');
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
        /*jshint camelcase: false */
        $rootScope.$emit('talend.loading.start');

        //save the head before transformation for undo
        var previousHead = RecipeService.getLastStep().transformation.stepId;

        return PreparationService.removeStep(state.playground.preparation.id, step.transformation.stepId)
            //update recipe and datagrid
            .then(function () {
                return $q.all([updateRecipe(), updatePreparationDatagrid()]);
            })
            //add entry in history for undo/redo
            .then(function () {
                var actualHead = RecipeService.getLastStep().transformation.stepId;
                var undo = setPreparationHead.bind(service, state.playground.preparation.id, previousHead, step.actionParameters.parameters.column_id);
                var redo = setPreparationHead.bind(service, state.playground.preparation.id, actualHead);
                HistoryService.addAction(undo, redo);
            })
            //hide loading screen
            .finally(function () {
                $rootScope.$emit('talend.loading.stop');
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
        $rootScope.$emit('talend.loading.start');

        return _getCurrentPreparation()
            .then((preparation) => PreparationService.copySteps(preparation.id, referenceId))
            .then(() => $q.all([updateRecipe(), updatePreparationDatagrid()]))
            .finally(() => { $rootScope.$emit('talend.loading.stop') });
    }

    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------------PREVIEW---------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name updatePreview
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @param {string} updateStep The step position index to update for the preview
     * @param {object} params The new step params
     * @description [PRIVATE] Call the preview service to display the diff between the original steps and the updated
     *     steps
     */
    function updatePreview(updateStep, params) {
        var originalParameters = updateStep.actionParameters.parameters;
        PreparationService.copyImplicitParameters(params, originalParameters);

        //Parameters has not changed
        if (updateStep.inactive || !PreparationService.paramsHasChanged(updateStep, params)) {
            return $q.when();
        }

        var currentStep = RecipeService.getLastActiveStep();
        var preparationId = state.playground.preparation.id;
        PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, params);
    }

    //------------------------------------------------------------------------------------------------------
    //------------------------------------------------PARAMETERS--------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name changeDatasetParameters
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @param {object} params The new dataset parameters
     * @description Update the parameters of the dataset and reload
     */
    function changeDatasetParameters(params) {
        var dataset = state.playground.dataset;
        var isPreparation = state.playground.preparation;
        var lastActiveStepIndex = isPreparation ?
            RecipeService.getActiveThresholdStepIndex() :
            null;
        return DatasetService.updateParameters(dataset, params)
            .then(function () {
                if (isPreparation) {
                    var activeStep = RecipeService.getStep(lastActiveStepIndex, true);
                    return loadStep(activeStep);
                }
                else {
                    initPlayground(dataset);
                }
            });
    }

    //------------------------------------------------------------------------------------------------------
    //---------------------------------------------------UTILS----------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name updatePreparationDatagrid
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @description Perform an datagrid refresh with the preparation head
     */
    function updatePreparationDatagrid() {
        return PreparationService.getContent(state.playground.preparation.id, 'head')
            .then(function (response) {
                DatagridService.updateData(response);
                PreviewService.reset(false);
            });
    }

    /**
     * @ngdoc method
     * @name updateRecipe
     * @methodOf data-prep.services.playground.service:PlaygroundService
     * @description Update the recipe
     */
    function updateRecipe() {
        return RecipeService.refresh()
            .then(function () {
                if (RecipeService.getRecipe().length === 1) { //first step append
                    StateService.showRecipe();
                    $state.go('playground.preparation', {prepid: state.playground.preparation.id});
                }
                else if (OnboardingService.shouldStartTour('recipe') && RecipeService.getRecipe().length === 3) { //third step append : show onboarding
                    StateService.showRecipe();
                    $timeout(OnboardingService.startTour.bind(null, 'recipe'), 300, false);
                }
            });
    }

    //TODO : temporary fix because asked to.
    //TODO : when error status during import and get dataset content is managed by backend,
    //TODO : remove this controle and the 'data-prep.services.utils'/MessageService dependency
    function checkRecords(data) {
        if (!data || !data.records) {
            MessageService.error('INVALID_DATASET_TITLE', 'INVALID_DATASET');
            throw Error('Empty data');
        }
        return data;
    }
}