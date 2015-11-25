(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.recipe.controller:RecipeCtrl
     * @description Recipe controller.
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.playground.service:PreviewService
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.filters.service:FilterAdapterService
     */
    function RecipeCtrl(state, RecipeService, PlaygroundService, PreparationService, PreviewService, MessageService, FilterAdapterService) {
        var vm = this;
        vm.recipeService = RecipeService;

        /**
         * @ngdoc method
         * @name resetParams
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} recipeItem - the item to reset
         * @description Reset the params of the recipe item by calling {@link data-prep.services.recipe.service:RecipeService RecipeService}
         * Called on param accordion open.
         */
        vm.resetParams = RecipeService.resetParams;

        //---------------------------------------------------------------------------------------------
        //------------------------------------------UPDATE STEP----------------------------------------
        //---------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name stepUpdateClosure
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to bind the closure
         * @description Create a closure function that call the step update with the provided step id
         * @returns {Function} The function closure binded with the provided step id
         */
        vm.stepUpdateClosure = function stepUpdateClosure(step) {
            return function(newParams) {
                vm.updateStep(step, newParams);
            };
        };

        /**
         * @ngdoc method
         * @name updateStep
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {string} step The step id to update
         * @param {object} newParams the new step parameters
         * @description Update a step parameters in the loaded preparation
         */
        vm.updateStep = function updateStep(step, newParams) {
            PreviewService.cancelPreview();
            PreparationService.copyImplicitParameters(newParams, step.actionParameters.parameters);

            if(! PreparationService.paramsHasChanged(step, newParams)) {
                return;
            }

            return PlaygroundService.updateStep(step, newParams)
                .then(function() {
                    vm.showModal = {};
                });
        };

        /**
         * @ngdoc method
         * @name removeStepFilter
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {string} step The step id to update
         * @param {object} filter the filter to be removed
         * @description removes a filter in the step and updates the step
         */
        vm.removeStepFilter = function removeStepFilter(step, filter){
            if(step.actionParameters.action === 'delete_lines' && step.filters.length === 1){
                MessageService.warning('REMOVE_LAST_STEP_FILTER_TITLE', 'REMOVE_LAST_STEP_FILTER_CONTENT', null);
            }
            else{
                var filterPos = step.filters.indexOf(filter);
                var removedFilter = step.filters.splice(filterPos, 1);
                var stepFiltersTree = FilterAdapterService.toTree(step.filters);

                //get step parameters and replace filter field (it is removed when there is no filter anymore)
                var updatedParameters = _.extend({}, _.omit(step.actionParameters.parameters, 'filter'), stepFiltersTree);
                vm.updateStep(step, updatedParameters)
                    .catch(function(){
                        step.filters.push(removedFilter[0]);
                    });
            }
        };

        //---------------------------------------------------------------------------------------------
        //------------------------------------------DELETE STEP----------------------------------------
        //---------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name remove
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to remove
         * @description Show a popup to confirm the removal and remove it when user confirm
         */
        vm.remove = PlaygroundService.removeStep;

        //---------------------------------------------------------------------------------------------
        //------------------------------------------PARAMETERS-----------------------------------------
        //---------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getAddedColumnsInLookup
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The current step
         * @description having the Ids od the added columns, it collects the responding names
         * @returns {String} array of the added columns names
         */
        vm.getAddedColumnsInLookup = function getAddedColumnsInLookup(step){
            /*jshint camelcase: false */
            return _.pluck(step.actionParameters.parameters.lookup_selected_cols, 'name').join(', ');
        };

        /**
         * @ngdoc method
         * @name hasParameters
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to test
         * @description Return if the step has parameters
         */
        vm.hasParameters = function hasParameters(step) {
            return vm.hasStaticParams(step) || vm.hasDynamicParams(step);
        };

        /**
         * @ngdoc method
         * @name hasStaticParams
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to test
         * @description Return if the step has static parameters
         */
        vm.hasStaticParams = function hasStaticParams(step) {
            return (step.transformation.parameters && step.transformation.parameters.length) ||
                (step.transformation.items && step.transformation.items.length);
        };

        /**
         * @ngdoc method
         * @name hasDynamicParams
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to test
         * @description Return if the step has dynamic parameters
         */
        vm.hasDynamicParams = function hasDynamicParams(step) {
            return step.transformation.cluster;
        };

        //---------------------------------------------------------------------------------------------
        //---------------------------------------------Preview-----------------------------------------
        //---------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name updatePreview
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {string} updateStep The step position index to update for the preview
         * @param {object} params The new step params
         * @description [PRIVATE] Call the preview service to display the diff between the original steps and the updated steps
         */
        var updatePreview = function updatePreview(updateStep, params) {
            var originalParameters = updateStep.actionParameters.parameters;
            PreparationService.copyImplicitParameters(params, originalParameters);

            //Parameters has not changed
            if(updateStep.inactive || ! PreparationService.paramsHasChanged(updateStep, params)) {
                return;
            }

            var currentStep = RecipeService.getLastActiveStep();
            var preparationId = state.playground.preparation.id;
            PreviewService.getPreviewUpdateRecords(preparationId, currentStep, updateStep, params);
        };

        /**
         * @ngdoc method
         * @name previewUpdateClosure
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {object} step The step to update
         * @description [PRIVATE] Create a closure with a target step that call the update preview on execution
         */
        vm.previewUpdateClosure = function previewUpdateClosure(step) {
            return function(params) {
                updatePreview(step, params);
            };
        };

        /**
         * @ngdoc method
         * @name getAllFiltersNames
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @param {array} stepFilters The step filters
         * @description Get all filters names
         */
        vm.getAllFiltersNames = function getAllFiltersNames(stepFilters) {
            return '(' + _.pluck(stepFilters,'colName').join(', ').toUpperCase() + ')';
        };

        /**
         * @ngdoc method
         * @name cancelPreview
         * @methodOf data-prep.recipe.controller:RecipeCtrl
         * @description Cancel current preview and restore original data
         */
        vm.cancelPreview = PreviewService.cancelPreview;
    }

    /**
     * @ngdoc property
     * @name recipe
     * @propertyOf data-prep.recipe.controller:RecipeCtrl
     * @description The recipe.
     * It is bound to {@link data-prep.services.recipe.service:RecipeService RecipeService} property
     * @type {object[]}
     */
    Object.defineProperty(RecipeCtrl.prototype,
        'recipe', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.recipeService.getRecipe();
            }
        });

    angular.module('data-prep.recipe')
        .controller('RecipeCtrl', RecipeCtrl);
})();