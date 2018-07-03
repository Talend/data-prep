/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const CLUSTER_TYPE = 'CLUSTER';

/**
 * @ngdoc controller
 * @name data-prep.recipe.controller:RecipeCtrl
 * @description Recipe controller.
 * @requires data-prep.services.filters.service:FilterAdapterService
 * @requires data-prep.services.lookup.service:LookupService
 * @requires data-prep.services.utils.service:MessageService
 * @requires data-prep.services.parameters.service:ParametersService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.recipe.service:RecipeKnotService
 */
export default class RecipeCtrl {

	constructor($timeout, FilterAdapterService, LookupService, MessageService, ParametersService,
		PlaygroundService, PreviewService, StateService, state, RecipeKnotService, RecipeService) {
		'ngInject';

		this.$timeout = $timeout;
		this.FilterAdapterService = FilterAdapterService;
		this.LookupService = LookupService;
		this.MessageService = MessageService;
		this.ParametersService = ParametersService;
		this.PlaygroundService = PlaygroundService;
		this.PreviewService = PreviewService;
		this.RecipeKnotService = RecipeKnotService;
		this.RecipeService = RecipeService;
		this.StateService = StateService;

		this.state = state;
		this.stepToBeDeleted = null;
		this.showModal = {};

		// Cancel current preview and restore original data
		this.cancelPreview = this.PreviewService.cancelPreview;

		// Flag that indicates if a step update is in progress
		this.updateStepInProgress = false;

		// Help to hide lines between step bullets while dragging
		this.isDragStart = false;
		this.dragControlListeners = {
			containment: '.recipe',
			dragStart: this.dragStart.bind(this),
			dragEnd: this.dragEnd.bind(this),
			dragMove: this.dragMove.bind(this),
			orderChanged: this.orderChanged.bind(this),
		};
	}

	/**
	 * @ngdoc method
	 * @name resetParams
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to reset
	 * @description Reset the params of the step
	 * Called on param accordion open.
	 */
	resetParams(step) {
		// simple parameters
		this.ParametersService.resetParamValue(step.transformation.parameters, null);

		// clusters
		this.ParametersService.resetParamValue(step.transformation.cluster, CLUSTER_TYPE);
	}

	//---------------------------------------------------------------------------------------------
	// ------------------------------------------STEP KNOT DISPLAY----------------------------------------
	//---------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name showTopLine
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Test if the top line of the knot should be displayed
	 * @returns {boolean} true if the top line should be displayed
	 */
	showTopLine(step) {
		return (!this.RecipeService.isStartChain(step) && !step.inactive)
			|| (this._toBeActivated(step) && !this.RecipeService.isStartChain(step));
	}

	/**
	 * @ngdoc method
	 * @name showBottomLine
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Test if the bottom line of the knot should be displayed
	 * @returns {boolean} true if the bottom line should be displayed
	 */
	showBottomLine(step) {
		return (!this.RecipeService.isEndChain(step) && !step.inactive && !this.RecipeService.isLastActive(step))
			|| (!this.RecipeService.isEndChain(step) && this._toBeActivated(step) && !this.isHoveredStep(step));
	}

	/**
	 * @ngdoc method
	 * @name _toBeActivated
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description checks if the active step is going to be activated
	 * @returns {boolean} true if the step will be activated
	 */
	_toBeActivated(step) {
		const hoveredStepPosition = this.state.playground.recipe.current.reorderedSteps.indexOf(this.state.playground.recipe.hoveredStep);
		const stepPosition = this.state.playground.recipe.current.reorderedSteps.indexOf(step);
		return hoveredStepPosition !== -1 && hoveredStepPosition >= stepPosition;
	}

	/**
	 * @ngdoc method
	 * @name _toBeDeactivated
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description checks if the active step is going to be deactivated
	 * @returns {boolean} true if the step will be deactivated
	 */
	_toBeDeactivated(step) {
		const hoveredStepPosition = this.state.playground.recipe.current.reorderedSteps.indexOf(this.state.playground.recipe.hoveredStep);
		const stepPosition = this.state.playground.recipe.current.reorderedSteps.indexOf(step);
		return hoveredStepPosition !== -1 && hoveredStepPosition <= stepPosition;
	}

	/**
	 * @ngdoc method
	 * @name toBeSwitched
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description checks the status of the knot: inactive and will be activated or active and it will be deactivated
	 * @param {object} step The step to be switched
	 * @returns {boolean} true if the knot will be activated or deactivated
	 */
	toBeSwitched(step) {
		return step.inactive ? this._toBeActivated(step) : this._toBeDeactivated(step);
	}

	/**
	 * @ngdoc method
	 * @name resetStepToBeDeleted
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description sets the step that will be deleted to null
	 */
	resetStepToBeDeleted() {
		this.stepToBeDeleted = null;
	}

