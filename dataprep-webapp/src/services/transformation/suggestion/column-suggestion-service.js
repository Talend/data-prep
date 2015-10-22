(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:ColumnSuggestionService
     * @description Transformation Column suggestion service. This service provide the current column suggestions
     * @requires data-prep.services.transformation.service:TransformationCacheService
     * @requires data-prep.services.utils.service:ConverterService
     */
    function ColumnSuggestionService(TransformationCacheService) {
        var COLUMN_CATEGORY = 'column_metadata';
        var self = this;

        /**
         * @ngdoc property
         * @name transformations
         * @propertyOf data-prep.services.transformation.service:ColumnSuggestionService
         * @description The currently selected column transformations
         * @type {Object}
         */
        self.transformations = null;


        //Sort by object key
        function sortObject(o) {
            var sorted = {},
                key, a = [];

            for (key in o) {
                if (o.hasOwnProperty(key)) {
                    a.push(key);
                }
            }

            a.sort();

            for (key = 0; key < a.length; key++) {
                sorted[a[key]] = o[a[key]];
            }
            return sorted;
        }

        /**
         * @ngdoc method
         * @name filterAndGroup
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {object[]} transfos The transformations list
         * @param {boolean} showAll show all transformation or some of them
         * @description Keep only the non 'columns' category and group them by category
         * @returns {object} An object containing {key: value} = {category: [transformations]}
         */
        function filterAndGroup(transfos, showAll) {

            //Add labelHtml which is copy of label in order to manage the highlight action label
            angular.forEach(transfos, function(item){
                item.labelHtml= item.label;
                item.categoryHtml= item.category.toUpperCase();
            });

            if (showAll) {
                var transfosFiltered = _.chain(transfos)
                                        .filter(function (transfo) {
                                            return transfo.category !== COLUMN_CATEGORY;
                                        })
                                        .sortBy(function (transfo) {
                                            return transfo.label.toLowerCase();
                                        })
                                        .value();
                return sortObject(_.groupBy(transfosFiltered, function(action){ return action.categoryHtml;}));
            }
            return _.chain(transfos)
                .filter(function (transfo) {
                    return transfo.category !== COLUMN_CATEGORY;
                })
                .value();
        }

        /**
         * @ngdoc method
         * @name initTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @param {object} column The target column
         * @param {boolean} showAll show all transformation or some of them
         * @description Get and preparation the transformations from backend
         */
        this.initTransformations = function initTransformations(column, showAll) {
            self.transformations = null;
            TransformationCacheService.getTransformations(column, showAll)
                .then(function (transformations) {
                    self.transformations = filterAndGroup(transformations, showAll);
                });
        };

        /**
         * @ngdoc method
         * @name updateTransformations
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @description update self.transformations keys when highlighting
         */
        this.updateTransformations = function updateTransformations() {
            var transfos = _.flatten(_.values(self.transformations));
            var transfosFiltered = _.chain(transfos)
                .filter(function (transfo) {
                    return transfo.category !== COLUMN_CATEGORY;
                })
                .sortBy(function (transfo) {
                    return transfo.label.toLowerCase();
                })
                .value();
            self.transformations =  sortObject(_.groupBy(transfosFiltered, function(action){ return action.categoryHtml;}));
        };

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.transformation.service:ColumnSuggestionService
         * @description Reset the current column and the transformations
         */
        this.reset = function reset() {
            self.transformations = null;
        };
    }

    angular.module('data-prep.services.transformation')
        .service('ColumnSuggestionService', ColumnSuggestionService);
})();