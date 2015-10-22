(function () {
    'use strict';
    /**
     * @ngdoc service
     * @name data-prep.services.statistics.service:StatisticsService
     * @description Extracts/structures the data to be visualized in charts
     * @requires data-prep.services.playground.service:DatagridService
     * @requires data-prep.services.filter.service:FilterService
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.statistics.service:StatisticsRestService
     * @requires data-prep.services.utils.service:ConverterService
     * @requires data-prep.services.utils.service:TextFormatService
     * @requires data-prep.services.utils.service:StorageService
     */
    function StatisticsService($timeout, $filter, state,
                               DatagridService, FilterService, RecipeService, StatisticsRestService,
                               ConverterService, TextFormatService, StorageService) {

        var service = {
            boxPlot: null,
            histogram: null,
            stateDistribution: null,
            statistics: null,

            //filters
            addRangeFilter: addRangeFilter,

            //statistics entry points
            processData: processData,                       // basic charts
            processAggregation: processAggregation,         // aggregation charts
            getAggregationColumns: getAggregationColumns,   // possible aggregation columns
            updateStatistics: updateStatistics,             // update stats + trigger chart
            reset: reset,                                   // reset charts/statistics/cache

            //TODO temporary method to be replaced with new geo chart
            getGeoDistribution: getGeoDistribution
        };

        return service;

        //
        // BELOW ARE ALL THE STATISTICS TABS FUNCTIONS FOR (1-CHART, 2-VALUES, 3-PATTERN, 4-OTHERS)
        //

        //--------------------------------------------------------------------------------------------------------------
        //-----------------------------------------------1- Visualization-----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * TEMPORARY : Calculate column value distribution
         * @param columnId
         * @param keyName - distribution key name (default : 'colValue');
         * @param valueName - distribution value name (default : 'frequency')
         * @param keyTransformer - transformer applied to the distribution key
         * @returns {Array} Column distribution array {colValue: string, frequency: integer}}
         * <ul>
         *     <li>colValue (or specified) : the grouped value</li>
         *     <li>frequency (or specified) : the nb of time the value appears</li>
         * </ul>
         */
        function getDistribution(columnId, keyName, valueName, keyTransformer) {
            keyName = keyName || 'colValue';
            valueName = valueName || 'frequency';

            var records = state.playground.data.records;

            return _.chain(records)
                .groupBy(function (item) {
                    return item[columnId];
                })
                .map(function (val, index) {
                    var item = {};
                    item[keyName] = keyTransformer ? keyTransformer(index) : index;
                    item[valueName] = val.length;
                    return item;
                })
                .sortBy(valueName)
                .reverse()
                .value();
        }

        /**
         * TEMPORARY : Calculate geo distribution, and targeted map
         * @param {object} column The target column
         * @returns {object} Geo distribution {map: string, data: [{}]}
         */
        function getGeoDistribution(column) {
            var keyPrefix = 'US-';
            var map = 'countries/us/us-all';

            return {
                map: map,
                data: getDistribution(column.id, 'hc-key', 'value', function (key) {
                    return keyPrefix + key;
                })
            };
        }

        /**
         * @ngdoc method
         * @name initRangeHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {Array} histoData Array of data
         * @description Adapt the numeric range data to fit histogram format
         */
        function initRangeHistogram(histoData) {
            var rangeData = _.map(histoData, function (histDatum) {
                return {
                    'data': [histDatum.range.min, histDatum.range.max],
                    'occurrences': histDatum.occurrences
                };
            });

            initVerticalHistogram('occurrences', 'Occurrences', rangeData);
        }

        /**
         * @ngdoc method
         * @name initClassicHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} key The value key
         * @param {string} label The value label
         * @param {Array} dataTable The table to display
         * @description Set the frequency table that fit the histogram format
         */
        function initClassicHistogram(key, label, dataTable) {
            service.histogram = {
                data: _.map(dataTable, function (rec) {
                    rec.formattedValue = TextFormatService.adaptToGridConstraints(rec.data);
                    return rec;
                }),
                key: key,
                label: label,
                column: state.playground.column
            };
        }

        /**
         * @ngdoc method
         * @name initVerticalHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} key The value key
         * @param {string} label The value label
         * @param {Array} dataTable The table to display
         * @description Set the records frequency ranges table that fit the histogram format
         */
        function initVerticalHistogram(key, label, dataTable) {
            service.histogram = {
                data: dataTable,
                key: key,
                label: label,
                column: state.playground.column,
                activeLimits: null,
                vertical: true
            };
        }

        /**
         * @ngdoc method
         * @name getValueWithinRange
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Get the corresponding value within [min, max] interval.
         * If the value is not in the interval, we return min or max if it is under min or above max respectively
         */
        function getValueWithinRange(value, min, max) {
            if (value < min) {
                return min;
            }
            if (value > max) {
                return max;
            }
            return value;
        }

        /**
         * @ngdoc method
         * @name initRangeLimits
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Set the range slider limits to update the rangeSlider handlers
         * and the active/inactive bars of the vertical barchart
         */
        function initRangeLimits() {
            var column = state.playground.column;
            var statistics = column.statistics;
            var currentRangeFilter = _.find(FilterService.filters, function (filter) {
                return filter.colId === column.id && filter.type === 'inside_range';
            });

            var rangeLimits = {
                min: statistics.min,
                max: statistics.max
            };

            if (currentRangeFilter) {
                var filterMin = currentRangeFilter.args.interval[0];
                var filterMax = currentRangeFilter.args.interval[1];

                rangeLimits.minFilterVal = filterMin;
                rangeLimits.maxFilterVal = filterMax;

                rangeLimits.minBrush = getValueWithinRange(filterMin, statistics.min, statistics.max);
                rangeLimits.maxBrush = getValueWithinRange(filterMax, statistics.min, statistics.max);

                service.histogram.activeLimits = [rangeLimits.minBrush, rangeLimits.maxBrush];
            }

            service.rangeLimits = rangeLimits;
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------2- Values----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name clean
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {number} value Value to clean the float
         * @description Cleans the value to have 2 decimals (5.2568845842587425588 -> 5.25)
         * @returns {number} The value in the clean format
         */
        function clean(value) {
            return isNaN(value) || value === parseInt(value, 10) ? value : +value.toFixed(2);
        }

        /**
         * @ngdoc method
         * @name initStatisticsValues
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {object} column The target column
         * @description Initialize the statistics to display in the values TAB of the stats part
         */
        function initStatisticsValues(column) {
            if (!column.statistics) {
                return;
            }

            var stats = column.statistics;
            var colType = ConverterService.simplifyType(column.type);
            var commonStats = {
                COUNT: stats.count,
                DISTINCT_COUNT: stats.distinctCount,
                DUPLICATE_COUNT: stats.duplicateCount,

                VALID: stats.valid,
                EMPTY: stats.empty,
                INVALID: stats.invalid
            };

            var specificStats = {};
            switch (colType) {
                case 'integer':
                case 'decimal':
                    specificStats.MIN = clean(stats.min);
                    specificStats.MAX = clean(stats.max);
                    specificStats.MEAN = clean(stats.mean);
                    specificStats.VARIANCE = clean(stats.variance);
                    if (stats.quantiles.lowerQuantile !== 'NaN') {
                        specificStats.MEDIAN = clean(stats.quantiles.median);
                        specificStats.LOWER_QUANTILE = clean(stats.quantiles.lowerQuantile);
                        specificStats.UPPER_QUANTILE = clean(stats.quantiles.upperQuantile);
                    }

                    break;
                case 'text':
                    specificStats.AVG_LENGTH = clean(stats.textLengthSummary.averageLength);
                    specificStats.MIN_LENGTH = stats.textLengthSummary.minimalLength;
                    specificStats.MAX_LENGTH = stats.textLengthSummary.maximalLength;
                    break;
            }

            service.statistics = {
                common: commonStats,
                specific: specificStats
            };
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------3- Pattern---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        //Currently there are no stats to be brought on the pattern data

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------4- Others----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name updateBoxplotData
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Gathers the boxPlot data from the specific stats of the columns having a 'number' type
         */
        function updateBoxplotData() {
            var specStats = service.statistics.specific;

            //waiting for DQ to process negative values
            if (specStats.LOWER_QUANTILE) {
                service.boxPlot = {
                    min: specStats.MIN,
                    max: specStats.MAX,
                    q1: specStats.LOWER_QUANTILE,
                    q2: specStats.UPPER_QUANTILE,
                    median: specStats.MEDIAN,
                    mean: specStats.MEAN,
                    variance: specStats.VARIANCE
                };
            }
            else {
                service.boxPlot = null;
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------FILTER-------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name addRangeFilter
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {Array} interval of the filter
         * @description Adds a rangefilter in the angular context
         */
        function addRangeFilter(interval) {
            var selectedColumn = state.playground.column;
            var removeFilterFn = function removeFilterFn(filter) {
                if (selectedColumn && filter.colId === selectedColumn.id) {
                    initRangeLimits();
                    //to reset the bars colors
                    service.histogram.activeLimits = [selectedColumn.statistics.min, selectedColumn.statistics.max];
                }
            };

            var filterFn = FilterService.addFilter.bind(null, 'inside_range', selectedColumn.id, selectedColumn.name, {interval: interval}, removeFilterFn);
            $timeout(function () {
                filterFn();
                initRangeLimits();
            });
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------NON AGGREGATION--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name processMapData
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {object} column The column to visualize
         * @description Remove the previous charts data and set the map chart
         */
        function processMapData(column) {
            service.stateDistribution = column;
        }

        /**
         * @ngdoc method
         * @name processNonMapData
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {object} column The column to visualize
         * @description Reset the map chart and calculate the needed data for visualization
         */
        function processNonMapData(column) {
            var simplifiedType = ConverterService.simplifyType(column.type);
            switch (simplifiedType) {
                case 'integer':
                case 'decimal':
                    initRangeHistogram(column.statistics.histogram);
                    updateBoxplotData();
                    initRangeLimits();
                    break;
                case 'text':
                case 'boolean':
                    initClassicHistogram('occurrences', 'Occurrences', column.statistics.frequencyTable);
                    break;
                default :
                    console.log('nor a number neither a boolean neither a string but a ' + simplifiedType);
            }
        }

        /**
         * @ngdoc method
         * @name processData
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Processes the statistics data for visualization on the selected column
         */
        function processData() {
            var column = state.playground.column;
            reset(true, false, false);

            //TODO replace with new geo chart
            if (column.domain.indexOf('STATE_CODE') !== -1) {
                processMapData(column);
            }
            //TODO Coming soon after the integration of the globe map : reset charts and init localization chart data
            // else if (column.domain === 'LOCALIZATION') {
            //    processLocalizationMapData(column);
            //}
            else {
                processNonMapData(column);
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------Aggregation--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name processAggregation
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {object} column The column to visualize
         * @param {object} aggregation The column to visualize
         * @description Processes the statistics aggregation for visualization
         */
        function processAggregation(column, aggregation) {
            if (!aggregation) {
                removeSavedColumnAggregation();
                return processData();
            }

            reset(true, false, false);
            var datasetId = state.playground.dataset.id;
            var preparationId = state.playground.preparation && state.playground.preparation.id;
            var stepId = preparationId && RecipeService.getLastActiveStep() && RecipeService.getLastActiveStep().id;
            var selectedColumn = state.playground.column;

            var aggregationParameters = {
                datasetId: preparationId ? null : datasetId,
                preparationId: preparationId,
                stepId: stepId,
                operations: [{
                    operator: aggregation,
                    columnId: column.id
                }],
                groupBy: [selectedColumn.id]
            };

            StatisticsRestService.getAggregations(aggregationParameters)
                .then(function (response) {
                    initClassicHistogram(aggregation, $filter('translate')(aggregation), response);
                    service.histogram.aggregationColumn = column;
                    service.histogram.aggregation = aggregation;

                    saveColumnAggregation();
                });
        }

        /**
         * @ngdoc method
         * @name getSavedColumnAggregation
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Get the saved dataset column aggregation.
         */
        function getSavedColumnAggregation() {
            var datasetId = state.playground.dataset && state.playground.dataset.id;
            var preparationId = state.playground.preparation &&  state.playground.preparation.id;
            var columnId = state.playground.column &&  state.playground.column.id;
            return StorageService.getAggregation(datasetId, preparationId, columnId);
        }

        /**
         * @ngdoc method
         * @name removeSavedColumnAggregation
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Delete the actual column aggregation key in localStorage
         */
        function removeSavedColumnAggregation() {
            var datasetId = state.playground.dataset && state.playground.dataset.id;
            var preparationId = state.playground.preparation &&  state.playground.preparation.id;
            var columnId = state.playground.column &&  state.playground.column.id;
            return StorageService.removeAggregation(datasetId, preparationId, columnId);
        }

        /**
         * @ngdoc method
         * @name saveColumnAggregation
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Update the actual dataset column aggregation in localStorage
         */
        function saveColumnAggregation() {
            var datasetId = state.playground.dataset && state.playground.dataset.id;
            var preparationId = state.playground.preparation &&  state.playground.preparation.id;
            var columnId = state.playground.column &&  state.playground.column.id;

            var aggregation = {};
            aggregation.aggregation = service.histogram.aggregation;
            aggregation.aggregationColumnId = service.histogram.aggregationColumn.id;

            return StorageService.setAggregation(datasetId, preparationId, columnId, aggregation);
        }

        /**
         * @ngdoc method
         * @name resetCharts
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Removes all the data to disable all visualization
         */
        function getAggregationColumns() {
            var column = state.playground.column;
            //TODO JSO : put a cache again that is invalidated when one of the columns change
            return DatagridService.getNumericColumns(column);
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------COMMON-------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name updateStatistics
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description update aggregation for a selected column
         */
        function updateStatistics() {
            var column = state.playground.column;
            reset(true, true, false);
            initStatisticsValues(column);

            var columnAggregation = getSavedColumnAggregation();
            var aggregatedColumn = columnAggregation && _.findWhere(getAggregationColumns(), {id: columnAggregation.aggregationColumnId});
            var aggregation = columnAggregation && columnAggregation.aggregation;

            service.processAggregation(aggregatedColumn, aggregation);
        }

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {boolean} charts Remove charts
         * @param {boolean} statistics Remove statistics data
         * @param {boolean} cache Clear cache
         * @description Removes data depending on the parameters
         */
        function reset(charts, statistics, cache) {
            if(charts) {
                service.boxPlot = null;
                service.histogram = null;
                service.rangeLimits = null;
                service.stateDistribution = null;
            }

            if(statistics) {
                service.statistics = null;
            }

            if(cache) {
                StatisticsRestService.resetCache();
            }
        }
    }

    angular.module('data-prep.services.statistics')
        .service('StatisticsService', StatisticsService);
})();