	/**
	 * @ngdoc method
	 * @name setStepToBeDeleted
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to be deleted
	 * @description sets the step that will be deleted
	 */
	setStepToBeDeleted(step) {
		this.stepToBeDeleted = step;
	}

	/**
	 * @ngdoc method
	 * @name shouldBeRemoved
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description checks if the step should be removed
	 * @param {object} step The step to be deleted
	 * @returns {boolean} true if the step will be removed from the recipe
	 */
	shouldBeRemoved(step) {
		return ((this.stepToBeDeleted && this.stepToBeDeleted.transformation.stepId === step.transformation.stepId)) ||
			(this.stepToBeDeleted && (this.stepToBeDeleted.diff.createdColumns.indexOf(step.actionParameters.parameters.column_id) > -1));
	}

	/**
	 * @ngdoc method
	 * @name isHoveredStep
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The hovered step
	 * @description checks if the step is beeing hovered
	 * @returns {boolean} true if the step is hovered
	 */
	isHoveredStep(step) {
		return step === this.state.playground.recipe.hoveredStep;
	}

	/**
	 * @ngdoc method
	 * @name stepHoverStart
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The hovered start step
	 * @description Trigger actions called at mouse enter
	 */
	stepHoverStart(step) {
		this.StateService.setHoveredStep(step);
		this.RecipeKnotService.stepHoverStart(step);
	}

	/**
	 * @ngdoc method
	 * @name stepHoverEnd
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The hovered end step
	 * @description Trigger actions called at mouse leave
	 */
	stepHoverEnd(step) {
		this.StateService.setHoveredStep(null);
		this.RecipeKnotService.stepHoverEnd(step);
	}

	/**
	 * @ngdoc method
	 * @name toggleStep
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to toggle
	 * @description Enable/disable step
	 */
	toggleStep(step) {
		this.PlaygroundService.toggleStep(step);
	}

	/**
	 * @ngdoc method
	 * @name hasSteps
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Return if the recipe has steps
	 */
	hasSteps() {
		const current = this.state.playground.recipe.current;
		return current && current.reorderedSteps && current.reorderedSteps.length > 0;
	}

	//---------------------------------------------------------------------------------------------
	// --------------------------------------------REORDER----------------------------------------
	//---------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name moveUp
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {number} stepPosition Current position of step to move up
	 * @param {object} $event The click event
	 * @description Move step up in recipe
	 */
	moveUp(stepPosition, $event) {
		$event.stopPropagation();
		const previousPosition = stepPosition;
		const nextPosition = stepPosition - 1;
		this.PlaygroundService.updateStepOrder(previousPosition, nextPosition);
	}

	/**
	 * @ngdoc method
	 * @name moveDown
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {number} stepPosition Current position of step to move down
	 * @param {object} $event The click event
	 * @description Move step down in recipe
	 */
	moveDown(stepPosition, $event) {
		$event.stopPropagation();
		const previousPosition = stepPosition;
		const nextPosition = stepPosition + 1;
		this.PlaygroundService.updateStepOrder(previousPosition, nextPosition);
	}

	/**
	 * @ngdoc method
	 * @name dragStart
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Callback on drag start
	 */
	dragStart() {
		this.isDragStart = true;
	}

	/**
	 * @ngdoc method
	 * @name dragMove
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Callback on while dragging
	 */
	dragMove(itemPosition, containment, event) {
		this.eventClientY = event.clientY;
	}

	/**
	 * @ngdoc method
	 * @name dragEnd
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description Callback on drag end
	 */
	dragEnd() {
		this.isDragStart = false;
	}

	/**
	 * @ngdoc method
	 * @name orderChanged
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} event Data embedded into drag and drop event
	 * @description Callback when step order is changed within the step list
	 */
	orderChanged(event) {
		const { source, dest } = event;
		const previousPosition = source.index;
		const nextPosition = dest.index;
		this.PlaygroundService
			.updateStepOrder(previousPosition, nextPosition, true)
			.catch(() => {
				// Must manually reset order if reorder request fails
				dest.sortableScope.removeItem(dest.index);
				source.itemScope.sortableScope.insertItem(source.index, source.itemScope.step);
			});
	}

	//---------------------------------------------------------------------------------------------
	// ------------------------------------------UPDATE STEP----------------------------------------
	//---------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name stepUpdateClosure
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to bind the closure
	 * @description Create a closure function that call the step update with the provided step id
	 * @returns {Function} The function closure binded with the provided step id
	 */
	stepUpdateClosure(step) {
		return (newParams) => {
			if (!this.updateStepInProgress) {
				this.updateStepInProgress = true;
				this.updateStep(step, newParams)
					.finally(() => {
						this.$timeout(() => {
							this.updateStepInProgress = false;
						}, 500, false);
					});
			}
		};
	}

