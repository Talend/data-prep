(function () {
    'use strict';

    var suggestionsState = {
        isLoading: false,
        line: {
            allTransformations: [],
            filteredTransformations: [],
            allCategories: null
        },
        column: {
            allSuggestions: [],             // all selected column suggestions
            allTransformations: [],         // all selected column transformations
            filteredTransformations: [],    // categories with their transformations to display, result of filter
            allCategories: null,
            searchActionString: ''
        },
        transformationsForEmptyCells: [],   // all column transformations applied to empty cells
        transformationsForInvalidCells: []  // all column transformations applied to invalid cells,
    };

    function SuggestionsStateService() {

        return {
            setColumnTransformations: setColumnTransformations,
            setLineTransformations: setLineTransformations,
            setTransformationsForEmptyCells: setTransformationsForEmptyCells,
            setTransformationsForInvalidCells: setTransformationsForInvalidCells,

            setLoading: setLoading,
            updateFilteredTransformations: updateFilteredTransformations,
            reset: reset
        };

        function setLoading(isLoading) {
            suggestionsState.isLoading = isLoading;
        }

        function setLineTransformations(lineTransformations) {
            suggestionsState.line = lineTransformations;
        }

        function setColumnTransformations(columnTransformations) {
            suggestionsState.column = columnTransformations || {
                    allSuggestions: [],
                    allTransformations: [],
                    filteredTransformations: [],
                    allCategories: null,
                    searchActionString: ''
                };
        }

        function updateFilteredTransformations(filteredTransformations) {
            suggestionsState.column.filteredTransformations = filteredTransformations;
        }

        function setTransformationsForEmptyCells(transformations) {
            suggestionsState.transformationsForEmptyCells = transformations;
        }

        function setTransformationsForInvalidCells(transformations) {
            suggestionsState.transformationsForInvalidCells = transformations;
        }

        function resetColumnSuggestions() {
            suggestionsState.column = {
                allSuggestions: [],
                allTransformations: [],
                filteredTransformations: [],
                allCategories: null,
                searchActionString: ''
            };
        }

        function resetLineSuggestions() {
            suggestionsState.line = {
                allTransformations: [],
                filteredTransformations: [],
                allCategories: null
            };
        }

        function reset() {
            suggestionsState.isLoading = false;

            resetLineSuggestions();
            resetColumnSuggestions();
        }
    }

    angular.module('data-prep.services.state')
        .service('SuggestionsStateService', SuggestionsStateService)
        .constant('suggestionsState', suggestionsState);
})();