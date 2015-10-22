(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @description Suggestion service. This service holds the selected suggestion tab
     */
    function SuggestionService(ColumnSuggestionService) {
        var tabIndex = {
            'TEXT': 0,
            'CELL': 1,
            'LINE': 2,
            'COLUMN': 3,
            'TABLE': 4
        };

        var service = {
            /**
             * @ngdoc property
             * @name tab
             * @propertyOf data-prep.services.transformation.service:SuggestionService
             * @description The currently Actions selected tab
             * @type {Object}
             */
            tab: null,
            /**
             * @ngdoc property
             * @name currentColumn
             * @propertyOf data-prep.services.transformation.service:SuggestionService
             * @description The currently selected column
             * @type {Object}
             */
            currentColumn: null,

            /**
             * @ngdoc property
             * @name showAllAction
             * @propertyOf data-prep.services.transformation.service:SuggestionService
             * @description show all actions or all of them
             * @type {Object}
             */
            showAllAction: false,

            /**
             * @ngdoc property
             * @name searchActionString
             * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
             * @description Actions to search
             */
            searchActionString: '',

            setColumn: setColumn,
            selectTab: selectTab,
            reset: reset
        };

        return service;



        /**
         * @ngdoc method
         * @name setColumn
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @param {object} column The new selected column
         * @description Set the selected column and init its suggested transformations and reset action search
         */
        function setColumn(column) {

            resetSearchAction();

            if (column === service.currentColumn) {
                return;
            }

            service.currentColumn = column;
            ColumnSuggestionService.initTransformations(column, service.showAllAction);
        }

        /**
         * @ngdoc method
         * @name selectTab
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @param {String} tab The tab to select
         * @description Set the selected tab
         */
        function selectTab(tab) {
            service.tab = tabIndex[tab];
        }

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @description Reset the suggestions
         */
        function reset() {
            service.currentColumn = null;
            ColumnSuggestionService.reset();
        }


        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @description Reset the suggestions
         */
        function resetSearchAction() {
            service.searchActionString = '';

            if(!service.showAllAction) {
                angular.forEach(ColumnSuggestionService.transformations, function(item){
                    item.labelHtml = item.label;
                });
            } else {
                angular.forEach(ColumnSuggestionService.transformations, function(transfo){
                    angular.forEach(transfo, function(item) {
                        item.categoryHtml = item.category.toUpperCase();
                    });
                });
                ColumnSuggestionService.updateTransformations();
            }
        }
    }

    angular.module('data-prep.services.transformation')
        .service('SuggestionService', SuggestionService);
})();