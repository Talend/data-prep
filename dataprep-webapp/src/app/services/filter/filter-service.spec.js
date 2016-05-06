/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter service', function () {
    'use strict';

    var stateMock;
    beforeEach(angular.mock.module('data-prep.services.filter', function ($provide) {
        var columns = [
            {id: '0000', name: 'id'},
            {id: '0001', name: 'name'}
        ];

        stateMock = {
            playground: {
                filter: {gridFilters: []},
                data: {metadata: {columns: columns}}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function(StateService, StatisticsService) {
        spyOn(StateService, 'addGridFilter').and.returnValue();
        spyOn(StatisticsService, 'updateFilteredStatistics').and.returnValue();
    }));

    describe('get range label for', function () {
        it('should construct range label', inject(function (FilterService) {
            //given
            const intervals = [
                {input: {min: 0, max: 10, isMaxReached: false}, output: '[0 .. 10['},
                {input: {min: 10, max: 10, isMaxReached: false}, output: '[10]'},
                {input: {min: 0, max: 10, isMaxReached: true}, output: '[0 .. 10]'},
                {input: {min: 'Jan 2015', max: 'Mar 2015', isMaxReached: true}, output: '[Jan 2015 .. Mar 2015]'}
            ];

            //when
            const fn = FilterService.getRangeLabelFor;

            //then
            intervals.forEach(interval => expect(fn(interval.input)).toEqual(interval.output));
        }));
    });

    describe('get splitted range label', function () {
        it('should isolate range values', inject(function (FilterService) {
            //given
            const labels = [
                {input: '', output: ['']},
                {input: '[]', output: ['']},
                {input: '[10]', output: ['10']},
                {input: '[0,10]', output: ['0', '10']},
                {input: '[0,10[', output: ['0', '10']},
                {input: '[0 .. 10]', output: ['0', '10']},
                {input: '[0 .. 10[', output: ['0', '10']},
                {input: '[Jan 2016,Jan 2017]', output: ['Jan 2016', 'Jan 2017']},
                {input: '[Jan 2016 .. Jan 2017[', output: ['Jan 2016', 'Jan 2017']}
            ];

            //when
            const fn = FilterService.getSplittedRangeLabelFor;

            //then
            labels.forEach(label => expect(fn(label.input)).toEqual(label.output));
        }));
    });

    describe('add filter', function () {
        describe('with "contains" type', function () {
            it('should create filter', inject(function (FilterService, StateService) {
                //given
                var removeFnCallback = function () {
                };
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when

                FilterService.addFilter('contains', 'col1', 'column name', {
                    caseSensitive: true,
                    phrase: [
                        {
                            value: 'toto\n'
                        }
                    ]
                }, removeFnCallback);

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('contains');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.editable).toBe(true);
                expect(filterInfo.args).toEqual({
                    caseSensitive: true,
                    phrase: [
                        {
                            label: 'toto\\n',
                            value: 'toto\n'
                        }
                    ]
                });
                expect(filterInfo.filterFn()({col1: ' toto\nest ici'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
                expect(filterInfo.removeFilterFn).toBe(removeFnCallback);
            }));

            it('should create filter with wildcard', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('contains', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'to*ici'
                        }
                    ]
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
            }));

            it('should remove filter when it already exists', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        phrase: [
                            {
                                value: 'toto'
                            }
                        ]
                    },
                    type: 'contains'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('contains', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'toto'
                        }
                    ]
                }, null);

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));

            it('should update filter when on already exists with a different value', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        phrase: [
                            {
                                value: 'tata'
                            }
                        ]
                    },
                    type: 'contains'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'updateGridFilter').and.returnValue();

                //when
                FilterService.addFilter('contains', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'toto'
                        }
                    ]
                }, null);

                //then
                expect(StateService.updateGridFilter).toHaveBeenCalled();
                expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
                var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
                expect(newFilter.type).toBe('contains');
                expect(newFilter.colId).toBe('col1');
                expect(newFilter.args).toEqual({
                    phrase: [
                        {
                            label: 'toto',
                            value: 'toto'
                        }
                    ]
                });

            }));
        });

        describe('with "exact" type', function () {
            it('should create filter with caseSensitive', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('exact', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'toici\n'
                        }
                    ],
                    caseSensitive: true
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.args).toEqual({
                    phrase: [
                        {
                            label: 'toici\\n',
                            value: 'toici\n'
                        }
                    ],
                    caseSensitive: true
                });

                expect(filterInfo.filterFn()({col1: 'toici\n'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: 'toici'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: 'Toici'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ' toici'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: 'toici '})).toBeFalsy();
            }));

            it('should create filter without caseSensitive', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('exact', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'toici'
                        }
                    ],
                    caseSensitive: false
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: 'Toici'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: ' toici'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: 'toici '})).toBeFalsy();
            }));

            it('should remove filter when it already exists', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        phrase: [
                            {
                                value: 'toto'
                            }
                        ]
                    },
                    type: 'exact'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('exact', 'col1', 'column name', {
                    phrase:
                        [
                            {
                                value: 'toto'
                            }
                        ]
                }, null);

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));

            it('should update filter when on already exists with a different value', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        phrase: [
                            {
                                value: 'tata'
                            }
                        ]
                    },
                    type: 'exact'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'updateGridFilter').and.returnValue();

                //when
                FilterService.addFilter('exact', 'col1', 'column name', {
                    phrase: [
                        {
                            value: 'toto'
                        }
                    ]
                }, null);

                //then
                expect(StateService.updateGridFilter).toHaveBeenCalled();
                expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
                var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
                expect(newFilter.type).toBe('exact');
                expect(newFilter.colId).toBe('col1');
                expect(newFilter.args).toEqual({
                    phrase: [
                        {
                            label: 'toto',
                            value: 'toto'
                        }
                    ]
                });

            }));
        });

        describe('with "invalid records" type', function () {
            it('should create filter', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();
                var invalidValues = ['NA', 'N/A', 'N.A'];
                var data = {
                    metadata: {
                        columns: [
                            {id: 'col0', quality: {invalidValues: []}},
                            {id: 'col1', quality: {invalidValues: invalidValues}}
                        ]
                    }
                };

                //when
                FilterService.addFilter('invalid_records', 'col1', 'column name');

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('invalid_records');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.value[0].label).toBe('invalid records');
                expect(filterInfo.editable).toBe(false);
                expect(filterInfo.args).toBeFalsy();
                expect(filterInfo.filterFn(data)({col1: 'NA'})).toBeTruthy();
                expect(filterInfo.filterFn(data)({col1: ' tata est ici'})).toBeFalsy();
            }));

            it('should remove filter when it already exists', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {colId: 'col1', type: 'invalid_records'};
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('invalid_records', 'col1', 'column name');

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));
        });

        describe('with "empty records" type', function () {
            it('should create filter', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('empty_records', 'col1', 'column name');

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('empty_records');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.value[0].label).toBe('empty records');
                expect(filterInfo.editable).toBe(false);
                expect(filterInfo.args).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ''})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
            }));

            it('should remove filter', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {colId: 'col1', type: 'empty_records'};
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('empty_records', 'col1', 'column name');

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));
        });

        describe('with "valid records" type', function () {
            it('should create filter', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();
                var invalidValues = ['m', 'p'];
                var data = {
                    metadata: {
                        columns: [
                            {id: 'col0', quality: {invalidValues: []}},
                            {id: 'col1', quality: {invalidValues: invalidValues}}
                        ]
                    }
                };

                //when
                FilterService.addFilter('valid_records', 'col1', 'column name');

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('valid_records');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.value[0].label).toBe('valid records');
                expect(filterInfo.editable).toBe(false);
                expect(filterInfo.args).toBeFalsy();
                expect(filterInfo.filterFn(data)({col1: 'a'})).toBeTruthy();
                expect(filterInfo.filterFn(data)({col1: 'm'})).toBeFalsy();
                expect(filterInfo.filterFn(data)({col1: ''})).toBeFalsy();
            }));

            it('should remove filter', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {colId: 'col1', type: 'valid_records'};
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('valid_records', 'col1', 'column name');

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));
        });

        describe('with "inside range" type', function () {
            it('should create number filter', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('inside_range', 'col1', 'column name', {
                    intervals: [
                        {
                            label: '[0 .. 22[',
                            value: [0, 22]
                        }
                    ],
                    type: 'integer',
                    isMaxReached: true
                });
                FilterService.addFilter('inside_range', 'col2', 'column name2', {
                    intervals: [
                        {
                            label: '[0 .. 1,000,000[',
                            value: [0, 1000000]
                        }
                    ],
                    type: 'integer',
                    isMaxReached: false
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();
                expect(StateService.addGridFilter.calls.count()).toBe(2);

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('inside_range');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.value[0].label).toBe('[0 .. 22[');
                expect(filterInfo.editable).toBe(false);
                expect(filterInfo.args).toEqual({
                    intervals: [
                        {
                            label: '[0 .. 22[',
                            value: [0, 22]
                        }
                    ],
                    type: 'integer',
                    isMaxReached: true
                });
                expect(filterInfo.filterFn()({col1: '5'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: '-5'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ''})).toBeFalsy();

                var filterInfo2 = StateService.addGridFilter.calls.argsFor(1)[0];
                expect(filterInfo2.type).toBe('inside_range');
                expect(filterInfo2.colId).toBe('col2');
                expect(filterInfo2.colName).toBe('column name2');
                expect(filterInfo2.value).toEqual([
                    {
                        label: '[0 .. 1,000,000[',
                        value: [0, 1000000]
                    }
                ]);
                expect(filterInfo2.editable).toBe(false);
                expect(filterInfo2.args).toEqual({
                    intervals: [
                        {
                            label: '[0 .. 1,000,000[',
                            value: [0, 1000000]
                        }
                    ],
                    type: 'integer',
                    isMaxReached: false
                });
                expect(filterInfo2.filterFn()({col2: '1000'})).toBeTruthy();
                expect(filterInfo2.filterFn()({col2: '-5'})).toBeFalsy();
                expect(filterInfo2.filterFn()({col2: ''})).toBeFalsy();
            }));

            it('should create date filter', inject(function (FilterService, StateService) {
                //given
                stateMock.playground.grid = {
                    selectedColumn: {
                        id: 'col1',
                        statistics: {
                            patternFrequencyTable: [{pattern: 'yyyy-MM-dd'}]
                        }
                    }
                };

                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter(
                    'inside_range',
                    'col1',
                    'column name',
                    {
                        intervals: [
                            {
                                label: 'Jan 2014',
                                value: [
                                    new Date(2014, 0, 1).getTime(),
                                    new Date(2014, 1, 1).getTime()
                                ]
                            }
                        ],
                        type: 'date'
                    },
                    null
                );

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.type).toBe('inside_range');
                expect(filterInfo.colId).toBe('col1');
                expect(filterInfo.colName).toBe('column name');
                expect(filterInfo.value).toEqual([
                    {
                        label: 'Jan 2014',
                        value: [
                            new Date(2014, 0, 1).getTime(),
                            new Date(2014, 1, 1).getTime()
                        ]
                    }
                ]);
                expect(filterInfo.editable).toBe(false);
                expect(filterInfo.args).toEqual({
                    intervals: [
                        {
                            label: 'Jan 2014',
                            value: [
                                new Date(2014, 0, 1).getTime(),
                                new Date(2014, 1, 1).getTime()
                            ]
                        }
                    ],
                    type: 'date'
                });
                expect(filterInfo.filterFn()({col1: '2014-01-10'})).toBe(true);
                expect(filterInfo.filterFn()({col1: '2015-12-10'})).toBe(false);
                expect(filterInfo.filterFn()({col1: 'NA'})).toBe(false);
            }));

            it('should remove filter', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    type: 'inside_range',
                    args: {
                        intervals: [
                            {
                                value: [0, 22]
                            }
                        ]
                    }
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('inside_range', 'col1', 'column name', {
                    intervals: [
                        {
                            value: [0, 22]
                        }
                    ]
                });

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));
        });

        describe('with "match" type', function () {
            it('should create filter with empty pattern', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value: ''
                        }
                    ]
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: '2015 12 o\'clock d 12'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: 'Aa9'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ''})).toBeTruthy();
            }));

            it('should create filter with alphanumeric patterns', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value:'Aa9/.,'
                        }
                    ]
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: 'Ha6/.,'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: ' ha6/.,'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: ' hah/.,'})).toBeFalsy();
            }));

            it('should create filter with date patterns', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value: 'yyyy-d-M'
                        }
                    ]
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: '2015-12-12'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: '2015-12-13'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: '2015/12/13'})).toBeFalsy();
            }));

            it('should create filter with customized date patterns', inject(function (FilterService, StateService) {
                //given
                expect(StateService.addGridFilter).not.toHaveBeenCalled();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value: 'yyyy d \'o\'\'clock d\' M HH:mm:ss'
                        }
                    ]
                });

                //then
                expect(StateService.addGridFilter).toHaveBeenCalled();

                var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
                expect(filterInfo.filterFn()({col1: '2015 12 o\'clock d 12 00:00:00'})).toBeTruthy();
                expect(filterInfo.filterFn()({col1: '2015 12 o\'clock D 12'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: '2015 12 12'})).toBeFalsy();
                expect(filterInfo.filterFn()({col1: '2015 12 \'o\'\'clock\' 13'})).toBeFalsy();
            }));

            it('should remove filter when it already exists', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        patterns: [
                            {
                                value: 'Aa'
                            }
                        ]
                    },
                    type: 'matches'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'removeGridFilter').and.returnValue();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value: 'Aa'
                        }
                    ]
                }, null);

                //then
                expect(StateService.removeGridFilter).toHaveBeenCalledWith(oldFilter);
            }));

            it('should update filter when it already exists with a different pattern', inject(function (FilterService, StateService) {
                //given
                var oldFilter = {
                    colId: 'col1',
                    args: {
                        patterns: [
                            {
                                value:'Aa'
                            }
                        ]
                    },
                    type: 'matches'
                };
                stateMock.playground.filter.gridFilters = [oldFilter];
                spyOn(StateService, 'updateGridFilter').and.returnValue();

                //when
                FilterService.addFilter('matches', 'col1', 'column name', {
                    patterns: [
                        {
                            value:'Aa9'
                        }
                    ]
                }, null);

                //then
                expect(StateService.updateGridFilter).toHaveBeenCalled();
                expect(StateService.updateGridFilter.calls.argsFor(0)[0]).toBe(oldFilter);
                var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
                expect(newFilter.type).toBe('matches');
                expect(newFilter.colId).toBe('col1');
                expect(newFilter.args.patterns).toEqual([
                    {
                        value:'Aa9'
                    }
                ]);

            }));
        });

        it('should not throw exception on non existing column (that could be removed by a step) in contains filter', inject(function (FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('contains', 'col_that_does_not_exist', 'column name', {
                phrase: [
                    {
                        value: 'toto'
                    }
                ]
            });

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('contains');
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
        }));

        it('should not throw exception on non existing column (that could be removed by a step) in exact filter', inject(function (FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('exact', 'col_that_does_not_exist', 'column name', {
                phrase: [
                    {
                        value: 'toto'
                    }
                ]
            });

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('exact');
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
        }));

        it('should trigger statistics update', inject(function (FilterService, StatisticsService) {
            //given
            var removeFnCallback = function () {
            };
            expect(StatisticsService.updateFilteredStatistics).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('contains', 'col1', 'column name', {
                phrase: [
                    {
                        value: 'toto'
                    }
                ]
            }, removeFnCallback);

            //then
            expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
        }));
    });

    describe('add filter and digest', function () {
        it('should add a filter wrapped in $timeout to trigger a digest', inject(function ($timeout, FilterService, StateService) {
            //given
            var removeFnCallback = function () {
            };
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilterAndDigest('contains', 'col1', 'column name', {
                caseSensitive: true,
                phrase: [
                    {
                        value: 'toto\n'
                    }
                ]
            }, removeFnCallback);
            expect(StateService.addGridFilter).not.toHaveBeenCalled();
            $timeout.flush();

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('contains');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.editable).toBe(true);
            expect(filterInfo.args).toEqual({
                caseSensitive: true,
                phrase: [
                    {
                        label: 'toto\\n',
                        value: 'toto\n'
                    }
                ]
            });
            expect(filterInfo.filterFn()({col1: ' toto\nest ici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
            expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
            expect(filterInfo.removeFilterFn).toBe(removeFnCallback);
        }));
    });

    describe('remove filter', function () {
        beforeEach(inject(function (StateService) {
            spyOn(StateService, 'removeGridFilter').and.returnValue();
            spyOn(StateService, 'removeAllGridFilters').and.returnValue();
        }));

        it('should remove all filters', inject(function (FilterService, StateService) {
            //when
            FilterService.removeAllFilters();

            //then
            expect(StateService.removeAllGridFilters).toHaveBeenCalled();
        }));

        it('should call each filter remove callback', inject(function (FilterService) {
            //given
            var removeFn1 = jasmine.createSpy('removeFilterCallback');
            var removeFn2 = jasmine.createSpy('removeFilterCallback');
            var filter0 = {};
            var filter1 = {removeFilterFn: removeFn1};
            var filter2 = {removeFilterFn: removeFn2};
            var filter3 = {};
            stateMock.playground.filter.gridFilters = [filter0, filter1, filter2, filter3];

            //when
            FilterService.removeAllFilters();

            //then
            expect(removeFn1).toHaveBeenCalled();
            expect(removeFn2).toHaveBeenCalled();
        }));

        it('should remove filter', inject(function (FilterService, StateService) {
            //given
            var filter = {};

            //when
            FilterService.removeFilter(filter);

            //then
            expect(StateService.removeGridFilter).toHaveBeenCalledWith(filter);
        }));

        it('should call filter remove callback', inject(function (FilterService) {
            //given
            var removeFn = jasmine.createSpy('removeFilterCallback');
            var filter = {removeFilterFn: removeFn};

            //when
            FilterService.removeFilter(filter);

            //then
            expect(removeFn).toHaveBeenCalled();
        }));

        it('should trigger statistics update on remove single', inject(function (FilterService, StatisticsService) {
            //given
            var filter = {};
            expect(StatisticsService.updateFilteredStatistics).not.toHaveBeenCalled();

            //when
            FilterService.removeFilter(filter);

            //then
            expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
        }));

        it('should trigger statistics update on remove all', inject(function (FilterService, StatisticsService) {
            //given
            expect(StatisticsService.updateFilteredStatistics).not.toHaveBeenCalled();

            //when
            FilterService.removeAllFilters();

            //then
            expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
        }));
    });

    describe('update filter', function () {
        beforeEach(inject(function (StateService) {
            spyOn(StateService, 'updateGridFilter').and.returnValue();
        }));

        it('should update "contains" filter', inject(function (FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'contains',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: [
                        {
                            value: 'Tata'
                        }
                    ]
                },
                filterFn: function () {
                }
            };
            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    value: 'Tata\\n'
                }
            ]);

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('contains');
            expect(newFilter.colId).toBe('col2');
            expect(newFilter.colName).toBe('column 2');
            expect(newFilter.args.phrase).toEqual([
                {
                    value: 'Tata\\n'
                }
            ]);

            expect(newFilter.filterFn()({col2: ' Tata\\n est ici'})).toBeTruthy();
            expect(newFilter.filterFn()({col2: ' Tata\n est ici'})).toBeFalsy();

        }));

        it('should update "exact" filter', inject(function (FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'exact',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: [
                        {
                            value: 'Toto'
                        }
                    ]
                },
                filterFn: function () {
                }
            };

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    value: 'Tata\\n'
                }
            ]);

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('exact');
            expect(newFilter.colId).toBe('col2');
            expect(newFilter.colName).toBe('column 2');
            expect(newFilter.args.phrase).toEqual([
                {
                    value: 'Tata\\n'
                }
            ]);
            expect(newFilter.value).toEqual([
                {
                    value: 'Tata\\n'
                }
            ]);

            expect(newFilter.filterFn()({col2: 'Tata\\n'})).toBeTruthy();
            expect(newFilter.filterFn()({col2: 'Tata\n'})).toBeFalsy();
        }));

        it('should update "inside_range" filter after a brush', inject(function (FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'inside_range',
                colId: 'col1',
                colName: 'column 1',
                args: {
                    intervals: [
                        {
                            label: '[5 .. 10[',
                            value: [5, 10]
                        }
                    ],
                    type: 'integer'
                },
                filterFn: function () {
                }
            };

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    value: [0, 22],
                    label: '[0 .. 22['
                }
            ]);

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('inside_range');
            expect(newFilter.colId).toBe('col1');
            expect(newFilter.colName).toBe('column 1');
            expect(newFilter.args).toEqual({
                intervals: [
                    {
                        value: [0, 22],
                        label: '[0 .. 22['
                    }
                ],
                type: 'integer'
            });
            expect(newFilter.value).toEqual([
                {
                    value: [0, 22],
                    label: '[0 .. 22['
                }
            ]);
        }));

        it('should update "inside_range" filter for date column', inject(function (FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'inside_range',
                colId: 'col1',
                colName: 'column 1',
                args: {
                    intervals: [
                        {
                            label: 'Jan 2014',
                            value: [
                                new Date(2014, 0, 1).getTime(),
                                new Date(2014, 1, 1).getTime()
                            ]
                        }
                    ],
                    type: 'date'
                },
                filterFn: function () {
                }
            };

            stateMock.playground.grid = {
                selectedColumn: {
                    id: '0000',
                    statistics: {
                        patternFrequencyTable: [{pattern: 'yyyy-MM-dd'}]
                    }
                }
            };


            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    label: 'Mar 2015',
                    value: [
                        new Date(2015, 2, 1).getTime(),
                        new Date(2015, 3, 1).getTime()
                    ]
                }
            ]);

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('inside_range');
            expect(newFilter.colId).toBe('col1');
            expect(newFilter.colName).toBe('column 1');
            expect(newFilter.args).toEqual({
                intervals: [
                    {
                        label: 'Mar 2015',
                        value: [
                            new Date(2015, 2, 1).getTime(),
                            new Date(2015, 3, 1).getTime()
                        ]
                    }
                ],
                type: 'date'
            });
            expect(newFilter.value).toEqual([
                {
                    label: 'Mar 2015',
                    value: [
                        new Date(2015, 2, 1).getTime(),
                        new Date(2015, 3, 1).getTime()
                    ]
                }
            ]);
        }));

        it('should update "inside range" filter when adding an existing range filter', inject(function (FilterService, StateService) {
            //given
            var removeCallback = function () {
            };
            FilterService.addFilter('inside_range', 'col1', 'column name', {
                intervals: [
                    {
                        label: '[0 .. 22[',
                        value: [0, 22]
                    }
                ],
                type: 'integer'
            }, removeCallback);

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();
            expect(StateService.addGridFilter.calls.count()).toBe(1);
            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.value).toEqual([
                {
                    label: '[0 .. 22[',
                    value: [0, 22]
                }
            ]);

            expect(filterInfo.filterFn({col1: '4'})).toBeTruthy();
            stateMock.playground.filter.gridFilters = [filterInfo];

            //when
            FilterService.addFilter('inside_range', 'col1', 'column name', {
                intervals: [
                    {
                        label: '[5 .. 10[',
                        value: [5, 10],
                        isMaxReached: true
                    }
                ],
                type: 'integer'
            });

            //then
            expect(StateService.updateGridFilter).toHaveBeenCalled();
            expect(StateService.addGridFilter.calls.count()).toBe(1);

            var oldFilterInfo = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(oldFilterInfo).not.toBe(filterInfo);

            var newFilterInfos = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilterInfos.type).toBe('inside_range');
            expect(newFilterInfos.colId).toBe('col1');
            expect(newFilterInfos.colName).toBe('column name');
            expect(newFilterInfos.value).toEqual([
                {
                    label: '[5 .. 10[',
                    value: [5, 10],
                    isMaxReached: true
                }
            ]);
            expect(newFilterInfos.editable).toBe(false);
            expect(newFilterInfos.args).toEqual({
                intervals: [
                    {
                        label: '[5 .. 10[',
                        value: [5, 10],
                        isMaxReached: true
                    }
                ],
                type: 'integer'
            });
            expect(newFilterInfos.filterFn()({col1: '8'})).toBeTruthy();
            //the 4 is no more inside the brush range
            expect(newFilterInfos.filterFn()({col1: '4'})).toBeFalsy();
            expect(newFilterInfos.removeFilterFn).toBe(removeCallback);
        }));

        it('should trigger statistics update', inject(function (FilterService, StatisticsService) {
            //given
            var oldFilter = {
                type: 'contains',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: [
                        {
                            value: 'Tata'
                        }
                    ]
                },
                filterFn: function () {
                }
            };
            expect(StatisticsService.updateFilteredStatistics).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    value: 'Tata'
                }
            ]);

            //then
            expect(StatisticsService.updateFilteredStatistics).toHaveBeenCalled();
        }));

        it('should update exact filter while several values are selected', inject(function (FilterService, StateService) {
            //given
            const oldFilter = {
                type: 'exact',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: [
                        {
                            value: 'Toto'
                        }
                    ]
                },
                filterFn: function () {
                }
            };

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    value: 'Tata'
                }
            ], 'ctrl');

            //then
            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.args.phrase).toEqual([
                {value: 'Toto'},
                {value: 'Tata'}
            ]);

            expect(newFilter.filterFn()({col2: 'Toto'})).toBeTruthy();
            expect(newFilter.filterFn()({col2: 'Tata'})).toBeTruthy();
            expect(newFilter.filterFn()({col2: 'Titi'})).toBeFalsy();
        }));

        it('should update range filter while several values are selected', inject(function (FilterService, StateService) {
            //given
            const oldFilter = {
                type: 'inside_range',
                colId: 'col1',
                colName: 'column 1',
                args: {
                    intervals: [
                        {
                            label: 'Jan 2014',
                            value: [
                                new Date(2014, 0, 1).getTime(),
                                new Date(2014, 1, 1).getTime()
                            ]
                        }
                    ],
                    type: 'date'
                },
                filterFn: function () {
                }
            };

            stateMock.playground.grid = {
                selectedColumn: {
                    id: '0000',
                    statistics: {
                        patternFrequencyTable: [{pattern: 'yyyy-MM-dd'}]
                    }
                }
            };

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    label: 'Feb 2014',
                    value: [
                        new Date(2014, 1, 1).getTime(),
                        new Date(2014, 2, 1).getTime()
                    ]
                }
            ], FilterService.CTRL_KEY_NAME);

            //then
            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.args).toEqual({
                intervals: [
                    {
                        label: 'Jan 2014',
                        value: [
                            new Date(2014, 0, 1).getTime(),
                            new Date(2014, 1, 1).getTime()
                        ]
                    },
                    {
                        label: 'Feb 2014',
                        value: [
                            new Date(2014, 1, 1).getTime(),
                            new Date(2014, 2, 1).getTime()
                        ]
                    }
                ],
                type: 'date'
            });

            expect(newFilter.filterFn()({col1: '2014-01-01'})).toBeTruthy();
            expect(newFilter.filterFn()({col1: '2014-02-01'})).toBeTruthy();
            expect(newFilter.filterFn()({col1: '2014-03-01'})).toBeFalsy();
        }));

        it('should update range filter while from-to values are selected', inject(function (FilterService, StateService) {
            //given
            const oldFilter = {
                type: 'inside_range',
                colId: 'col1',
                colName: 'column 1',
                args: {
                    intervals: [
                        {
                            label: 'Jan 2014',
                            value: [
                                new Date(2014, 0, 1).getTime(),
                                new Date(2014, 1, 1).getTime()
                            ]
                        }
                    ],
                    type: 'date'
                },
                filterFn: function () {
                }
            };

            stateMock.playground.grid = {
                selectedColumn: {
                    id: '0000',
                    statistics: {
                        patternFrequencyTable: [{pattern: 'yyyy-MM-dd'}]
                    }
                }
            };

            //when
            FilterService.updateFilter(oldFilter, [
                {
                    label: 'Apr 2014',
                    value: [
                        new Date(2014, 3, 1).getTime(),
                        new Date(2014, 4, 1).getTime()
                    ]
                }
            ], FilterService.SHIFT_KEY_NAME);

            //then
            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.value).toEqual(
                [
                    {
                        label: '[Jan 2014 .. Apr 2014[',
                        value: [
                            new Date(2014, 0, 1).getTime(),
                            new Date(2014, 4, 1).getTime()
                        ]
                    }
                ]
            );
            expect(newFilter.filterFn()({col1: '2014-02-01'})).toBeTruthy();
            expect(newFilter.filterFn()({col1: '2014-03-01'})).toBeTruthy();
            expect(newFilter.filterFn()({col1: '2014-05-01'})).toBeFalsy();
        }));
    });
});