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
            const removeFilterFn = jasmine.createSpy('removeFilterFn');

            // when
            const filter = TqlFilterAdapterService.createFilter(CONTAINS, COL_ID, colName, editable, args, removeFilterFn);

            // then
            expect(filter.type).toBe(CONTAINS);
            expect(filter.colId).toBe(COL_ID);
            expect(filter.colName).toBe(colName);
            expect(filter.editable).toBe(editable);
            expect(filter.args).toBe(args);
            expect(filter.removeFilterFn).toBe(removeFilterFn);
        }));

        describe('get value', () => {
            it('should return value on CONTAINS filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');

                // when
                const filter = TqlFilterAdapterService.createFilter(CONTAINS, null, null, null, args, null);

                // then
                expect(filter.value).toEqual([{ value: 'Charles' }]);
            }));

            it('should return value on EXACT filter', inject((TqlFilterAdapterService) => {
                // given
                const args = getArgs('phrase', 'Charles');

                // when
                const filter = TqlFilterAdapterService.createFilter(EXACT, null, null, null, args, null);

                // then
                expect(filter.value).toEqual([{ value: 'Charles' }]);
            }));


            it('should return value on INVALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                //when
                const filter = TqlFilterAdapterService.createFilter(QUALITY, null, null, null, { invalid: true, empty: false }, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with invalid values' }]);
            }));

            it('should return value on QUALITY filter', inject((TqlFilterAdapterService) => {
                //when
                const filter = TqlFilterAdapterService.createFilter(QUALITY, null, null, null, { invalid: true, empty: true }, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with invalid or empty values' }]);
            }));

            it('should return value on EMPTY_RECORDS filter', inject((TqlFilterAdapterService) => {
                //when
                const filter = TqlFilterAdapterService.createFilter(QUALITY, null, null, null, { invalid: false, empty: true }, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: 'rows with empty values',
                        isEmpty: true,
                        value: '',
                    },
                ]);
            }));

            it('should return value on VALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                //when
                const filter = TqlFilterAdapterService.createFilter(QUALITY, null, null, null, { valid: true }, null);

                //then
                expect(filter.value).toEqual([{ label: 'rows with valid values' }]);
            }));

            it('should return value on INSIDE_RANGE filter', inject((TqlFilterAdapterService) => {
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
                const filter = TqlFilterAdapterService.createFilter(INSIDE_RANGE, null, null, null, args, null);

                //then
                expect(filter.value).toEqual([
                    {
                        label: '[1,000 .. 2,000[',
                        value: [1000, 2000],
                    },
                ]);
            }));

            it('should return value on MATCHES filter', inject((TqlFilterAdapterService) => {
                //given
                const args = {
                    patterns: [
                        {
                            value: 'Aa9',
                        },
                    ],
                };

                //when
                const filter = TqlFilterAdapterService.createFilter(MATCHES, null, null, null, args, null);

                //then
                expect(filter.value).toEqual([{ value: 'Aa9' }]);
            }));
        });
    });
});
