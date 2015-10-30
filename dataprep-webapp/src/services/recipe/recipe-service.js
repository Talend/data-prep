(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.recipe.service:RecipeService
     * @description Recipe service. This service provide the entry point to manipulate properly the recipe
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.transformation.service:TransformationService
     */
    function RecipeService(state, PreparationService, TransformationService, FilterService) {
        var clusterType = 'CLUSTER';

        /**
         * @ngdoc property
         * @name recipeStateBeforePreview
         * @propertyOf data-prep.services.recipe.service:RecipeService
         * @description [PRIVATE] The recipe state before early preview
         * @type {object}
         */
        var recipeStateBeforePreview;

        /**
         * @ngdoc property
         * @name recipe
         * @propertyOf data-prep.services.recipe.service:RecipeService
         * @description [PRIVATE] the recipe step list
         * @type {object[]}
         */
        var recipe = [];

        /**
         * @ngdoc property
         * @name activeThresholdStep
         * @propertyOf data-prep.services.recipe.service:RecipeService
         * @description [PRIVATE] the last recipe step that is active
         * @type {object}
         */
        var activeThresholdStep = null;

        /**
         * @ngdoc property
         * @name initialState
         * @propertyOf data-prep.services.recipe.service:RecipeService
         * @description [PRIVATE] the recipe initial step (without transformation)
         * @type {object}
         */
        var initialState;

        return {
            //step utils
            getActiveThresholdStep: getActiveThresholdStep,
            getActiveThresholdStepIndex: getActiveThresholdStepIndex,
            getAllActionsFrom: getAllActionsFrom,
            getLastActiveStep: getLastActiveStep,
            getLastStep: getLastStep,
            getPreviousStep: getPreviousStep,
            getRecipe: getRecipe,
            getStep: getStep,
            getStepBefore: getStepBefore,
            getStepIndex: getStepIndex,
            isFirstStep: isFirstStep,
            isLastStep: isLastStep,

            //recipe and steps manipulation
            disableStepsAfter: disableStepsAfter,
            resetParams: resetParams,
            refresh: refresh,

            //append step preview
            earlyPreview: earlyPreview,
            cancelEarlyPreview: cancelEarlyPreview
        };

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------STEP UTILS------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getRecipe
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Return recipe step list
         * @returns {object[]} The recipe step list
         */
        function getRecipe() {
            return recipe;
        }

        /**
         * @ngdoc method
         * @name getStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {number} index The wanted index
         * @param {boolean} defaultLast Return the last step if no step is identified by the index
         * @description Return a recipe step identified by index
         * @returns {object} The recipe step
         */
        function getStep(index, defaultLast) {
            if(index < 0) {
                return initialState;
            }
            if(index >= recipe.length || index < 0) {
                return defaultLast ? recipe[recipe.length - 1] : null;
            }
            return recipe[index];
        }

        /**
         * @ngdoc method
         * @name getStepBefore
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {number} index The targeted step index
         * @description Return the step just before the provided index
         * @returns {object} The recipe step
         */
        function getStepBefore(index) {
            if(index <= 0) {
                return initialState;
            }
            else if(index >= recipe.length) {
                return recipe[recipe.length - 1];
            }

            return recipe[index - 1];
        }

        /**
         * @ngdoc method
         * @name getPreviousStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} step The given step
         * @description Get the step before the given one
         */
        function getPreviousStep(step) {
            var index = recipe.indexOf(step);
            return getStepBefore(index);
        }

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description [PRIVATE] Reset the current recipe
         */
        function reset() {
            initialState = null;
            recipe = [];
            activeThresholdStep = null;
            recipeStateBeforePreview = null;
        }

        /**
         * @ngdoc method
         * @name getActiveThresholdStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get the last active step
         * @returns {object} - the last active step
         */
        function getActiveThresholdStep() {
            return activeThresholdStep;
        }

        /**
         * @ngdoc method
         * @name getActiveThresholdStepIndex
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get the last active step index
         * @returns {number} The last active step index
         */
        function getActiveThresholdStepIndex() {
            return activeThresholdStep ? recipe.indexOf(activeThresholdStep) : recipe.length -1;
        }

        /**
         * @ngdoc method
         * @name getStepIndex
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} step The step
         * @description Get the current clicked step index
         * @returns {number} The current step index
         */
        function getStepIndex(step) {
            return recipe.indexOf(step);
        }

        /**
         * @ngdoc method
         * @name getLastActiveStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get the last active step (last step if activeThresholdStep var is not set)
         * @returns {object} The last active step
         */
        function getLastActiveStep() {
            return activeThresholdStep ? activeThresholdStep : getLastStep();
        }

        /**
         * @ngdoc method
         * @name isFirstStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} step The step to test
         * @description Test if the provided step is the first step of the recipe
         * @returns {object} The step to test
         */
        function isFirstStep(step) {
            return  getStepIndex(step) === 0;
        }

        /**
         * @ngdoc method
         * @name isLastStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} step The step to test
         * @description Test if the provided step is the last step of the recipe
         * @returns {object} The step to test
         */
        function isLastStep(step) {
            return  step === getLastStep();
        }

        /**
         * @ngdoc method
         * @name getLastStep
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get the last step of the recipe
         * @returns {object} The last step
         */
        function getLastStep() {
            return recipe.length > 0 ? recipe[recipe.length - 1] : initialState;
        }

        /**
         * @ngdoc method
         * @name getAllStepsFrom
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get all steps from provided step to the head
         * @param {object} step The starting step
         * @returns {object} The sublist from 'step' to head
         */
        function getAllStepsFrom(step) {
            var index = getStepIndex(step);
            return recipe.slice(index);
        }

        /**
         * @ngdoc method
         * @name getAllActionsFrom
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Get all actions from provided step to the head
         * @param {object} step The starting step
         * @returns {object} The actions array
         */
        function getAllActionsFrom(step) {
            var steps = getAllStepsFrom(step);
            return _.map(steps, 'actionParameters');
        }

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------STEP PARAMS-----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name resetParams
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} recipeItem The item to reset
         * @description Reset all params of the recipe item, with saved values (param.initialValue)
         */
        function resetParams(recipeItem) {
            //simple parameters
            TransformationService.resetParamValue(recipeItem.transformation.parameters, null);

            //clusters
            TransformationService.resetParamValue(recipeItem.transformation.cluster, clusterType);
        }

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
            var getOldStepById = function(step) {
                return _.find(recipes.old, function(oldStep) {
                    return oldStep.transformation.stepId === step.transformation.stepId;
                });
            };

            var initOnStep = function(step) {
                var oldStep = getOldStepById(step);
                if(oldStep) {
                    step.transformation.parameters = oldStep.transformation.parameters;
                    step.transformation.cluster = oldStep.transformation.cluster;
                }
                else {
                    var infos = {
                        columnId: step.column.id,
                        preparationId: state.playground.preparation.id,
                        stepId: getPreviousStep(step).transformation.stepId
                    };
                    return TransformationService.initDynamicParameters(step.transformation, infos)
                        .then(function() {
                            return TransformationService.initParamsValues(step.transformation, step.actionParameters.parameters);
                        });
                }
            };

            return _.chain(recipes.new)
                .filter(function(step) {
                    return step.transformation.dynamic;
                })
                .forEach(initOnStep)
                .value();
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------------STEPS--------------------------------------------------
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
            var stepId = actionStep[0];
            var actionValues = actionStep[1];
            var metadata = actionStep[2];
            var diff = actionStep[3];

            var flatFilters = [];
            FilterService.flattenFiltersTree(actionValues.parameters.filter, flatFilters);

            var item = {
                column: {
                    /*jshint camelcase: false */
                    id: actionValues.parameters.column_id,
                    name: actionValues.parameters.column_name
                },
                transformation: {
                    stepId: stepId,
                    name: actionValues.action,
                    label: metadata.label,
                    description: metadata.description,
                    parameters: metadata.parameters,
                    dynamic: metadata.dynamic
                },
                actionParameters: actionValues,
                diff: diff,
                filters: flatFilters
            };

            TransformationService.initParamsValues(item.transformation, actionValues.parameters);

            return item;
        }

        /**
         * @ngdoc method
         * @name refresh
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Refresh recipe items with current preparation steps
         */
        function refresh() {
            recipeStateBeforePreview = null;

            if(!state.playground.preparation) {
                return reset();
            }

            return PreparationService.getDetails(state.playground.preparation.id)
                .then(function(resp) {
                    //steps ids are in reverse order and the last is the 'no-transformation' id
                    var steps = resp.data.steps.slice(0);
                    var initialStepId = steps.shift();
                    initialState = {transformation: {stepId: initialStepId}};

                    var oldRecipe = recipe;
                    var newRecipe = _.chain(steps)
                        .zip(resp.data.actions, resp.data.metadata, resp.data.diff)
                        .map(createItem)
                        .value();
                    activeThresholdStep = null;
                    recipeStateBeforePreview = null;
                    recipe = newRecipe;

                    //TODO : Move this in a recipe-bullet-directive
                    //remove "single-maillon-cables-disabled" class of bullet cables when refreshing recipe
                    var allDisabledCables = angular.element('.recipe').eq(0).find('.single-maillon-cables-disabled').toArray();
                    _.each(allDisabledCables, function(cable) {
                        cable.setAttribute('class', '');
                    });

                    return {
                        'old': oldRecipe,
                        'new': newRecipe
                    };
                })
                .then(function(recipes) {
                    initDynamicParams(recipes);
                });
        }

        /**
         * @ngdoc method
         * @name disableStepsAfter
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} step The limit between active and inactive
         * @description Disable all steps after the given one
         */
        function disableStepsAfter(step) {
            var stepFound = step === initialState;
            _.forEach(recipe, function(nextStep) {
                if(stepFound) {
                    nextStep.inactive = true;
                }
                else {
                    nextStep.inactive = false;
                    if(nextStep === step) {
                        stepFound = true;
                    }
                }
            });
            activeThresholdStep = step;
        }

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------------PREVIEW--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------


        function getStepFilters (){
            if(state.playground.filter.applyTransformationOnFilters){
                return state.playground.filter.gridFilters.slice(0);
            }
            else {
                return [];
            }
        }

        /**
         * @ngdoc method
         * @name earlyPreview
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @param {object} column The target step column
         * @param {object} transformation The transformation
         * @param {object} params The transformation params
         * @description Add a preview step in the recipe. The state before preview is saved to be able to revert.
         */
        function earlyPreview(column, transformation, params) {
            //save state if not already in preview mode
            recipeStateBeforePreview = recipeStateBeforePreview || {
                recipe: recipe,
                lastActiveStep: activeThresholdStep
            };

            var stepFilters = getStepFilters();

            //create the preview step
            var previewStep = {
                column: {
                    id: column.id,
                    name: column.name
                },
                transformation: {
                    stepId: 'early preview',
                    name: transformation.name,
                    label: transformation.label,
                    description: transformation.description,
                    parameters: _.cloneDeep(transformation.parameters),
                    dynamic: transformation.dynamic
                },
                actionParameters: {
                    action: transformation.name,
                    parameters: params
                },
                preview: true,
                filters: stepFilters
            };
            TransformationService.initParamsValues(previewStep.transformation, params);

            //set the new state : add the step and enable all steps
            recipe = recipeStateBeforePreview.recipe.slice(0);
            recipe.push(previewStep);
            disableStepsAfter(previewStep);
        }

        /**
         * @ngdoc method
         * @name cancelEarlyPreview
         * @methodOf data-prep.services.recipe.service:RecipeService
         * @description Set back the state before preview and reset preview state
         */
        function cancelEarlyPreview() {
            if(!recipeStateBeforePreview) {
                return;
            }

            recipe = recipeStateBeforePreview.recipe;
            disableStepsAfter(recipeStateBeforePreview.lastActiveStep);
            recipeStateBeforePreview = null;
        }
    }

    angular.module('data-prep.services.recipe')
        .service('RecipeService', RecipeService);
})();