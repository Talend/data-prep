(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @description Suggestion service. This service holds the selected suggestion tab
     */
    function SuggestionService(LineSuggestionService, ColumnSuggestionService) {
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

            setColumn: setColumn,
            setLine: setLine,
            selectTab: selectTab
        };

        return service;

        /**
         * @ngdoc method
         * @name setColumn
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @param {object} column The new selected column
         * @description Set the selected column and init its suggested transformations
         */
        function setColumn(column) {
            ColumnSuggestionService.initTransformations(column);
        }

        /**
         * @ngdoc method
         * @name setLine
         * @methodOf data-prep.services.transformation.service:SuggestionService
         * @param {object} line The new selected line
         * @description Set the selected line and init its suggested transformations
         */
        function setLine(line) {
            if(line) {
                LineSuggestionService.initTransformations();
            }
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
    }

    angular.module('data-prep.services.transformation')
        .service('SuggestionService', SuggestionService);
})();