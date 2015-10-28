(function() {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.transformation.service:TransformationCacheService
     * @description Transformation cache service. This service provide the entry point to get transformations suggestions.
     * It holds a cache that should be invalidated at each new playground load
     * @requires data-prep.services.transformation.service:TransformationService
     */
    function TransformationCacheService($q, $cacheFactory, TransformationService) {
        var transformationsCache = $cacheFactory('transformationsCache', {capacity: 10});
        var suggestionsCache = $cacheFactory('suggestionsCache', {capacity: 10});

        return {
            invalidateCache: invalidateCache,
            getSuggestions: getSuggestions,
            getTransformations: getTransformations
        };

        /**
         * @ngdoc method
         * @name getKey
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @param {object} column The column to set as key
         * @description [PRIVATE] Generate a unique key for the column.
         */
        function getKey(column) {
            return JSON.stringify(column);
        }

        function getValue(column, cache, restCall) {
            var key = getKey(column);

            //if cache contains the key, the value is either the values or the fetch promise
            var value = cache.get(key);
            if(value) {
                return $q.when(value);
            }

            //fetch value from REST and adapt them. The Promise is put in cache, it is then replaced by the value.
            var fetchPromise = restCall(column)
                .then(function(value) {
                    cache.put(key, value);
                    return value;
                });

            cache.put(key, fetchPromise);
            return fetchPromise;
        }

        /**
         * @ngdoc method
         * @name getTransformations
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @param {object} column The transformations target column
         * @description Get transformations from cache if present, from REST call otherwise.
         */
        function getTransformations(column) {
            return getValue(column, transformationsCache, TransformationService.getTransformations);
        }

        /**
         * @ngdoc method
         * @name getTransformations
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @param {object} column The transformations target column
         * @description Get suggestions from cache if present, from REST call otherwise.
         */
        function getSuggestions(column) {
            return getValue(column, suggestionsCache, TransformationService.getSuggestions);
        }

        /**
         * @ngdoc method
         * @name invalidateCache
         * @methodOf data-prep.services.transformation.service:TransformationCacheService
         * @description Invalidate all cache entries
         */
        function invalidateCache() {
            transformationsCache.removeAll();
            suggestionsCache.removeAll();
        }
    }

    angular.module('data-prep.services.transformation')
        .service('TransformationCacheService', TransformationCacheService);
})();