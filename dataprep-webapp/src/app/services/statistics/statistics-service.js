/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';

/* eslint-disable import/no-extraneous-dependencies */
const DateOccurrenceWorker = require('worker-loader!./date-occurence.worker');
const PatternOccurrenceWorker = require('worker-loader!./pattern-occurence.worker');
/* eslint-enable import/no-extraneous-dependencies */


/**
 * @ngdoc service
 * @name data-prep.services.statistics.service:StatisticsService
 * @description Extracts/structures the data to be visualized in charts
 * @requires data-prep.services.statistics.service:StatisticsRestService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:FilterAdapterService
 * @requires data-prep.services.utils.service:TextFormatService
 * @requires data-prep.services.utils.service:StepUtilsService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.utils.service:DateService
 */
export default function StatisticsService($q, $log, $filter, state, StateService,
                                          StepUtilsService, StatisticsRestService,
                                          ConverterService, FilterAdapterService, TextFormatService,
                                          StorageService) {
	'ngInject';

	const service = {
		dateWorker: null,
		patternWorker: null,

        // update range
		initRangeLimits,

        // filters
		getRangeFilterRemoveFn,

        // statistics entry points
		processAggregation,             // aggregation charts
		processClassicChart,            // classic charts (not aggregation)
		updateStatistics,               // update all stats (values, charts)
		updateFilteredStatistics,       // update filtered entries stats
		reset,                          // reset charts/statistics/cache
	};
	return service;

    //
    // BELOW ARE ALL THE STATISTICS TABS FUNCTIONS FOR (1-CHART, 2-VALUES, 3-PATTERN, 4-OTHERS)
    //

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------- 1.1 Common barchart ---------------------------------------------
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
			data,
			label,
			keyField,
			valueField,
			column: state.playground.grid.selectedColumns[0],
			vertical: false,
			className,
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
			data,
			keyField,
			valueField,
			label,
			column: state.playground.grid.selectedColumns[0],
			vertical: true,
		};
	}

    //--------------------------------------------------------------------------------------------------------------
    // ------------------------------------------ 1.2 Number Range barcharts ----------------------------------------
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
	const numberValue = Number(value);
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
		const histoData = state.playground.grid.selectedColumns[0].statistics.histogram;
		if (!histoData) {
			return;
		}

		const rangeData = [];
		const filteredRangeData = [];
		_.forEach(histoData.items, function (histDatum) {
			const min = histDatum.range.min;
			const max = histDatum.range.max;
			const range = {
				type: 'number',
				min,
				max,
			};

			rangeData.push({
				data: range,
				occurrences: histDatum.occurrences,
			});
			filteredRangeData.push({
				data: range,
				filteredOccurrences: state.playground.filter.gridFilters.length ? getRangeFilteredOccurrence(min, max) : histDatum.occurrences,
			});
		});

		return {
			histogram: initVerticalHistogram('data', 'occurrences', 'Occurrences', rangeData),
			filteredHistogram: initVerticalHistogram('data', 'filteredOccurrences', 'Filtered Occurrences', filteredRangeData),
		};
	}

    /**
     * @ngdoc method
     * @name initRangeHistogram
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description Adapt the numeric range data to fit histogram format
     */
	function initRangeHistogram() {
		const histograms = createNumberRangeHistograms();
		if (histograms) {
			StateService.setStatisticsHistogram(histograms.histogram);
			StateService.setStatisticsFilteredHistogram(histograms.filteredHistogram);
		}
	}

    //--------------------------------------------------------------------------------------------------------------
    // ------------------------------------------ 1.2 bis Number Range limits ---------------------------------------
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

		const column = state.playground.grid.selectedColumns[0];
		const statistics = column.statistics;
		const currentRangeFilter = _.find(state.playground.filter.gridFilters, function (filter) {
			return filter.colId === column.id && filter.type === 'inside_range';
		});

		let rangeLimits;
		if (state.playground.grid.selectedColumns[0].type === 'date') {
			const firstHistogramItem = _.first(state.playground.grid.selectedColumns[0].statistics.histogram.items);
			const lastHistogramItem = _.last(state.playground.grid.selectedColumns[0].statistics.histogram.items);
			rangeLimits = {
				min: firstHistogramItem.range.min,
				max: lastHistogramItem.range.max,
			};
		}
		else {
			rangeLimits = {
				min: statistics.min,
				max: statistics.max,
			};
		}

		rangeLimits.type = state.playground.grid.selectedColumns[0].type;

		if (currentRangeFilter) {
			const currentRangeFilterIntervals = currentRangeFilter.args.intervals;
			const { filterMin, filterMax } = currentRangeFilterIntervals.reduce(
                (accu, interval) => {
	const [intervalMin, intervalMax] = interval.value;
	return {
		filterMin: Math.min(accu.filterMin, intervalMin),
		filterMax: Math.max(accu.filterMax, intervalMax),
	};
},

                { filterMin: Infinity, filterMax: -Infinity }
            );

			rangeLimits.minFilterVal = filterMin;
			rangeLimits.maxFilterVal = filterMax;

			rangeLimits.minBrush = getValueWithinRange(filterMin, rangeLimits.min, rangeLimits.max);
			rangeLimits.maxBrush = getValueWithinRange(filterMax, rangeLimits.min, rangeLimits.max);

			StateService.setStatisticsHistogramActiveLimits([rangeLimits.minBrush, rangeLimits.maxBrush]);
		}

		StateService.setStatisticsRangeLimits(rangeLimits);
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------- 1.3 Date Range barcharts ----------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name createDateRangeHistogram
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description prepares the date data to be visualized
     */
	function createDateRangeHistogram() {
		const histoData = state.playground.grid.selectedColumns[0].statistics.histogram;
		if (!histoData) {
			return;
		}

		const dateRangeData = _.map(histoData.items, function (histDatum) {
            // range are UTC dates. We convert them to local zone date, so the app date manipulation is easier.
			const minDate = new Date(histDatum.range.min);
			minDate.setTime(minDate.getTime() + (minDate.getTimezoneOffset() * 60 * 1000));
			const maxDate = new Date(histDatum.range.max);
			maxDate.setTime(maxDate.getTime() + (maxDate.getTimezoneOffset() * 60 * 1000));

			return {
				data: {
					type: 'date',
					label: getDateLabel(histoData.pace, minDate, maxDate),
					min: minDate.getTime(),
					max: maxDate.getTime(),
				},
				occurrences: histDatum.occurrences,
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
		const dateRangeData = createDateRangeHistogram();
		if (dateRangeData) {
			StateService.setStatisticsHistogram(dateRangeData);
			return createFilteredDateRangeHistogram()
                .then((filteredDateRangeHistogram) => {
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
		const parameters = {
			rangeData: state.playground.statistics.histogram.data,
			patterns: _.chain(state.playground.grid.selectedColumns[0].statistics.patternFrequencyTable)
                .map('pattern')
                .map(TextFormatService.convertJavaDateFormatToMomentDateFormat)
                .value(),
			filteredOccurrences: state.playground.filter.gridFilters.length ? state.playground.grid.filteredOccurences : null,
		};

		const defer = $q.defer();
		const dateWorker = new DateOccurrenceWorker();
		dateWorker.onmessage = (event) => {
			const filteredRangeData = event.data;
			const histo = initVerticalHistogram('data', 'filteredOccurrences', 'Filtered Occurrences', filteredRangeData);
			defer.resolve(histo);
		};

		dateWorker.onerror = (error) => {
			defer.reject(error);
		};

		dateWorker.postMessage(parameters);
		service.dateWorker = dateWorker;
		return defer.promise;
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
			return '\'H\'' + ((startDate.getMonth() / 6) + 1) + ' yyyy';
		case 'QUARTER':
			return 'Q' + ((startDate.getMonth() / 3) + 1) + ' yyyy';
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
		const dateFilter = $filter('date');
		const format = getDateFormat(pace, minDate);

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
    // -------------------------------------------- 1.4 Classical barcharts -----------------------------------------
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
		const histograms = createClassicHistograms();
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
		const dataTable = state.playground.grid.selectedColumns[0].statistics.frequencyTable;
		if (!dataTable || !dataTable.length) {
			return;
		}

		const keyField = 'formattedValue';
		const filteredValueField = 'filteredOccurrences';
		const valueField = 'occurrences';
		const adaptedData = [];
		const adaptedFilteredData = [];
		_.forEach(dataTable, function (rec) {
			const formattedValue = TextFormatService.adaptToGridConstraints(rec.data);

			const item = {};
			item[keyField] = formattedValue;
			item[valueField] = rec[valueField];
			item.data = rec.data;
			adaptedData.push(item);

			const filteredItem = {};
			filteredItem[keyField] = formattedValue;
			filteredItem[filteredValueField] = getClassicFilteredOccurrence(rec.data);
			adaptedFilteredData.push(filteredItem);
		});

		return {
			histogram: initHorizontalHistogram(keyField, valueField, 'Occurrences', adaptedData, null),
			filteredHistogram: initHorizontalHistogram(keyField, filteredValueField, null, adaptedFilteredData, 'blueBar'),
		};
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------- 1.5 Aggregation barcharts ---------------------------------------
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
		const keyField = 'formattedValue';

		_.forEach(data, function (rec) {
			rec[keyField] = TextFormatService.adaptToGridConstraints(rec.data);
		});

		return initHorizontalHistogram(keyField, valueField, label, data, 'blueBar');
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------2- Values----------------------------------------------------
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
		const column = state.playground.grid.selectedColumns[0];
		const stats = column.statistics;
		const colType = ConverterService.simplifyType(column.type);
		const commonStats = {
			COUNT: stats.count,
			DISTINCT_COUNT: stats.distinctCount,
			DUPLICATE_COUNT: stats.duplicateCount,

			VALID: stats.valid,
			EMPTY: stats.empty,
			INVALID: stats.invalid,
		};

		const specificStats = {};
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
			specific: specificStats,
		});
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------3- Pattern---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name adaptPatternsToGridConstraints
     * @description adapt pattern list to the grid constraints by adding a new 'formattedPattern' field
     */
	function adaptPatternsToGridConstraints(items) {
		return items && items.length ? items.map((item) => {
			return {
				...item,
				formattedPattern: TextFormatService.adaptToGridConstraints(item.pattern),
			};
		}) : [];
	}

    /**
     * @ngdoc method
     * @name createFilteredPatternsFrequency
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description update patterns statistics
     */
	function initPatternsFrequency() {
		const patternFrequency = state.playground.grid.selectedColumns[0].statistics.patternFrequencyTable;
		if (patternFrequency) {
			StateService.setStatisticsPatterns(adaptPatternsToGridConstraints(patternFrequency));
			createFilteredPatternsFrequency()
			.then((filteredPatternFrequency) => {
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
		const column = state.playground.grid.selectedColumns[0];
		const parameters = {
			columnId: column.id,
			patternFrequencyTable: column.statistics.patternFrequencyTable,
			filteredRecords: state.playground.filter.gridFilters.length ? state.playground.grid.filteredRecords : null,
		};

		const defer = $q.defer();
		service.patternWorker = new PatternOccurrenceWorker();
		service.patternWorker.onmessage = (event) => {
			defer.resolve(adaptPatternsToGridConstraints(event.data));
		};

		service.patternWorker.onerror = (error) => {
			defer.reject(error);
		};

		service.patternWorker.postMessage(parameters);
		return defer.promise;
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------4- Others----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name initBoxplotData
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description Gathers the boxPlot data from the specific stats of the columns having a 'number' type
     */
	function initBoxplotData() {
		const specStats = state.playground.statistics.details.specific;

		StateService.setStatisticsBoxPlot(
			{
				min: specStats.MIN,
				max: specStats.MAX,
				q1: specStats.LOWER_QUANTILE,
				q2: specStats.UPPER_QUANTILE,
				median: specStats.MEDIAN,
				mean: specStats.MEAN,
				variance: specStats.VARIANCE,
			}
        );
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------FILTER-------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name getRangeFilterRemoveFn
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description Create a remove callback to reinit the current active limits on the current column range chart
     */
	function getRangeFilterRemoveFn() {
		const selectedColumn = state.playground.grid.selectedColumns[0];
		const columnMin = selectedColumn.statistics.min;
		const columnMax = selectedColumn.statistics.max;

		return function removeFilterFn(filter) {
			const actualSelectedColumn = state.playground.grid.selectedColumns[0];
			if (filter.colId === actualSelectedColumn.id) {
				initRangeLimits();
                // to reset the vertical bars colors
				StateService.setStatisticsHistogramActiveLimits([columnMin, columnMax]);
			}
		};
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------NON AGGREGATION--------------------------------------------------
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

		const column = state.playground.grid.selectedColumns[0];
		const simplifiedType = ConverterService.simplifyType(column.type);
		switch (simplifiedType) {
		case 'integer':
		case 'decimal':
			initBoxplotData();
			initRangeHistogram();
			initRangeLimits();
			break;
		case 'date': {
			const promise = initDateRangeHistogram();
			initRangeLimits();
			return promise;
		}

		case 'text':
		case 'boolean':
			initClassicHistogram('occurrences', 'Occurrences', column.statistics.frequencyTable);
			break;
		default:
			$log.debug('nor a number neither a boolean, neither a string, neither a date but a ' + simplifiedType);
		}
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------Aggregation--------------------------------------------------
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

		const datasetId = state.playground.dataset.id;
		const preparationId = state.playground.preparation && state.playground.preparation.id;
		const lastActiveStep = preparationId && StepUtilsService.getLastActiveStep(state.playground.recipe);
		const stepId = lastActiveStep && lastActiveStep.transformation.stepId;
		const selectedColumn = state.playground.grid.selectedColumns[0];

		let aggregationParameters = {
			datasetId: preparationId ? null : datasetId,
			preparationId,
			stepId,
			operations: [{
				operator: aggregationName,
				columnId: column.id,
			}],
			groupBy: [selectedColumn.id],
		};

        // add filter in parameters only if there are filters
		aggregationParameters = _.extend(aggregationParameters, FilterAdapterService.toTree(state.playground.filter.gridFilters));

		return StatisticsRestService.getAggregations(aggregationParameters)
            .then(function (response) {
	const histogram = getAggregationHistogram(aggregationName, $filter('translate')(aggregationName), response);
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
		const datasetId = state.playground.dataset && state.playground.dataset.id;
		const preparationId = state.playground.preparation && state.playground.preparation.id;
		const columnId = state.playground.grid.selectedColumns[0] && state.playground.grid.selectedColumns[0].id;
		return StorageService.getAggregation(datasetId, preparationId, columnId);
	}

    /**
     * @ngdoc method
     * @name removeSavedColumnAggregation
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description Delete the actual column aggregation key in localStorage
     */
	function removeSavedColumnAggregation() {
		const datasetId = state.playground.dataset && state.playground.dataset.id;
		const preparationId = state.playground.preparation && state.playground.preparation.id;
		const columnId = state.playground.grid.selectedColumns[0] && state.playground.grid.selectedColumns[0].id;
		return StorageService.removeAggregation(datasetId, preparationId, columnId);
	}

    /**
     * @ngdoc method
     * @name saveColumnAggregation
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description Update the actual dataset column aggregation in localStorage
     */
	function saveColumnAggregation(aggregationName, colId) {
		const datasetId = state.playground.dataset && state.playground.dataset.id;
		const preparationId = state.playground.preparation && state.playground.preparation.id;
		const columnId = state.playground.grid.selectedColumns[0] && state.playground.grid.selectedColumns[0].id;

		const aggregation = {
			aggregation: aggregationName,
			aggregationColumnId: colId,
		};

		return StorageService.setAggregation(datasetId, preparationId, columnId, aggregation);
	}

    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------COMMON-------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name updateFilteredStatistics
     * @methodOf data-prep.services.statistics.service:StatisticsService
     * @description responsible for updating the filtered data according to the selected column type
     */
	function updateFilteredStatistics() {
		resetWorkers();
		const asyncProcess = [];

		const columnAggregation = getSavedColumnAggregation();
		const aggregationName = columnAggregation && columnAggregation.aggregation;

		if (!aggregationName) {
			const column = state.playground.grid.selectedColumns[0];
			const numberOrDateHistogram = column.statistics.histogram;
			const frequencyTable = column.statistics.frequencyTable;
			if (!column || (!numberOrDateHistogram && !frequencyTable.length)) {
				return;
			}
			const simplifiedType = ConverterService.simplifyType(column.type);
			switch (simplifiedType) {
			case 'integer':
			case 'decimal':
				StateService.setStatisticsFilteredHistogram(createNumberRangeHistograms().filteredHistogram);
				initRangeLimits();
				break;
			case 'date': {
				const dateRangeProcess = createFilteredDateRangeHistogram()
                        .then((histogram) => {
	StateService.setStatisticsFilteredHistogram(histogram);
	initRangeLimits();
});
				asyncProcess.push(dateRangeProcess);
				break;
			}

			case 'text':
			case 'boolean':
				StateService.setStatisticsFilteredHistogram(createClassicHistograms().filteredHistogram);
				break;
			}
		}
		else {
			const aggregatedColumn = columnAggregation && _.find(state.playground.grid.numericColumns, { id: columnAggregation.aggregationColumnId });
			if (aggregatedColumn) {
				const aggregationProcess = processAggregation(aggregatedColumn, aggregationName);
				asyncProcess.push(aggregationProcess);
			}
		}

        // should be outside the IF(!aggregationName) statement because it does not depend on the aggregation
		const patternProcess = createFilteredPatternsFrequency()
            .then((filteredPatternFrequency) => {
	StateService.setStatisticsFilteredPatterns(filteredPatternFrequency);
});
		asyncProcess.push(patternProcess);

		return $q.all(asyncProcess);
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

		const columnAggregation = getSavedColumnAggregation();
		const aggregatedColumn = columnAggregation && _.find(state.playground.grid.numericColumns, { id: columnAggregation.aggregationColumnId });
		const aggregation = columnAggregation && columnAggregation.aggregation;
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
		if (service.dateWorker) {
			service.dateWorker.terminate();
			service.dateWorker = null;
		}

		if (service.patternWorker) {
			service.patternWorker.terminate();
			service.patternWorker = null;
		}
	}
}
