(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.playground.service:PlaygroundService
     * @description Playground service. This service provides the entry point to load properly the playground
     * @requires data-prep.services.dataset.service:DatasetService
     * @requires data-prep.services.playground.service:DatagridService
     * @requires data-prep.services.playground.service:PreviewService
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.transformation.service:TransformationCacheService
     * @requires data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.utils.service:MessageService
     * @requires data-prep.services.statistics.service:StatisticsService
     * @requires data-prep.services.history.service:HistoryService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.onboarding:OnboardingService
     */
    function PlaygroundService($rootScope, $q, state, DatasetService, DatagridService, PreviewService,
                               RecipeService, TransformationCacheService, SuggestionService, PreparationService,
                               MessageService, StatisticsService, HistoryService, StateService, OnboardingService) {
        var DEFAULT_NAME = 'Preparation draft';

        var service = {
            /**
             * @ngdoc property
             * @name preparationName
             * @propertyOf data-prep.services.playground.service:PlaygroundService
             * @description the current preparation
             */
            preparationName: '',

            //init/load
            initPlayground: initPlayground,     // load dataset
            load: load,                         // load preparation
            loadStep: loadStep,                 // load preparation step

            //preparation
            createOrUpdatePreparation: createOrUpdatePreparation,
            appendStep: appendStep,
            updateStep: updateStep,
            removeStep: removeStep,
            editCell: editCell
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
            StateService.removeAllGridFilters();

            RecipeService.refresh();
            StatisticsService.reset(true, true, true);
            TransformationCacheService.invalidateCache();
            SuggestionService.reset();
            HistoryService.clear();
            PreviewService.reset(false);
        }

        /**
         * @ngdoc method
         * @name initPlayground
         * @methodOf data-prep.services.playground.service:PlaygroundService
         * @param {object} dataset The dataset to load
         * @description Initiate a new preparation from dataset.
         - If there is no preparation yet and the dataset to load is still the last loaded, the playground is not changed.
         - Otherwise, the playground is reset with the wanted dataset
         * @returns {Promise} The process promise
         */
        function initPlayground(dataset) {
            if(!state.playground.dataset || state.playground.preparation || dataset.id !== state.playground.dataset.id) {

                $rootScope.$emit('talend.loading.start');
                return DatasetService.getContent(dataset.id, false)
                    .then(function(data) {
                        //TODO : temporary fix because asked to.
                        //TODO : when error status during import and get dataset content is managed by backend,
                        //TODO : remove this controle and the 'data-prep.services.utils'/MessageService dependency
                        if(!data || !data.records) {
                            MessageService.error('INVALID_DATASET_TITLE', 'INVALID_DATASET');
                            throw Error('Empty data');
                        }

                        service.preparationName = '';
                        reset(dataset, data);
                        StateService.hideRecipe();
                        StateService.setNameEditionMode(true);
                    })
                    .then(function() {
                        if(OnboardingService.shouldStartTour('playground')) {
                            setTimeout(OnboardingService.startTour.bind(null, 'playground'), 300);
                        }
                    })
                    .finally(function() {
                        $rootScope.$emit('talend.loading.stop');
                    });
            }
            else {
                return $q.when(true);
            }
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
            if(!state.playground.preparation || state.playground.preparation.id !== preparation.id) {

                $rootScope.$emit('talend.loading.start');
                return PreparationService.getContent(preparation.id, 'head')
                    .then(function(response) {
                        service.preparationName = preparation.name;
                        reset(preparation.dataset ? preparation.dataset : {id: preparation.dataSetId}, response, preparation);
                        StateService.showRecipe();
                        StateService.setNameEditionMode(false);
                    })
                    .finally(function() {
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
            //step already loaded
            if(RecipeService.getActiveThresholdStep() === step) {
                return;
            }

            $rootScope.$emit('talend.loading.start');
            return PreparationService.getContent(state.playground.preparation.id, step.transformation.stepId)
                .then(function(response) {
                    DatagridService.updateData(response);
                    RecipeService.disableStepsAfter(step);
                    PreviewService.reset(false);
                })
                .finally(function() {
                    $rootScope.$emit('talend.loading.stop');
                });
        }

        //------------------------------------------------------------------------------------------------------
        //------------------------------------------------PREPARATION-------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createOrUpdatePreparation
         * @methodOf data-prep.services.playground.service:PlaygroundService
         * @param {string} name The preparation name to create or update
         * @description Create a new preparation or change its name if it already exists
         * @returns {Promise} The process promise
         */
        function createOrUpdatePreparation(name) {
            var promise = state.playground.preparation ?
                PreparationService.setName(state.playground.preparation.id, name) :
                PreparationService.create(state.playground.dataset.id, name);

            return promise.then(function(preparation) {
                StateService.setCurrentPreparation(preparation);
                service.preparationName = name;
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
            if(RecipeService.getLastActiveStep() !== RecipeService.getLastStep()) {
                var lastActiveStepIndex = RecipeService.getActiveThresholdStepIndex();
                promise = promise.then(updateRecipe)
                    // The grid update cannot be done in parallel because the update change the steps ids
                    // We have to wait for the recipe update to complete
                    .then(function() {
                        var activeStep = RecipeService.getStep(lastActiveStepIndex, true);
                        return loadStep(activeStep);
                    });
            }
            //load the recipe and grid head in parallel
            else {
                promise = promise.then(function() {
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

            //create the preparation and taf it draft if it does not exist
            var prepCreation = state.playground.preparation ?
                $q.when(state.playground.preparation) :
                createOrUpdatePreparation(DEFAULT_NAME)
                    .then(function(preparation) {
                        preparation.draft = true;
                        service.preparationName = '';
                        return preparation;
                    });

            return prepCreation
                //append step
                .then(function(preparation) {
                    return PreparationService.appendStep(preparation.id, {action: action, parameters: parameters});
                })
                //update recipe and datagrid
                .then(function(){
                    var columnToFocus = parameters.column_id;
                    return $q.all([updateRecipe(), updatePreparationDatagrid(columnToFocus)]);
                })
                //add entry in history for undo/redo
                .then(function() {
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
            /*jshint camelcase: false */
            $rootScope.$emit('talend.loading.start');

            //save the head before transformation for undo
            var previousHead = RecipeService.getLastStep().transformation.stepId;
            //save the last active step index to load this step after update
            var lastActiveStepIndex = RecipeService.getActiveThresholdStepIndex();

            return PreparationService.updateStep(state.playground.preparation.id, step, newParams)
                .then(updateRecipe)
                //get step id to load and update datagrid with it
                .then(function() {
                    var activeStep = RecipeService.getStep(lastActiveStepIndex, true);
                    return loadStep(activeStep);
                })
                //add entry in history for undo/redo
                .then(function() {
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
                .then(function() {
                    return $q.all([updateRecipe(), updatePreparationDatagrid()]);
                })
                //add entry in history for undo/redo
                .then(function() {
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
         * @name editCell
         * @methodOf data-prep.services.playground.service:PlaygroundService
         * @param {Object} rowItem The row
         * @param {object} column The column where to execute the transformation
         * @param {string} newValue The new value to put on th target
         * @param {boolean} updateAllCellWithValue Indicates the scope (cell or column) of the transformaton
         * @description Perform a cell or a column edition
         */
        function editCell(rowItem, column, newValue, updateAllCellWithValue) {
            /*jshint camelcase: false */
            var params = {
                scope : updateAllCellWithValue ? 'column' : 'cell',
                column_id: column.id,
                column_name: column.name,
                row_id: rowItem.tdpId,
                cell_value: rowItem[column.id],
                replace_value: newValue
            };
            var action = 'replace_on_value';

            return appendStep(action, params);
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
                .then(function(response) {
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
                .then(function() {
                    if(RecipeService.getRecipe().length === 1) { //first step append
                        StateService.showRecipe();
                    }
                    else if(OnboardingService.shouldStartTour('recipe') && RecipeService.getRecipe().length === 3) { //third step append : show onboarding
                        StateService.showRecipe();
                        setTimeout(OnboardingService.startTour.bind(null, 'recipe'), 300);
                    }
                });
        }
    }

    angular.module('data-prep.services.playground')
        .service('PlaygroundService', PlaygroundService);
})();