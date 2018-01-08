/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { filter } from 'lodash';

const COLUMN = 'column';
const EMPTY_CELLS = 'empty';
const INVALID_CELLS = 'invalid';

const TAB_INDEXES = {
	column: 0,
	line: 1,
	dataset: 2,
};

function initActions() {
	return {
		allSuggestions: [],             // all selected column suggestions
		allTransformations: [],         // all selected column transformations
		filteredTransformations: [],    // categories with their transformations to display, result of filter
		allCategories: null,
		searchActionString: '',
	};
}

function isAppliedToCells(type) {
	return (transformation) => {
		return transformation.actionScope && (transformation.actionScope.indexOf(type) !== -1);
	};
}

export const suggestionsState = {
	tab: null,
	isLoading: false,
	line: initActions(),
	column: initActions(),
	dataset: initActions(),
	transformationsForEmptyCells: [],   // all column transformations applied to empty cells
	transformationsForInvalidCells: [],  // all column transformations applied to invalid cells,
};

export function SuggestionsStateService() {
	return {
		selectTab,
		setTransformations,
		setLoading,
		updateFilteredTransformations,
		reset,
	};

	function setLoading(isLoading) {
		suggestionsState.isLoading = isLoading;
	}

	function setTransformations(scope, actionsPayload) {
		suggestionsState[scope] = actionsPayload;

		if (scope === COLUMN) {
			if (!suggestionsState.transformationsForEmptyCells.length) {
				suggestionsState.transformationsForEmptyCells =
                    filter(actionsPayload.allTransformations, isAppliedToCells(EMPTY_CELLS));
			}

			if (!suggestionsState.transformationsForInvalidCells.length) {
				suggestionsState.transformationsForInvalidCells =
                    filter(actionsPayload.allTransformations, isAppliedToCells(INVALID_CELLS));
			}
		}
	}

	function updateFilteredTransformations(scope, filteredTransformations) {
		suggestionsState[scope].filteredTransformations = filteredTransformations;
	}

	function selectTab(tab) {
		suggestionsState.tab = TAB_INDEXES[tab];
	}

	function reset() {
		suggestionsState.tab = null;
		suggestionsState.isLoading = false;
		suggestionsState.line = initActions();
		suggestionsState.column = initActions();
		suggestionsState.dataset = initActions();
	}
}
