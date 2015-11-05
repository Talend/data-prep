(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:ColumnSuggestionService
     * @description Transformation Column suggestion service. This service provide the current column suggestions
     * @requires data-prep.services.transformation.service:TransformationCacheService
     * @requires data-prep.services.utils.service:TextFormatService
     */
    function ColumnSuggestionService($q, TransformationCacheService, TextFormatService) {
        var COLUMN_CATEGORY = 'column_metadata';
        var FILTERED_CATEGORY = 'filtered';
        var SUGGESTION_CATEGORY = 'suggestion';
        var EMPTY_CELLS = 'empty';
        var INVALID_CELLS = 'invalid';

        var allCategories = null;
        var service = {
            allTransformations: [],                 // all selected column transformations
            allSuggestions: [],                     // all selected column suggestions
            transformationsForEmptyCells: [],       // all column transformations applied to empty cells
            transformationsForInvalidCells: [],     // all column transformations applied to invalid cells
            searchActionString: '',                 // current user input to filter transformations
            filteredTransformations: null,          // categories with their transformations to display, result of filter

            initTransformations: initTransformations,
            filterTransformations: filterTransformations,
            reset: reset
        };
        return service;

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------INIT------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        function setHtmlDisplayLabels(transformations) {
            _.forEach(transformations, function(transfo) {
                transfo.labelHtml = transfo.label + (transfo.parameters || transfo.dynamic ? '...' : '');
            });
        }

        function isNotColumnCategory(category) {
            return function (item) {
                return item.category !== category;
            };
        }

        function isColumnCategory(category) {
            return function (item) {
                return item.category === category;
            };
        }

        function labelCriteria(transfo) {
            return transfo.label.toLowerCase();
        }


        function isAplliedToCells(type) {
            return function (item) {
                return item.actionScope && (item.actionScope.indexOf(type) !== -1);
            };
        }

        /**
         * @ngdoc method
         * @name prepareTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {object} suggestions Suggested transformations category
         * @param {array} transformations All transformations list
         * @description Keep only the non 'column_metadata', non 'filtered' categories and group them by category, then add suggestions before
         * @returns {object} An object containing {key: value} = {category: [transformations]}
         */
        function prepareTransformations(suggestions, transformations) {
            var groupedTransformations = _.chain(transformations)
                .filter(isNotColumnCategory(COLUMN_CATEGORY))
                .filter(isNotColumnCategory(FILTERED_CATEGORY))
                .sortBy(labelCriteria)
                .groupBy('category')
                .value();

            var adaptedTransformations = _.chain(Object.getOwnPropertyNames(groupedTransformations))
                .sort()
                .map(function(key) {
                    return {
                        category: key,
                        categoryHtml: key.toUpperCase(),
                        transformations: groupedTransformations[key]
                    };
                })
                .value();

            return [suggestions].concat(adaptedTransformations);
        }

        /**
         * @ngdoc method
         * @name prepareSuggestions
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {array} suggestions Suggested transformations list
         * @param {array} transformations All transformations list
         * @description Add 'filtered' category in suggestions
         * @returns {object} The suggestions category containing real suggestions and 'filtered' category transformations
         */
        function prepareSuggestions(suggestions, transformations) {

            var adaptedFilteredTransformations = _.chain(transformations)
                .filter(isColumnCategory(FILTERED_CATEGORY))
                .sortBy(labelCriteria)
                .value();

            var adaptedSuggestions = _.chain(suggestions)
                .filter(isNotColumnCategory(COLUMN_CATEGORY))
                .value();

            return {
                category: SUGGESTION_CATEGORY,
                categoryHtml: SUGGESTION_CATEGORY.toUpperCase(),
                transformations: adaptedFilteredTransformations.concat(adaptedSuggestions)
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
            reset();

            $q
                .all([
                    TransformationCacheService.getSuggestions(column),
                    TransformationCacheService.getTransformations(column)
                ])
                .then(function (values) {
                    setHtmlDisplayLabels(values[0]);
                    setHtmlDisplayLabels(values[1]);

                    var suggestions = prepareSuggestions(values[0], values[1]);
                    allCategories = prepareTransformations(suggestions, values[1]);
                    service.filteredTransformations = allCategories;
                    service.allSuggestions = values[0];
                    service.allTransformations = values[1];

                    service.transformationsForEmptyCells = _.chain(values[1])
                                                            .filter(isAplliedToCells(EMPTY_CELLS))
                                                            .sortBy(labelCriteria)
                                                            .value();
                    service.transformationsForInvalidCells = _.chain(values[1])
                                                              .filter(isAplliedToCells(INVALID_CELLS))
                                                              .sortBy(labelCriteria)
                                                              .value();
                });
        }

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------FILTER----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        function categoryMatchSearch(category, searchValue) {
            return category.toLowerCase().indexOf(searchValue) !== -1;
        }

        function transfosMatchSearch(searchValue) {
            return function(transfo) {
                return transfo.labelHtml.toLowerCase().indexOf(searchValue) !== -1 ||
                    transfo.description.toLowerCase().indexOf(searchValue) !== -1;
            };
        }

        function extractTransfosThatMatch(searchValue) {
            return function(catTransfos) {
                var category = catTransfos.category;
                var transformations = catTransfos.transformations;

                //category matches : display all this category transformations
                //category does NOT match : filter to only have matching displayed label or description
                if(! categoryMatchSearch(category, searchValue)) {
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
            return function(catTransfo) {
                highlight(catTransfo, 'categoryHtml', searchValue);
                _.forEach(catTransfo.transformations, function(transfo) {
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
            setHtmlDisplayLabels(service.allSuggestions);
            setHtmlDisplayLabels(service.allTransformations);

            var searchValue = service.searchActionString.toLowerCase();

            service.filteredTransformations = !searchValue ?
                allCategories :
                _.chain(allCategories)
                    .map(extractTransfosThatMatch(searchValue))
                    .filter(hasTransformations)
                    .map(highlightDisplayedLabels(searchValue))
                    .value();
        }

        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------------RESET-----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @description Reset the current column and the transformations
         */
        function reset() {
            service.allTransformations = [];
            service.allSuggestions = [];
            service.searchActionString = '';
            service.filteredTransformations = null;
            allCategories = null;
        }
    }

    angular.module('data-prep.services.transformation')
        .service('ColumnSuggestionService', ColumnSuggestionService);
})();