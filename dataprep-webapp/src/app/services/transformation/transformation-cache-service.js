/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationCacheService
 * @description Transformation cache service.
 * It holds a cache that should be invalidated at each new playground load
 */
export default class TransformationCacheService {
	constructor($cacheFactory) {
		'ngInject';
		this.transformationsCache = $cacheFactory('transformationsCache', { capacity: 10 });
		this.suggestionsCache = $cacheFactory('suggestionsCache', { capacity: 10 });
	}

    /**
     * @ngdoc method
     * @name getKey
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {string} scope The scope
     * @param {object} entity The entity to set as key
     * @description [PRIVATE] Generate a unique key for the scope/entity.
     */
	getKey(scope, entity) {
		return scope + JSON.stringify(entity);
	}

    /**
     * @ngdoc method
     * @name getTransformations
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {string} scope The transformations scope
     * @param {object} entity The entity key
     * @description Get transformations from cache if present
     */
	getTransformations(scope, entity) {
		const key = this.getKey(scope, entity);
		return this.transformationsCache.get(key);
	}

    /**
     * @ngdoc method
     * @name getSuggestions
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {string} scope The suggestions scope
     * @param {object} entity The entity key
     * @description Get suggestions from cache if present
     */
	getSuggestions(scope, entity) {
		const key = this.getKey(scope, entity);
		return this.suggestionsCache.get(key);
	}

    /**
     * @ngdoc method
     * @name setTransformations
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {string} scope The transformations scope
     * @param {object} entity The entity key
     * @param {object} transformations The transformations to cache
     * @description Set transformations in cache
     */
	setTransformations(scope, entity, transformations) {
		const key = this.getKey(scope, entity);
		this.transformationsCache.put(key, transformations);
	}

    /**
     * @ngdoc method
     * @name setSuggestions
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @param {string} scope The suggestions scope
     * @param {object} entity The entity key
     * @param {object} suggestions The suggestions to cache
     * @description Set suggestions in cache
     */
	setSuggestions(scope, entity, suggestions) {
		const key = this.getKey(scope, entity);
		return this.suggestionsCache.put(key, suggestions);
	}

    /**
     * @ngdoc method
     * @name invalidateCache
     * @methodOf data-prep.services.transformation.service:TransformationCacheService
     * @description Invalidate all cache entries
     */
	invalidateCache() {
		this.transformationsCache.removeAll();
		this.suggestionsCache.removeAll();
	}
}
