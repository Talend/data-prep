/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Statistics service', () => {
    const barChartNumCol = {
        domain: 'barchartAndNumeric',
        type: 'numeric',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                items: [
                    {
                        occurrences: 5,
                        range: {
                            min: 0,
                            max: 10,
                        },
                    },
                    {
                        occurrences: 15,
                        range: {
                            min: 10,
                            max: 20,
                        },
                    },
                ],
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateCol = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'MONTH',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1420070400000, // 01/01/2015
                            max: 1422748800000, // 01/02/2015
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1422748800000, // 01/02/2015
                            max: 1425168000000, // 01/03/2015
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColCENTURY = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'CENTURY',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 946684800000, // 01/01/2000
                            max: 4102444800000, // 01/01/2100
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 4102444800000, // 01/01/2100
                            max: 7258118400000, // 01/01/2200
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColDECADE = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'DECADE',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 946684800000, // 01/01/2000
                            max: 1262304000000, // 01/01/2010
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1262304000000, // 01/01/2010
                            max: 1577836800000, // 01/01/2020
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColYEAR = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'YEAR',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1388534400000, // 01/01/2014
                            max: 1420070400000, // 01/01/2015
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1420070400000, // 01/01/2015
                            max: 1451606400000, // 01/01/2016
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColHAFLYEAR = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'HALF_YEAR',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1388534400000, // 01/01/2014
                            max: 1404172800000, // 01/07/2014
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1404172800000, // 01/07/2014
                            max: 1420070400000, // 01/01/2015
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColQUARTER = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'QUARTER',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1388534400000, // 01/01/2015
                            max: 1396310400000, // 01/04/2015
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1396310400000, // 01/04/2015
                            max: 1404172800000, // 01/07/2014
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColMONTH = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'MONTH',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1420070400000, // 01/01/2015
                            max: 1422748800000, // 01/02/2015
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1422748800000, // 01/02/2015
                            max: 1425168000000, // 01/03/2015
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColWEEK = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'WEEK',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1451865600000, // 04/01/2016
                            max: 1452470400000, // 11/01/2016
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1452470400000, // 11/01/2016
                            max: 1453075200000, // 18/01/2016
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColDAY = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: {
                pace: 'DAY',
                items: [
                    {
                        occurrences: 15,
                        range: {
                            min: 1451606400000, // 01/01/2016
                            max: 1451692800000, // 02/01/2016
                        },
                    },
                    {
                        occurrences: 5,
                        range: {
                            min: 1451692800000, // 02/01/2016
                            max: 1451779200000, // 03/01/2016
                        },
                    },
                ],
            },
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartDateColWithoutHistogram = {
        domain: 'barchartAndDate',
        type: 'date',
        id: '0000',
        statistics: {
            frequencyTable: [],
            histogram: null,
            patternFrequencyTable: [
                {
                    pattern: 'd/M/yyyy',
                    frequency: 15,
                },
                {
                    pattern: 'M/d/yyyy',
                    frequency: 5,
                },
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartStrCol = {
        id: '0010',
        domain: 'barchartAndString',
        type: 'string',
        statistics: {
            frequencyTable: [
                {
                    data: '   toto',
                    occurrences: 202,
                },
                {
                    data: 'titi',
                    occurrences: 2,
                },
                {
                    data: 'coucou',
                    occurrences: 102,
                },
                {
                    data: 'cici',
                    occurrences: 22,
                },
            ],
            patternFrequencyTable: [
                {
                    pattern: '   Aa9',
                    occurrences: 202,
                },
                {
                    pattern: 'yyyy-M-d',
                    occurrences: 2,
                },
            ],
            textLengthSummary: {
                averageLength: 10.13248646854654,
                minimalLength: 12,
                maximalLength: 14,
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartStrCol2 = {
        id: '0010',
        domain: 'barchartAndString',
        type: 'string',
        statistics: {
            frequencyTable: [
                {
                    data: '   toto',
                    occurrences: 1,
                },
                {
                    data: 'titi',
                    occurrences: 1,
                },
                {
                    data: 'coucou',
                    occurrences: 1,
                },
                {
                    data: 'cici',
                    occurrences: 1,
                },
            ],
            textLengthSummary: {
                averageLength: 10.13248646854654,
                minimalLength: 12,
                maximalLength: 14,
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
                lowerQuantile: 'NaN',
            },
        },
    };

    const barChartBoolCol = {
        domain: 'barchartAndBool',
        type: 'boolean',
        statistics: {
            frequencyTable: [
                {
                    data: 'true',
                    occurrences: 2,
                },
                {
                    data: 'false',
                    occurrences: 20,
                },
                {
                    data: '',
                    occurrences: 10,
                },
            ],
        },
    };

    const unknownTypeCol = {
        domain: '',
        type: 'unknown',
        statistics: {},
    };

    let stateMock;
    let originalJasmineTimeout;

    beforeEach(angular.mock.module('data-prep.services.statistics', ($provide) => {
        stateMock = {
            playground: {
                grid: {},
                filter: { gridFilters: [] },
                data: {},
                statistics: {},
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, StateService) => {
        originalJasmineTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 20000;

        spyOn(StateService, 'setStatisticsHistogramActiveLimits').and.returnValue();
        spyOn(StateService, 'setStatisticsPatterns').and.returnValue();
        spyOn(StateService, 'setStatisticsFilteredPatterns').and.returnValue();
        spyOn(StateService, 'setStatisticsHistogram').and.returnValue();
        spyOn(StateService, 'setStatisticsFilteredHistogram').and.returnValue();
        spyOn(StateService, 'setStatisticsBoxPlot').and.returnValue();
        spyOn(StateService, 'setStatisticsRangeLimits').and.returnValue();
        spyOn(StateService, 'setStatisticsLoading').and.returnValue();
        spyOn(StateService, 'setStatisticsDetails').and.callFake((details) => {
            stateMock.playground.statistics.details = details;
        });
    }));

    afterEach(() => {
        jasmine.DEFAULT_TIMEOUT_INTERVAL = originalJasmineTimeout;
    });

    describe('filters', () => {

        it('should create a function that reinit range limits when the selected column is the same', inject(function (StatisticsService, StateService) {
            //given
            const column = { id: '0000', type: 'integer', statistics: { min: 5, max: 55 } };
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.selectedColumns = [column];

            const removeCallback = StatisticsService.getRangeFilterRemoveFn();

            //when
            removeCallback({ colId: '0000' });

            //then
            expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                min: 5,
                max: 55,
                type: 'integer',
            });
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([column.statistics.min, column.statistics.max]);
        }));

        it('should create a function that do nothing when the selected column is NOT the same', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.selectedColumns = [{
                id: '0000',
                statistics: { min: 5, max: 55 }
            }];

            const removeCallback = StatisticsService.getRangeFilterRemoveFn();

            //when
            removeCallback({ colId: '0001' }); // not the same column as the filters one

            //then
            expect(StateService.setStatisticsRangeLimits).not.toHaveBeenCalled();
        }));
    });

    describe('init Statistics : The statistics values', () => {
        it('should init number statistics whithout quantile', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
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
                        lowerQuantile: 'NaN',
                    },
                },
            }];
            expect(StateService.setStatisticsDetails).not.toHaveBeenCalled();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsDetails).toHaveBeenCalledWith({
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8,
                },
                specific: {
                    MIN: 10,
                    MAX: 11,
                    MEAN: 12,
                    VARIANCE: 13,
                },
            });
        }));

        it('should init number statistics with quantile', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
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
                        upperQuantile: 16,
                    },
                },
            }];
            expect(StateService.setStatisticsDetails).not.toHaveBeenCalled();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsDetails).toHaveBeenCalledWith({
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8,
                },
                specific: {
                    MIN: 10,
                    MAX: 11,
                    MEAN: 12,
                    VARIANCE: 13,
                    MEDIAN: 14,
                    LOWER_QUANTILE: 15,
                    UPPER_QUANTILE: 16,
                },
            });
        }));

        it('should init text statistics', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
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
                        maximalLength: 14,
                    },
                },
            }];
            expect(StateService.setStatisticsDetails).not.toHaveBeenCalled();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsDetails).toHaveBeenCalledWith({
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8,
                },
                specific: {
                    AVG_LENGTH: 10.13,
                    MIN_LENGTH: 12,
                    MAX_LENGTH: 14,
                },
            });
        }));

        it('should init common statistics when the column type is not "number" or "text"', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                type: 'boolean',
                domain: 'US_STATE_CODE',
                statistics: {
                    count: 4,
                    distinctCount: 5,
                    duplicateCount: 6,
                    empty: 7,
                    invalid: 8,
                    valid: 9,
                },
            }];
            expect(StateService.setStatisticsDetails).not.toHaveBeenCalled();

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsDetails).toHaveBeenCalledWith({
                common: {
                    COUNT: 4,
                    DISTINCT_COUNT: 5,
                    DUPLICATE_COUNT: 6,
                    VALID: 9,
                    EMPTY: 7,
                    INVALID: 8,
                },
                specific: {},
            });
        }));
    });

    describe('Update Statistics : The statistics values', () => {
        beforeEach(inject(() => {
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'city',
                type: 'date',
                domain: 'date',
                statistics: {
                    patternFrequencyTable: [],
                },
            }];
            stateMock.playground.grid.filteredRecords = [{ '0001': '10-12-2015' }, { '0001': '2015-12-02' }, { '0001': 'To,/' }, { '0001': '2015-12-02' }, { '0001': '10/12-20' }];
        }));

        it('should update empty pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics.patternFrequencyTable = [
                {
                    pattern: '',
                    occurrences: 1,
                },
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [{
                '0001': 'toto',
            },];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsLoading).toHaveBeenCalledWith(true);
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    pattern: '',
                    occurrences: 1,
                    formattedPattern: '',
                },
            ]);
        }));

        it('should update date pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics.patternFrequencyTable = [
                {
                    pattern: 'd-M-yyyy',
                    occurrences: 1,
                },
                {
                    pattern: 'yyyy-M-d',
                    occurrences: 2,
                },
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                { '0001': '18-01-2015' },
                { '0001': '2016-01-01' },
            ];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsLoading).toHaveBeenCalledWith(true);
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    pattern: 'd-M-yyyy',
                    occurrences: 1,
                    formattedPattern: 'd-M-yyyy',
                },
                {
                    pattern: 'yyyy-M-d',
                    occurrences: 2,
                    formattedPattern: 'yyyy-M-d',
                },
            ]);
        }));

        it('should update non date pattern statistics', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics.patternFrequencyTable = [
                {
                    pattern: 'Aa,/',
                    occurrences: 3,
                },
                {
                    pattern: '99/99-99',
                    occurrences: 4,
                },
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                { '0001': 'Bg,/' },
                { '0001': '26/42-98' },
            ];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsLoading).toHaveBeenCalledWith(true);
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    pattern: 'Aa,/',
                    occurrences: 3,
                    formattedPattern: 'Aa,/',
                },
                {
                    pattern: '99/99-99',
                    occurrences: 4,
                    formattedPattern: '99/99-99',
                },
            ]);
        }));


        it('should format pattern according to grid constraints', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics.patternFrequencyTable = [
                {
                    pattern: ' aaaa ',
                    occurrences: 1,
                },
            ];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [{
                '0001': 'toto',
            },];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsLoading).toHaveBeenCalledWith(true);
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    pattern: ' aaaa ',
                    occurrences: 1,
                    formattedPattern: '<span class="hiddenChars"> </span>aaaa<span class="hiddenChars"> </span>',
                },
            ]);
        }));

        it('should update pattern statistics without filter', inject(function ($rootScope, StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics.patternFrequencyTable = [
                {
                    pattern: 'Aa,/',
                    occurrences: 3,
                },
                {
                    pattern: '99/99-99',
                    occurrences: 4,
                },
            ];
            stateMock.playground.filter.gridFilters = [];
            stateMock.playground.grid.filteredRecords = [];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StateService.setStatisticsLoading).toHaveBeenCalledWith(true);
            expect(StateService.setStatisticsPatterns).toHaveBeenCalledWith([
                {
                    pattern: 'Aa,/',
                    occurrences: 3,
                    formattedPattern: 'Aa,/',
                },
                {
                    pattern: '99/99-99',
                    occurrences: 4,
                    formattedPattern: '99/99-99',
                },
            ]);
        }));
    });

    describe('Update Statistics : Statistics routing (basic / aggregations)', () => {
        const currentColumn = { // the selected column
            id: '0001',
            name: 'city',
            domain: '',
            type: '',
            statistics: { patternFrequencyTable: [] },
        };
        const datasetId = '13654634856752';                   // the current data id
        const preparationId = '2132548345365';                // the current preparation id
        const stepId = '9878645468';                          // the currently viewed step id
        const column = { id: '0002', name: 'state' };       // the column where to perform the aggregation
        const aggregation = 'MAX';                            // the aggregation operation

        const getAggregationsResponse = [                     // the REST aggregation GET result
            { data: 'Lansing', MAX: 15 },
            { data: 'Helena', MAX: 5 },
            { data: 'Baton Rouge', MAX: 64 },
            { data: 'Annapolis', MAX: 4 },
            { data: 'Pierre', MAX: 104 },
        ];

        beforeEach(inject(function (StatisticsService, StepUtilsService, StorageService) {
            stateMock.playground.grid.selectedColumns = [currentColumn];
            stateMock.playground.dataset = { id: datasetId };
            stateMock.playground.preparation = { id: preparationId };
            spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue({ transformation: { stepId: stepId } });
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();
        }));

        it('should update histogram data with classical occurrence when there is no saved aggregation on the current preparation/dataset/column with filter', inject(function ($rootScope, StatisticsService, StorageService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };
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
                        formattedValue: '<span class="hiddenChars">   </span>toto',
                    },
                    { data: 'titi', occurrences: 2, formattedValue: 'titi' },
                    { data: 'coucou', occurrences: 102, formattedValue: 'coucou' },
                    { data: 'cici', occurrences: 22, formattedValue: 'cici' },
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                vertical: false,
                className: null,
                column: barChartStrCol,
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        formattedValue: '<span class="hiddenChars">   </span>toto',
                        filteredOccurrences: 3
                    },
                    { formattedValue: 'titi', filteredOccurrences: 2 },
                    { formattedValue: 'coucou', filteredOccurrences: 0 },
                    { formattedValue: 'cici', filteredOccurrences: 0 },
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                vertical: false,
                className: 'blueBar',
                column: barChartStrCol,
            });
        }));

        it('should update histogram data with classical occurrence when saved aggregation column does NOT exist anymore', inject(function ($rootScope, StatisticsService, StorageService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };
            stateMock.playground.grid.numericColumns = [
                { id: '0002', name: 'state' },
                { id: '0003', name: 'name' },
            ];
            spyOn(StorageService, 'getAggregation').and.returnValue({
                aggregationColumnId: '9999', // do NOT exist
                aggregation: 'MAX',
            });

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
                        formattedValue: '<span class="hiddenChars">   </span>toto',
                    },
                    { data: 'titi', occurrences: 2, formattedValue: 'titi' },
                    { data: 'coucou', occurrences: 102, formattedValue: 'coucou' },
                    { data: 'cici', occurrences: 22, formattedValue: 'cici' },
                ],
                keyField: 'formattedValue',
                valueField: 'occurrences',
                label: 'Occurrences',
                vertical: false,
                className: null,
                column: barChartStrCol,
            });
            expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        formattedValue: '<span class="hiddenChars">   </span>toto',
                        filteredOccurrences: 3
                    },
                    { formattedValue: 'titi', filteredOccurrences: 2 },
                    { formattedValue: 'coucou', filteredOccurrences: 0 },
                    { formattedValue: 'cici', filteredOccurrences: 0 },
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                vertical: false,
                className: 'blueBar',
                column: barChartStrCol,
            });
        }));

        it('should update histogram data from saved aggregation configuration', inject(function ($q, $rootScope, StatisticsService, StatisticsRestService, StorageService, StateService) {
            //given
            spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.when(getAggregationsResponse));
            const savedAggregation = {
                aggregationColumnId: '0002',
                aggregation: 'MAX',
            };
            spyOn(StorageService, 'getAggregation').and.returnValue(savedAggregation);
            stateMock.playground.grid.numericColumns = [
                { id: '0002', name: 'state' },
                { id: '0003', name: 'name' },
            ];

            //when
            StatisticsService.updateStatistics();
            $rootScope.$digest();

            //then
            expect(StatisticsRestService.getAggregations).toHaveBeenCalledWith({
                datasetId: null,
                preparationId: '2132548345365',
                stepId: '9878645468',
                operations: [{ operator: 'MAX', columnId: '0002' }],
                groupBy: ['0001'],
            });
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    { data: 'Lansing', MAX: 15, formattedValue: 'Lansing' },
                    { data: 'Helena', MAX: 5, formattedValue: 'Helena' },
                    { data: 'Baton Rouge', MAX: 64, formattedValue: 'Baton Rouge' },
                    { data: 'Annapolis', MAX: 4, formattedValue: 'Annapolis' },
                    { data: 'Pierre', MAX: 104, formattedValue: 'Pierre' },
                ],
                keyField: 'formattedValue',
                label: 'MAX',
                valueField: 'MAX',
                vertical: false,
                className: 'blueBar',
                column: stateMock.playground.grid.selectedColumns[0],
                aggregationColumn: column,
                aggregation: aggregation,
            });
        }));

        it('should reset non histogram charts', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [barChartNumCol];

            //when
            StatisticsService.updateStatistics();

            //then
            expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
        }));
    });

    describe('Process Aggregations', () => {
        const currentColumn = { id: '0001', name: 'city' }; // the selected column
        const datasetId = '13654634856752';                   // the current data id
        const preparationId = '2132548345365';                // the current preparation id
        const stepId = '9878645468';                          // the currently viewed step id
        const column = { id: '0002', name: 'state' };       // the column where to perform the aggregation
        const aggregation = 'MAX';                            // the aggregation operation

        const getAggregationsResponse = [                     // the REST aggregation GET result
            { data: 'Lansing', MAX: 15 },
            { data: 'Helena', MAX: 5 },
            { data: 'Baton Rouge', MAX: 64 },
            { data: 'Annapolis', MAX: 4 },
            { data: 'Pierre', MAX: 104 },
        ];

        beforeEach(inject(function (StatisticsService, StepUtilsService, StorageService) {
            stateMock.playground.grid.selectedColumns = [currentColumn];
            stateMock.playground.dataset = { id: datasetId };
            stateMock.playground.preparation = { id: preparationId };
            spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue({ transformation: { stepId: stepId } });
            spyOn(StorageService, 'setAggregation').and.returnValue();
        }));

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
                operations: [{ operator: 'MAX', columnId: '0002' }],
                groupBy: ['0001'],
            });
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    { data: 'Lansing', MAX: 15, formattedValue: 'Lansing' },
                    { data: 'Helena', MAX: 5, formattedValue: 'Helena' },
                    { data: 'Baton Rouge', MAX: 64, formattedValue: 'Baton Rouge' },
                    { data: 'Annapolis', MAX: 4, formattedValue: 'Annapolis' },
                    { data: 'Pierre', MAX: 104, formattedValue: 'Pierre' },
                ],
                keyField: 'formattedValue',
                valueField: 'MAX',
                label: 'MAX',
                vertical: false,
                className: 'blueBar',
                column: stateMock.playground.grid.selectedColumns[0],
                aggregationColumn: column,
                aggregation: aggregation,
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
                operations: [{ operator: 'MAX', columnId: '0002' }],
                groupBy: ['0001'],
            });
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                data: [
                    { data: 'Lansing', MAX: 15, formattedValue: 'Lansing' },
                    { data: 'Helena', MAX: 5, formattedValue: 'Helena' },
                    { data: 'Baton Rouge', MAX: 64, formattedValue: 'Baton Rouge' },
                    { data: 'Annapolis', MAX: 4, formattedValue: 'Annapolis' },
                    { data: 'Pierre', MAX: 104, formattedValue: 'Pierre' },
                ],
                keyField: 'formattedValue',
                valueField: 'MAX',
                label: 'MAX',
                column: stateMock.playground.grid.selectedColumns[0],
                vertical: false,
                className: 'blueBar',
                aggregationColumn: column,
                aggregation: aggregation,
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
                aggregationColumnId: '0002',
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

    describe('Process classic charts', () => {
        describe('The range slider', () => {
            beforeEach(() => {
                stateMock.playground.grid.selectedColumns = [{
                    id: '0001',
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
                            lowerQuantile: 'NaN',
                        },
                        histogram: { items: [] },
                    },
                }];
                stateMock.playground.statistics.histogram = {};
                stateMock.playground.statistics.details = {
                    common: {
                        COUNT: 4,
                        DISTINCT_COUNT: 5,
                        DUPLICATE_COUNT: 6,
                        VALID: 9,
                        EMPTY: 7,
                        INVALID: 8,
                    },
                    specific: {
                        MIN: 0,
                        MAX: 11,
                        MEAN: 12,
                        VARIANCE: 13,
                    },
                };
            });

            it('should set range and brush limits to the min and max of the column', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.filter.gridFilters = [];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null); // due to the reset
                expect(StateService.setStatisticsHistogramActiveLimits.calls.count()).toBe(1); // not called with value
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    type: 'integer',
                });
            }));

            it('should update the brush limits to the existing range filter values', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [5, 10],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 5,
                    maxBrush: 10,
                    minFilterVal: 5,
                    maxFilterVal: 10,
                    type: 'integer',
                });
                expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([5, 10]);
            }));

            it('should update the brush limits to the minimum', inject(function (StatisticsService, StateService) {
                //given : -5 < 0(minimum)
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [-15, -10],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 0,
                    maxBrush: 0,
                    minFilterVal: -15,
                    maxFilterVal: -10,
                    type: 'integer',
                });
            }));

            it('should update the brush limits to the [minimum, maximum] ', inject(function (StatisticsService, StateService) {
                //given : -5 < 0(minimum)
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [-15, 20],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 0,
                    maxBrush: 11,
                    minFilterVal: -15,
                    maxFilterVal: 20,
                    type: 'integer',
                });
            }));

            it('should update the brush limits to the maximum', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [25, 30],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 11,
                    maxBrush: 11,
                    minFilterVal: 25,
                    maxFilterVal: 30,
                    type: 'integer',
                });
            }));

            it('should update the brush limits to [minBrush, maximum]', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [5, 30],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 5,
                    maxBrush: 11,
                    minFilterVal: 5,
                    maxFilterVal: 30,
                    type: 'integer',
                });
            }));

            it('should update the brush limits to [minimum, maxBrush]', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.filter.gridFilters = [{
                    colId: '0001',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [-25, 10],
                            },
                        ],
                    },
                },];

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsRangeLimits).toHaveBeenCalledWith({
                    min: 0,
                    max: 11,
                    minBrush: 0,
                    maxBrush: 10,
                    minFilterVal: -25,
                    maxFilterVal: 10,
                    type: 'integer',
                });
            }));
        });

        describe('The boxplot data', () => {
            it('should reset boxplotData when quantile values are NaN', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [{
                    id: '0001',
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
                            lowerQuantile: 'NaN',
                        },
                    },
                }];
                stateMock.playground.statistics.details = {
                    common: {
                        COUNT: 4,
                        DISTINCT_COUNT: 5,
                        DUPLICATE_COUNT: 6,
                        VALID: 9,
                        EMPTY: 7,
                        INVALID: 8,
                    },
                    specific: {
                        MIN: 10,
                        MAX: 11,
                        MEAN: 12,
                        VARIANCE: 13,
                    },
                };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
            }));

            it('should set boxplotData statistics with quantile', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [{
                    id: '0001',
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
                            upperQuantile: 16,
                        },
                    },
                }];
                stateMock.playground.statistics.details = {
                    common: {
                        COUNT: 4,
                        DISTINCT_COUNT: 5,
                        DUPLICATE_COUNT: 6,
                        VALID: 9,
                        EMPTY: 7,
                        INVALID: 8,
                    },
                    specific: {
                        MIN: 10,
                        MAX: 11,
                        MEAN: 12,
                        VARIANCE: 13,
                        MEDIAN: 14,
                        LOWER_QUANTILE: 15,
                        UPPER_QUANTILE: 16,
                    },
                };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith({
                    min: 10,
                    max: 11,
                    q1: 15,
                    q2: 16,
                    median: 14,
                    mean: 12,
                    variance: 13,
                });
            }));
        });

        describe('process horizontal chart', () => {
            it('should reset non histogram data when column type is "string"', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [barChartStrCol];
                stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
            }));

            it('should set the frequency data with formatted value when column type is "string" with filter', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [barChartStrCol];
                stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: '   toto',
                            occurrences: 202,
                            formattedValue: '<span class="hiddenChars">   </span>toto',
                        },
                        { data: 'titi', occurrences: 2, formattedValue: 'titi' },
                        { data: 'coucou', occurrences: 102, formattedValue: 'coucou' },
                        { data: 'cici', occurrences: 22, formattedValue: 'cici' },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartStrCol,
                    vertical: false,
                    className: null,
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            formattedValue: '<span class="hiddenChars">   </span>toto',
                            filteredOccurrences: 3
                        },
                        { formattedValue: 'titi', filteredOccurrences: 2 },
                        { formattedValue: 'coucou', filteredOccurrences: 0 },
                        { formattedValue: 'cici', filteredOccurrences: 0 },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'filteredOccurrences',
                    label: null,
                    column: barChartStrCol,
                    vertical: false,
                    className: 'blueBar',
                });
            }));

            it('should set the frequency data with formatted value when column type is "string" without filter', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [barChartStrCol2];
                stateMock.playground.grid.filteredOccurences = {
                    '   toto': 1,
                    coucou: 1,
                    cici: 1,
                    titi: 1
                };
                expect(StatisticsService.histogram).toBeFalsy();

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            data: '   toto',
                            occurrences: 1,
                            formattedValue: '<span class="hiddenChars">   </span>toto',
                        },
                        { data: 'titi', occurrences: 1, formattedValue: 'titi' },
                        { data: 'coucou', occurrences: 1, formattedValue: 'coucou' },
                        { data: 'cici', occurrences: 1, formattedValue: 'cici' },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartStrCol2,
                    vertical: false,
                    className: null,
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        {
                            formattedValue: '<span class="hiddenChars">   </span>toto',
                            filteredOccurrences: 1
                        },
                        { formattedValue: 'titi', filteredOccurrences: 1 },
                        { formattedValue: 'coucou', filteredOccurrences: 1 },
                        { formattedValue: 'cici', filteredOccurrences: 1 },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'filteredOccurrences',
                    label: null,
                    column: barChartStrCol2,
                    vertical: false,
                    className: 'blueBar',
                });
            }));

            it('should reset non histogram data when column type is "boolean"', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [barChartBoolCol];
                stateMock.playground.grid.filteredOccurences = { true: 3, false: 2 };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
            }));

            it('should set the frequency data when column type is "boolean"', inject(function (StatisticsService, StateService) {
                //given
                stateMock.playground.grid.selectedColumns = [barChartBoolCol];
                stateMock.playground.grid.filteredOccurences = { true: 3, false: 2 };

                //when
                StatisticsService.processClassicChart();

                //then
                expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                    data: [
                        { formattedValue: 'true', occurrences: 2, data: 'true' },
                        { formattedValue: 'false', occurrences: 20, data: 'false' },
                        { formattedValue: '', occurrences: 10, data: '' },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'occurrences',
                    label: 'Occurrences',
                    column: barChartBoolCol,
                    vertical: false,
                    className: null,
                });
                expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                    data: [
                        { formattedValue: 'true', filteredOccurrences: 3 },
                        { formattedValue: 'false', filteredOccurrences: 2 },
                        { formattedValue: '', filteredOccurrences: 0 },
                    ],
                    keyField: 'formattedValue',
                    valueField: 'filteredOccurrences',
                    label: null,
                    column: barChartBoolCol,
                    vertical: false,
                    className: 'blueBar',
                });
            }));
        });

        describe('process vertical chart', () => {
            describe('number', () => {
                it('should set the range data frequency when column type is "number" with filters', inject(function ($rootScope, StatisticsService, StateService) {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartNumCol];
                    stateMock.playground.filter.gridFilters = [{}];
                    stateMock.playground.grid.filteredOccurences = { 1: 2, 3: 1, 11: 6 };
                    stateMock.playground.statistics.details = {
                        common: {
                            COUNT: 4,
                            DISTINCT_COUNT: 5,
                            DUPLICATE_COUNT: 6,
                            VALID: 9,
                            EMPTY: 7,
                            INVALID: 8,
                        }, specific: { MIN: 10, MAX: 11, MEAN: 12, VARIANCE: 13 },
                    };

                    //when
                    StatisticsService.processClassicChart();
                    $rootScope.$digest();

                    //then
                    expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                        data: [
                            { data: { type: 'number', min: 0, max: 10 }, occurrences: 5 },
                            { data: { type: 'number', min: 10, max: 20 }, occurrences: 15 },
                        ],
                        keyField: 'data',
                        valueField: 'occurrences',
                        label: 'Occurrences',
                        column: barChartNumCol,
                        vertical: true,
                    });
                    expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                        data: [
                            { data: { type: 'number', min: 0, max: 10 }, filteredOccurrences: 3 },
                            { data: { type: 'number', min: 10, max: 20 }, filteredOccurrences: 6 },
                        ],
                        keyField: 'data',
                        valueField: 'filteredOccurrences',
                        label: 'Filtered Occurrences',
                        column: barChartNumCol,
                        vertical: true,
                    });
                }));

                it('should set the range data frequency without filters', inject(function ($rootScope, StatisticsService, StateService) {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartNumCol];
                    stateMock.playground.filter.gridFilters = [];
                    stateMock.playground.grid.filteredOccurences = null;
                    stateMock.playground.statistics.details = {
                        common: {
                            COUNT: 4,
                            DISTINCT_COUNT: 5,
                            DUPLICATE_COUNT: 6,
                            VALID: 9,
                            EMPTY: 7,
                            INVALID: 8,
                        }, specific: { MIN: 10, MAX: 11, MEAN: 12, VARIANCE: 13 },
                    };

                    //when
                    StatisticsService.processClassicChart();
                    $rootScope.$digest();

                    //then
                    expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                        data: [
                            { data: { type: 'number', min: 0, max: 10 }, occurrences: 5 },
                            { data: { type: 'number', min: 10, max: 20 }, occurrences: 15 },
                        ],
                        keyField: 'data',
                        valueField: 'occurrences',
                        label: 'Occurrences',
                        column: barChartNumCol,
                        vertical: true,
                    });
                    expect(StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                        data: [
                            { data: { type: 'number', min: 0, max: 10 }, filteredOccurrences: 5 },
                            { data: { type: 'number', min: 10, max: 20 }, filteredOccurrences: 15 },
                        ],
                        keyField: 'data',
                        valueField: 'filteredOccurrences',
                        label: 'Filtered Occurrences',
                        column: barChartNumCol,
                        vertical: true,
                    });
                }));
            });

            describe('date', () => {
                let _$rootScope;
                let _StatisticsService;
                let _StateService;
                beforeEach(inject(($rootScope, StatisticsService, StateService) => {
                    _$rootScope = $rootScope;
                    _StatisticsService = StatisticsService;
                    _StateService = StateService;
                }));

                it('should NOT set the range histogram when there is no histogram', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColWithoutHistogram];
                    stateMock.playground.statistics = {};

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    expect(_StateService.setStatisticsHistogram).toHaveBeenCalledWith(null);
                    expect(_StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null);
                });

                it('should set the range data frequency when column type is "date" with filters', (done) => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateCol];
                    stateMock.playground.filter.gridFilters = [{}];
                    stateMock.playground.grid.filteredOccurences = {
                        '05/01/2015': 6,
                        '12/01/2015': 4,
                        aze: 2,
                        '02/25/2015': 3,
                    };
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Jan 2015',
                                        min: new Date(2015, 0, 1).getTime(),
                                        max: new Date(2015, 1, 1).getTime(),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Feb 2015',
                                        min: new Date(2015, 1, 1).getTime(),
                                        max: new Date(2015, 2, 1).getTime(),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart()
                        .then(() => {
                            //then
                            expect(_StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                                data: [
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Jan 2015',
                                            min: new Date(2015, 0, 1).getTime(),
                                            max: new Date(2015, 1, 1).getTime(),
                                        },
                                        occurrences: 15,
                                    },
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Feb 2015',
                                            min: new Date(2015, 1, 1).getTime(),
                                            max: new Date(2015, 2, 1).getTime(),
                                        },
                                        occurrences: 5,
                                    },],
                                keyField: 'data',
                                valueField: 'occurrences',
                                label: 'Occurrences',
                                column: barChartDateCol,
                                vertical: true,
                            });
                            expect(_StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                                data: [
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Jan 2015',
                                            min: new Date(2015, 0, 1).getTime(),
                                            max: new Date(2015, 1, 1).getTime(),
                                        },
                                        occurrences: 15,
                                        filteredOccurrences: 10,
                                    },
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Feb 2015',
                                            min: new Date(2015, 1, 1).getTime(),
                                            max: new Date(2015, 2, 1).getTime(),
                                        },
                                        occurrences: 5,
                                        filteredOccurrences: 3,
                                    },],
                                keyField: 'data',
                                valueField: 'filteredOccurrences',
                                label: 'Filtered Occurrences',
                                column: barChartDateCol,
                                vertical: true,
                            });
                            done();
                        });
                    setTimeout(() => _$rootScope.$digest(), 15000);
                });

                it('should set the range data frequency with no filters', (done) => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateCol];
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
                                        max: new Date(2015, 1, 1).getTime(),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Feb 2015',
                                        min: new Date(2015, 1, 1).getTime(),
                                        max: new Date(2015, 2, 1).getTime(),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart()
                        .then(() => {
                            //then
                            expect(_StateService.setStatisticsHistogram).toHaveBeenCalledWith({
                                data: [
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Jan 2015',
                                            min: new Date(2015, 0, 1).getTime(),
                                            max: new Date(2015, 1, 1).getTime(),
                                        },
                                        occurrences: 15,
                                    },
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Feb 2015',
                                            min: new Date(2015, 1, 1).getTime(),
                                            max: new Date(2015, 2, 1).getTime(),
                                        },
                                        occurrences: 5,
                                    },],
                                keyField: 'data',
                                valueField: 'occurrences',
                                label: 'Occurrences',
                                column: barChartDateCol,
                                vertical: true,
                            });
                            expect(_StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                                data: [
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Jan 2015',
                                            min: new Date(2015, 0, 1).getTime(),
                                            max: new Date(2015, 1, 1).getTime(),
                                        },
                                        occurrences: 15,
                                        filteredOccurrences: 15,
                                    },
                                    {
                                        data: {
                                            type: 'date',
                                            label: 'Feb 2015',
                                            min: new Date(2015, 1, 1).getTime(),
                                            max: new Date(2015, 2, 1).getTime(),
                                        },
                                        occurrences: 5,
                                        filteredOccurrences: 5,
                                    },],
                                keyField: 'data',
                                valueField: 'filteredOccurrences',
                                label: 'Filtered Occurrences',
                                column: barChartDateCol,
                                vertical: true,
                            });
                            done();
                        });
                    setTimeout(() => _$rootScope.$digest(), 15000);
                });

                it('should adapt date range label to century', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColCENTURY];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: '[2000, 2100[',
                                        min: new Date(2000, 0, 1),
                                        max: new Date(2100, 0, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: '[2100, 2200[',
                                        min: new Date(2100, 0, 1),
                                        max: new Date(2200, 0, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: '[2000, 2100[',
                        min: new Date(2000, 0, 1).getTime(),
                        max: new Date(2100, 0, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: '[2100, 2200[',
                        min: new Date(2100, 0, 1).getTime(),
                        max: new Date(2200, 0, 1).getTime(),
                    });
                });

                it('should adapt date range label to decade', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColDECADE];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: '[2000, 2010[',
                                        min: new Date(2000, 0, 1),
                                        max: new Date(2010, 0, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: '[2010, 2020[',
                                        min: new Date(2010, 0, 1),
                                        max: new Date(2020, 0, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: '[2000, 2010[',
                        min: new Date(2000, 0, 1).getTime(),
                        max: new Date(2010, 0, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: '[2010, 2020[',
                        min: new Date(2010, 0, 1).getTime(),
                        max: new Date(2020, 0, 1).getTime(),
                    });
                });

                it('should adapt date range label to year', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColYEAR];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: '2014',
                                        min: new Date(2014, 0, 1),
                                        max: new Date(2015, 0, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: '2015',
                                        min: new Date(2015, 0, 1),
                                        max: new Date(2016, 0, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: '2014',
                        min: new Date(2014, 0, 1).getTime(),
                        max: new Date(2015, 0, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: '2015',
                        min: new Date(2015, 0, 1).getTime(),
                        max: new Date(2016, 0, 1).getTime(),
                    });
                });

                it('should adapt date range label to half year', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColHAFLYEAR];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'H1 2014',
                                        min: new Date(2014, 0, 1),
                                        max: new Date(2014, 6, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'H2 2014',
                                        min: new Date(2014, 6, 1),
                                        max: new Date(2015, 0, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: 'H1 2014',
                        min: new Date(2014, 0, 1).getTime(),
                        max: new Date(2014, 6, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: 'H2 2014',
                        min: new Date(2014, 6, 1).getTime(),
                        max: new Date(2015, 0, 1).getTime(),
                    });
                });

                it('should adapt date range label to quarter', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColQUARTER];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Q1 2014',
                                        min: new Date(2014, 0, 1),
                                        max: new Date(2014, 3, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Q2 2014',
                                        min: new Date(2014, 3, 1),
                                        max: new Date(2014, 6, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: 'Q1 2014',
                        min: new Date(2014, 0, 1).getTime(),
                        max: new Date(2014, 3, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: 'Q2 2014',
                        min: new Date(2014, 3, 1).getTime(),
                        max: new Date(2014, 6, 1).getTime(),
                    });
                });

                it('should adapt date range label to month', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColMONTH];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Jan 2015',
                                        min: new Date(2015, 0, 1),
                                        max: new Date(2015, 1, 1),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Feb 2015',
                                        min: new Date(2015, 1, 1),
                                        max: new Date(2015, 2, 1),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: 'Jan 2015',
                        min: new Date(2015, 0, 1).getTime(),
                        max: new Date(2015, 1, 1).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: 'Feb 2015',
                        min: new Date(2015, 1, 1).getTime(),
                        max: new Date(2015, 2, 1).getTime(),
                    });
                });

                it('should adapt date range label to week', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColWEEK];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'W01 2016',
                                        min: new Date(2016, 0, 4),
                                        max: new Date(2016, 0, 11),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'W02 2016',
                                        min: new Date(2016, 0, 11),
                                        max: new Date(2016, 0, 18),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: 'W01 2016',
                        min: new Date(2016, 0, 4).getTime(),
                        max: new Date(2016, 0, 11).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: 'W02 2016',
                        min: new Date(2016, 0, 11).getTime(),
                        max: new Date(2016, 0, 18).getTime(),
                    });
                });

                it('should adapt date range label to day', () => {
                    //given
                    stateMock.playground.grid.selectedColumns = [barChartDateColDAY];
                    stateMock.playground.statistics = {
                        histogram: {
                            data: [
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Jan 1, 2016',
                                        min: new Date(2016, 0, 1),
                                        max: new Date(2016, 0, 2),
                                    },
                                    occurrences: 15,
                                    filteredOccurrences: 10,
                                },
                                {
                                    data: {
                                        type: 'date',
                                        label: 'Jan 2, 2016',
                                        min: new Date(2016, 0, 2),
                                        max: new Date(2016, 0, 3),
                                    },
                                    occurrences: 5,
                                    filteredOccurrences: 3,
                                },
                            ],
                        },
                    };

                    //when
                    _StatisticsService.processClassicChart();

                    //then
                    const histogram = _StateService.setStatisticsHistogram.calls.argsFor(1)[0]; // first call is the reset
                    expect(histogram.data[0].data).toEqual({
                        type: 'date',
                        label: 'Jan 1, 2016',
                        min: new Date(2016, 0, 1).getTime(),
                        max: new Date(2016, 0, 2).getTime(),
                    });
                    expect(histogram.data[1].data).toEqual({
                        type: 'date',
                        label: 'Jan 2, 2016',
                        min: new Date(2016, 0, 2).getTime(),
                        max: new Date(2016, 0, 3).getTime(),
                    });
                });
            });
        });

        it('should reset charts data when column type is not supported', inject(function (StatisticsService, StateService) {
            //given
            stateMock.playground.grid.selectedColumns = [unknownTypeCol];

            //when
            StatisticsService.processClassicChart();

            //then
            expect(StateService.setStatisticsHistogram).toHaveBeenCalledWith(null);
            expect(StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith(null);
            expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
        }));

        it('should remove saved aggregation on current column/preparation/dataset from storage', inject(function (StatisticsService, StorageService) {
            //given
            const datasetId = '13654634856752';
            const preparationId = '2132548345365';
            stateMock.playground.dataset = { id: datasetId };
            stateMock.playground.preparation = { id: preparationId };
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };
            spyOn(StorageService, 'removeAggregation').and.returnValue();
            expect(StorageService.removeAggregation).not.toHaveBeenCalled();

            //when
            StatisticsService.processClassicChart();

            //then
            expect(StorageService.removeAggregation).toHaveBeenCalledWith(datasetId, preparationId, barChartStrCol.id);
        }));
    });

    describe('Update Filtered Statistics', () => {
        const currentColumn = { // the selected column
            id: '0001',
            name: 'city',
            domain: '',
            type: '',
            statistics: { patternFrequencyTable: [] },
        };
        const datasetId = '13654634856752';                   // the current data id
        const preparationId = '2132548345365';                // the current preparation id
        const stepId = '9878645468';                          // the currently viewed step id

        let _$rootScope;
        let _StatisticsService;
        let _StepUtilsService;
        let _StorageService;
        let _StateService;

        beforeEach(inject(($rootScope, StatisticsService, StepUtilsService, StorageService, StateService) => {
            stateMock.playground.grid.selectedColumns = [currentColumn];
            stateMock.playground.dataset = { id: datasetId };
            stateMock.playground.preparation = { id: preparationId };
            spyOn(StepUtilsService, 'getLastActiveStep').and.returnValue({ transformation: { stepId: stepId } });
            spyOn(StorageService, 'setAggregation').and.returnValue();
            spyOn(StorageService, 'removeAggregation').and.returnValue();

            _$rootScope = $rootScope;
            _StatisticsService = StatisticsService;
            _StepUtilsService = StepUtilsService;
            _StorageService = StorageService;
            _StateService = StateService;
        }));

        it('should do nothing when there is no statistics yet', () => {
            //given
            stateMock.playground.grid.selectedColumns = [{
                statistics: {
                    frequencyTable: [],
                    histogram: null,
                }
            }];
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.filteredOccurences = {
                1: 2,
                3: 1,
                11: 6,
            };
            spyOn(_StorageService, 'getAggregation').and.returnValue();

            //when
            _StatisticsService.updateFilteredStatistics();
            _$rootScope.$digest();

            //then
            expect(_StateService.setStatisticsFilteredHistogram).not.toHaveBeenCalled();
        });

        it('should update filtered Numeric column', () => {
            //given
            stateMock.playground.grid.selectedColumns = [barChartNumCol];
            stateMock.playground.filter.gridFilters = [{
                colId: '0000',
                type: 'inside_range',
                args: {
                    intervals: [
                        {
                            value: [5, 10],
                        },
                    ],
                },
            },];
            stateMock.playground.statistics.histogram = {};
            stateMock.playground.grid.filteredOccurences = {
                1: 2,
                3: 1,
                11: 6,
            };
            spyOn(_StorageService, 'getAggregation').and.returnValue();

            //when
            _StatisticsService.updateFilteredStatistics();
            _$rootScope.$digest();

            //then
            expect(_StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        data: {
                            type: 'number',
                            min: 0,
                            max: 10,
                        },
                        filteredOccurrences: 3,
                    },
                    {
                        data: {
                            type: 'number',
                            min: 10,
                            max: 20,
                        },
                        filteredOccurrences: 6,
                    },
                ],
                keyField: 'data',
                valueField: 'filteredOccurrences',
                label: 'Filtered Occurrences',
                column: barChartNumCol,
                vertical: true,
            });
            expect(_StateService.setStatisticsHistogramActiveLimits).toHaveBeenCalledWith([5, 10]);
        });

        it('should update filtered Date column', (done) => {
            //given
            spyOn(_StorageService, 'getAggregation').and.returnValue();
            stateMock.playground.grid.selectedColumns = [barChartDateCol];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredOccurences = {
                '05/01/2015': 6,
                '12/01/2015': 4,
                aze: 2,
                '02/25/2015': 3,
            };
            stateMock.playground.statistics = {
                histogram: {
                    data: [
                        {
                            data: {
                                type: 'date',
                                label: 'Jan 2015',
                                min: new Date(2015, 0, 1),
                                max: new Date(2015, 1, 1),
                            },
                            occurrences: 15,
                            filteredOccurrences: 10,
                        },
                        {
                            data: {
                                type: 'date',
                                label: 'Feb 2015',
                                min: new Date(2015, 1, 1),
                                max: new Date(2015, 2, 1),
                            },
                            occurrences: 5,
                            filteredOccurrences: 3,
                        },
                    ],
                },
            };

            //when
            _StatisticsService.updateFilteredStatistics()
                .then(() => {
                    //then
                    expect(_StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                        data: [
                            {
                                data: {
                                    type: 'date',
                                    label: 'Jan 2015',
                                    min: new Date(2015, 0, 1),
                                    max: new Date(2015, 1, 1),
                                },
                                occurrences: 15,
                                filteredOccurrences: 10,
                            },
                            {
                                data: {
                                    type: 'date',
                                    label: 'Feb 2015',
                                    min: new Date(2015, 1, 1),
                                    max: new Date(2015, 2, 1),
                                },
                                occurrences: 5,
                                filteredOccurrences: 3,
                            },],
                        keyField: 'data',
                        valueField: 'filteredOccurrences',
                        label: 'Filtered Occurrences',
                        column: barChartDateCol,
                        vertical: true,
                    });
                    done();
                });
            setTimeout(() => _$rootScope.$apply(), 15000);
        });

        it('should update filtered Text column', () => {
            //given
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            spyOn(_StorageService, 'getAggregation').and.returnValue();
            stateMock.playground.grid.filteredOccurences = { '   toto': 3, titi: 2 };

            //when
            _StatisticsService.updateFilteredStatistics();

            //then
            expect(_StateService.setStatisticsFilteredHistogram).toHaveBeenCalledWith({
                data: [
                    {
                        formattedValue: '<span class="hiddenChars">   </span>toto',
                        filteredOccurrences: 3
                    },
                    { formattedValue: 'titi', filteredOccurrences: 2 },
                    { formattedValue: 'coucou', filteredOccurrences: 0 },
                    { formattedValue: 'cici', filteredOccurrences: 0 },
                ],
                keyField: 'formattedValue',
                valueField: 'filteredOccurrences',
                label: null,
                vertical: false,
                className: 'blueBar',
                column: barChartStrCol,
            });
        });

        it('should update filtered Patterns Frequency', (done) => {
            //given
            stateMock.playground.grid.selectedColumns[0].statistics = {
                patternFrequencyTable: [{ pattern: '', occurrences: 1 }],
                frequencyTable: [{}],
            };
            stateMock.playground.grid.filteredRecords = [{ '0001': 'toto' }];

            //when
            _StatisticsService.updateFilteredStatistics()
                .then(() => {
                    //then
                    expect(_StateService.setStatisticsFilteredPatterns).toHaveBeenCalledWith([{
                        pattern: '',
                        occurrences: 1,
                        filteredOccurrences: 1,
                        formattedPattern: '',
                    },]);
                    done();
                });
            setTimeout(() => _$rootScope.$digest(), 15000);
        });

        it('should NOT update filtered data when there is an aggregation', () => {
            //given
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            const savedAggregation = {
                aggregationColumnId: '0002',
                aggregation: 'MAX',
            };
            spyOn(_StorageService, 'getAggregation').and.returnValue(savedAggregation);
            stateMock.playground.grid.numericColumns = [{ id: '0003' }];

            //when
            _StatisticsService.updateFilteredStatistics();

            //then
            expect(_StateService.setStatisticsFilteredHistogram).not.toHaveBeenCalled();
            expect(_StateService.setStatisticsHistogramActiveLimits).not.toHaveBeenCalled();
            expect(_StateService.setStatisticsFilteredPatterns).not.toHaveBeenCalled();
        });

        it('should update aggregation chart when at least 1 filter exists', inject(($q, StatisticsService, StatisticsRestService, StorageService) => {
            //given
            stateMock.playground.grid.selectedColumns = [barChartStrCol];
            stateMock.playground.grid.numericColumns = [{ id: '0002' }];
            const savedAggregation = {
                aggregationColumnId: '0002',
                aggregation: 'MAX',
            };
            spyOn(StorageService, 'getAggregation').and.returnValue(savedAggregation);
            spyOn(StatisticsRestService, 'getAggregations').and.returnValue($q.reject());

            //when
            StatisticsService.updateFilteredStatistics();

            //then
            expect(StatisticsRestService.getAggregations).toHaveBeenCalledWith({
                datasetId: null,
                preparationId: '2132548345365',
                stepId: '9878645468',
                operations: [{
                    operator: 'MAX',
                    columnId: '0002',
                },],
                groupBy: ['0010'],
            });
        }));
    });

    describe('utils', () => {
        beforeEach(inject(function (StatisticsRestService) {
            spyOn(StatisticsRestService, 'resetCache').and.returnValue();
        }));

        it('should reset all charts, statistics, cache', inject(function (StatisticsRestService, StatisticsService, StateService) {
            //when
            StatisticsService.reset();

            //then
            expect(StateService.setStatisticsBoxPlot).toHaveBeenCalledWith(null);
            expect(StateService.setStatisticsDetails).toHaveBeenCalledWith(null);
            expect(StatisticsRestService.resetCache).toHaveBeenCalled();
        }));

        it('should reset date filtered occurrence worker', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumns = [barChartDateCol];
            stateMock.playground.filter.gridFilters = [];
            stateMock.playground.grid.filteredOccurences = null;
            StatisticsService.statistics = {};
            stateMock.playground.statistics = {
                histogram: {
                    data: [],
                },
            };
            expect(StatisticsService.histogram).toBeFalsy();

            StatisticsService.processClassicChart(); //create the worker
            const dateWorker = StatisticsService.dateWorker;
            expect(dateWorker).toBeDefined();
            spyOn(dateWorker, 'terminate').and.callThrough();

            //when
            StatisticsService.reset();

            //then
            expect(dateWorker.terminate).toHaveBeenCalled();
            expect(StatisticsService.dateWorker).toBe(null);
        }));

        it('should reset date pattern filtered occurrence worker', inject(function (StatisticsService) {
            //given
            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'city',
                type: 'date',
                domain: 'date',
                statistics: {
                    patternFrequencyTable: [
                        {
                            pattern: 'd-M-yyyy',
                            occurrences: 1,
                        },
                        {
                            pattern: 'yyyy-M-d',
                            occurrences: 2,
                        },
                    ],
                },
            }];
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.grid.filteredRecords = [
                { '0001': '18-01-2015' },
                { '0001': '2016-01-01' },
            ];

            StatisticsService.updateStatistics(); //create the worker
            const patternWorker = StatisticsService.patternWorker;
            expect(patternWorker).toBeDefined();
            spyOn(patternWorker, 'terminate').and.callThrough();

            //when
            StatisticsService.reset();

            //then
            expect(patternWorker.terminate).toHaveBeenCalled();
            expect(StatisticsService.patternWorker).toBe(null);
        }));
    });
});
