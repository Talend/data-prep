(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:ColumnSuggestionService
     * @description Transformation Column suggestion service. This service provide the current column suggestions
     * @requires data-prep.services.transformation.service:TransformationCacheService
     * @requires data-prep.services.utils.service:TextFormatService
     * @requires data-prep.state.service:StateService
     */
    function ColumnSuggestionService(state, $q, TransformationCacheService, TextFormatService, StateService) {
        var FILTERED_CATEGORY = 'filtered';
        var SUGGESTION_CATEGORY = 'suggestion';
        var EMPTY_CELLS = 'empty';
        var INVALID_CELLS = 'invalid';

        return {
            initTransformations: initTransformations,
            filterTransformations: filterTransformations
        };

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------INIT------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        function resetDisplayLabels(transformations) {
            _.forEach(transformations, function (transfo) {
                transfo.labelHtml = transfo.label + (transfo.parameters || transfo.dynamic ? '...' : '');
            });
        }

        function isAppliedToCells(type) {
            return function (item) {
                return item.actionScope && (item.actionScope.indexOf(type) !== -1);
            };
        }

        /**
         * @ngdoc method
         * @name prepareTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {object} suggestions Suggested transformations category
         * @param {array} transformationsCategories All transformations grouped by category
         * @description Keep only non 'filtered' categories and add suggestions before
         * @returns {object} The transformations grouped by category without the 'filtering' category
         */
        function prepareTransformations(suggestions, transformationsCategories) {
            var groupedTransfoWithoutFilterCat = _.filter(transformationsCategories, function (item) {
                return item.category !== FILTERED_CATEGORY;
            });

            return [suggestions].concat(groupedTransfoWithoutFilterCat);
        }

        /**
         * @ngdoc method
         * @name prepareSuggestions
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {array} suggestions Suggested transformations list
         * @param {array} transformationsCategories All transformations grouped by category
         * @description Add 'filtered' category in suggestions
         * @returns {object} The suggestions category containing suggestions and 'filtered' category transformations
         */
        function prepareSuggestions(suggestions, transformationsCategories) {
            var filterCategory = _.find(transformationsCategories, {category: FILTERED_CATEGORY});

            return {
                category: SUGGESTION_CATEGORY,
                categoryHtml: SUGGESTION_CATEGORY.toUpperCase(),
                transformations: (filterCategory ? filterCategory.transformations : []).concat(suggestions)
            };
        }

        /**
         * @ngdoc method
         * @name initTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {object} column The target column
         * @description Get and process the transformations from backend
         */
        function initTransformations(column) {
            StateService.setSuggestionsLoading(true);
            StateService.setColumnTransformations(); //clear current transformations

            $q
                .all([
                    TransformationCacheService.getColumnSuggestions(column),
                    TransformationCacheService.getColumnTransformations(column)
                ])
                .then(function (values) {
                    var suggestions = prepareSuggestions(values[0], values[1].allCategories);
                    var allCategories = prepareTransformations(suggestions, values[1].allCategories);

                    var colTransformations = {
                        allSuggestions: values[0],
                        allTransformations: values[1].allTransformations,
                        filteredTransformations: allCategories,
                        allCategories: allCategories,
                        searchActionString: ''
                    };
                    StateService.setColumnTransformations(colTransformations);

                    if(!state.playground.suggestions.transformationsForEmptyCells.length) {
                        var transfosForEmptyCells = _.filter(values[1].allTransformations, isAppliedToCells(EMPTY_CELLS));
                        StateService.setTransformationsForEmptyCells(transfosForEmptyCells);
                    }

                    if(!state.playground.suggestions.transformationsForInvalidCells.length) {
                        var transfosForInvalidCells = _.filter(values[1].allTransformations, isAppliedToCells(INVALID_CELLS));
                        StateService.setTransformationsForInvalidCells(transfosForInvalidCells);
                    }
                })
                .finally(function () {
                    StateService.setSuggestionsLoading(false);
                });
        }

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------FILTER----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        function categoryMatchSearch(category, searchValue) {
            return category.toLowerCase().indexOf(searchValue) !== -1;
        }

        function transfosMatchSearch(searchValue) {
            return function (transfo) {
                return transfo.labelHtml.toLowerCase().indexOf(searchValue) !== -1 ||
                    transfo.description.toLowerCase().indexOf(searchValue) !== -1;
            };
        }

        function extractTransfosThatMatch(searchValue) {
            return function (catTransfos) {
                var category = catTransfos.category;
                var transformations = catTransfos.transformations;

                //category matches : display all this category transformations
                //category does NOT match : filter to only have matching displayed label or description
                if (!categoryMatchSearch(category, searchValue)) {
                    transformations = _.filter(transformations, transfosMatchSearch(searchValue));
                }

                return {
                    category: category,
                    categoryHtml: category.toUpperCase(),
                    transformations: transformations
                };
            };
        }

        function hasTransformations(catTransfo) {
            return catTransfo.transformations.length;
        }

        function highlight(object, key, highlightText) {
            var originalValue = object[key];
            if (originalValue.toLowerCase().indexOf(highlightText) !== -1) {
                object[key] = originalValue.replace(
                    new RegExp('(' + TextFormatService.escapeRegex(highlightText) + ')', 'gi'),
                    '<span class="highlighted">$1</span>');
            }
        }

        function highlightDisplayedLabels(searchValue) {
            return function (catTransfo) {
                highlight(catTransfo, 'categoryHtml', searchValue);
                _.forEach(catTransfo.transformations, function (transfo) {
                    highlight(transfo, 'labelHtml', searchValue);
                });
                return catTransfo;
            };
        }

        /**
         * @ngdoc method
         * @name filterTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @description Filter transformations list by searchString
         */
        function filterTransformations() {
            resetDisplayLabels(state.playground.suggestions.column.allSuggestions);
            resetDisplayLabels(state.playground.suggestions.column.allTransformations);


            var searchValue = state.playground.suggestions.column.searchActionString.toLowerCase();
            StateService.updateFilteredTransformations(!searchValue ?
                state.playground.suggestions.column.allCategories :
                _.chain(state.playground.suggestions.column.allCategories)
                    .map(extractTransfosThatMatch(searchValue))
                    .filter(hasTransformations)
                    .map(highlightDisplayedLabels(searchValue))
                    .value());
        }
    }

    angular.module('data-prep.services.transformation')
        .service('ColumnSuggestionService', ColumnSuggestionService);
})();