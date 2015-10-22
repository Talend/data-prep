(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:TransformationCacheService
     * @description Transformation cache service. This service provide the entry point to get transformations suggestions.
     * It holds a cache that should be invalidated at each new playground load
     * @requires data-prep.services.transformation.service:TransformationService
     */
    function TransformationCacheService($q, TransformationService) {
        var suggestionsCache = [];

        /**
         * @ngdoc method
         * @name getKey
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @param {object} column The column to set as key
         * @description [PRIVATE] Generate a unique key for the column.
         */
        var getKey = function getKey(column, showAll) {
            return showAll? JSON.stringify(column)+ '_showAll': JSON.stringify(column);
        };

        /**
         * @ngdoc method
         * @name getTransformations
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @param {object} column The transformations target column
         * @param {boolean} showAll show all transformation or some of them
         * @description Get transformations from cache if present, from REST call otherwise.
         * It clean and adapt them.
         */
        this.getTransformations = function getTransformations(column, showAll) {
            var key = getKey(column, showAll);

            //if cache contains the key, the value is either the values or the fetch promise
            var menus = suggestionsCache[key];
            if(menus) {
                return $q.when(menus);
            }

            //fetch menus from REST and adapt them. The Promise is put in cache, it is then replaced by the value.
            var fetchPromise = TransformationService.getTransformations(JSON.stringify(column), showAll)
                .then(function(menus) {
                    suggestionsCache[key] = menus;
                    return menus;
                });

            suggestionsCache[key] = fetchPromise;
            return fetchPromise;
        };

        /**
         * @ngdoc method
         * @name invalidateCache
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @description Invalidate all cache entries
         */
        this.invalidateCache = function invalidateCache() {
            suggestionsCache = [];
        };
    }

    angular.module('data-prep.services.transformation')
        .service('TransformationCacheService', TransformationCacheService);
})();