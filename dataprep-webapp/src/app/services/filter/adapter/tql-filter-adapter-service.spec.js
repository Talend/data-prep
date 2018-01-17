/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import {
    CONTAINS,
    EXACT,
    INVALID_RECORDS,
    VALID_RECORDS,
    EMPTY_RECORDS,
    INSIDE_RANGE,
    MATCHES,
    QUALITY,
    EMPTY,
} from './tql-filter-adapter-service';

import i18n from './../../../../i18n/en.json';

describe('TQL Filter Adapter Service', () => {
    const COL_ID = '0001';
    const getArgs = (key, ...args) => ({ [key]: args.map(a => ({ value: a })) });

    beforeEach(angular.mock.module('data-prep.services.filter-adapter'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

    describe('create filter', () => {
        it('should create filter', inject((TqlFilterAdapterService) => {
            // given
            const colName = 'firstname';
            const editable = true;
            const args = {};
            const filterFn = jasmine.createSpy('filterFn');
            const removeFilterFn = jasmine.createSpy('removeFilterFn');

            // when
            const filter = TqlFilterAdapterService.createFilter(CONTAINS, COL_ID, colName, editable, args, filterFn, removeFilterFn);

            // then
            expect(filter.type).toBe(CONTAINS);
            expect(filter.colId).toBe(COL_ID);
            expect(filter.colName).toBe(colName);
            expect(filter.editable).toBe(editable);
            expect(filter.args).toBe(args);
            expect(filter.filterFn).toBe(filterFn);
            expect(filter.removeFilterFn).toBe(removeFilterFn);
        }));

        describe('get value', () => {
            it('should return value on CONTAINS filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');

                // when
                const filter = TqlFilterAdapterService.createFilter(CONTAINS, null, null, null, args, null, null);

                // then
                expect(filter.value).toEqual([{ value: 'Charles' }]);
            }));

            it('should return value on EXACT filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');

                // when
                const filter = TqlFilterAdapterService.createFilter(EXACT, null, null, null, args, null, null);

                // then
                expect(filter.value).toEqual([{ value: 'Charles' }]);
            }));


            it('should return value on INVALID_RECORDS filter', inject((FilterAdapterService) => {
                //when
                const filter = FilterAdapterService.createFilter(INVALID_RECORDS, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with invalid values' }]);
            }));

            it('should return value on QUALITY filter', inject((FilterAdapterService) => {
                //when
                const filter = FilterAdapterService.createFilter(QUALITY, null, null, null, { invalid: true, empty: true }, null, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with invalid or empty values' }]);
            }));

            it('should return value on EMPTY_RECORDS filter', inject((FilterAdapterService) => {
                //when
                const filter = FilterAdapterService.createFilter(EMPTY_RECORDS, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with empty values',
                        isEmpty: true,
                    },
                ]);
            }));

            it('should return value on VALID_RECORDS filter', inject((FilterAdapterService) => {
                //when
                const filter = FilterAdapterService.createFilter(VALID_RECORDS, null, null, null, null, null, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with valid values' }]);
            }));

            it('should return value on INSIDE_RANGE filter', inject((FilterAdapterService) => {
                //given
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
                const filter = FilterAdapterService.createFilter(INSIDE_RANGE, null, null, null, args, null, null);

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
                const args = {
                    patterns: [
                        {
                            value: 'Aa9',
                        },
                    ],
                };

                //when
                const filter = FilterAdapterService.createFilter(MATCHES, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([{ value: 'Aa9' }]);
            }));




        });

        describe('to TQL', () => {
            it('should return tql corresponding to CONTAINS filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');
                const filter = TqlFilterAdapterService.createFilter(CONTAINS, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual("(0001 contains 'Charles')");
            }));

            it('should return tql corresponding to EXACT filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');
                const filter = TqlFilterAdapterService.createFilter(EXACT, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual("(0001 = 'Charles')");
            }));

            it('should return tree corresponding to EXACT multi-valued filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles', 'Nico', 'Fabien');
                const filter = TqlFilterAdapterService.createFilter(EXACT, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual("(((0001 = 'Charles') or (0001 = 'Nico')) or (0001 = 'Fabien'))");
            }));

            it('should return tree corresponding to INVALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                // given
                const filter = TqlFilterAdapterService.createFilter(INVALID_RECORDS, COL_ID, null, null, null, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual('(0001 is invalid)');
            }));

            it('should return tree corresponding to VALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                // given
                const filter = TqlFilterAdapterService.createFilter(VALID_RECORDS, COL_ID, null, null, null, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual('(0001 is valid)');
            }));

            it('should return tree corresponding to INSIDE_RANGE filter', inject((TqlFilterAdapterService) => {
                // given
                const args = {
                    intervals: [
                        {
                            label: '[1,000 .. 2,000[',
                            value: [1000, 2000],
                        },
                    ],
                    type: 'integer',
                };
                const filter = TqlFilterAdapterService.createFilter(INSIDE_RANGE, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual('(0001 >= 1000) and (0001 <= 2000)');
            }));

            it('should return tree corresponding to MATCHES filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('patterns', 'Aa9');
                const filter = TqlFilterAdapterService.createFilter(MATCHES, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual("(0001 complies to 'Aa9')");
            }));

            it('should handle empty value when operator has an operand', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', '');
                const filter = TqlFilterAdapterService.createFilter(EXACT, COL_ID, null, null, args, null, null);

                // when
                const tql = filter.toTQL();

                // then
                expect(tql).toEqual("(0001 is empty)");
            }));
        });
    });
});
