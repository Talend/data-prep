describe('Statistics service', function () {
    'use strict';

    var barChartNumCol = {
        'domain': 'barchartAndNumeric',
        'type': 'numeric',
        'id': '0000',
        'statistics': {
            'frequencyTable': [],
            'histogram': [
                {
                    'occurrences': 5,
                    'range': {
                        'min': 0,
                        'max': 10
                    }
                },
                {
                    'occurrences': 15,
                    'range': {
                        'min': 10,
                        'max': 20
                    }
                }
            ],
            count: 4,
            distinctCount: 5,
            duplicateCount: 6,
            empty: 7,
            invalid: 8,
            valid: 9,
            min: 10,
            max: 11,
            mean: 12,
            variance: 13,
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartStrCol = {
        id: '0010',
        'domain': 'barchartAndString',
        'type': 'string',
        'statistics': {
            'frequencyTable': [
                {
                    'data': '   toto',
                    'occurences': 202
                },
                {
                    'data': 'titi',
                    'occurences': 2
                },
                {
                    'data': 'coucou',
                    'occurences': 102
                },
                {
                    'data': 'cici',
                    'occurences': 22
                }
            ],
            textLengthSummary: {
                averageLength: 10.13248646854654,
                minimalLength: 12,
                maximalLength: 14
            },
            count: 4,
            distinctCount: 5,
            duplicateCount: 6,
            empty: 7,
            invalid: 8,
            valid: 9,
            min: 10,
            max: 11,
            mean: 12,
            variance: 13,
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var mapCol = {
        'domain': 'US_STATE_CODE',
        'type': '',
        'statistics': {
            'frequencyTable': [
                {
                    'data': 'MI',
                    'occurences': 202
                },
                {
                    'data': 'WA',
                    'occurences': 2
                },
                {
                    'data': 'DE',
                    'occurences': 102
                },
                {
                    'data': 'IL',
                    'occurences': 22
                }
            ],
            textLengthSummary: {
                averageLength: 10.13248646854654,
                minimalLength: 12,
                maximalLength: 14
            },
            count: 4,
            distinctCount: 5,
            duplicateCount: 6,
            empty: 7,
            invalid: 8,
            valid: 9,
            min: 10,
            max: 11,
            mean: 12,
            variance: 13,
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartBoolCol = {
        'domain': 'barchartAndBool',
        'type': 'boolean',
        'statistics': {
            'frequencyTable': [
                {
                    'data': 'true',
                    'occurences': 2
                },
                {
                    'data': 'false',
                    'occurences': 20
                },
                {
                    'data': '',
                    'occurences': 10
                }
            ]
        }
    };

    var unknownTypeCol = {
        'domain': '',
        'type': 'unknown'
    };
    var stateMock;

    beforeEach(module('data-prep.services.statistics', function ($provide) {
        stateMock = {playground: {}};
        $provide.constant('state', stateMock);
    }));

    describe('filters', function () {
        beforeEach(inject(function (FilterService) {
            spyOn(FilterService, 'addFilter').and.returnValue();
        }));

        it('should add a new "inside_range" filter', inject(function (StatisticsService, FilterService, $timeout) {
            //given
            StatisticsService.rangeLimits = {};
            stateMock.playground.column = {id: '0000', statistics: {min: 5, max: 55}};

            //when
            StatisticsService.addRangeFilter([0, 22]);
            $timeout.flush();

            //then
            expect(FilterService.addFilter).toHaveBeenCalled();

            var args = FilterService.addFilter.calls.argsFor(0);
            expect(args[0]).toBe('inside_range');
            expect(args[1]).toBe('0000');
            expect(args[2]).not.toBeDefined();
            expect(args[3]).toEqual({interval: [0, 22]});
        }));

        it('should update rangeLimits brush on new "inside_range" filter add', inject(function (StatisticsService, FilterService, $timeout) {
            //given
            StatisticsService.rangeLimits = {};
            StatisticsService.histogram = {};
            stateMock.playground.column = {id: '0000', statistics: {min: 5, max: 55}};

            //when
            FilterService.filters = [{colId: '0000', type: 'inside_range', args: {interval: [10, 22]}}];
            StatisticsService.addRangeFilter([10, 22]);
            $timeout.flush();

            //then
            expect(StatisticsService.rangeLimits.minBrush).toBe(10);
            expect(StatisticsService.rangeLimits.maxBrush).toBe(22);
            expect(StatisticsService.histogram.activeLimits).toEqual([10, 22]);
        }));

        it('should reinit range limits on "inside_range" filter remove when the selected column is the same', inject(function (StatisticsService, FilterService, $timeout) {
            //given
            var originalRangeLimits = {};
            StatisticsService.histogram = {};
            StatisticsService.rangeLimits = originalRangeLimits;
            var column = {id: '0000', statistics: {min: 5, max: 55}};
            stateMock.playground.column = column;

            FilterService.filters = [{colId: '0000', type: 'inside_range', args: {interval: [0, 22]}}];
            StatisticsService.addRangeFilter([0, 22]);
            $timeout.flush();

            expect(StatisticsService.rangeLimits).toEqual({
                min: 5,
                max: 55,
                minBrush: 5,
                maxBrush: 22,
                minFilterVal: 0,
                maxFilterVal: 22
            });
            expect(FilterService.addFilter).toHaveBeenCalled();
            var removeCallback = FilterService.addFilter.calls.argsFor(0)[4];

            //when
            FilterService.filters = [];
            removeCallback({colId: '0000'});

            //then
            expect(StatisticsService.rangeLimits).not.toBe(originalRangeLimits);
            expect(StatisticsService.rangeLimits).toEqual({
                min: 5,
                max: 55
            });
            expect(StatisticsService.histogram.activeLimits).toEqual([column.statistics.min, column.statistics.max]);
        }));

        it('should do nothing on "inside_range" filter remove when the selected column is NOT the same', inject(function (StatisticsService, FilterService, $timeout) {
            //given
            StatisticsService.rangeLimits = {};
            StatisticsService.histogram = {};
            stateMock.playground.column = {id: '0000', statistics: {min: 5, max: 55}};
            StatisticsService.addRangeFilter([0, 22]);
            $timeout.flush();

            expect(FilterService.addFilter).toHaveBeenCalled();
            var removeCallback = FilterService.addFilter.calls.argsFor(0)[4];

            //when
            removeCallback({colId: '0001'});

            //then
            expect(StatisticsService.rangeLimits).toEqual(
                {min: 5, max: 55});
        }));
    });

    describe('Process Data : Basic Charts', function () {
        describe('Map', function () {
            it('should set stateDistribution for geo chart when the column domain contains STATE_CODE', inject(function (StatisticsService) {
                //given
                expect(StatisticsService.stateDistribution).toBeFalsy();
                stateMock.playground.column = mapCol;

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.stateDistribution).toBe(mapCol);
            }));

            it('should reset non geo chart data when the column domain contains STATE_CODE', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = mapCol;
                StatisticsService.boxPlot = {};
                StatisticsService.histogram = {};

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.boxPlot).toBeFalsy();
                expect(StatisticsService.histogram).toBeFalsy();
            }));
        });

        describe('Histogram', function () {
            it('should reset non histogram data when column type is "string"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartStrCol;
                StatisticsService.boxPlot = {};
                StatisticsService.stateDistribution = {};

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.boxPlot).toBeFalsy();
                expect(StatisticsService.stateDistribution).toBeFalsy();
            }));

            it('should set the frequency data with formatted value when column type is "string"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartStrCol;
                expect(StatisticsService.histogram).toBeFalsy();

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.histogram).toEqual({
                    data: [
                        {data: '   toto', occurences: 202, formattedValue: '<span class="hiddenChars">   </span>toto'},
                        {data: 'titi', occurences: 2, formattedValue: 'titi'},
                        {data: 'coucou', occurences: 102, formattedValue: 'coucou'},
                        {data: 'cici', occurences: 22, formattedValue: 'cici'}
                    ],
                    key: 'occurrences',
                    label: 'Occurrences',
                    column: barChartStrCol
                });
            }));

            it('should reset non histogram data when column type is "boolean"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartBoolCol;
                StatisticsService.boxPlot = {};
                StatisticsService.stateDistribution = {};

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.boxPlot).toBeFalsy();
                expect(StatisticsService.stateDistribution).toBeFalsy();
            }));

            it('should set the frequency data when column type is "boolean"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartBoolCol;
                expect(StatisticsService.histogram).toBeFalsy();

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.histogram).toEqual({
                    key: 'occurrences',
                    label: 'Occurrences',
                    column: barChartBoolCol,
                    data: barChartBoolCol.statistics.frequencyTable
                });
            }));

            it('should set the range data frequency when column type is "number"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartNumCol;
                StatisticsService.statistics = {
                    common: {
                        COUNT: 4,
                        DISTINCT_COUNT: 5,
                        DUPLICATE_COUNT: 6,
                        VALID: 9,
                        EMPTY: 7,
                        INVALID: 8
                    }, specific: {MIN: 10, MAX: 11, MEAN: 12, VARIANCE: 13}
                };
                expect(StatisticsService.histogram).toBeFalsy();

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.histogram.data[1].data).toEqual([barChartNumCol.statistics.histogram[1].range.min, barChartNumCol.statistics.histogram[1].range.max]);
            }));

            it('should set histogram vertical mode to true when column type is "number"', inject(function (StatisticsService) {
                //given
                stateMock.playground.column = barChartNumCol;
                StatisticsService.statistics = {
                    common: {
                        COUNT: 4,
                        DISTINCT_COUNT: 5,
                        DUPLICATE_COUNT: 6,
                        VALID: 9,
                        EMPTY: 7,
                        INVALID: 8
                    }, specific: {MIN: 10, MAX: 11, MEAN: 12, VARIANCE: 13}
                };
                expect(StatisticsService.histogram).toBeFalsy();

                //when
                StatisticsService.processData();

                //then
                expect(StatisticsService.histogram.vertical).toBe(true);
            }));
        });

        it('should reset charts data when column type is not supported', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = unknownTypeCol;
            StatisticsService.boxPlot = {};
            StatisticsService.histogram = {};
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.histogram).toBeFalsy();
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));
    });

    describe('Process Data : The range slider', function () {
        beforeEach(inject(function(StatisticsService) {
            stateMock.playground.column = {
                'id': '0001',
                type: 'integer',
                domain: 'city name',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    min: 0,
                    max: 11,
                    mean: 12,
                    variance: 13,
                    quantiles: {
                        lowerQuantile: 'NaN'
                    }
                }
            };

            StatisticsService.statistics = {
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8
                },
                specific: {
                    MIN: 0,
                    MAX: 11,
                    MEAN: 12,
                    VARIANCE: 13
                }
            };
        }));

        it('should set range and brush limits to the min and max of the column', inject(function (StatisticsService, FilterService) {
            //given
            FilterService.filters = [];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11
            });
            expect(StatisticsService.histogram.activeLimits).toBe(null);
        }));

        it('should update the brush limits to the existing range filter values', inject(function (StatisticsService, FilterService) {
            //given
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [5, 10]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 5,
                maxBrush: 10,
                minFilterVal: 5,
                maxFilterVal: 10
            });
            expect(StatisticsService.histogram.activeLimits).toEqual([5, 10]);
        }));

        it('should update the brush limits to the minimum', inject(function (StatisticsService, FilterService) {
            //given : -5 < 0(minimum)
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [-15, -10]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 0,
                maxBrush: 0,
                minFilterVal: -15,
                maxFilterVal: -10
            });
        }));

        it('should update the brush limits to the [minimum, maximum] ', inject(function (StatisticsService, FilterService) {
            //given : -5 < 0(minimum)
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [-15, 20]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 0,
                maxBrush: 11,
                minFilterVal: -15,
                maxFilterVal: 20
            });
        }));

        it('should update the brush limits to the maximum', inject(function (StatisticsService, FilterService) {
            //given
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [25, 30]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 11,
                maxBrush: 11,
                minFilterVal: 25,
                maxFilterVal: 30
            });
        }));

        it('should update the brush limits to [minBrush, maximum]', inject(function (StatisticsService, FilterService) {
            //given
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [5, 30]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 5,
                maxBrush: 11,
                minFilterVal: 5,
                maxFilterVal: 30
            });
        }));

        it('should update the brush limits to [minimum, maxBrush]', inject(function (StatisticsService, FilterService) {
            //given
            FilterService.filters = [{colId: '0001', type: 'inside_range', args: {interval: [-25, 10]}}];

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 0,
                maxBrush: 10,
                minFilterVal: -25,
                maxFilterVal: 10
            });
        }));
    });

    describe('Process Data : The boxplot data', function () {
        it('should reset boxplotData when quantile values are NaN', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                'id': '0001',
                type: 'integer',
                domain: 'city name',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    min: 10,
                    max: 11,
                    mean: 12,
                    variance: 13,
                    quantiles: {
                        lowerQuantile: 'NaN'
                    }
                }
            };

            StatisticsService.statistics = {
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8
                },
                specific: {
                    MIN: 10,
                    MAX: 11,
                    MEAN: 12,
                    VARIANCE: 13
                }
            };

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
        }));

        it('should set boxplotData statistics with quantile', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                'id': '0001',
                type: 'integer',
                domain: 'code postal',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    min: 10,
                    max: 11,
                    mean: 12,
                    variance: 13,
                    quantiles: {
                        median: 14,
                        lowerQuantile: 15,
                        upperQuantile: 16
                    }
                }
            };

            StatisticsService.statistics = {
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8
                },
                specific: {MIN: 10, MAX: 11, MEAN: 12, VARIANCE: 13, MEDIAN: 14, LOWER_QUANTILE: 15, UPPER_QUANTILE: 16}
            };

            //when
            StatisticsService.processData();

            //then
            expect(StatisticsService.boxPlot).toEqual({
                min: 10,
                max: 11,
                q1: 15,
                q2: 16,
                median: 14,
                mean: 12,
                variance: 13
            });
        }));
    });

    describe('Process Aggregations : Aggregation charts', function () {
        var currentColumn = {'id': '0001', 'name': 'city'}; // the selected column
        var datasetId = '13654634856752';                   // the current data id
        var preparationId = '2132548345365';                // the current preparation id
        var stepId = '9878645468';                          // the currently viewed step id
        var column = {'id': '0002', 'name': 'state'};       // the column where to perform the aggregation
        var aggregation = 'MAX';                            // the aggregation operation

        var getAggregationsResponse = [                     // the REST aggregation GET result
            {'data': 'Lansing', 'max': 15},
            {'data': 'Helena', 'max': 5},
            {'data': 'Baton Rouge', 'max': 64},
            {'data': 'Annapolis', 'max': 4},
            {'data': 'Pierre', 'max': 104}
        ];

        beforeEach(inject(function (StatisticsService, RecipeService, StorageService) {
            stateMock.playground.column = currentColumn;
            stateMock.playground.dataset = {id: datasetId};
            stateMock.playground.preparation = {id: preparationId};
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({id: stepId});
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        describe('with NO provided aggregation', function () {
            it('should update histogram data with classical occurrence histogram', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService) {
                //given
                stateMock.playground.column = barChartStrCol;
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when());

                //when
                StatisticsService.processAggregation();

                //then
                expect(StatisticsRestService.getAggregations).not.toHaveBeenCalled();
                expect(StatisticsService.histogram).toEqual({
                    data: [
                        {data: '   toto', occurences: 202, formattedValue: '<span class="hiddenChars">   </span>toto'},
                        {data: 'titi', occurences: 2, formattedValue: 'titi'},
                        {data: 'coucou', occurences: 102, formattedValue: 'coucou'},
                        {data: 'cici', occurences: 22, formattedValue: 'cici'}
                    ],
                    key: 'occurrences',
                    label: 'Occurrences',
                    column: barChartStrCol
                });
            }));

            it('should remove saved aggregation on current column/preparation/dataset from storage', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StorageService) {
                //given
                stateMock.playground.column = barChartStrCol;
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when());
                expect(StorageService.removeAggregation).not.toHaveBeenCalled();

                //when
                StatisticsService.processAggregation();

                //then
                expect(StorageService.removeAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
            }));
        });

        describe('with provided aggregation', function () {
            it('should update histogram data from REST call result and aggregation infos on dataset', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService) {
                //given
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when(getAggregationsResponse));
                stateMock.playground.preparation = null;

                //when
                StatisticsService.processAggregation(column, aggregation);
                $rootScope.$digest();

                //then
                expect(StatisticsRestService.getAggregations).toHaveBeenCalledWith({
                    datasetId: '13654634856752',
                    preparationId: null,
                    stepId: null,
                    operations: [{operator: 'MAX', columnId: '0002'}],
                    groupBy: ['0001']
                });
                expect(StatisticsService.histogram).toEqual({
                    data: [
                        {'data': 'Lansing', 'max': 15, 'formattedValue': 'Lansing'},
                        {'data': 'Helena', 'max': 5, 'formattedValue': 'Helena'},
                        {'data': 'Baton Rouge', 'max': 64, 'formattedValue': 'Baton Rouge'},
                        {'data': 'Annapolis', 'max': 4, 'formattedValue': 'Annapolis'},
                        {'data': 'Pierre', 'max': 104, 'formattedValue': 'Pierre'}
                    ],
                    key: 'MAX',
                    label: 'MAX',
                    column: stateMock.playground.column,
                    aggregationColumn: column,
                    aggregation: aggregation
                });
            }));

            it('should update histogram data from aggregation infos on preparation', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService) {
                //given
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when(getAggregationsResponse));

                //when
                StatisticsService.processAggregation(column, aggregation);
                $rootScope.$digest();

                //then
                expect(StatisticsRestService.getAggregations).toHaveBeenCalledWith({
                    datasetId: null,
                    preparationId: '2132548345365',
                    stepId: '9878645468',
                    operations: [{operator: 'MAX', columnId: '0002'}],
                    groupBy: ['0001']
                });
                expect(StatisticsService.histogram).toEqual({
                    data: [
                        {'data': 'Lansing', 'max': 15, 'formattedValue': 'Lansing'},
                        {'data': 'Helena', 'max': 5, 'formattedValue': 'Helena'},
                        {'data': 'Baton Rouge', 'max': 64, 'formattedValue': 'Baton Rouge'},
                        {'data': 'Annapolis', 'max': 4, 'formattedValue': 'Annapolis'},
                        {'data': 'Pierre', 'max': 104, 'formattedValue': 'Pierre'}
                    ],
                    key: 'MAX',
                    label: 'MAX',
                    column: stateMock.playground.column,
                    aggregationColumn: column,
                    aggregation: aggregation
                });
            }));

            it('should save column aggregation in storage', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StorageService) {
                //given
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when(getAggregationsResponse));
                expect(StorageService.setAggregation).not.toHaveBeenCalled();

                //when
                StatisticsService.processAggregation(column, aggregation);
                $rootScope.$digest();

                //then
                expect(StorageService.setAggregation).toHaveBeenCalledWith('13654634856752', '2132548345365', '0001', {
                    aggregation: 'MAX',
                    aggregationColumnId: '0002'
                });
            }));

            it('should reset histogram when REST WS call fails', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService) {
                //given
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.reject());

                //when
                StatisticsService.processAggregation(datasetId, preparationId, stepId, column, aggregation);
                $rootScope.$digest();

                //then
                expect(StatisticsService.histogram).toBeFalsy();
            }));
        });
    });

    describe('Update Statistics : The statistics values', function () {
        it('should init number statistics whithout quantile', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                'id': '0001',
                type: 'integer',
                domain: 'city name',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    min: 10,
                    max: 11,
                    mean: 12,
                    variance: 13,
                    quantiles: {
                        lowerQuantile: 'NaN'
                    }
                }
            };

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.statistics).toBeTruthy();
            expect(StatisticsService.statistics.common).toEqual({
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8
            });
            expect(StatisticsService.statistics.specific).toEqual({
                MIN: 10,
                MAX: 11,
                MEAN: 12,
                VARIANCE: 13
            });
        }));

        it('should init number statistics with quantile', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                'id': '0001',
                type: 'integer',
                domain: 'code postal',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    min: 10,
                    max: 11,
                    mean: 12,
                    variance: 13,
                    quantiles: {
                        median: 14,
                        lowerQuantile: 15,
                        upperQuantile: 16
                    }
                }
            };

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.statistics).toBeTruthy();
            expect(StatisticsService.statistics.common).toEqual({
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8
            });
            expect(StatisticsService.statistics.specific).toEqual({
                MIN: 10,
                MAX: 11,
                MEAN: 12,
                VARIANCE: 13,
                MEDIAN: 14,
                LOWER_QUANTILE: 15,
                UPPER_QUANTILE: 16
            });
        }));

        it('should init text statistics', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                'id': '0001',
                type: 'string',
                domain: 'text',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                    textLengthSummary: {
                        averageLength: 10.13248646854654,
                        minimalLength: 12,
                        maximalLength: 14
                    }
                }
            };
            expect(StatisticsService.statistics).toBeFalsy();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.statistics).toBeTruthy();
            expect(StatisticsService.statistics.common).toEqual({
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8
            });
            expect(StatisticsService.statistics.specific).toEqual({
                AVG_LENGTH: 10.13,
                MIN_LENGTH: 12,
                MAX_LENGTH: 14
            });
        }));

        it('should init common statistics when the column type is not "number" or "text"', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = {
                id: '0001',
                type: 'boolean',
                domain: 'US_STATE_CODE',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9
                }
            };

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.statistics).toBeTruthy();
            expect(StatisticsService.statistics.common).toEqual({
                COUNT: 4,
                DISTINCT_COUNT: 5,
                DUPLICATE_COUNT: 6,
                VALID: 9,
                EMPTY: 7,
                INVALID: 8
            });
            expect(StatisticsService.statistics.specific).toEqual({});
        }));
    });

    describe('Update Statistics : Statistics routing (basic / aggregations)', function () {
        var currentColumn = {'id': '0001', 'name': 'city'}; // the selected column
        var datasetId = '13654634856752';                   // the current data id
        var preparationId = '2132548345365';                // the current preparation id
        var stepId = '9878645468';                          // the currently viewed step id
        var column = {'id': '0002', 'name': 'state'};       // the column where to perform the aggregation
        var aggregation = 'MAX';                            // the aggregation operation

        var getAggregationsResponse = [                     // the REST aggregation GET result
            {'data': 'Lansing', 'max': 15},
            {'data': 'Helena', 'max': 5},
            {'data': 'Baton Rouge', 'max': 64},
            {'data': 'Annapolis', 'max': 4},
            {'data': 'Pierre', 'max': 104}
        ];

        beforeEach(inject(function (StatisticsService, RecipeService, StorageService) {
            stateMock.playground.column = currentColumn;
            stateMock.playground.dataset = {id: datasetId};
            stateMock.playground.preparation = {id: preparationId};
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({id: stepId});
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        it('should update histogram data with classical occurrence when there is no saved aggregation on the current preparation/dataset/column', inject(function ($rootScope, StatisticsService, StorageService) {
            //given
            stateMock.playground.column = barChartStrCol;
            spyOn(StorageService, 'getAggregation').and.returnValue();

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StorageService.getAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
            expect(StatisticsService.histogram).toEqual({
                data: [
                    {data: '   toto', occurences: 202, formattedValue: '<span class="hiddenChars">   </span>toto'},
                    {data: 'titi', occurences: 2, formattedValue: 'titi'},
                    {data: 'coucou', occurences: 102, formattedValue: 'coucou'},
                    {data: 'cici', occurences: 22, formattedValue: 'cici'}
                ],
                key: 'occurrences',
                label: 'Occurrences',
                column: barChartStrCol
            });
        }));

        it('should update histogram data from saved aggregation configuration', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, DatagridService, StorageService) {
            //given
            spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when(getAggregationsResponse));
            var savedAggregation = {
                aggregationColumnId: '0002',
                aggregation: 'MAX'
            };
            spyOn(StorageService, 'getAggregation').and.returnValue(savedAggregation);
            var datagridNumericColumns = [
                {id: '0002', name: 'state'},
                {id: '0003', name: 'name'}
            ];
            spyOn(DatagridService, 'getNumericColumns').and.returnValue(datagridNumericColumns);

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StatisticsRestService.getAggregations).toHaveBeenCalledWith({
                datasetId: null,
                preparationId: '2132548345365',
                stepId: '9878645468',
                operations: [{operator: 'MAX', columnId: '0002'}],
                groupBy: ['0001']
            });
            expect(StatisticsService.histogram).toEqual({
                data: [
                    {'data': 'Lansing', 'max': 15, 'formattedValue': 'Lansing'},
                    {'data': 'Helena', 'max': 5, 'formattedValue': 'Helena'},
                    {'data': 'Baton Rouge', 'max': 64, 'formattedValue': 'Baton Rouge'},
                    {'data': 'Annapolis', 'max': 4, 'formattedValue': 'Annapolis'},
                    {'data': 'Pierre', 'max': 104, 'formattedValue': 'Pierre'}
                ],
                key: 'MAX',
                label: 'MAX',
                column: stateMock.playground.column,
                aggregationColumn: column,
                aggregation: aggregation
            });
        }));

        it('should reset non histogram charts', inject(function (StatisticsService) {
            //given
            stateMock.playground.column = barChartNumCol;
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));
    });

    describe('utils', function () {
        beforeEach(inject(function (StatisticsRestService) {
            spyOn(StatisticsRestService, 'resetCache').and.returnValue();
        }));

        it('should reset all charts, statistics, cache', inject(function (StatisticsRestService, StatisticsService) {
            //given
            StatisticsService.boxPlot = {};
            StatisticsService.histogram = {};
            StatisticsService.stateDistribution = {};
            StatisticsService.statistics = {};

            //when
            StatisticsService.reset(true, true, true);

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.histogram).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
            expect(StatisticsService.statistics).toBeFalsy();
            expect(StatisticsRestService.resetCache).toHaveBeenCalled();
        }));

        it('should NOT reset charts', inject(function (StatisticsService) {
            //given
            StatisticsService.boxPlot = {};
            StatisticsService.histogram = {};
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.reset(false, true, true);

            //then
            expect(StatisticsService.boxPlot).toBeTruthy();
            expect(StatisticsService.histogram).toBeTruthy();
            expect(StatisticsService.stateDistribution).toBeTruthy();
        }));

        it('should NOT reset statistics', inject(function (StatisticsService) {
            //given
            StatisticsService.statistics = {};

            //when
            StatisticsService.reset(true, false, true);

            //then
            expect(StatisticsService.statistics).toBeTruthy();
        }));

        it('should NOT reset cache', inject(function (StatisticsRestService, StatisticsService) {
            //when
            StatisticsService.reset(true, true, false);

            //then
            expect(StatisticsRestService.resetCache).not.toHaveBeenCalled();
        }));

        it('should get numeric columns (as aggregation columns) from datagrid service', inject(function (StatisticsService, DatagridService) {
            //given
            var selectedcolumn = {id: '0001'};
            stateMock.playground.column = selectedcolumn;

            var datagridNumericColumns = [
                {id: '0002'},
                {id: '0003'}
            ];
            spyOn(DatagridService, 'getNumericColumns').and.returnValue(datagridNumericColumns);

            //when
            var aggregationColumns = StatisticsService.getAggregationColumns();

            //then
            expect(aggregationColumns).toBe(datagridNumericColumns);
            expect(DatagridService.getNumericColumns).toHaveBeenCalledWith(selectedcolumn);
        }));
    });

});
