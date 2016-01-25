(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description Column profile controller.
     * @requires data-prep.services.state.constant:state
     * @requires data-prep.statistics.service:StatisticsService
     * @requires data-prep.services.filter.service:FilterService
     * @requires data-prep.services.playground.service:PlaygroundService
     */
    function ColumnProfileCtrl($scope, $timeout, state, StatisticsService, FilterService, PlaygroundService) {
        var vm = this;
        vm.statisticsService = StatisticsService;
        vm.state = state;
        vm.chartConfig = {};

        //------------------------------------------------------------------------------------------------------
        //------------------------------------------------FILTER------------------------------------------------
        //------------------------------------------------------------------------------------------------------
        function addExactFilter(value) {
            var column = state.playground.grid.selectedColumn;
            return value.length ?
                FilterService.addFilterAndDigest('exact', column.id, column.name, {
                    phrase: value,
                    caseSensitive: true
                }) :
                FilterService.addFilterAndDigest('empty_records', column.id, column.name);
        }

        /**
         * @ngdoc property
         * @name addBarchartFilter
         * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description Add an "exact" case sensitive filter if the value is not empty, an "empty_records" filter otherwise
         * @type {array}
         */
        vm.addBarchartFilter = function addBarchartFilter(item) {
            return addExactFilter(item.data);
        };

        /**
         * @ngdoc method
         * @name addRangeFilter
         * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description Add an "range" filter
         * @param {object} interval The interval [min, max] to filter
         */
        vm.addRangeFilter = function addRangeFilter(interval) {
            var selectedColumn = state.playground.grid.selectedColumn;

            if(!interval.label) {
                var min = d3.format(',')(interval.min);
                var max = d3.format(',')(interval.max);
                interval.label = min === max ? '[' + min + ']' : '[' + min + ' .. ' + max + '[';
            }
            var removeFilterFn = StatisticsService.getRangeFilterRemoveFn();
            FilterService.addFilterAndDigest('inside_range',
                selectedColumn.id,
                selectedColumn.name,
                {
                    interval: [interval.min, interval.max],
                    label: interval.label,
                    type: selectedColumn.type
                },
                removeFilterFn);
        };

        //------------------------------------------------------------------------------------------------------
        //----------------------------------------------AGGREGATION---------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc property
         * @name aggregations
         * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description The list of possible aggregations
         * @type {array}
         */
        vm.aggregations = ['SUM', 'MAX', 'MIN', 'AVERAGE'];

        /**
         * @ngdoc method
         * @name getCurrentAggregation
         * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description The current aggregations
         * @return {string} The current aggregation name
         */
        vm.getCurrentAggregation = function getCurrentAggregation() {
            return state.playground.statistics.histogram && state.playground.statistics.histogram.aggregation ?
                state.playground.statistics.histogram.aggregation :
                'LINE_COUNT';
        };

        /**
         * @ngdoc method
         * @name changeAggregation
         * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @param {object} column The column to aggregate
         * @param {object} aggregation The aggregation to perform
         * @description Trigger a new aggregation graph
         */
        vm.changeAggregation = function changeAggregation(column, aggregation) {
            if (state.playground.statistics.histogram &&
                state.playground.statistics.histogram.aggregationColumn === column &&
                state.playground.statistics.histogram.aggregation === aggregation) {
                return;
            }

            StatisticsService.processAggregation(column, aggregation);
        };

        //------------------------------------------------------------------------------------------------------
        //----------------------------------------------GEO CHARTS ---------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * Common highcharts options
         * @param clickFn - the click callback
         * @returns {{exporting: {enabled: boolean}, legend: {enabled: boolean}}}
         */
        var initCommonChartOptions = function (clickFn) {
            return {
                credits: {
                    enabled: false
                },
                exporting: {
                    enabled: false
                },
                legend: {
                    enabled: false
                },
                plotOptions: {
                    series: {
                        cursor: 'pointer',
                        point: {
                            events: {
                                click: clickFn
                            }
                        }
                    }
                }
            };
        };

        /**
         * Geo specific highcharts options
         * @param clickFn - the click callback
         * @param min - min value (defined for color)
         * @param max - max value (defined for color)
         * @returns {{exporting, legend}|{exporting: {enabled: boolean}, legend: {enabled: boolean}}}
         */
        var initGeoChartOptions = function (clickFn, min, max) {
            var options = initCommonChartOptions(clickFn);

            options.tooltip = {
                enabled: true,
                headerFormat: '',
                pointFormat: '{point.name}: <b>{point.value}</b>'
            };
            options.colorAxis = {
                min: min,
                max: max
            };
            options.mapNavigation = {
                enabled: true,
                buttonOptions: {
                    verticalAlign: 'bottom'
                }
            };

            return options;
        };

        /**
         * Init a geo distribution chart
         * @param column
         */
        var buildGeoDistribution = function (column) {
            var geoChartAction = function () {
                console.log('State: ' + this['hc-key'] + ', value: ' + this.value);
                return addExactFilter(this['hc-key'].substring(3));
            };

            vm.stateDistribution = StatisticsService.getGeoDistribution(column);

            var data = vm.stateDistribution.data;
            var min = data[data.length - 1].value;
            var max = data[0].value;

            vm.chartConfig = {
                options: initGeoChartOptions(geoChartAction, min, max),
                chartType: 'map',
                title: {text: column.name + ' distribution'},
                series: [
                    {
                        id: column.id,
                        data: data,
                        mapData: Highcharts.maps[vm.stateDistribution.map],
                        joinBy: 'hc-key',
                        states: {
                            hover: {
                                color: '#BADA55'
                            }
                        }
                    }
                ],
                size: {
                    width: 300,
                    height: 400
                }
            };
        };

        //------------------------------------------------------------------------------------------------------
        //----------------------------------------------CHART REFRESH-------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name shouldFetchStatistics
         * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description Check if we have the statistics or we have to fetch them
         */
        function shouldFetchStatistics() {
            return !state.playground.statistics.histogram &&// no histogram means no statistics yet whereas empty histogram means no data to display
                !vm.stateDistribution; // and not a state distribution chart
        }

        /**
         * @ngdoc method
         * @name fetchStatistics
         * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
         * @description Fetch the statistics. If the update fails (no statistics yet) a retry is triggered after 1s
         */
        function fetchStatistics() {
            PlaygroundService.updateStatistics()
                .catch(function () {
                    $timeout(fetchStatistics, 1500, false);
                });
        }

        if (shouldFetchStatistics()) {
            fetchStatistics();
        }
        //------------------------------------------------------------------------------------------------------
        //-------------------------------------------------WATCHERS---------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * Init chart on column selection change
         */
        $scope.$watch(
            function () {
                return StatisticsService.stateDistribution;
            },
            function (column) {
                vm.stateDistribution = null;
                if (column) {
                    buildGeoDistribution(column);
                }
            }
        );

    }

    /**
     * @ngdoc property
     * @name aggregationColumns
     * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description The numeric columns list of the dataset.
     * This is bound to {@link data-prep.statistics:StatisticsService StatisticsService}.getAggregationColumns()
     */
    Object.defineProperty(ColumnProfileCtrl.prototype,
        'aggregationColumns', {
            enumerable: true,
            configurable: true,
            get: function () {
                return this.statisticsService.getAggregationColumns();
            }
        });

    angular.module('data-prep.column-profile')
        .controller('ColumnProfileCtrl', ColumnProfileCtrl);
})();
