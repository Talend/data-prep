/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.recipe.controller:RecipeCtrl
 * @description Recipe controller.
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.filters.service:FilterAdapterService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.lookup.service:LookupService
 */
export default function RecipeCtrl(state, RecipeService, PlaygroundService, PreviewService, MessageService, FilterAdapterService, StateService, LookupService) {
    'ngInject';

    var vm = this;
    vm.recipeService = RecipeService;
    vm.showModal = {};
    vm.state = state;


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
        return function (newParams) {
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
        return PlaygroundService.updateStep(step, newParams)
            .then(function () {
                vm.showModal = {};
            });
    };

    /**
     * @ngdoc method
     * @name updateStepFilter
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @description update a step
     * @param {Object} step The step to update
     * @param {Object} filter The filter to update
     * @param {Object} value The new filter value
     */
    vm.updateStepFilter = function updateStepFilter(step, filter, value) {
        const adaptedFilter = FilterAdapterService.createFilter(filter.type, filter.colId, filter.colName, filter.editable, filter.args);
        adaptedFilter.args = { ... filter.args };
        adaptedFilter.value = value;

        const adaptedFilterList = step.filters.map((nextFilter) => {
            return nextFilter === filter ? adaptedFilter : nextFilter;
        });
        const stepFiltersTree = FilterAdapterService.toTree(adaptedFilterList);

        const updatedParameters = {
            ...step.actionParameters.parameters,
            filter: stepFiltersTree.filter
        };
        vm.updateStep(step, updatedParameters);
    };

    /**
     * @ngdoc method
     * @name removeStepFilter
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {string} step The step id to update
     * @param {object} filter the filter to be removed
     * @description removes a filter in the step and updates the step
     */
    vm.removeStepFilter = function removeStepFilter(step, filter) {
        if (step.actionParameters.action === 'delete_lines' && step.filters.length === 1) {
            MessageService.warning('REMOVE_LAST_STEP_FILTER_TITLE', 'REMOVE_LAST_STEP_FILTER_CONTENT', null);
        }
        else {
            const updatedFilters = step.filters.filter((nextFilter) => nextFilter !== filter);
            const stepFiltersTree = FilterAdapterService.toTree(updatedFilters);

            //get step parameters and replace filter field (it is removed when there is no filter anymore)
            const updatedParameters = {
                ...step.actionParameters.parameters,
                filter: stepFiltersTree.filter
            };
            vm.updateStep(step, updatedParameters);
        }
    };

    /**
     * @ngdoc method
     * @name toogleStep
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {object} step The selected step
     * @description Action on step selection.
     * It display dynamic parameters modal and treat specific params (ex: lookup)
     */
    vm.select = function select(step) {
        toggleDynamicParams(step);
        toggleSpecificParams(step);
    };

    function toggleDynamicParams(step) {
        vm.showModal[step.transformation.stepId] = !!vm.hasDynamicParams(step);
    }

    function toggleSpecificParams(step) {
        if (state.playground.lookup.visibility && state.playground.lookup.step === step) {
            StateService.setLookupVisibility(false);
        }
        else if (step.transformation.name === 'lookup') {
            LookupService.loadFromStep(step)
                .then(StateService.setLookupVisibility.bind(null, true));
        }
    }

    //---------------------------------------------------------------------------------------------
    //------------------------------------------DELETE STEP----------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name remove
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {object} step The step to remove
     * @param {object} $event The click event
     * @description Show a popup to confirm the removal and remove it when user confirm
     */
    vm.remove = function remove(step, $event) {
        $event.stopPropagation();
        PlaygroundService.removeStep(step)
            .then(function () {
                if (state.playground.lookup.visibility && state.playground.lookup.step) {
                    StateService.setLookupVisibility(false);
                }
            });
    };

    //---------------------------------------------------------------------------------------------
    //------------------------------------------PARAMETERS-----------------------------------------
    //---------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name hasParameters
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {object} step The step to test
     * @description Return if the step has parameters
     */
    vm.hasParameters = function hasParameters(step) {
        return !isSpecificParams(step) && (vm.hasStaticParams(step) || vm.hasDynamicParams(step));
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

    /**
     * @ngdoc method
     * @name isSpecificParams
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {object} step The step to test
     * @description Return if the step has parameters that will be treated specifically
     */
    function isSpecificParams(step) {
        return step.transformation.name === 'lookup';
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------------------------Preview-----------------------------------------
    //---------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name previewUpdateClosure
     * @methodOf data-prep.recipe.controller:RecipeCtrl
     * @param {object} step The step to update
     * @description [PRIVATE] Create a closure with a target step that call the update preview on execution
     */
    vm.previewUpdateClosure = function previewUpdateClosure(step) {
        return function (params) {
            PlaygroundService.updatePreview(step, params);
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
        return '(' + _.pluck(stepFilters, 'colName').join(', ').toUpperCase() + ')';
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