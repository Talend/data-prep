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
 * @name data-prep.services.statistics.service:StatisticsRestService
 * @description Statistics REST service.
 */
export default function StatisticsRestService($q, $http, $cacheFactory, RestURLs) {
	'ngInject';

	const aggregationCache = $cacheFactory('aggregationStatistics', { capacity: 5 });

    /**
     * @ngdoc method
     * @name resetCache
     * @methodOf data-prep.services.statistics.service:StatisticsRestService
     * @description Reset the cache
     */
	this.resetCache = function resetCache() {
		aggregationCache.removeAll();
	};

    /**
     * @ngdoc method
     * @name getAggregations
     * @methodOf data-prep.services.statistics.service:StatisticsRestService
     * @param {object} parameters The aggregation parameters
     * @description Fetch the aggregation on a column of the dataset
     * @returns {Promise} The POST promise
     */
	this.getAggregations = function (parameters) {
		const cacheKey = JSON.stringify(parameters);
		const resultFromCache = aggregationCache.get(cacheKey);

		return resultFromCache ?
            $q.when(resultFromCache) :
            $http.post(RestURLs.aggregationUrl, parameters)
                .then((response) => {
	aggregationCache.put(cacheKey, response.data);
	return response.data;
});
	};
}
