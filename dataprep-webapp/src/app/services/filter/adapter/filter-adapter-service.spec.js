/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import i18n from './../../../../i18n/en.json';

describe('Filter Adapter Service', () => {

    const columns = [
        { id: '0000', name: 'firstname' },
        { id: '0001', name: 'lastname' },
        { id: '0002', name: 'birthdate' },
        { id: '0003', name: 'address' },
        { id: '0004', name: 'gender' },
    ];

    beforeEach(angular.mock.module('data-prep.services.filter-adapter'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

    describe('create filter', () => {
        it('should create filter', inject((FilterAdapterService) => {
            //given
            const type = 'contains';
            const colId = '0001';
            const colName = 'firstname';
            const editable = true;
            const args = {};
            const filterFn = jasmine.createSpy('filterFn');
            const removeFilterFn = jasmine.createSpy('removeFilterFn');

            //when
            const filter = FilterAdapterService.createFilter(type, colId, colName, editable, args, filterFn, removeFilterFn);

            //then
            expect(filter.type).toBe(type);
            expect(filter.colId).toBe(colId);
            expect(filter.colName).toBe(colName);
            expect(filter.editable).toBe(editable);
            expect(filter.args).toBe(args);
            expect(filter.filterFn).toBe(filterFn);
            expect(filter.removeFilterFn).toBe(removeFilterFn);
        }));

        describe('get value', () => {
            it('should return value on CONTAINS filter', inject((FilterAdapterService) => {
                //given
                const type = 'contains';
                const args = {
                    phrase: [
                        {
                            value: 'Jimmy',
                        },
                    ],
                };

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        value: 'Jimmy',
                    },
                ]);
            }));

            it('should return value on EXACT filter', inject((FilterAdapterService) => {
                //given
                const type = 'exact';
                const args = {
                    phrase: [
                        {
                            value: 'Jimmy',
                        },
                    ],
                };

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        value: 'Jimmy',
                    },
                ]);
            }));

            it('should return value on INVALID_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'invalid_records';

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with invalid values',
                    },
                ]);
            }));

            it('should return value on QUALITY filter', inject((FilterAdapterService) => {
                //given
                const type = 'quality';

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, { invalid: true, empty: true }, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with invalid or empty values',
                    },
                ]);
            }));

            it('should return value on EMPTY_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'empty_records';

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with empty values',
                        isEmpty: true,
                    },
                ]);
            }));

            it('should return value on VALID_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'valid_records';

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with valid values',
                    },
                ]);
            }));

            it('should return value on INSIDE_RANGE filter', inject((FilterAdapterService) => {
                //given
                const type = 'inside_range';
                const args = {
                    intervals: [
                        {
                            label: '[1,000 .. 2,000[',
                            value: [1000, 2000],
                        },
                    ],
                    type: 'integer',
                };

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ]);
            }));

            it('should return value on MATCHES filter', inject((FilterAdapterService) => {
                //given
                const type = 'matches';
                const args = {
                    patterns: [
                        {
                            value: 'Aa9',
                        },
                    ],
                };

                //when
                const filter = FilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        value: 'Aa9',
                    },
                ]);
            }));
        });

        describe('to tree', () => {
            it('should return tree corresponding to CONTAINS filter', inject((FilterAdapterService) => {
                //given
                const type = 'contains';
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Jimmy',
                        },
                    ],
                };

                const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    contains: {
                        field: '0001',
                        value: 'Jimmy',
                    },
                });
            }));

            it('should return tree corresponding to EXACT filter', inject((FilterAdapterService) => {
                //given
                const type = 'exact';
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Jimmy',
                        },
                    ],
                };

                const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    eq: {
                        field: '0001',
                        value: 'Jimmy',
                    },
                });
            }));

            it('should return tree corresponding to EXACT multi-valued filter', inject((FilterAdapterService) => {
                //given
                const type = 'exact';
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Jimmy',
                        },
                        {
                            value: 'François',
                        },
                        {
                            value: 'Vincent',
                        },
                    ],
                };

                const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    or: [
                        {
                            or: [
                                {
                                    eq: {
                                        field: '0001',
                                        value: 'Jimmy',
                                    },
                                },
                                {
                                    eq: {
                                        field: '0001',
                                        value: 'François',
                                    },
                                },
                            ],
                        },
                        {
                            eq: {
                                field: '0001',
                                value: 'Vincent',
                            },
                        },
                    ],
                });
            }));

            it('should return tree corresponding to QUALITY filter', inject((FilterAdapterService) => {
                //given
                const type = 'quality';

                const filter = FilterAdapterService.createFilter(type, null, null, null, { invalid: true, empty: true }, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    or: [{
                        invalid: {
                            field: null,
                        },
                    }, {
                        empty: {
                            field: null,
                        },
                    }],
                });
            }));

            it('should return tree corresponding to INVALID_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'invalid_records';
                const colId = '0001';

                const filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    invalid: {
                        field: '0001',
                    },
                });
            }));

            it('should return tree corresponding to EMPTY_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'empty_records';
                const colId = '0001';

                const filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    empty: {
                        field: '0001',
                    },
                });
            }));

            it('should return tree corresponding to VALID_RECORDS filter', inject((FilterAdapterService) => {
                //given
                const type = 'valid_records';
                const colId = '0001';

                const filter = FilterAdapterService.createFilter(type, colId, null, null, null, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    valid: {
                        field: '0001',
                    },
                });
            }));

            it('should return tree corresponding to INSIDE_RANGE filter', inject((FilterAdapterService) => {
                //given
                const type = 'inside_range';
                const colId = '0001';
                const args = {
                    intervals: [
                        {
                            label: '[1000 .. 2000[',
                            value: [1000, 2000],
                        },
                    ],
                    type: 'integer',
                };

                const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    range: {
                        field: '0001',
                        start: 1000,
                        end: 2000,
                        type: 'integer',
                        label: '[1000 .. 2000[',
                    },
                });
            }));

            it('should return tree corresponding to MATCHES filter', inject((FilterAdapterService) => {
                //given
                const type = 'matches';
                const colId = '0001';
                const args = {
                    patterns: [
                        {
                            value: 'Aa9',
                        },
                    ],
                };

                const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tree = filter.toTree();

                //then
                expect(tree).toEqual({
                    matches: {
                        field: '0001',
                        value: 'Aa9',
                    },
                });
            }));
        });
    });

    describe('adaptation to tree', () => {
        it('should return empty object when there is no filter', inject((FilterAdapterService) => {
            //when
            const tree = FilterAdapterService.toTree([]);

            //then
            expect(tree).toEqual({});
        }));

        it('should create single filter tree', inject((FilterAdapterService) => {
            //given
            const type = 'inside_range';
            const colId = '0001';
            const args = {
                intervals: [
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ],
                type: 'integer',
            };

            const filter = FilterAdapterService.createFilter(type, colId, null, null, args, null, null);

            //when
            const tree = FilterAdapterService.toTree([filter]);

            //then
            expect(tree).toEqual({
                filter: {
                    range: {
                        field: '0001',
                        start: 1000,
                        end: 2000,
                        type: 'integer',
                        label: '[1,000 .. 2,000[',
                    },
                },
            });
        }));

        it('should create multiple filters tree', inject((FilterAdapterService) => {
            //given
            const rangeArgs = {
                intervals: [
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ],
                type: 'integer',
            };
            const containsArgs = {
                phrase: [
                    {
                        value: 'Jimmy',
                    },
                ],
            };
            const exactArgs = {
                phrase: [
                    {
                        value: 'Jimmy',
                    },
                    {
                        value: 'François',
                    },
                    {
                        value: 'Vincent',
                    },
                ],
            };
            const dateRangeOffset = new Date(-631152000000).getTimezoneOffset() * 60 * 1000;
            const dateRangeArgs = {
                intervals: [
                    {
                        label: '[1950, 1960[',
                        value: [
                            -631152000000 + dateRangeOffset,
                            -315619200000 + dateRangeOffset,
                        ],
                    },
                ],
                type: 'date',
            };

            const rangeFilter = FilterAdapterService.createFilter('inside_range', '0001', null, null, rangeArgs, null, null);
            const containsFilter = FilterAdapterService.createFilter('contains', '0002', null, null, containsArgs, null, null);
            const exactFilter = FilterAdapterService.createFilter('exact', '0003', null, null, exactArgs, null, null);
            const dateRangeFilter = FilterAdapterService.createFilter('inside_range', '0004', null, null, dateRangeArgs, null, null);

            //when
            const tree = FilterAdapterService.toTree([rangeFilter, containsFilter, exactFilter, dateRangeFilter]);

            //then
            expect(tree).toEqual({
                filter: {
                    and: [
                        {
                            and: [
                                {
                                    and: [
                                        {
                                            range: {
                                                field: '0001',
                                                start: 1000,
                                                end: 2000,
                                                type: 'integer',
                                                label: '[1,000 .. 2,000[',
                                            },
                                        },
                                        {
                                            contains: {
                                                field: '0002',
                                                value: 'Jimmy',
                                            },
                                        },
                                    ],
                                },
                                {
                                    or: [
                                        {
                                            or: [
                                                {
                                                    eq: {
                                                        field: '0003',
                                                        value: 'Jimmy',
                                                    },
                                                },
                                                {
                                                    eq: {
                                                        field: '0003',
                                                        value: 'François',
                                                    },
                                                },
                                            ],
                                        },
                                        {
                                            eq: {
                                                field: '0003',
                                                value: 'Vincent',
                                            },
                                        },
                                    ],
                                },
                            ],
                        },
                        {
                            range: {
                                field: '0004',
                                start: -631152000000, //timestamp without timezone offset to have UTC date
                                end: -315619200000,  //timestamp without timezone offset to have UTC date
                                type: 'date',
                                label: '[1950, 1960[',
                            },
                        },
                    ],
                },
            });
        }));
    });

    describe('adaptation from tree', () => {
        it('should return nothing when there is no filter tree', inject((FilterAdapterService) => {
            //when
            const filters = FilterAdapterService.fromTree();

            //then
            expect(filters).toBeFalsy();
        }));

        it('should create single CONTAINS filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                contains: {
                    field: '0001',
                    value: 'Jimmy',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('contains');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({
                phrase: [
                    {
                        value: 'Jimmy',
                    },
                ],
            });
        }));

        it('should create single EXACT filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                eq: {
                    field: '0001',
                    value: 'Jimmy',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('exact');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({
                phrase: [
                    {
                        value: 'Jimmy',
                    },
                ],
            });
        }));

        it('should create single number INSIDE_RANGE filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                range: {
                    field: '0001',
                    start: 1000,
                    end: 2000,
                    label: '[1,000 .. 2,000[',
                    type: 'integer',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('inside_range');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({
                intervals: [
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ],
                type: 'integer',
            });
        }));

        it('should create single date INSIDE_RANGE filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                range: {
                    field: '0001',
                    start: -631152000000, // UTC 1950-01-01
                    end: -315619200000, // UTC 1960-01-01
                    type: 'date',
                    label: '[1950, 1960[',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('inside_range');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({
                intervals: [
                    {
                        label: '[1950, 1960[',
                        value: [
                            //timestamps are in the client timezone
                            new Date(1950, 0, 1).getTime(),
                            new Date(1960, 0, 1).getTime(),
                        ],
                    },
                ],
                type: 'date',
            });
        }));

        it('should create single QUALITY filter from OR subtree', inject((FilterAdapterService) => {
            //given
            const tree = {
                or: [{
                    invalid: {
                    },
                }, {
                    empty: {
                    },
                }],
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('quality');
            expect(singleFilter.colId).toBe(undefined);
            expect(singleFilter.colName).toBe(undefined);
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({ invalid: true, empty: true });
        }));

        it('should create single INVALID_RECORDS filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                invalid: {
                    field: '0001',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('invalid_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create single EMPTY_RECORDS filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                empty: {
                    field: '0001',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('empty_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create single VALID_RECORDS filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                valid: {
                    field: '0001',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('valid_records');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toBeFalsy();
        }));

        it('should create single MATCHES filter from leaf', inject((FilterAdapterService) => {
            //given
            const tree = {
                matches: {
                    field: '0001',
                    value: 'Aa9',
                },
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(1);

            const singleFilter = filters[0];
            expect(singleFilter.type).toBe('matches');
            expect(singleFilter.colId).toBe('0001');
            expect(singleFilter.colName).toBe('lastname');
            expect(singleFilter.editable).toBe(false);
            expect(singleFilter.args).toEqual({
                patterns: [
                    {
                        value: 'Aa9',
                    },
                ],
            });
        }));

        it('should create multiple filters from tree', inject((FilterAdapterService) => {
            //given
            const tree = {
                and: [
                    {
                        and: [
                            {
                                and: [
                                    {
                                        range: {
                                            field: '0001',
                                            start: 1000,
                                            end: 2000,
                                            label: '[1,000 .. 2,000[',
                                            type: 'integer',
                                        },
                                    },
                                    {
                                        contains: {
                                            field: '0002',
                                            value: 'Jimmy',
                                        },
                                    },
                                ],
                            },
                            {
                                eq: {
                                    field: '0003',
                                    value: 'Toto',
                                },
                            },
                        ],
                    },
                    {
                        matches: {
                            field: '0004',
                            value: 'Aa9',
                        },
                    },
                ],
            };

            //when
            const filters = FilterAdapterService.fromTree(tree, columns);

            //then
            expect(filters.length).toBe(4);

            const rangeFilter = filters[0];
            expect(rangeFilter.type).toBe('inside_range');
            expect(rangeFilter.colId).toBe('0001');
            expect(rangeFilter.colName).toBe('lastname');
            expect(rangeFilter.editable).toBe(false);
            expect(rangeFilter.args).toEqual({
                intervals: [
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ],
                type: 'integer',
            });

            const containsFilter = filters[1];
            expect(containsFilter.type).toBe('contains');
            expect(containsFilter.colId).toBe('0002');
            expect(containsFilter.colName).toBe('birthdate');
            expect(containsFilter.editable).toBe(false);
            expect(containsFilter.args).toEqual({
                phrase: [
                    {
                        value: 'Jimmy',
                    },
                ],
            });

            const exactFilter = filters[2];
            expect(exactFilter.type).toBe('exact');
            expect(exactFilter.colId).toBe('0003');
            expect(exactFilter.colName).toBe('address');
            expect(exactFilter.editable).toBe(false);
            expect(exactFilter.args).toEqual({
                phrase: [
                    {
                        value: 'Toto',
                    },
                ],
            });

            const matchesFilter = filters[3];
            expect(matchesFilter.type).toBe('matches');
            expect(matchesFilter.colId).toBe('0004');
            expect(matchesFilter.colName).toBe('gender');
            expect(matchesFilter.editable).toBe(false);
            expect(matchesFilter.args).toEqual({
                patterns: [
                    {
                        value: 'Aa9',
                    },
                ],
            });
        }));
    });
});
