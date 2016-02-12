/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationCacheService
 * @description Transformation cache service. This service provide the entry point to get transformations suggestions.
 * It holds a cache that should be invalidated at each new playground load
 * @requires data-prep.services.transformation.service:TransformationService
 */
export default function TransformationCacheService($q, $cacheFactory, TransformationService) {
    'ngInject';

    var transformationsCache = $cacheFactory('transformationsCache', {capacity: 10});
    var suggestionsCache = $cacheFactory('suggestionsCache', {capacity: 10});

    return {
        invalidateCache: invalidateCache,
        getColumnSuggestions: getColumnSuggestions,
        getColumnTransformations: getColumnTransformations,
        getLineTransformations: getLineTransformations
    };

    /**
     * @ngdoc method
     * @name getColumnKey
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {object} column The column to set as key
     * @description [PRIVATE] Generate a unique key for the column.
     */
    function getColumnKey(column) {
        return JSON.stringify(column);
    }

    function getValue(key, cache, restCall) {
        //if cache contains the key, the value is either the values or the fetch promise
        var value = cache.get(key);
        if (value) {
            return $q.when(value);
        }

        //fetch value from REST and adapt them. The Promise is put in cache, it is then replaced by the value.
        var fetchPromise = restCall()
            .then(function (value) {
                cache.put(key, value);
                return value;
            });

        cache.put(key, fetchPromise);
        return fetchPromise;
    }

    /**
     * @ngdoc method
     * @name getLineTransformations
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @description Get transformations from cache if present, from REST call otherwise.
     */
    function getLineTransformations() {
        return getValue('line', transformationsCache, TransformationService.getLineTransformations.bind(null));
    }

    /**
     * @ngdoc method
     * @name getColumnTransformations
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {object} column The transformations target column
     * @description Get transformations from cache if present, from REST call otherwise.
     */
    function getColumnTransformations(column) {
        return getValue(getColumnKey(column), transformationsCache, TransformationService.getColumnTransformations.bind(null, column));
    }

    /**
     * @ngdoc method
     * @name getColumnTransformations
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {object} column The transformations target column
     * @description Get suggestions from cache if present, from REST call otherwise.
     */
    function getColumnSuggestions(column) {
        return getValue(getColumnKey(column), suggestionsCache, TransformationService.getColumnSuggestions.bind(null, column));
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