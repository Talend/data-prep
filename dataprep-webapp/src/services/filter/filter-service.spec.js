describe('Filter service', function() {
    'use strict';

    var stateMock;
    beforeEach(module('data-prep.services.filter', function ($provide) {
        var columns =  [
            {id: '0000', name: 'id'},
            {id: '0001', name: 'name'}
        ];

        stateMock = {playground: {
            filter: {gridFilters: []},
            data:{columns:columns}
        }};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function(DatagridService, StateService) {
        spyOn(StateService, 'addGridFilter').and.returnValue();
    }));

    describe('utils', function() {

    });

    describe('add filter', function() {
        it('should add "contains" filter', inject(function(FilterService, StateService) {
            //given
            var removeFnCallback = function() {};
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('contains', 'col1', 'column name', {phrase: 'toto'}, removeFnCallback);

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('contains');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.editable).toBe(true);
            expect(filterInfo.args).toEqual({phrase: 'toto'});
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
            expect(filterInfo.removeFilterFn).toBe(removeFnCallback);
        }));

        it('should add "contains" filter with wildcard', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('contains', 'col1', 'column name', {phrase: 'to*ici'});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
        }));

        it('should add "exact" filter with caseSensitive', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('exact', 'col1', 'column name', {phrase: 'toici', caseSensitive: true});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.filterFn()({col1: 'toici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: 'Toici'})).toBeFalsy();
            expect(filterInfo.filterFn()({col1: ' toici'})).toBeFalsy();
            expect(filterInfo.filterFn()({col1: 'toici '})).toBeFalsy();
        }));

        it('should add "exact" filter without caseSensitive', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('exact', 'col1', 'column name', {phrase: 'toici', caseSensitive: false});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.filterFn()({col1: 'Toici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' toici'})).toBeFalsy();
            expect(filterInfo.filterFn()({col1: 'toici '})).toBeFalsy();
        }));

        it('should add "invalid records" filter', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();
            var invalidValues = ['NA', 'N/A', 'N.A'];
            var data = {
                columns: [
                    {id: 'col0', quality: {invalidValues: []}},
                    {id: 'col1', quality: {invalidValues: invalidValues}}
                ]
            };

            //when
            FilterService.addFilter('invalid_records', 'col1', 'column name');

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('invalid_records');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.value).toBe('invalid records');
            expect(filterInfo.editable).toBe(false);
            expect(filterInfo.args).toBeFalsy();
            expect(filterInfo.filterFn(data)({col1: 'NA'})).toBeTruthy();
            expect(filterInfo.filterFn(data)({col1: ' tata est ici'})).toBeFalsy();
        }));

        it('should add "empty records" filter', inject(function(FilterService, StateService) {
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
            expect(filterInfo.value).toBe('empty records');
            expect(filterInfo.editable).toBe(false);
            expect(filterInfo.args).toBeFalsy();
            expect(filterInfo.filterFn()({col1: ''})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
        }));

        it('should add "valid records" filter', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();
            var invalidValues = ['m', 'p'];
            var data = {
                columns: [
                    {id: 'col0', quality: {invalidValues: []}},
                    {id: 'col1', quality: {invalidValues: invalidValues}}
                ]
            };

            //when
            FilterService.addFilter('valid_records', 'col1', 'column name');

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('valid_records');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.value).toBe('valid records');
            expect(filterInfo.editable).toBe(false);
            expect(filterInfo.args).toBeFalsy();
            expect(filterInfo.filterFn(data)({col1: 'a'})).toBeTruthy();
            expect(filterInfo.filterFn(data)({col1: 'm'})).toBeFalsy();
            expect(filterInfo.filterFn(data)({col1: ''})).toBeFalsy();
        }));

        it('should add "inside range" filter', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('inside_range', 'col1', 'column name', {interval: [0, 22]});
            FilterService.addFilter('inside_range', 'col2', 'column name2', {interval: [0, 1000000]});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();
            expect(StateService.addGridFilter.calls.count()).toBe(2);

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('inside_range');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.value).toBe('[0 .. 22]');
            expect(filterInfo.editable).toBe(false);
            expect(filterInfo.args).toEqual({interval: [0, 22]});
            expect(filterInfo.filterFn()({col1:'5'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1:'-5'})).toBeFalsy();

            var filterInfo2 = StateService.addGridFilter.calls.argsFor(1)[0];
            expect(filterInfo2.type).toBe('inside_range');
            expect(filterInfo2.colId).toBe('col2');
            expect(filterInfo2.colName).toBe('column name2');
            expect(filterInfo2.value).toBe('[0 .. 1,000,000]');
            expect(filterInfo2.editable).toBe(false);
            expect(filterInfo2.args).toEqual({interval:  [0, 1000000]});
            expect(filterInfo2.filterFn()({col2: '1000'})).toBeTruthy();
            expect(filterInfo2.filterFn()({col2: '-5'})).toBeFalsy();
        }));

        it('should not throw exception on non existing column (that could be removed by a step) in contains filter', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('contains', 'col_that_does_not_exist', 'column name', {phrase: 'toto'});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('contains');
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
        }));

        it('should not throw exception on non existing column (that could be removed by a step) in exact filter', inject(function(FilterService, StateService) {
            //given
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilter('exact', 'col_that_does_not_exist', 'column name', {phrase: 'toto'});

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('exact');
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeFalsy();
        }));

        it('should return filter value info for "contains" filter', inject(function(FilterService, StateService) {
            //given
            FilterService.addFilter('contains', 'col1', 'column 1', {phrase: 'Toto'});
            var filter = StateService.addGridFilter.calls.argsFor(0)[0];

            //when
            var value = filter.value;

            //then
            expect(value).toBe('Toto');
        }));
    });

    describe('add filter and digest', function() {
        it('should add a filter wrapped in $timeout to trigger a digest', inject(function($timeout, FilterService, StateService) {
            //given
            var removeFnCallback = function() {};
            expect(StateService.addGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.addFilterAndDigest('contains', 'col1', 'column name', {phrase: 'toto'}, removeFnCallback);
            expect(StateService.addGridFilter).not.toHaveBeenCalled();
            $timeout.flush();

            //then
            expect(StateService.addGridFilter).toHaveBeenCalled();

            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];
            expect(filterInfo.type).toBe('contains');
            expect(filterInfo.colId).toBe('col1');
            expect(filterInfo.colName).toBe('column name');
            expect(filterInfo.editable).toBe(true);
            expect(filterInfo.args).toEqual({phrase: 'toto'});
            expect(filterInfo.filterFn()({col1: ' toto est ici'})).toBeTruthy();
            expect(filterInfo.filterFn()({col1: ' tata est ici'})).toBeFalsy();
            expect(filterInfo.removeFilterFn).toBe(removeFnCallback);
        }));
    });

    describe('remove filter', function() {
        beforeEach(inject(function(StateService) {
            spyOn(StateService, 'removeGridFilter').and.returnValue();
            spyOn(StateService, 'removeAllGridFilters').and.returnValue();
        }));

        it('should remove all filters', inject(function(FilterService, StateService) {
            //when
            FilterService.removeAllFilters();

            //then
            expect(StateService.removeAllGridFilters).toHaveBeenCalled();
        }));

        it('should call each filter remove callback', inject(function(FilterService) {
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

        it('should remove filter', inject(function(FilterService, StateService) {
            //given
            var filter = {};

            //when
            FilterService.removeFilter(filter);

            //then
            expect(StateService.removeGridFilter).toHaveBeenCalledWith(filter);
        }));

        it('should call filter remove callback', inject(function(FilterService) {
            //given
            var removeFn = jasmine.createSpy('removeFilterCallback');
            var filter = {removeFilterFn: removeFn};

            //when
            FilterService.removeFilter(filter);

            //then
            expect(removeFn).toHaveBeenCalled();
        }));
    });

    describe('update filter', function() {
        beforeEach(inject(function(StateService) {
            spyOn(StateService, 'updateGridFilter').and.returnValue();
        }));

        it('should update "contains" filter', inject(function(FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'contains',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: 'Tata'
                },
                filterFn: function() {}
            };
            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, 'Tata');

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('contains');
            expect(newFilter.colId).toBe('col2');
            expect(newFilter.colName).toBe('column 2');
            expect(newFilter.args.phrase).toBe('Tata');
        }));

        it('should update "exact" filter', inject(function(FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'exact',
                colId: 'col2',
                colName: 'column 2',
                args: {
                    phrase: 'Toto'
                },
                filterFn: function() {}
            };

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, 'Tata');

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('exact');
            expect(newFilter.colId).toBe('col2');
            expect(newFilter.colName).toBe('column 2');
            expect(newFilter.args.phrase).toBe('Tata');
            expect(newFilter.value).toBe('Tata');
        }));

        it('should update "inside_range" filter after a brush', inject(function(FilterService, StateService) {
            //given
            var oldFilter = {
                type: 'inside_range',
                colId: 'col1',
                colName: 'column 1',
                args: {
                    interval: [5,10]
                },
                filterFn: function() {}
            };

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();

            //when
            FilterService.updateFilter(oldFilter, [0,22]);

            //then
            var argsOldFilter = StateService.updateGridFilter.calls.argsFor(0)[0];
            expect(argsOldFilter).toBe(oldFilter);

            var newFilter = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilter).not.toBe(oldFilter);
            expect(newFilter.type).toBe('inside_range');
            expect(newFilter.colId).toBe('col1');
            expect(newFilter.colName).toBe('column 1');
            expect(newFilter.args.interval).toEqual([0,22]);
            expect(newFilter.value).toBe('[0 .. 22]');
        }));

        it('should update "inside range" filter when adding an existing range filter', inject(function(FilterService, StateService) {
            //given
            var removeCallback = function() {};
            FilterService.addFilter('inside_range', 'col1', 'column name', {interval: [0, 22]}, removeCallback);

            expect(StateService.updateGridFilter).not.toHaveBeenCalled();
            expect(StateService.addGridFilter.calls.count()).toBe(1);
            var filterInfo = StateService.addGridFilter.calls.argsFor(0)[0];

            expect(filterInfo.filterFn({col1: '4'})).toBeTruthy();
            stateMock.playground.filter.gridFilters = [filterInfo];

            //when
            FilterService.addFilter('inside_range', 'col1', 'column name', {interval: [5, 10]});

            //then
            expect(StateService.updateGridFilter).toHaveBeenCalled();
            expect(StateService.addGridFilter.calls.count()).toBe(1);

            var oldFilterInfo = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(oldFilterInfo).not.toBe(filterInfo);

            var newFilterInfos = StateService.updateGridFilter.calls.argsFor(0)[1];
            expect(newFilterInfos.type).toBe('inside_range');
            expect(newFilterInfos.colId).toBe('col1');
            expect(newFilterInfos.colName).toBe('column name');
            expect(newFilterInfos.value).toBe('[5 .. 10]');
            expect(newFilterInfos.editable).toBe(false);
            expect(newFilterInfos.args).toEqual({interval:  [5, 10]});
            expect(newFilterInfos.filterFn()({col1: '8'})).toBeTruthy();
            //the 4 is no more inside the brush range
            expect(newFilterInfos.filterFn()({col1: '4'})).toBeFalsy();
            expect(newFilterInfos.removeFilterFn).toBe(removeCallback);
        }));
    });

    describe('filters tree construction', function() {
        it('should constructs AND pairs tree with more than 1 filter', inject(function(FilterService) {
            //given
            var filters = [
                {
                    'type': 'valid_records',
                    'colId': '0000',
                    'colName': 'name',
                    'value': 'valid records'
                },
                {
                    'type': 'exact',
                    'colId': '0000',
                    'colName': 'name',
                    'args': {
                        'phrase': '        AMC  ',
                        'caseSensitive': true
                    },
                    'value': '        AMC  '
                },
                {
                    'type': 'contains',
                    'colId': '0000',
                    'colName': 'name',
                    'args': {
                        'phrase': 'amc'
                    },
                    'value': 'amc'
                },
                {
                    'type': 'inside_range',
                    'colId': '0005',
                    'colName': 'weight (lb)',
                    'args': {
                        'interval': [
                            1786,
                            5140
                        ]
                    },
                    'value': '[1,786 .. 5,140]'
                },
                {
                    'type': 'invalid_records',
                    'colId': '0001',
                    'colName': 'economy (mpg)',
                    'value': 'invalid records'
                },
                {
                    'type': 'empty_records',
                    'colId': '0004',
                    'colName': 'power (hp)',
                    'value': 'empty records'
                }
            ];

            //when
            var filtersTree = FilterService.convertFiltersArrayToTreeFormat(filters);

            //then
            expect(filtersTree.filter).toEqual({
                'and': [
                    {
                        'and': [
                            {
                                'and': [
                                    {
                                        'valid': {
                                            'field': '0000'
                                        }
                                    },
                                    {
                                        'eq': {
                                            'field': '0000',
                                            'value': '        AMC  '
                                        }
                                    }
                                ]
                            },
                            {
                                'and': [
                                    {
                                        'contains': {
                                            'field': '0000',
                                            'value': 'amc'
                                        }
                                    },
                                    {
                                        'range': {
                                            'field': '0005',
                                            'start': '1786',
                                            'end': '5140'
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        'and': [
                            {
                                'invalid': {
                                    'field': '0001'
                                }
                            },
                            {
                                'empty': {
                                    'field': '0004'
                                }
                            }
                        ]
                    }
                ]
            });
        }));

        it('should constructs AND pairs tree with only 1 filter', inject(function(FilterService) {
            //given
            var filters = [
                {
                    'type': 'exact',
                    'colId': '0000',
                    'colName': 'name',
                    'args': {
                        'phrase': '        AMC  ',
                        'caseSensitive': true
                    },
                    'value': '        AMC  '
                }
            ];

            //when
            var filtersTree = FilterService.convertFiltersArrayToTreeFormat(filters);

            //then
            expect(filtersTree.filter).toEqual({
                'eq': {
                    'field': '0000',
                    'value': '        AMC  '
                }
            });
        }));

        it('should return empty object', inject(function(FilterService) {
            //given
            var filters = [];

            //when
            var filtersTree = FilterService.convertFiltersArrayToTreeFormat(filters);

            //then
            expect(filtersTree.filter).toBe(undefined);
            expect(filtersTree).toEqual({});
        }));
    });

    describe('filters tree flattening', function() {
        it('should return empty object', inject(function(FilterService) {
            //given
            var filtersTree = {
                'and': [
                    {
                        'invalid': {
                            'field': '0000'
                        }
                    },
                    {
                        'eq': {
                            'field': '0001',
                            'value': 'Toum'
                        }
                    }
                ]
            };
            var result = [];

            //when
            FilterService.flattenFiltersTree(filtersTree, result);

            //then
            expect(result[0].type).toEqual('invalid_records');
            expect(result[0].colId).toEqual('0000');
            expect(result[0].colName).toEqual('id');
            expect(result[0].args).toBe(undefined);

            expect(result[1].type).toBe('exact');
            expect(result[1].colId).toBe('0001');
            expect(result[1].colName).toBe('name');
            expect(result[1].args).toEqual({ phrase: 'Toum' });
        }));
    });
});