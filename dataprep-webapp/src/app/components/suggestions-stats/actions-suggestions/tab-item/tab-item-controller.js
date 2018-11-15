/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { find } from 'lodash';

const SUGGESTIONS = 'suggestions';
const FILTERED_COLUMN = 'column_filtered';
const SCOPES = {
	COLUMN: 'column',
	MULTI_COLUMNS: 'multi_columns',
	DATASET: 'dataset',
	LINE: 'line',
};

/**
 * @ngdoc controller
 * @name data-prep.tab-item.controller:TabItemCtrl
 * @description Actions suggestion controller
 * @requires data-prep.services.transformation.service:TransformationService
 */
export default function TabItemCtrl(state, TransformationService) {
	'ngInject';

	const vm = this;
	vm.TransformationService = TransformationService;
	vm.state = state;

	/**
	 * Predicate to define if a suggestion should be rendered
	 *  - filtered actions only when we will apply on filtered data
	 *  - other suggestions only when we have only 1 selected column
	 * @param action
	 * @returns {*|boolean}
	 */
	function shouldRenderSuggestion(action) {
		if (action.actionScope.includes(FILTERED_COLUMN)) {
			return state.playground.filter.applyTransformationOnFilters;
		}
		return state.playground.grid.selectedColumns.length === 1;
	}

	function getSelectedColumnsCount() {
		const { selectedColumns } = state.playground.grid;
		return (selectedColumns && selectedColumns.length) || 0;
	}


	/**
	 * @ngdoc method
	 * @name shouldRenderAction
	 * @methodOf data-prep.tab-item.controller:TabItemCtrl
	 * @param {object} categoryItem The category
	 * @param {object} action The transformation to test
	 * @description Determine if the transformation should be rendered.
	 * The 'filtered' category transformations are not rendered if the applyTransformationOnFilters flag is false
	 * @returns {boolean} True if the transformation should be rendered, False otherwise
	 */
	vm.shouldRenderAction = function shouldRenderAction(categoryItem, action) {
		if (categoryItem.category !== SUGGESTIONS) {
			return true;
		}
		return shouldRenderSuggestion(action);
	};

	/**
	 * @ngdoc method
	 * @name shouldRenderCategory
	 * @methodOf data-prep.tab-item.controller:TabItemCtrl
	 * @param {object} categoryItem The categories with their transformations
	 * @description Determine if the category should be rendered.
	 * The 'suggestions' category is rendered if it has transformations to render
	 * @returns {boolean} True if the category should be rendered, False otherwise
	 */
	vm.shouldRenderCategory = function shouldRenderCategory(categoryItem) {
		// render all non Suggestions category
		// render Suggestions if one of the transformations should be rendered
		return categoryItem.category !== SUGGESTIONS ||
			find(categoryItem.transformations, action => shouldRenderSuggestion(action));
	};

	/**
	 * @ngdoc method
	 * @name shouldRender
	 * @methodOf data-prep.tab-item.controller:TabItemCtrl
	 * @description Determine if tab content should be rendered
	 * @returns {boolean} True if the tab content should be rendered, False otherwise
	 */
	vm.shouldRender = function () {
		switch (vm.scope) {
		case SCOPES.DATASET:
			return true;
		case SCOPES.COLUMN:
			return !!getSelectedColumnsCount();
		case SCOPES.LINE:
			return !!vm.state.playground.grid.selectedLine;
		}
	};


	vm.getFilteringScope = function () {
		const scope = vm.scope;
		if (scope === SCOPES.COLUMN && getSelectedColumnsCount() > 1) {
			return SCOPES.MULTI_COLUMNS;
		}
		return scope;
	};

	/**
	 * @ngdoc method
	 * @name getSuggestionsState
	 * @methodOf data-prep.tab-item.controller:TabItemCtrl
	 * @description Returns the appropriated state depending on the current scope
	 * @returns {Object} The appropriated state
	 */
	vm.getSuggestionsState = function () {
		return vm.state.playground.suggestions[vm.getFilteringScope()];
	};

	/**
	 * @ngdoc method
	 * @name getInvalidSelectionKey
	 * @methodOf data-prep.tab-item.controller:TabItemCtrl
	 * @description Returns the translation key to be used when the selection is not valid.
	 * @returns {string} The translation key
	 */
	vm.getInvalidSelectionKey = function () {
		return `SELECT_${vm.scope.toUpperCase()}_TO_DISPLAY_ACTIONS`;
	};
}
