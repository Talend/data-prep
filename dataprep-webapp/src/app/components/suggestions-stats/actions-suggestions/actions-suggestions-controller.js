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

/**
 * @ngdoc controller
 * @name data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
 * @description Actions suggestion controller
 * @requires data-prep.services.transformation.service:TransformationService
 */
export default function ActionsSuggestionsCtrl(state, TransformationService) {
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

	/**
	 * @ngdoc method
	 * @name shouldRenderAction
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
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
	 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
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
}
