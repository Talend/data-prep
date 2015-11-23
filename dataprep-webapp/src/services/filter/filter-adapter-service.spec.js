describe('Filter Adapter Service', function () {
    'use strict';

    var stateMock;
    beforeEach(module('data-prep.services.filter', function ($provide) {
        stateMock = {playground: {
            data: {columns: [
                {id: '0000', name: 'firstname'},
                {id: '0001', name: 'lastname'},
                {id: '0002', name: 'birthdate'},
                {id: '0003', name: 'address'}
            ]}
        }};
        $provide.constant('state', stateMock);
    }));

    describe('create filter', function () {
        it('should create filter', inject(function (FilterAdapterService) {
            //given
            var type = 'contains';
            var colId = '0001';
            var colName = 'firstname';
            var editable = true;
            var args = {};
            var filterFn = jasmine.createSpy('filterFn');
            var removeFilterFn = jasmine.createSpy('removeFilterFn');

            //when
            var filter = FilterAdapterService.createFilter(type, colId, colName, editable, args, filterFn, removeFilterFn);

            //then
            expect(filter.type).toBe(type);
            expect(filter.colId).toBe(colId);
            expect(filter.colName).toBe(colName);
            expect(filter.editable).toBe(editable);
            expect(filter.args).toBe(args);
            expect(filter.filterFn).toBe(filterFn);
            expect(filter.removeFilterFn).toBe(removeFilterFn);
        }));

        describe('get value', function() {
            it('should return value on CONTAINS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'contains';
                var args = {phrase: 'Jimmy'};

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toBe('Jimmy');
            }));

            it('should return value on EXACT filter', inject(function (FilterAdapterService) {
                //given
                var type = 'exact';
                var args = {phrase: 'Jimmy'};

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toBe('Jimmy');
            }));

            it('should return value on INVALID_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'invalid_records';

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toBe('invalid records');
            }));

            it('should return value on EMPTY_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'empty_records';

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toBe('empty records');
            }));

            it('should return value on VALID_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'valid_records';

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toBe('valid records');
            }));

            it('should return value on INSIDE_RANGE filter', inject(function (FilterAdapterService) {
                //given
                var type = 'inside_range';
                var args = {
                    interval: [1000, 2000]
                };

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toBe('[1,000 .. 2,000[');
            }));
            
            it('should return value on a single value INSIDE_RANGE filter', inject(function (FilterAdapterService) {
                //given
                var type = 'inside_range';
                var args = {
                    interval: [5, 5]
                };

                //when
                var filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toBe('[5]');
            }));
        });

        describe('to tree', function() {
            it('should return tree corresponding to CONTAINS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'contains';
                var colId = '0001';
                var args = {phrase: 'Jimmy'};

                var filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    contains: {
                        field: '0001',
                        value: 'Jimmy'
                    }
                });
            }));

            it('should return tree corresponding to EXACT filter', inject(function (FilterAdapterService) {
                //given
                var type = 'exact';
                var colId = '0001';
                var args = {phrase: 'Jimmy'};

                var filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    eq: {
                        field: '0001',
                        value: 'Jimmy'
                    }
                });
            }));

            it('should return tree corresponding to INVALID_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'invalid_records';
                var colId = '0001';

                var filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    invalid: {
                        field: '0001'
                    }
                });
            }));

            it('should return tree corresponding to EMPTY_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'empty_records';
                var colId = '0001';

                var filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    empty: {
                        field: '0001'
                    }
                });
            }));

            it('should return tree corresponding to VALID_RECORDS filter', inject(function (FilterAdapterService) {
                //given
                var type = 'valid_records';
                var colId = '0001';

                var filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    valid: {
                        field: '0001'
                    }
                });
            }));

            it('should return tree corresponding to INSIDE_RANGE filter', inject(function (FilterAdapterService) {
                //given
                var type = 'inside_range';
                var colId = '0001';
                var args = {
                    interval: [1000, 2000]
                };

                var filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                var tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    range: {
                        field: '0001',
                        start: '1000',
                        end: '2000'
                    }
                });
            }));
        });
    });

    describe('adaptation to tree', function () {
        it('should return empty object when there is no filter', inject(function (FilterAdapterService) {
            //when
            var tree = FilterAdapterService.toTree([]);

            //then
            expect(tree).toEqual({});
        }));

        it('should create single filter tree', inject(function (FilterAdapterService) {
            //given
            var type = 'inside_range';
            var colId = '0001';
            var args = {
                interval: [1000, 2000]
            };

            var filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

            //when
            var tree = FilterAdapterService.toTree([filter]);

            //then
            expect(tree).toEqual({filter: {
                range: {
                    field: '0001',
                    start: '1000',
                    end: '2000'
                }
            }});
        }));

        it('should create multiple filters tree', inject(function (FilterAdapterService) {
            //given
            var rangeArgs = {
                interval: [1000, 2000]
            };
            var containsArgs = {phrase: 'Jimmy'};
            var exactArgs = {phrase: 'Toto'};

            var rangeFilter = FilterAdapterService.createFilter('inside_range', '0001', null, null, rangeArgs, null, null);
            var containsFilter = FilterAdapterService.createFilter('contains', '0002', null, null, containsArgs, null, null);
            var exactFilter = FilterAdapterService.createFilter('exact', '0003', null, null, exactArgs, null, null);

            //when
            var tree = FilterAdapterService.toTree([rangeFilter, containsFilter, exactFilter]);

            //then
            expect(tree).toEqual({filter: {
                and: [
                    {
                        and: [
                            {
                                range: {
                                    field: '0001',
                                    start: '1000',
                                    end: '2000'
                                }
                            },
                            {
                                contains: {
                                    field: '0002',
                                    value: 'Jimmy'
                                }
                            }
                        ]
                    },
                    {
                        eq: {
                            field: '0003',
                            value: 'Toto'
                        }
                    }
                ]
            }});
        }));
    });

    describe('adaptation from tree', function () {
        it('should return nothing when there is no filter tree', inject(function (FilterAdapterService) {
            //when
            var filters = FilterAdapterService.fromTree();

            //then
            expect(filters).toBeFalsy();
        }));

        it('should create single CONTAINS filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                contains: {
                    field: '0001',
                    value: 'Jimmy'
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('contains');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({phrase: 'Jimmy'});
        }));

        it('should create single EXACT filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                eq: {
                    field: '0001',
                    value: 'Jimmy'
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('exact');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({phrase: 'Jimmy'});
        }));

        it('should create single INSIDE_RANGE filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                range: {
                    field: '0001',
                    start: 1000,
                    end: 2000
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('inside_range');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({interval: [1000, 2000]});
        }));

        it('should create single INVALID_RECORDS filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                invalid: {
                    field: '0001'
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('invalid_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create single EMPTY_RECORDS filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                empty: {
                    field: '0001'
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('empty_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create single VALID_RECORDS filter from leaf', inject(function (FilterAdapterService) {
            //given
            var tree = {
                valid: {
                    field: '0001'
                }
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(1);

            var singleFilter = filters[0];
            expect(singleFilter.type).toBe('valid_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create multiple filters from tree', inject(function (FilterAdapterService) {
            //given
            var tree = {
                and: [
                    {
                        and: [
                            {
                                range: {
                                    field: '0001',
                                    start: '1000',
                                    end: '2000'
                                }
                            },
                            {
                                contains: {
                                    field: '0002',
                                    value: 'Jimmy'
                                }
                            }
                        ]
                    },
                    {
                        eq: {
                            field: '0003',
                            value: 'Toto'
                        }
                    }
                ]
            };

            //when
            var filters = FilterAdapterService.fromTree(tree);

            //then
            expect(filters.length).toBe(3);

            var rangeFilter = filters[0];
            expect(rangeFilter.type).toBe('inside_range');
            expect(rangeFilter.colId).toBe('0001');
            expect(rangeFilter.colName).toBe('lastname');
            expect(rangeFilter.editable).toBe(false);
            expect(rangeFilter.args).toEqual({interval: ['1000', '2000']});

            var containsFilter = filters[1];
            expect(containsFilter.type).toBe('contains');
            expect(containsFilter.colId).toBe('0002');
            expect(containsFilter.colName).toBe('birthdate');
            expect(containsFilter.editable).toBe(false);
            expect(containsFilter.args).toEqual({phrase: 'Jimmy'});

            var exactFilter = filters[2];
            expect(exactFilter.type).toBe('exact');
            expect(exactFilter.colId).toBe('0003');
            expect(exactFilter.colName).toBe('address');
            expect(exactFilter.editable).toBe(false);
            expect(exactFilter.args).toEqual({phrase: 'Toto'});
        }));
    });
});
