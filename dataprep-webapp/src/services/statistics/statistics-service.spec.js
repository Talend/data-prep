describe('Statistics service', function () {
    'use strict';

    var barChartNumCol = {
        'domain': 'barchartAndNumeric',
        'type': 'numeric',
        'id': '0000',
        'statistics': {
            'frequencyTable': [],
            'histogram': {
                items: [
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
                ]
            },
            count: 4,
            distinctCount: 5,
            duplicateCount: 6,
            empty: 7,
            invalid: 8,
            valid: 9,
            min: 0,
            max: 20,
            mean: 12,
            variance: 13,
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateCol = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'MONTH',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1420070400000, // 01/01/2015
                            'max': 1422748800000 // 01/02/2015
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1422748800000, // 01/02/2015
                            'max': 1425168000000 // 01/03/2015
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColCENTURY = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'CENTURY',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 946684800000, // 01/01/2000
                            'max': 4102444800000 // 01/01/2100
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 4102444800000, // 01/01/2100
                            'max': 7258118400000 // 01/01/2200
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColDECADE = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'DECADE',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 946684800000, // 01/01/2000
                            'max': 1262304000000 // 01/01/2010
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1262304000000, // 01/01/2010
                            'max': 1577836800000 // 01/01/2020
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColYEAR = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'YEAR',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1388534400000, // 01/01/2014
                            'max': 1420070400000 // 01/01/2015
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1420070400000, // 01/01/2015
                            'max': 1451606400000 // 01/01/2016
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColHAFLYEAR = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'HALF_YEAR',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1388534400000, // 01/01/2014
                            'max': 1404172800000 // 01/07/2014
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1404172800000, // 01/07/2014
                            'max': 1420070400000 // 01/01/2015
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColQUARTER = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'QUARTER',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1388534400000, // 01/01/2015
                            'max': 1396310400000 // 01/04/2015
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1396310400000, // 01/04/2015
                            'max': 1404172800000 // 01/07/2014
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColMONTH = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'MONTH',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1420070400000, // 01/01/2015
                            'max': 1422748800000 // 01/02/2015
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1422748800000, // 01/02/2015
                            'max': 1425168000000 // 01/03/2015
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColWEEK = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'WEEK',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1451865600000, // 04/01/2016
                            'max': 1452470400000 // 11/01/2016
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1452470400000, // 11/01/2016
                            'max': 1453075200000 // 18/01/2016
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColDAY = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: {
                pace: 'DAY',
                items: [
                    {
                        'occurrences': 15,
                        'range': {
                            'min': 1451606400000, // 01/01/2016
                            'max': 1451692800000 // 02/01/2016
                        }
                    },
                    {
                        'occurrences': 5,
                        'range': {
                            'min': 1451692800000, // 02/01/2016
                            'max': 1451779200000 // 03/01/2016
                        }
                    }
                ]
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
            quantiles: {
                lowerQuantile: 'NaN'
            }
        }
    };

    var barChartDateColWithoutHistogram = {
        'domain': 'barchartAndDate',
        'type': 'date',
        'id': '0000',
        'statistics': {
            frequencyTable: [],
            histogram: null,
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5
                }
            ],
            count: 20,
            distinctCount: 14,
            duplicateCount: 6,
            empty: 0,
            invalid: 0,
            valid: 0,
            min: 'NaN',
            max: 'NaN',
            mean: 'NaN',
            variance: 'NaN',
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
                    'occurrences': 202
                },
                {
                    'data': 'titi',
                    'occurrences': 2
                },
                {
                    'data': 'coucou',
                    'occurrences': 102
                },
                {
                    'data': 'cici',
                    'occurrences': 22
                }
            ],
            'patternFrequencyTable': [
                {
                    'pattern': '   Aa9',
                    'occurrences': 202
                },
                {
                    'pattern': 'yyyy-M-d',
                    'occurrences': 2
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

    var barChartStrCol2 = {
        id: '0010',
        'domain': 'barchartAndString',
        'type': 'string',
        'statistics': {
            'frequencyTable': [
                {
                    'data': '   toto',
                    'occurrences': 1
                },
                {
                    'data': 'titi',
                    'occurrences': 1
                },
                {
                    'data': 'coucou',
                    'occurrences': 1
                },
                {
                    'data': 'cici',
                    'occurrences': 1
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
                    'occurrences': 202
                },
                {
                    'data': 'WA',
                    'occurrences': 2
                },
                {
                    'data': 'DE',
                    'occurrences': 102
                },
                {
                    'data': 'IL',
                    'occurrences': 22
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
                    'occurrences': 2
                },
                {
                    'data': 'false',
                    'occurrences': 20
                },
                {
                    'data': '',
                    'occurrences': 10
                }
            ]
        }
    };

    var unknownTypeCol = {
        'domain': '',
        'type': 'unknown',
        statistics: {}
    };

    var stateMock;
    var workerWrapper;

    beforeEach(module('data-prep.services.statistics', function ($provide) {
        stateMock = {
            playground: {
                grid: {},
                filter: {gridFilters: []},
                data: {},
                statistics: {}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, WorkerService, StateService) {
        spyOn(WorkerService, 'create').and.callFake(function (importLibs, helperFns, mainFn) {
            workerWrapper = {
                postMessage: function (args) {
                    var workerPostedMessage = mainFn.apply(null, args);
                    return $q.when(workerPostedMessage);
                },
                terminate: jasmine.createSpy('terminate'),
                clean: jasmine.createSpy('clean')
            };
            return workerWrapper;
        });
        spyOn(StateService, 'setStatisticsHistogramActiveLimits').and.returnValue();
        spyOn(StateService, 'setStatisticsPatterns').and.returnValue();
        spyOn(StateService, 'setStatisticsFilteredPatterns').and.returnValue();
        spyOn(StateService, 'setStatisticsHistogram').and.returnValue();
        spyOn(StateService, 'setStatisticsFilteredHistogram').and.returnValue();
    }));

    describe('filters', function () {
        beforeEach(inject(function (FilterService) {
            spyOn(FilterService, 'addFilter').and.returnValue();
        }));

        it('should create a function that reinit range limits when the selected column is the same', inject(function (StatisticsService, StateService) {
            //given
            var originalRangeLimits = {
                min: 5,
                max: 55,
                minBrush: 5,
                maxBrush: 22,
                minFilterVal: 0,
                maxFilterVal: 22
            };
            StatisticsService.rangeLimits = originalRangeLimits;
            var column = {id: '0000', statistics: {min: 5, max: 55}};
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.selectedColumn = column;

            var removeCallback = StatisticsService.getRangeFilterRemoveFn();

            //when
            removeCallback({colId: '0000'});

            //then
            expect(StatisticsService.rangeLimits).not.toBe(originalRangeLimits);
            expect(StatisticsService.rangeLimits).toEqual({
                min: 5,
                max: 55
            });
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([column.statistics.min, column.statistics.max]);
        }));

        it('should create a function that do nothing when the selected column is NOT the same', inject(function (StatisticsService) {
            //given
            var originalRangeLimits = {
                min: 5,
                max: 55,
                minBrush: 5,
                maxBrush: 22,
                minFilterVal: 0,
                maxFilterVal: 22
            };
            StatisticsService.rangeLimits = originalRangeLimits;
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.selectedColumn = {id: '0000', statistics: {min: 5, max: 55}};

            var removeCallback = StatisticsService.getRangeFilterRemoveFn();

            //when
            removeCallback({colId: '0001'}); // not the same column as the filters one

            //then
            expect(StatisticsService.rangeLimits).toBe(originalRangeLimits);
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
            {'data': 'Lansing', 'MAX': 15},
            {'data': 'Helena', 'MAX': 5},
            {'data': 'Baton Rouge', 'MAX': 64},
            {'data': 'Annapolis', 'MAX': 4},
            {'data': 'Pierre', 'MAX': 104}
        ];

        beforeEach(inject(function (StatisticsService, RecipeService, StorageService) {
            stateMock.playground.grid.selectedColumn = currentColumn;
            stateMock.playground.dataset = {id: datasetId};
            stateMock.playground.preparation = {id: preparationId};
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({transformation: {stepId: stepId}});
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        describe('with NO provided aggregation', function () {
            it('should update histogram data with classical occurrence histogram with filter', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartStrCol;
                stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};
                spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when());

                //when
                StatisticsService.processAggregation();

                //then
                expect(StatisticsRestService.getAggregations).not.toHaveBeenCalled();
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: '   toto',
                            occurrences: 202,
                            formattedValue: '<span class="hiddenChars">   </span>toto'
                        },
                        {data: 'titi', occurrences: 2, formattedValue: 'titi'},
                        {data: 'coucou', occurrences: 102, formattedValue: 'coucou'},
                        {data: 'cici', occurrences: 22, formattedValue: 'cici'}
                    ],
                    keyField: 'formattedValue',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartStrCol,
                    vertical: false,
                    className: null
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {formattedValue: '<span class="hiddenChars">   </span>toto', filteredOccurrences: 3},
                        {formattedValue: 'titi', filteredOccurrences: 2},
                        {formattedValue: 'coucou', filteredOccurrences: 0},
                        {formattedValue: 'cici', filteredOccurrences: 0}
                    ],
                    keyField: 'formattedValue',
                    valueField: 'filteredOccurrences',
                    label: null,
                    column: barChartStrCol,
                    vertical: false,
                    className: 'blueBar'
                });
            }));

            it('should remove saved aggregation on current column/preparation/dataset from storage when the aggregation related falsy', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StorageService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartStrCol;
                stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};
                expect(StorageService.removeAggregation).not.toHaveBeenCalled();

                //when
                StatisticsService.processAggregation(undefined, 'MAX');

                //then
                expect(StorageService.removeAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
            }));

            it('should remove saved aggregation on current column/preparation/dataset from storage when the aggregation name is falsy', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StorageService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartStrCol;
                stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};
                expect(StorageService.removeAggregation).not.toHaveBeenCalled();

                //when
                StatisticsService.processAggregation({}, undefined);

                //then
                expect(StorageService.removeAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
            }));
        });

        describe('with provided aggregation', function () {
            it('should update histogram data from REST call result and aggregation infos on dataset', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StateService) {
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
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {'data': 'Lansing', 'MAX': 15, 'formattedValue': 'Lansing'},
                        {'data': 'Helena', 'MAX': 5, 'formattedValue': 'Helena'},
                        {'data': 'Baton Rouge', 'MAX': 64, 'formattedValue': 'Baton Rouge'},
                        {'data': 'Annapolis', 'MAX': 4, 'formattedValue': 'Annapolis'},
                        {'data': 'Pierre', 'MAX': 104, 'formattedValue': 'Pierre'}
                    ],
                    keyField: 'formattedValue',
                    valueField: 'MAX',
                    label: 'MAX',
                    vertical: false,
                    className: 'blueBar',
                    column: stateMock.playground.grid.selectedColumn,
                    aggregationColumn: column,
                    aggregation: aggregation
                });
            }));

            it('should update histogram data from aggregation infos on preparation', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StateService) {
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
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {'data': 'Lansing', 'MAX': 15, 'formattedValue': 'Lansing'},
                        {'data': 'Helena', 'MAX': 5, 'formattedValue': 'Helena'},
                        {'data': 'Baton Rouge', 'MAX': 64, 'formattedValue': 'Baton Rouge'},
                        {'data': 'Annapolis', 'MAX': 4, 'formattedValue': 'Annapolis'},
                        {'data': 'Pierre', 'MAX': 104, 'formattedValue': 'Pierre'}
                    ],
                    keyField: 'formattedValue',
                    valueField: 'MAX',
                    label: 'MAX',
                    column: stateMock.playground.grid.selectedColumn,
                    vertical: false,
                    className: 'blueBar',
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

    describe('init Statistics : The statistics values', function () {
        it('should init number statistics whithout quantile', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = {
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
            stateMock.playground.grid.selectedColumn = {
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
            stateMock.playground.grid.selectedColumn = {
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
            stateMock.playground.grid.selectedColumn = {
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

    describe('Update Statistics : The statistics values', function () {

        beforeEach(inject(function () {
            stateMock.playground.grid.selectedColumn = {
                'id': '0001',
                'name': 'city',
                'type': 'date',
                'domain': 'date',
                'statistics': {
                    'patternFrequencyTable': []
                }
            };
            stateMock.playground.grid.filteredRecords = [{'0001': '10-12-2015'}, {'0001': '2015-12-02'}, {'0001': 'To,/'}, {'0001': '2015-12-02'}, {'0001': '10/12-20'}];
        }));

        it('should update empty pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn.statistics.patternFrequencyTable = [
                {
                    'pattern': '',
                    'occurrences': 1
                }
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [{
                '0001': 'toto'
            }];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    'pattern': '',
                    'occurrences': 1,
                    'filteredOccurrences': 0
                }
            ]);
        }));

        it('should update date pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn.statistics.patternFrequencyTable = [
                {
                    'pattern': 'd-M-yyyy',
                    'occurrences': 1
                },
                {
                    'pattern': 'yyyy-M-d',
                    'occurrences': 2
                }
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                {'0001': '18-01-2015'},
                {'0001': '2016-01-01'}
            ];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    'pattern': 'd-M-yyyy',
                    'occurrences': 1,
                    'filteredOccurrences': 1
                },
                {
                    'pattern': 'yyyy-M-d',
                    'occurrences': 2,
                    'filteredOccurrences': 1
                }
            ]);
        }));

        it('should update non date pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn.statistics.patternFrequencyTable = [
                {
                    'pattern': 'Aa,/',
                    'occurrences': 3
                },
                {
                    'pattern': '99/99-99',
                    'occurrences': 4
                }
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                {'0001': 'Bg,/'},
                {'0001': '26/42-98'}
            ];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    'pattern': 'Aa,/',
                    'occurrences': 3,
                    'filteredOccurrences': 1
                },
                {
                    'pattern': '99/99-99',
                    'occurrences': 4,
                    'filteredOccurrences': 1
                }
            ]);
        }));

        it('should update pattern statistics without filter', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn.statistics.patternFrequencyTable = [
                {
                    'pattern': 'Aa,/',
                    'occurrences': 3
                },
                {
                    'pattern': '99/99-99',
                    'occurrences': 4
                }
            ];
            stateMock.playground.filter.gridFilters = [];
            stateMock.playground.grid.filteredRecords = [];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    'pattern': 'Aa,/',
                    'occurrences': 3,
                    'filteredOccurrences': 3
                },
                {
                    'pattern': '99/99-99',
                    'occurrences': 4,
                    'filteredOccurrences': 4
                }
            ]);
        }));
    });

    describe('Update Statistics : Statistics routing (basic / aggregations)', function () {
        var currentColumn = {                               // the selected column
            'id': '0001',
            'name': 'city',
            'domain': '',
            'type': '',
            'statistics': {'patternFrequencyTable': []}
        };
        var datasetId = '13654634856752';                   // the current data id
        var preparationId = '2132548345365';                // the current preparation id
        var stepId = '9878645468';                          // the currently viewed step id
        var column = {'id': '0002', 'name': 'state'};       // the column where to perform the aggregation
        var aggregation = 'MAX';                            // the aggregation operation

        var getAggregationsResponse = [                     // the REST aggregation GET result
            {'data': 'Lansing', 'MAX': 15},
            {'data': 'Helena', 'MAX': 5},
            {'data': 'Baton Rouge', 'MAX': 64},
            {'data': 'Annapolis', 'MAX': 4},
            {'data': 'Pierre', 'MAX': 104}
        ];

        beforeEach(inject(function (StatisticsService, RecipeService, StorageService) {
            stateMock.playground.grid.selectedColumn = currentColumn;
            stateMock.playground.dataset = {id: datasetId};
            stateMock.playground.preparation = {id: preparationId};
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({transformation: {stepId: stepId}});
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        it('should update histogram data with classical occurrence when there is no saved aggregation on the current preparation/dataset/column with filter', inject(function ($rootScope, StatisticsService, StorageService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol;
            stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};
            spyOn(StorageService, 'getAggregation').and.returnValue();

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StorageService.getAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        data: '   toto',
                        occurrences: 202,
                        formattedValue: '<span class="hiddenChars">   </span>toto'
                    },
                    {data: 'titi', occurrences: 2, formattedValue: 'titi'},
                    {data: 'coucou', occurrences: 102, formattedValue: 'coucou'},
                    {data: 'cici', occurrences: 22, formattedValue: 'cici'}
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                vertical: false,
                className: null,
                column: barChartStrCol
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {formattedValue: '<span class="hiddenChars">   </span>toto', filteredOccurrences: 3},
                    {formattedValue: 'titi', filteredOccurrences: 2},
                    {formattedValue: 'coucou', filteredOccurrences: 0},
                    {formattedValue: 'cici', filteredOccurrences: 0}
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                vertical: false,
                className: 'blueBar',
                column: barChartStrCol
            });
        }));

        it('should update histogram data from saved aggregation configuration', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, DatagridService, StorageService, StateService) {
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
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    {'data': 'Lansing', 'MAX': 15, 'formattedValue': 'Lansing'},
                    {'data': 'Helena', 'MAX': 5, 'formattedValue': 'Helena'},
                    {'data': 'Baton Rouge', 'MAX': 64, 'formattedValue': 'Baton Rouge'},
                    {'data': 'Annapolis', 'MAX': 4, 'formattedValue': 'Annapolis'},
                    {'data': 'Pierre', 'MAX': 104, 'formattedValue': 'Pierre'}
                ],
                keyField: 'formattedValue',
                label: 'MAX',
                valueField: 'MAX',
                vertical: false,
                className: 'blueBar',
                column: stateMock.playground.grid.selectedColumn,
                aggregationColumn: column,
                aggregation: aggregation
            });
        }));

        it('should reset non histogram charts', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartNumCol;
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));
    });

    describe('Update Statistics : The range slider', function () {
        beforeEach(inject(function (StatisticsService) {
            stateMock.playground.grid.selectedColumn = {
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
                    },
                    histogram: {items: []}
                }
            };
            stateMock.playground.statistics.histogram = {};
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

        it('should set range and brush limits to the min and max of the column', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.filter.gridFilters = [];

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null); // due to the reset
            expect(StateService.setStatisticsHistogramActiveLimits.calls.count()).toBe(1); // not called with value
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11
            });
        }));

        it('should update the brush limits to the existing range filter values', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [5, 10]}
            }];

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.rangeLimits).toEqual({
                min: 0,
                max: 11,
                minBrush: 5,
                maxBrush: 10,
                minFilterVal: 5,
                maxFilterVal: 10
            });
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([5, 10]);
        }));

        it('should update the brush limits to the minimum', inject(function (StatisticsService) {
            //given : -5 < 0(minimum)
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [-15, -10]}
            }];

            //when
            StatisticsService.updateStatistics();

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

        it('should update the brush limits to the [minimum, maximum] ', inject(function (StatisticsService) {
            //given : -5 < 0(minimum)
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [-15, 20]}
            }];

            //when
            StatisticsService.updateStatistics();

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

        it('should update the brush limits to the maximum', inject(function (StatisticsService) {
            //given
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [25, 30]}
            }];

            //when
            StatisticsService.updateStatistics();

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

        it('should update the brush limits to [minBrush, maximum]', inject(function (StatisticsService) {
            //given
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [5, 30]}
            }];

            //when
            StatisticsService.updateStatistics();

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

        it('should update the brush limits to [minimum, maxBrush]', inject(function (StatisticsService) {
            //given
            stateMock.playground.filter.gridFilters = [{
                colId: '0001',
                type: 'inside_range',
                args: {interval: [-25, 10]}
            }];

            //when
            StatisticsService.updateStatistics();

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

    describe('Update Statistics : The boxplot data', function () {
        it('should reset boxplotData when quantile values are NaN', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = {
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
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
        }));

        it('should set boxplotData statistics with quantile', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = {
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
            StatisticsService.updateStatistics();

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

    describe('Update Statistics : process Geo chart', function() {
        it('should set stateDistribution for geo chart when the column domain contains STATE_CODE', inject(function (StatisticsService) {
            //given
            expect(StatisticsService.stateDistribution).toBeFalsy();
            stateMock.playground.grid.selectedColumn = mapCol;

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.stateDistribution).toBe(mapCol);
        }));

        it('should reset non geo chart data when the column domain contains STATE_CODE', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = mapCol;
            StatisticsService.boxPlot = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith(null);
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null);
        }));
    });

    describe('Update Statistics : process horizontal chart', function() {
        it('should reset non histogram data when column type is "string"', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol;
            stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};
            StatisticsService.boxPlot = {};
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));

        it('should set the frequency data with formatted value when column type is "string" with filter', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol;
            stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        data: '   toto',
                        occurrences: 202,
                        formattedValue: '<span class="hiddenChars">   </span>toto'
                    },
                    {data: 'titi', occurrences: 2, formattedValue: 'titi'},
                    {data: 'coucou', occurrences: 102, formattedValue: 'coucou'},
                    {data: 'cici', occurrences: 22, formattedValue: 'cici'}
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                column: barChartStrCol,
                vertical: false,
                className: null
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {formattedValue: '<span class="hiddenChars">   </span>toto', filteredOccurrences: 3},
                    {formattedValue: 'titi', filteredOccurrences: 2},
                    {formattedValue: 'coucou', filteredOccurrences: 0},
                    {formattedValue: 'cici', filteredOccurrences: 0}
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                column: barChartStrCol,
                vertical: false,
                className: 'blueBar'
            });
        }));

        it('should set the frequency data with formatted value when column type is "string" without filter', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol2;
            stateMock.playground.grid.filteredOccurences = {'   toto': 1, 'coucou': 1, 'cici': 1, 'titi': 1};
            expect(StatisticsService.histogram).toBeFalsy();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        data: '   toto',
                        occurrences: 1,
                        formattedValue: '<span class="hiddenChars">   </span>toto'
                    },
                    {data: 'titi', occurrences: 1, formattedValue: 'titi'},
                    {data: 'coucou', occurrences: 1, formattedValue: 'coucou'},
                    {data: 'cici', occurrences: 1, formattedValue: 'cici'}
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                column: barChartStrCol2,
                vertical: false,
                className: null
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {formattedValue: '<span class="hiddenChars">   </span>toto', filteredOccurrences: 1},
                    {formattedValue: 'titi', filteredOccurrences: 1},
                    {formattedValue: 'coucou', filteredOccurrences: 1},
                    {formattedValue: 'cici', filteredOccurrences: 1}
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                column: barChartStrCol2,
                vertical: false,
                className: 'blueBar'
            });
        }));

        it('should reset non histogram data when column type is "boolean"', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartBoolCol;
            stateMock.playground.grid.filteredOccurences = {'true': 3, 'false': 2};
            StatisticsService.boxPlot = {};
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));

        it('should set the frequency data when column type is "boolean"', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartBoolCol;
            stateMock.playground.grid.filteredOccurences = {'true': 3, 'false': 2};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    {'formattedValue': 'true', 'occurrences': 2, 'data': 'true'},
                    {'formattedValue': 'false', 'occurrences': 20, 'data': 'false'},
                    {'formattedValue': '', 'occurrences': 10, 'data': ''}
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                column: barChartBoolCol,
                vertical: false,
                className: null
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {'formattedValue': 'true', 'filteredOccurrences': 3},
                    {'formattedValue': 'false', 'filteredOccurrences': 2},
                    {'formattedValue': '', 'filteredOccurrences': 0}
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                column: barChartBoolCol,
                vertical: false,
                className: 'blueBar'
            });
        }));
    });

    describe('Update Statistics : process vertical chart', function() {
        describe('number', function () {
            it('should set the range data frequency when column type is "number" with filters', inject(function ($rootScope, StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartNumCol;
                stateMock.playground.filter.gridFilters = [{}];
                stateMock.playground.grid.filteredOccurences = {1: 2, 3: 1, 11: 6};
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

                //when
                StatisticsService.updateStatistics();
                $rootScope.$digest();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {data: {type: 'number', min: 0, max: 10}, occurrences: 5},
                        {data: {type: 'number', min: 10, max: 20}, occurrences: 15}
                    ],
                    keyField: 'data',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartNumCol,
                    vertical: true
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {data: {type: 'number', min: 0, max: 10}, filteredOccurrences: 3},
                        {data: {type: 'number', min: 10, max: 20}, filteredOccurrences: 6}
                    ],
                    keyField: 'data',
                    valueField: 'filteredOccurrences',
                    label: 'Filtered Occurrences',
                    column: barChartNumCol,
                    vertical: true
                });
            }));

            it('should set the range data frequency without filters', inject(function ($rootScope, StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartNumCol;
                stateMock.playground.filter.gridFilters = [];
                stateMock.playground.grid.filteredOccurences = null;
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

                //when
                StatisticsService.updateStatistics();
                $rootScope.$digest();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {data: {type: 'number', min: 0, max: 10}, occurrences: 5},
                        {data: {type: 'number', min: 10, max: 20}, occurrences: 15}
                    ],
                    keyField: 'data',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartNumCol,
                    vertical: true
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {data: {type: 'number', min: 0, max: 10}, filteredOccurrences: 5},
                        {data: {type: 'number', min: 10, max: 20}, filteredOccurrences: 15}
                    ],
                    keyField: 'data',
                    valueField: 'filteredOccurrences',
                    label: 'Filtered Occurrences',
                    column: barChartNumCol,
                    vertical: true
                });
            }));
        });

        describe('date', function () {
            it('should NOT set the range histogram when there is no histogram', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColWithoutHistogram;
                stateMock.playground.statistics = {};

                //when
                StatisticsService.updateStatistics();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith(null);
                expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null);
            }));

            it('should set the range data frequency when column type is "date" with filters', inject(function ($rootScope, StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateCol;
                stateMock.playground.filter.gridFilters = [{}];
                stateMock.playground.grid.filteredOccurences = {
                    '05/01/2015': 6,
                    '12/01/2015': 4,
                    'aze': 2,
                    '02/25/2015': 3
                };
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 2015',
                                    min: new Date(2015, 0, 1).getTime(),
                                    max: new Date(2015, 1, 1).getTime()
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Feb 2015',
                                    min:  new Date(2015, 1, 1).getTime(),
                                    max: new Date(2015, 2, 1).getTime()
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();
                $rootScope.$digest();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1).getTime(),
                                max: new Date(2015, 1, 1).getTime()
                            },
                            occurrences: 15
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min:  new Date(2015, 1, 1).getTime(),
                                max: new Date(2015, 2, 1).getTime()
                            },
                            occurrences: 5
                        }],
                    keyField: 'data',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartDateCol,
                    vertical: true
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1).getTime(),
                                max: new Date(2015, 1, 1).getTime()
                            },
                            occurrences: 15,
                            filteredOccurrences: 10
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min:  new Date(2015, 1, 1).getTime(),
                                max: new Date(2015, 2, 1).getTime()
                            },
                            occurrences: 5,
                            filteredOccurrences: 3
                        }],
                    keyField: 'data',
                    valueField: 'filteredOccurrences',
                    label: 'Filtered Occurrences',
                    column: barChartDateCol,
                    vertical: true
                });
            }));

            it('should set the range data frequency with no filters', inject(function ($rootScope, StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateCol;
                stateMock.playground.filter.gridFilters = [];
                stateMock.playground.grid.filteredOccurences = null;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 2015',
                                    min: new Date(2015, 0, 1).getTime(),
                                    max: new Date(2015, 1, 1).getTime()
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Feb 2015',
                                    min:  new Date(2015, 1, 1).getTime(),
                                    max: new Date(2015, 2, 1).getTime()
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();
                $rootScope.$digest();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1).getTime(),
                                max: new Date(2015, 1, 1).getTime()
                            },
                            occurrences: 15
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min:  new Date(2015, 1, 1).getTime(),
                                max: new Date(2015, 2, 1).getTime()
                            },
                            occurrences: 5
                        }],
                    keyField: 'data',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartDateCol,
                    vertical: true
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1).getTime(),
                                max: new Date(2015, 1, 1).getTime()
                            },
                            occurrences: 15,
                            filteredOccurrences: 15
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min:  new Date(2015, 1, 1).getTime(),
                                max: new Date(2015, 2, 1).getTime()
                            },
                            occurrences: 5,
                            filteredOccurrences: 5
                        }],
                    keyField: 'data',
                    valueField: 'filteredOccurrences',
                    label: 'Filtered Occurrences',
                    column: barChartDateCol,
                    vertical: true
                });
            }));

            it('should adapt date range label to century', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColCENTURY;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: '[2000, 2100[',
                                    min: new Date(2000, 0, 1),
                                    max: new Date(2100, 0, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: '[2100, 2200[',
                                    min: new Date(2100, 0, 1),
                                    max: new Date(2200, 0, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: '[2000, 2100[',
                    min: new Date(2000, 0, 1).getTime(),
                    max: new Date(2100, 0, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: '[2100, 2200[',
                    min: new Date(2100, 0, 1).getTime(),
                    max: new Date(2200, 0, 1).getTime()
                });
            }));

            it('should adapt date range label to decade', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColDECADE;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: '[2000, 2010[',
                                    min: new Date(2000, 0, 1),
                                    max: new Date(2010, 0, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: '[2010, 2020[',
                                    min: new Date(2010, 0, 1),
                                    max: new Date(2020, 0, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: '[2000, 2010[',
                    min: new Date(2000, 0, 1).getTime(),
                    max: new Date(2010, 0, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: '[2010, 2020[',
                    min: new Date(2010, 0, 1).getTime(),
                    max: new Date(2020, 0, 1).getTime()
                });
            }));

            it('should adapt date range label to year', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColYEAR;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: '2014',
                                    min: new Date(2014, 0, 1),
                                    max: new Date(2015, 0, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: '2015',
                                    min: new Date(2015, 0, 1),
                                    max: new Date(2016, 0, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: '2014',
                    min: new Date(2014, 0, 1).getTime(),
                    max: new Date(2015, 0, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: '2015',
                    min: new Date(2015, 0, 1).getTime(),
                    max: new Date(2016, 0, 1).getTime()
                });
            }));

            it('should adapt date range label to half year', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColHAFLYEAR;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'H1 2014',
                                    min: new Date(2014, 0, 1),
                                    max: new Date(2014, 6, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'H2 2014',
                                    min: new Date(2014, 6, 1),
                                    max: new Date(2015, 0, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: 'H1 2014',
                    min: new Date(2014, 0, 1).getTime(),
                    max: new Date(2014, 6, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: 'H2 2014',
                    min: new Date(2014, 6, 1).getTime(),
                    max: new Date(2015, 0, 1).getTime()
                });
            }));

            it('should adapt date range label to quarter', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColQUARTER;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Q1 2014',
                                    min: new Date(2014, 0, 1),
                                    max: new Date(2014, 3, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Q2 2014',
                                    min: new Date(2014, 3, 1),
                                    max: new Date(2014, 6, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: 'Q1 2014',
                    min: new Date(2014, 0, 1).getTime(),
                    max: new Date(2014, 3, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: 'Q2 2014',
                    min: new Date(2014, 3, 1).getTime(),
                    max: new Date(2014, 6, 1).getTime()
                });
            }));

            it('should adapt date range label to month', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColMONTH;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 2015',
                                    min: new Date(2015, 0, 1),
                                    max: new Date(2015, 1, 1)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Feb 2015',
                                    min: new Date(2015, 1, 1),
                                    max: new Date(2015, 2, 1)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: 'Jan 2015',
                    min: new Date(2015, 0, 1).getTime(),
                    max: new Date(2015, 1, 1).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: 'Feb 2015',
                    min: new Date(2015, 1, 1).getTime(),
                    max: new Date(2015, 2, 1).getTime()
                });
            }));

            it('should adapt date range label to week', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColWEEK;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'W01 2016',
                                    min: new Date(2016, 0, 4),
                                    max: new Date(2016, 0, 11)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'W02 2016',
                                    min: new Date(2016, 0, 11),
                                    max: new Date(2016, 0, 18)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: 'W01 2016',
                    min: new Date(2016, 0, 4).getTime(),
                    max: new Date(2016, 0, 11).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: 'W02 2016',
                    min: new Date(2016, 0, 11).getTime(),
                    max: new Date(2016, 0, 18).getTime()
                });
            }));

            it('should adapt date range label to day', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumn = barChartDateColDAY;
                stateMock.playground.statistics = {
                    histogram: {
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 1, 2016',
                                    min: new Date(2016, 0, 1),
                                    max: new Date(2016, 0, 2)
                                },
                                occurrences: 15,
                                filteredOccurrences: 10
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 2, 2016',
                                    min: new Date(2016, 0, 2),
                                    max: new Date(2016, 0, 3)
                                },
                                occurrences: 5,
                                filteredOccurrences: 3
                            }
                        ]
                    }
                };

                //when
                StatisticsService.updateStatistics();

                //then
                var histogram = StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                expect(histogram.data[0].data).toEqual({
                    type: 'date',
                    label: 'Jan 1, 2016',
                    min: new Date(2016, 0, 1).getTime(),
                    max: new Date(2016, 0, 2).getTime()
                });
                expect(histogram.data[1].data).toEqual({
                    type: 'date',
                    label: 'Jan 2, 2016',
                    min: new Date(2016, 0, 2).getTime(),
                    max: new Date(2016, 0, 3).getTime()
                });
            }));
        });
    });

    describe('Update Statistics : not supported column type', function() {
        it('should reset charts data', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumn = unknownTypeCol;
            StatisticsService.boxPlot = {};
            StatisticsService.stateDistribution = {};

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith(null);
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null);
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
        }));
    });

    describe('Update Filtered Statistics', function(){

        var currentColumn = {                               // the selected column
            'id': '0001',
            'name': 'city',
            'domain': '',
            'type': '',
            'statistics': {'patternFrequencyTable': []}
        };
        var datasetId = '13654634856752';                   // the current data id
        var preparationId = '2132548345365';                // the current preparation id
        var stepId = '9878645468';                          // the currently viewed step id

        beforeEach(inject(function (StatisticsService, RecipeService, StorageService) {
            stateMock.playground.grid.selectedColumn = currentColumn;
            stateMock.playground.dataset = {id: datasetId};
            stateMock.playground.preparation = {id: preparationId};
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({transformation: {stepId: stepId}});
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        it('should update filtered Numeric column', inject(function(StatisticsService, StateService, StorageService){
            //given
            stateMock.playground.grid.selectedColumn = barChartNumCol;
            stateMock.playground.filter.gridFilters = [{
                colId: '0000',
                type: 'inside_range',
                args: {interval: [5, 10]}
            }];
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.filteredOccurences = {
                1: 2,
                3: 1,
                11: 6
            };
            spyOn(StorageService, 'getAggregation').and.returnValue();

            //when
            StatisticsService.updateFilteredStatistics();

            //then
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                'data': [
                    {
                        'data': {
                            'type': 'number',
                            'min': 0,
                            'max': 10
                        },
                        'filteredOccurrences': 3
                    },
                    {
                        'data': {
                            'type': 'number',
                            'min': 10,
                            'max': 20
                        },
                        'filteredOccurrences': 6
                    }
                ],
                'keyField': 'data',
                'valueField': 'filteredOccurrences',
                'label': 'Filtered Occurrences',
                'column': barChartNumCol,
                'vertical': true
            });
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([5, 10]);
        }));

        it('should update filtered Date column', inject(function($rootScope, StatisticsService, StorageService, StateService){
            //given
            spyOn(StorageService, 'getAggregation').and.returnValue();
            stateMock.playground.grid.selectedColumn = barChartDateCol;
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredOccurences = {
                '05/01/2015': 6,
                '12/01/2015': 4,
                'aze': 2,
                '02/25/2015': 3
            };
            stateMock.playground.statistics = {
                histogram: {
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1),
                                max: new Date(2015, 1, 1)
                            },
                            occurrences: 15,
                            filteredOccurrences: 10
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min:  new Date(2015, 1, 1),
                                max: new Date(2015, 2, 1)
                            },
                            occurrences: 5,
                            filteredOccurrences: 3
                        }
                    ]
                }
            };

            //when
            StatisticsService.updateFilteredStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        data: {
                            type: 'date',
                            label: 'Jan 2015',
                            min: new Date(2015, 0, 1),
                            max: new Date(2015, 1, 1)
                        },
                        occurrences: 15,
                        filteredOccurrences: 10
                    },
                    {
                        data: {
                            type: 'date',
                            label: 'Feb 2015',
                            min:  new Date(2015, 1, 1),
                            max: new Date(2015, 2, 1)
                        },
                        occurrences: 5,
                        filteredOccurrences: 3
                    }],
                keyField: 'data',
                valueField: 'filteredOccurrences',
                label: 'Filtered Occurrences',
                column: barChartDateCol,
                vertical: true
            });
        }));

        it('should update filtered Text column', inject(function(StorageService, StatisticsService, StateService){
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol;
            spyOn(StorageService, 'getAggregation').and.returnValue();
            stateMock.playground.grid.filteredOccurences = {'   toto': 3, 'titi': 2};

            //when
            StatisticsService.updateFilteredStatistics();

            //then
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {formattedValue: '<span class="hiddenChars">   </span>toto', filteredOccurrences: 3},
                    {formattedValue: 'titi', filteredOccurrences: 2},
                    {formattedValue: 'coucou', filteredOccurrences: 0},
                    {formattedValue: 'cici', filteredOccurrences: 0}
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                vertical: false,
                className: 'blueBar',
                column: barChartStrCol
            });
        }));

        it('should update filtered Patterns Frequency', inject(function($rootScope, StatisticsService, StateService){
            //given
            stateMock.playground.grid.selectedColumn.statistics.patternFrequencyTable = [
                {
                    'pattern': '',
                    'occurrences': 1
                }
            ];
            stateMock.playground.grid.filteredRecords = [{
                '0001': 'toto'
            }];

            //when
            StatisticsService.updateFilteredStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsFilteredPatterns).toHaveBeenCalledWith([{
                'pattern': '',
                'occurrences': 1,
                'filteredOccurrences': 1
            }]);
        }));

        it('should NOT update filtered data when there is an aggregation', inject(function(StorageService, StatisticsService, StateService){
            //given
            stateMock.playground.grid.selectedColumn = barChartStrCol;
            var savedAggregation = {
                aggregationColumnId: '0002',
                aggregation: 'MAX'
            };
            spyOn(StorageService, 'getAggregation').and.returnValue(savedAggregation);

            //when
            StatisticsService.updateFilteredStatistics();

            //then
            expect(StateService.setStatisticsFilteredHistogram).not.toHaveBeenCalled();
            expect(StateService.setStatisticsHistogramActiveLimits).not.toHaveBeenCalled();
            expect(StateService.setStatisticsFilteredPatterns).not.toHaveBeenCalled();
        }));
    });

    describe('utils', function () {
        beforeEach(inject(function (StatisticsRestService) {
            spyOn(StatisticsRestService, 'resetCache').and.returnValue();
        }));

        it('should reset all charts, statistics, cache', inject(function (StatisticsRestService, StatisticsService) {
            //given
            StatisticsService.boxPlot = {};
            StatisticsService.stateDistribution = {};
            StatisticsService.statistics = {};

            //when
            StatisticsService.reset(true, true, true);

            //then
            expect(StatisticsService.boxPlot).toBeFalsy();
            expect(StatisticsService.stateDistribution).toBeFalsy();
            expect(StatisticsService.statistics).toBeFalsy();
            expect(StatisticsRestService.resetCache).toHaveBeenCalled();
        }));

        it('should reset date filtered occurrence worker', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = barChartDateCol;
            stateMock.playground.filter.gridFilters = [];
            stateMock.playground.grid.filteredOccurences = null;
            StatisticsService.statistics = {};
            stateMock.playground.statistics = {
                histogram: {
                    data: []
                }
            };
            expect(StatisticsService.histogram).toBeFalsy();

            StatisticsService.updateStatistics(); //create the worker
            expect(workerWrapper.terminate).not.toHaveBeenCalled();

            //when
            StatisticsService.reset();

            //then
            expect(workerWrapper.terminate).toHaveBeenCalled();
        }));

        it('should reset date pattern filtered occurrence worker', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumn = {
                'id': '0001',
                'name': 'city',
                'type': 'date',
                'domain': 'date',
                statistics: {
                    patternFrequencyTable: [
                        {
                            'pattern': 'd-M-yyyy',
                            'occurrences': 1
                        },
                        {
                            'pattern': 'yyyy-M-d',
                            'occurrences': 2
                        }
                    ]
                }
            };
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                {'0001': '18-01-2015'},
                {'0001': '2016-01-01'}
            ];

            StatisticsService.updateStatistics(); //create the worker
            expect(workerWrapper.terminate).not.toHaveBeenCalled();

            //when
            StatisticsService.reset();

            //then
            expect(workerWrapper.terminate).toHaveBeenCalled();
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

        it('should get numeric columns (as aggregation columns) from lookup-datagrid service', inject(function (StatisticsService, DatagridService) {
            //given
            var selectedcolumn = {id: '0001'};
            stateMock.playground.grid.selectedColumn = selectedcolumn;

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


        it('should NOT get numeric columns when there is no data', inject(function (StatisticsService, DatagridService) {
            //given
            stateMock.playground.data = null;
            spyOn(DatagridService, 'getNumericColumns').and.returnValue();

            //when
            StatisticsService.getAggregationColumns();

            //then
            expect(DatagridService.getNumericColumns).not.toHaveBeenCalled();
        }));
    });

});