	/**
	 * @ngdoc method
	 * @name updateStep
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {string} step The step id to update
	 * @param {object} newParams the new step parameters
	 * @description Update a step parameters in the loaded preparation
	 */
	updateStep(step, newParams) {
		return this.PlaygroundService.updateStep(step, newParams)
			.then(() => {
				this.showModal = {};
			});
	}

	/**
	 * @ngdoc method
	 * @name updateStepFilter
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @description update a step
	 * @param {Object} step The step to update
	 * @param {Object} filter The filter to update
	 * @param {Object} value The new filter value
	 */
	updateStepFilter(step, filter, value) {
		const adaptedFilter = this.FilterAdapterService.createFilter(filter.type, filter.colId, filter.colName, filter.editable, filter.args);
		adaptedFilter.args = { ...filter.args };
		adaptedFilter.value = value;

		const adaptedFilterList = step.filters.map((nextFilter) => {
			return nextFilter === filter ? adaptedFilter : nextFilter;
		});
		const stepFiltersTree = this.FilterAdapterService.toTree(adaptedFilterList);

		const updatedParameters = {
			...step.actionParameters.parameters,
			filter: stepFiltersTree.filter,
		};
		this.updateStep(step, updatedParameters);
	}

	/**
	 * @ngdoc method
	 * @name removeStepFilter
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {string} step The step id to update
	 * @param {object} filter the filter to be removed
	 * @description removes a filter in the step and updates the step
	 */
	removeStepFilter(step, filter) {
		if (step.actionParameters.action === 'delete_lines' && step.filters.length === 1) {
			this.MessageService.warning('REMOVE_LAST_STEP_FILTER_TITLE', 'REMOVE_LAST_STEP_FILTER_CONTENT', null);
		}
		else {
			const updatedFilters = step.filters.filter(nextFilter => nextFilter !== filter);
			const stepFiltersTree = this.FilterAdapterService.toTree(updatedFilters);

			// get step parameters and replace filter field (it is removed when there is no filter anymore)
			const updatedParameters = {
				...step.actionParameters.parameters,
				filter: stepFiltersTree.filter,
			};
			this.updateStep(step, updatedParameters);
		}
	}

	/**
	 * @ngdoc method
	 * @name select
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The selected step
	 * @description Action on step selection.
	 * It display dynamic parameters modal and treat specific params (ex: lookup)
	 */
	select(step) {
		this._toggleDynamicParams(step);
		this._toggleSpecificParams(step);
	}

	_toggleDynamicParams(step) {
		this.showModal[step.transformation.stepId] = !!this.RecipeService.hasDynamicParams(step);
	}

	_toggleSpecificParams(step) {
		if (this.state.playground.lookup.visibility && this.state.playground.stepInEditionMode === step) {
			this.StateService.setLookupVisibility(false);
			this.StateService.setStepInEditionMode(null);
		}
		else if (step.transformation.name === 'lookup') {
			this.StateService.setStepInEditionMode(step);
			this.StateService.setLookupVisibility(true);
			this.LookupService.loadFromStep(step);
		}
	}

	//---------------------------------------------------------------------------------------------
	// ------------------------------------------DELETE STEP----------------------------------------
	//---------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name remove
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to remove
	 * @param {object} $event The click event
	 * @description Show a popup to confirm the removal and remove it when user confirms
	 */
	remove(step, $event) {
		$event.stopPropagation();
		if (!step.deleting) {
			step.deleting = true;
			this.PlaygroundService.removeStep(step)
				.then(() => {
					this.resetStepToBeDeleted(); // otherwise it will wrongly appear when undo
					if (this.state.playground.lookup.visibility && this.state.playground.stepInEditionMode) {
						this.StateService.setLookupVisibility(false);
						this.StateService.setStepInEditionMode(null);
					}
				})
				.finally(() => step.deleting = false);
		}
	}

	//---------------------------------------------------------------------------------------------
	// ------------------------------------------PARAMETERS-----------------------------------------
	//---------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name hasParameters
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to test
	 * @description Return if the step has parameters
	 */
	hasParameters(step) {
		return !this._isSpecificParams(step) && (this.RecipeService.hasStaticParams(step) || this.RecipeService.hasDynamicParams(step));
	}

	/**
	 * @ngdoc method
	 * @name _isSpecificParams
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to test
	 * @description Return if the step has parameters that will be treated specifically
	 */
	_isSpecificParams(step) {
		return step.transformation.name === 'lookup';
	}

	//---------------------------------------------------------------------------------------------
	// ---------------------------------------------Preview-----------------------------------------
	//---------------------------------------------------------------------------------------------

	/**
	 * @ngdoc method
	 * @name previewUpdateClosure
	 * @methodOf data-prep.recipe.controller:RecipeCtrl
	 * @param {object} step The step to update
	 * @description Create a closure with a target step that call the update preview on execution
	 */
	previewUpdateClosure(step) {
		return (params) => {
			this.PreviewService.updatePreview(step, params);
		};
	}
}
