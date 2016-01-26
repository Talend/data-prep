(function () {
    'use strict';
    /**
     * @ngdoc service
     * @name data-prep.services.statistics.service:StatisticsService
     * @description Extracts/structures the data to be visualized in charts
     * @requires data-prep.services.playground.service:DatagridService
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.statistics.service:StatisticsRestService
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.utils.service:ConverterService
     * @requires data-prep.services.utils.service:FilterAdapterService
     * @requires data-prep.services.utils.service:TextFormatService
     * @requires data-prep.services.utils.service:StorageService
     * @requires data-prep.services.utils.service:WorkerService
     * @requires data-prep.services.utils.service:DateService
     */
    function StatisticsService($filter, state, StateService,
                               DatagridService, RecipeService, StatisticsRestService,
                               ConverterService, FilterAdapterService, TextFormatService,
                               StorageService, WorkerService, DateService) {

        var dateFilteredWorkerWrapper;
        var datePatternWorkerWrapper;

        //workerFn[0-9] are preserved words that won't be mangled during uglification
        //those should be used to pass external functions, that are directly used here, to the web worker
        var workerFn0 = TextFormatService.convertJavaDateFormatToMomentDateFormat;
        var workerFn1 = TextFormatService.convertPatternToRegexp;
        var workerFn2 = DateService.isInDateLimits;

        return {
            //update range
            initRangeLimits: initRangeLimits,

            //filters
            getRangeFilterRemoveFn: getRangeFilterRemoveFn,

            //statistics entry points
            processAggregation: processAggregation,             // aggregation charts
            processClassicChart: processClassicChart,           // classic charts (not aggregation)
            getAggregationColumns: getAggregationColumns,       // possible aggregation columns
            updateStatistics: updateStatistics,                 // update all stats (values, charts)
            updateFilteredStatistics: updateFilteredStatistics, // update filtered entries stats
            reset: reset,                                       // reset charts/statistics/cache

            //Pattern
            valueMatchPatternFn: valueMatchPatternFn
        };

        //
        // BELOW ARE ALL THE STATISTICS TABS FUNCTIONS FOR (1-CHART, 2-VALUES, 3-PATTERN, 4-OTHERS)
        //

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------- 1.1 Common barchart ---------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name initHorizontalHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} keyField The key field prop name
         * @param {string} valueField The value field prop name
         * @param {string} label The value label
         * @param {Array} data The data to display
         * @param {string} className The bar class name
         * @description Create a records frequency ranges table that fit the histogram format
         */
        function initHorizontalHistogram(keyField, valueField, label, data, className) {
            return {
                data: data,
                label: label,
                keyField: keyField,
                valueField: valueField,
                column: state.playground.grid.selectedColumn,
                vertical: false,
                className: className
            };
        }

        /**
         * @ngdoc method
         * @name initVerticalHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} keyField The key field prop name
         * @param {string} valueField The value field prop name
         * @param {string} label The value label
         * @param {Array} data The data to display
         * @description Create a records frequency ranges table that fit the histogram format
         */
        function initVerticalHistogram(keyField, valueField, label, data) {
            return {
                data: data,
                keyField: keyField,
                valueField: valueField,
                label: label,
                column: state.playground.grid.selectedColumn,
                vertical: true
            };
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------ 1.2 Number Range barcharts ----------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getRangeFilteredOccurrence
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {number} min The range min value
         * @param {number} max The range max value
         * @description Compute The filtered records that fullfill the given predicate
         * @returns {number} The Number of records
         */
        function getRangeFilteredOccurrence(min, max) {
            return _.chain(state.playground.grid.filteredOccurences)
                .keys()
                .filter(function (value) {
                    var numberValue = Number(value);
                    return !isNaN(numberValue) &&
                        ((numberValue === min) || (numberValue > min && numberValue < max));
                })
                .map(function (key) {
                    return state.playground.grid.filteredOccurences[key];
                })
                .reduce(function (accu, value) {
                    return accu + value;
                }, 0)
                .value();
        }

        /**
         * @ngdoc method
         * @name createNumberRangeHistograms
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @return {Object} containing the original and the filtered data
         * @description prepares the numeric data details
         */
        function createNumberRangeHistograms() {
            var histoData = state.playground.grid.selectedColumn.statistics.histogram;
            if (!histoData) {
                return;
            }

            var rangeData = [];
            var filteredRangeData = [];
            _.forEach(histoData.items, function (histDatum) {
                var min = histDatum.range.min;
                var max = histDatum.range.max;
                var range = {
                    type: 'number',
                    min: min,
                    max: max
                };

                rangeData.push({
                    'data': range,
                    'occurrences': histDatum.occurrences
                });
                filteredRangeData.push({
                    'data': range,
                    'filteredOccurrences': state.playground.filter.gridFilters.length ? getRangeFilteredOccurrence(min, max) : histDatum.occurrences
                });
            });

            return {
                histogram: initVerticalHistogram('data', 'occurrences', 'Occurrences', rangeData),
                filteredHistogram: initVerticalHistogram('data', 'filteredOccurrences', 'Filtered Occurrences', filteredRangeData)
            };
        }

        /**
         * @ngdoc method
         * @name initRangeHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Adapt the numeric range data to fit histogram format
         */
        function initRangeHistogram() {
            var histograms = createNumberRangeHistograms();
            if (histograms) {
                StateService.setStatisticsHistogram(histograms.histogram);
                StateService.setStatisticsFilteredHistogram(histograms.filteredHistogram);
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------ 1.2 bis Number Range limits ---------------------------------------
        //--------------------------------------------------------------------------------------------------------------
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
            if (!state.playground.statistics.histogram) {
                return;
            }

            var column = state.playground.grid.selectedColumn;
            var statistics = column.statistics;
            var currentRangeFilter = _.find(state.playground.filter.gridFilters, function (filter) {
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

                StateService.setStatisticsHistogramActiveLimits([rangeLimits.minBrush, rangeLimits.maxBrush]);
            }

            StateService.setStatisticsRangeLimits(rangeLimits);
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------- 1.3 Date Range barcharts ----------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createDateRangeHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description prepares the date data to be visualized
         */
        function createDateRangeHistogram() {
            var histoData = state.playground.grid.selectedColumn.statistics.histogram;
            if (!histoData) {
                return;
            }

            var dateRangeData = _.map(histoData.items, function (histDatum) {
                //range are UTC dates. We convert them to local zone date, so the app date manipulation is easier.
                var minDate = new Date(histDatum.range.min);
                minDate.setTime(minDate.getTime() + (minDate.getTimezoneOffset() * 60 * 1000));
                var maxDate = new Date(histDatum.range.max);
                maxDate.setTime(maxDate.getTime() + (maxDate.getTimezoneOffset() * 60 * 1000));

                return {
                    'data': {
                        type: 'date',
                        label: getDateLabel(histoData.pace, minDate, maxDate),
                        min: minDate.getTime(),
                        max: maxDate.getTime()
                    },
                    'occurrences': histDatum.occurrences
                };
            });

            return initVerticalHistogram('data', 'occurrences', 'Occurrences', dateRangeData);
        }

        /**
         * @ngdoc method
         * @name initDateRangeHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Adapt the date range data to fit histogram format
         */
        function initDateRangeHistogram() {
            var dateRangeData = createDateRangeHistogram();
            if (dateRangeData) {
                StateService.setStatisticsHistogram(dateRangeData);
                createFilteredDateRangeHistogram()
                    .then(function (filteredDateRangeHistogram) {
                        StateService.setStatisticsFilteredHistogram(filteredDateRangeHistogram);
                    });
            }
        }

        /**
         * @ngdoc method
         * @name createFilteredDateRangeHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @params {Object} dateRangeData containing the data and the patterns
         * @description Use a Worker to compute the date FilteredHistogram
         */
        function createFilteredDateRangeHistogram() {
            var rangeData = state.playground.statistics.histogram.data;
            var patterns = _.chain(state.playground.grid.selectedColumn.statistics.patternFrequencyTable)
                .pluck('pattern')
                .map(TextFormatService.convertJavaDateFormatToMomentDateFormat)
                .value();

            //execute a web worker that will compute the filtered occurrences
            dateFilteredWorkerWrapper = WorkerService.create(
                ['/worker/moment.js', '/worker/moment-jdateformatparser.js', '/worker/lodash.js'],
                [{workerFn2: workerFn2}],
                dateFilteredOccurrenceWorker);

            var filteredOccurrences = state.playground.filter.gridFilters.length ? state.playground.grid.filteredOccurences : null;
            return dateFilteredWorkerWrapper.postMessage([rangeData, patterns, filteredOccurrences])
                .then(function (filteredRangeData) {
                    return initVerticalHistogram('data', 'filteredOccurrences', 'Filtered Occurrences', filteredRangeData);
                })
                .finally(function () {
                    dateFilteredWorkerWrapper.clean();
                });
        }

        /**
         * @ngdoc method
         * @name dateFilteredOccurrenceWorker
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Web worker function to execute to get the date pattern filtered occurrences
         * @param {object} rangeData The range data
         * @param {Array} patterns The patterns to use for date parsing
         * @param {Array} filteredOccurences The filtered occurrences
         */
        function dateFilteredOccurrenceWorker(rangeData, patterns, filteredOccurences) {
            _.forEach(rangeData, function (range) {
                var minTimestamp = range.data.min;
                var maxTimestamp = range.data.max;

                range.filteredOccurrences = !filteredOccurences ?
                    range.occurrences :
                    _.chain(filteredOccurences)
                        .keys()
                        .filter(workerFn2(minTimestamp, maxTimestamp, patterns))
                        .map(function (key) {
                            return filteredOccurences[key];
                        })
                        .reduce(function (accu, value) {
                            return accu + value;
                        }, 0)
                        .value();
            });

            return rangeData;
        }

        /**
         * @ngdoc method
         * @name getDateFormat
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {String} pace The histogram time pace
         * @param {Date} startDate The range starting date
         * @description Returns the date pattern that fit the pace at the starting date
         */
        function getDateFormat(pace, startDate) {
            switch (pace) {
                case 'CENTURY':
                case 'DECADE':
                case 'YEAR':
                    return 'yyyy';
                case 'HALF_YEAR':
                    return '\'H\'' + (startDate.getMonth() / 6 + 1) + ' yyyy';
                case 'QUARTER':
                    return 'Q' + (startDate.getMonth() / 3 + 1) + ' yyyy';
                case 'MONTH':
                    return 'MMM yyyy';
                case 'WEEK':
                    return 'Www yyyy';
                default:
                    return 'mediumDate';
            }
        }

        /**
         * @ngdoc method
         * @name getDateLabel
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} pace The histogram time pace
         * @param {Date} minDate The range starting date
         * @param {Date} maxDate The range ending date
         * @description Returns the range label
         */
        function getDateLabel(pace, minDate, maxDate) {
            var dateFilter = $filter('date');
            var format = getDateFormat(pace, minDate);

            switch (pace) {
                case 'YEAR':
                case 'HALF_YEAR':
                case 'QUARTER':
                case 'MONTH':
                case 'WEEK':
                case 'DAY':
                    return dateFilter(minDate, format);
                default:
                    return '[' + dateFilter(minDate, format) + ', ' + dateFilter(maxDate, format) + '[';
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------- 1.4 Classical barcharts -----------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getClassicFilteredOccurrence
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} value The value to have
         * @description Get the occurrence of the provided value
         * @returns {number} The Number of records
         */
        function getClassicFilteredOccurrence(value) {
            return state.playground.grid.filteredOccurences[value] || 0;
        }

        /**
         * @ngdoc method
         * @name initClassicHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Set the frequency table that fit the histogram format (filter is managed in frontend)
         */
        function initClassicHistogram() {
            var histograms = createClassicHistograms();
            if (histograms) {
                StateService.setStatisticsHistogram(histograms.histogram);
                StateService.setStatisticsFilteredHistogram(histograms.filteredHistogram);
            }
        }

        /**
         * @ngdoc method
         * @name createClassicHistograms
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description prepares the frequency data
         * @return {Object} The classical and filtered histograms
         */
        function createClassicHistograms() {
            var dataTable = state.playground.grid.selectedColumn.statistics.frequencyTable;
            if (!dataTable || !dataTable.length) {
                return;
            }

            var keyField = 'formattedValue';
            var filteredValueField = 'filteredOccurrences';
            var valueField = 'occurrences';
            var adaptedData = [];
            var adaptedFilteredData = [];
            _.forEach(dataTable, function (rec) {
                var formattedValue = TextFormatService.adaptToGridConstraints(rec.data);

                var item = {};
                item[keyField] = formattedValue;
                item[valueField] = rec[valueField];
                item.data = rec.data;
                adaptedData.push(item);

                var filteredItem = {};
                filteredItem[keyField] = formattedValue;
                filteredItem[filteredValueField] = getClassicFilteredOccurrence(rec.data);
                adaptedFilteredData.push(filteredItem);
            });

            return {
                histogram: initHorizontalHistogram(keyField, valueField, 'Occurrences', adaptedData, null),
                filteredHistogram: initHorizontalHistogram(keyField, filteredValueField, null, adaptedFilteredData, 'blueBar')
            };
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------- 1.5 Aggregation barcharts ---------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getAggregationHistogram
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {string} valueField The value prop name
         * @param {string} label The value label
         * @param {Array} data The data to display
         * @description Create the histogram format for aggregation (filter is managed in backend)
         */
        function getAggregationHistogram(valueField, label, data) {
            var keyField = 'formattedValue';

            _.forEach(data, function (rec) {
                rec[keyField] = TextFormatService.adaptToGridConstraints(rec.data);
            });

            return initHorizontalHistogram(keyField, valueField, label, data, 'blueBar');
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------2- Values----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name cleanNumber
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @param {number} value Value to clean the float
         * @description Cleans the value to have 2 decimals (5.2568845842587425588 -> 5.25)
         * @returns {number} The value in the clean format
         */
        function cleanNumber(value) {
            return isNaN(value) || value === parseInt(value, 10) ? value : +value.toFixed(2);
        }

        /**
         * @ngdoc method
         * @name initStatisticsValues
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Initialize the statistics to display in the values TAB of the stats part
         */
        function initStatisticsValues() {
            var column = state.playground.grid.selectedColumn;
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
                    specificStats.MIN = cleanNumber(stats.min);
                    specificStats.MAX = cleanNumber(stats.max);
                    specificStats.MEAN = cleanNumber(stats.mean);
                    specificStats.VARIANCE = cleanNumber(stats.variance);
                    if (stats.quantiles.lowerQuantile !== 'NaN') {
                        specificStats.MEDIAN = cleanNumber(stats.quantiles.median);
                        specificStats.LOWER_QUANTILE = cleanNumber(stats.quantiles.lowerQuantile);
                        specificStats.UPPER_QUANTILE = cleanNumber(stats.quantiles.upperQuantile);
                    }

                    break;
                case 'text':
                    specificStats.AVG_LENGTH = cleanNumber(stats.textLengthSummary.averageLength);
                    specificStats.MIN_LENGTH = stats.textLengthSummary.minimalLength;
                    specificStats.MAX_LENGTH = stats.textLengthSummary.maximalLength;
                    break;
            }

            StateService.setStatisticsDetails({
                common: commonStats,
                specific: specificStats
            });
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------3- Pattern---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createFilteredPatternsFrequency
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description update patterns statistics
         */
        function initPatternsFrequency() {
            var patternFrequency = state.playground.grid.selectedColumn.statistics.patternFrequencyTable;
            if (patternFrequency) {
                StateService.setStatisticsPatterns(patternFrequency);
                createFilteredPatternsFrequency()
                    .then(function (filteredPatternFrequency) {
                        StateService.setStatisticsFilteredPatterns(filteredPatternFrequency);
                    });
            }
        }

        /**
         * @ngdoc method
         * @name createFilteredPatternsFrequency
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Create the filtered patterns statistics
         */
        function createFilteredPatternsFrequency() {
            datePatternWorkerWrapper = WorkerService.create(
                ['/worker/moment.js', '/worker/lodash.js', '/worker/moment-jdateformatparser.js'],
                [
                    {
                        workerFn0: workerFn0,
                        workerFn1: workerFn1
                    },
                    TextFormatService.escapeRegex,
                    valueMatchPatternFn,
                    isDatePattern,
                    valueMatchDatePatternFn,
                    valueMatchRegexFn,
                    valueMatchPatternFn
                ],
                patternOccurrenceWorker
            );

            var column = state.playground.grid.selectedColumn;
            var patternFrequency = column.statistics.patternFrequencyTable;
            var filteredRecords = state.playground.filter.gridFilters.length ? state.playground.grid.filteredRecords : null;
            return datePatternWorkerWrapper.postMessage([column.id, patternFrequency, filteredRecords])
                .finally(function () {
                    datePatternWorkerWrapper.clean();
                });
        }

        /**
         * @ngdoc method
         * @name patternOccurrenceWorker
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Web worker function to execute to get the pattern filtered occurrences
         * @param {string} columnId The column id
         * @param {Array} patternFrequencyTable The pattern frequencies to update
         * @param {Array} filteredRecords The filtered records to process for the filtered occurrences number
         */
        function patternOccurrenceWorker(columnId, patternFrequencyTable, filteredRecords) {
            _.forEach(patternFrequencyTable, function (patternFrequency) {
                var pattern = patternFrequency.pattern;
                var matchingFn = valueMatchPatternFn(pattern);

                patternFrequency.filteredOccurrences = !filteredRecords ?
                    patternFrequency.occurrences :
                    _.chain(filteredRecords)
                        .pluck(columnId)
                        .filter(matchingFn)
                        .groupBy(function (value) {
                            return value;
                        })
                        .mapValues('length')
                        .reduce(function (accu, value) {
                            return accu + value;
                        }, 0)
                        .value();
            });

            return patternFrequencyTable;
        }

        /**
         * @ngdoc method
         * @name isDatePattern
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Check if the pattern is a date pattern
         * @param {string} pattern The pattern to check
         */
        function isDatePattern(pattern) {
            return (pattern.indexOf('d') > -1 ||
            pattern.indexOf('M') > -1 ||
            pattern.indexOf('y') > -1 ||
            pattern.indexOf('H') > -1 ||
            pattern.indexOf('h') > -1 ||
            pattern.indexOf('m') > -1 ||
            pattern.indexOf('s') > -1);
        }

        /**
         * @ngdoc method
         * @name valueMatchDatePatternFn
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Create a predicate that check if a value match the date pattern
         * @param {string} pattern The date pattern to match
         */
        function valueMatchDatePatternFn(pattern) {
            var datePattern = workerFn0(pattern);
            return function (value) {
                return value && moment(value, datePattern, true).isValid();
            };
        }

        /**
         * @ngdoc method
         * @name valueMatchRegexFn
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Create a predicate that check if a value match the regex pattern
         * @param {string} pattern The pattern to match
         */
        function valueMatchRegexFn(pattern) {
            var regex = workerFn1(pattern);
            return function (value) {
                return value && value.match(regex);
            };
        }

        /**
         * @ngdoc method
         * @name valueMatchPatternFn
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Create the adequat predicate that match the pattern. It can be empty, a date pattern, or an alphanumeric pattern
         * @param {string} pattern The pattern to match
         */
        function valueMatchPatternFn(pattern) {
            if (pattern === '') {
                return function (value) {
                    return value === '';
                };
            }
            else if (isDatePattern(pattern)) {
                return valueMatchDatePatternFn(pattern);
            }
            else {
                return valueMatchRegexFn(pattern);
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------4- Others----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name initBoxplotData
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Gathers the boxPlot data from the specific stats of the columns having a 'number' type
         */
        function initBoxplotData() {
            var specStats = state.playground.statistics.details.specific;

            StateService.setStatisticsBoxPlot(
                specStats.LOWER_QUANTILE && //quantile computation is async, it is possible that we get falsy value
                {
                    min: specStats.MIN,
                    max: specStats.MAX,
                    q1: specStats.LOWER_QUANTILE,
                    q2: specStats.UPPER_QUANTILE,
                    median: specStats.MEDIAN,
                    mean: specStats.MEAN,
                    variance: specStats.VARIANCE
                }
            );
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------FILTER-------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name getRangeFilterRemoveFn
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Create a remove callback to reinit the current active limits on the current column range chart
         */
        function getRangeFilterRemoveFn() {
            var selectedColumn = state.playground.grid.selectedColumn;
            var columnMin = selectedColumn.statistics.min;
            var columnMax = selectedColumn.statistics.max;

            return function removeFilterFn(filter) {
                var actualSelectedColumn = state.playground.grid.selectedColumn;
                if (filter.colId === actualSelectedColumn.id) {
                    initRangeLimits();
                    //to reset the vertical bars colors
                    StateService.setStatisticsHistogramActiveLimits([columnMin, columnMax]);
                }
            };
        }

        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------NON AGGREGATION--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name processClassicChart
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Compute the needed data for chart visualization
         */
        function processClassicChart() {
            resetCharts();
            removeSavedColumnAggregation();

            var column = state.playground.grid.selectedColumn;
            var simplifiedType = ConverterService.simplifyType(column.type);
            switch (simplifiedType) {
                case 'integer':
                case 'decimal':
                    initBoxplotData();
                    initRangeHistogram();
                    initRangeLimits();
                    break;
                case 'date':
                    initDateRangeHistogram();
                    break;
                case 'text':
                case 'boolean':
                    initClassicHistogram('occurrences', 'Occurrences', column.statistics.frequencyTable);
                    break;
                default :
                    console.log('nor a number neither a boolean, neither a string, neither a date but a ' + simplifiedType);
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
         * @param {string} aggregationName The aggregation to perform
         * @description Processes the statistics aggregation for visualization
         */
        function processAggregation(column, aggregationName) {
            resetCharts();

            var datasetId = state.playground.dataset.id;
            var preparationId = state.playground.preparation && state.playground.preparation.id;
            var stepId = preparationId && RecipeService.getLastActiveStep() && RecipeService.getLastActiveStep().transformation.stepId;
            var selectedColumn = state.playground.grid.selectedColumn;

            var aggregationParameters = {
                datasetId: preparationId ? null : datasetId,
                preparationId: preparationId,
                stepId: stepId,
                operations: [{
                    operator: aggregationName,
                    columnId: column.id
                }],
                groupBy: [selectedColumn.id]
            };

            //add filter in parameters only if there are filters
            aggregationParameters = _.extend(aggregationParameters, FilterAdapterService.toTree(state.playground.filter.gridFilters));

            StatisticsRestService.getAggregations(aggregationParameters)
                .then(function (response) {
                    var histogram = getAggregationHistogram(aggregationName, $filter('translate')(aggregationName), response);
                    histogram.aggregationColumn = column;
                    histogram.aggregation = aggregationName;

                    StateService.setStatisticsHistogram(histogram);

                    saveColumnAggregation(aggregationName, column.id);
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
            var preparationId = state.playground.preparation && state.playground.preparation.id;
            var columnId = state.playground.grid.selectedColumn && state.playground.grid.selectedColumn.id;
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
            var preparationId = state.playground.preparation && state.playground.preparation.id;
            var columnId = state.playground.grid.selectedColumn && state.playground.grid.selectedColumn.id;
            return StorageService.removeAggregation(datasetId, preparationId, columnId);
        }

        /**
         * @ngdoc method
         * @name saveColumnAggregation
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Update the actual dataset column aggregation in localStorage
         */
        function saveColumnAggregation(aggregationName, colId) {
            var datasetId = state.playground.dataset && state.playground.dataset.id;
            var preparationId = state.playground.preparation && state.playground.preparation.id;
            var columnId = state.playground.grid.selectedColumn && state.playground.grid.selectedColumn.id;

            var aggregation = {
                aggregation: aggregationName,
                aggregationColumnId: colId
            };

            return StorageService.setAggregation(datasetId, preparationId, columnId, aggregation);
        }

        /**
         * @ngdoc method
         * @name getAggregationColumns
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Return the columns available for aggregation
         */
        function getAggregationColumns() {
            if (state.playground.data) {
                var column = state.playground.grid.selectedColumn;
                //TODO JSO : put a cache again that is invalidated when one of the columns change
                return DatagridService.getNumericColumns(column);
            }
        }

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------------COMMON-------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name updateFilteredStatistics
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description responsible for updating the filtered data according to the selected column type
         */
        function updateFilteredStatistics() {
            resetWorkers();

            var columnAggregation = getSavedColumnAggregation();
            var aggregationName = columnAggregation && columnAggregation.aggregation;

            if (!aggregationName) {
                var column = state.playground.grid.selectedColumn;

                var simplifiedType = ConverterService.simplifyType(column.type);
                switch (simplifiedType) {
                    case 'integer':
                    case 'decimal':
                        StateService.setStatisticsFilteredHistogram(createNumberRangeHistograms().filteredHistogram);
                        initRangeLimits();
                        break;
                    case 'date':
                        createFilteredDateRangeHistogram()
                            .then(function (histogram) {
                                StateService.setStatisticsFilteredHistogram(histogram);
                            });
                        break;
                    case 'text':
                    case 'boolean':
                        StateService.setStatisticsFilteredHistogram(createClassicHistograms().filteredHistogram);
                        break;
                }
            }

            //should be outside the IF(!aggregationName) statement because it does not depend on the aggregation
            createFilteredPatternsFrequency()
                .then(function (filteredPatternFrequency) {
                    StateService.setStatisticsFilteredPatterns(filteredPatternFrequency);
                });
        }

        /**
         * @ngdoc method
         * @name updateStatistics
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description update statistics for the selected column.
         * If an aggregation is stored, we process the computation to init this aggregation chart.
         * Otherwise, we init classic charts
         */
        function updateStatistics() {
            resetStatistics();
            resetWorkers();

            initStatisticsValues();
            initPatternsFrequency();

            var columnAggregation = getSavedColumnAggregation();
            var aggregatedColumn = columnAggregation && _.findWhere(getAggregationColumns(), {id: columnAggregation.aggregationColumnId});
            var aggregation = columnAggregation && columnAggregation.aggregation;
            if (aggregatedColumn && aggregation) {
                processAggregation(aggregatedColumn, aggregation);
            }
            else {
                processClassicChart();
            }
        }

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.statistics.service:StatisticsService
         * @description Removes all data (charts, statistics values, cache, workers)
         */
        function reset() {
            resetCharts();
            resetStatistics();
            resetCache();

            resetWorkers();
        }

        /**
         * Reset the charts
         */
        function resetCharts() {
            StateService.setStatisticsRangeLimits(null);
            StateService.setStatisticsBoxPlot(null);
            StateService.setStatisticsHistogram(null);
            StateService.setStatisticsHistogramActiveLimits(null);
        }

        /**
         * Reset the statistics
         */
        function resetStatistics() {
            StateService.setStatisticsDetails(null);
        }

        /**
         * Reset the statistics cache
         */
        function resetCache() {
            StatisticsRestService.resetCache();
        }

        /**
         * Reset web workers
         */
        function resetWorkers() {
            if (dateFilteredWorkerWrapper) {
                dateFilteredWorkerWrapper.terminate();
                dateFilteredWorkerWrapper = null;
            }
            if (datePatternWorkerWrapper) {
                datePatternWorkerWrapper.terminate();
                datePatternWorkerWrapper = null;
            }
        }
    }

    angular.module('data-prep.services.statistics')
        .service('StatisticsService', StatisticsService);
})();