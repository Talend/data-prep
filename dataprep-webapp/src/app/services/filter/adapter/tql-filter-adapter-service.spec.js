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
	INSIDE_RANGE,
	MATCHES,
	MATCHES_WORDS,
	QUALITY,
} from './tql-filter-adapter-service';

import i18n from './../../../../i18n/en.json';

describe('TQL Filter Adapter Service', () => {
    const COL_ID = '0001';
    const getArgs = (key, ...args) => ({ [key]: args.map(a => ({ value: a })) });
	const columns = [{ id: '0000', name: 'id'}, { id: '0001', name: 'name'}];

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

            it('should return value on MATCHES_WORDS filter', inject((TqlFilterAdapterService) => {
                //given
                const args = {
                    patterns: [
                        {
                            value: '[alnum]',
                        },
                    ],
                };

                //when
                const filter = TqlFilterAdapterService.createFilter(MATCHES_WORDS, null, null, null, args, null);

                //then
                expect(filter.value).toEqual([{ value: '[alnum]' }]);
            }));
        });
    });

	describe('toTQL', () => {
		it('should return tql for CONTAINS filter', inject((TqlFilterAdapterService) => {
			// when
			const args = getArgs('phrase', 'Charles');
			const filter = TqlFilterAdapterService.createFilter(CONTAINS, '0000', 'id', null, args, null);
			// then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 contains 'Charles'))");
		}));

		it('should return tql for EXACT filter', inject((TqlFilterAdapterService) => {
			// when
			const args = getArgs('phrase', 'Charles');
			const filter = TqlFilterAdapterService.createFilter(EXACT, '0000', 'id', null, args, null);

			// then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 = 'Charles'))");
		}));


		it('should return tql for INVALID_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			//when
			const filter = TqlFilterAdapterService.createFilter(QUALITY, '0000', 'id', null, { invalid: true, empty: false }, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("(0000 is invalid)");
		}));

		it('should return tql for EMPTY_INVALID_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			//when
			const filter = TqlFilterAdapterService.createFilter(QUALITY, null, null, null, { invalid: true, empty: true }, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((* is empty) or (* is invalid))");
		}));

		it('should return tql for EMPTY_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			//when
			const filter = TqlFilterAdapterService.createFilter(QUALITY, '0000', 'id', null, { invalid: false, empty: true }, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("(0000 is empty)");
		}));

		it('should return tql for VALID_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			//when
			const filter = TqlFilterAdapterService.createFilter(QUALITY, '0000', 'id', null, { valid: true }, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("(0000 is valid)");
		}));

		it('should return tql for INSIDE_RANGE filter', inject((TqlFilterAdapterService) => {
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
			const filter = TqlFilterAdapterService.createFilter(INSIDE_RANGE, '0000', 'id', null, args, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 between [1000, 2000]))");
		}));

		it('should return tql for MATCHES filter', inject((TqlFilterAdapterService) => {
			//given
			const args = {
				patterns: [
					{
						value: 'Aa9',
					},
				],
			};

			//when
			const filter = TqlFilterAdapterService.createFilter(MATCHES, '0000', 'id', null, args, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 complies 'Aa9'))");
		}));

		it('should return tql for MATCHES_WORDS filter', inject((TqlFilterAdapterService) => {
			//given
			const args = {
				patterns: [
					{
						value: '[alnum]',
					},
				],
			};

			//when
			const filter = TqlFilterAdapterService.createFilter(MATCHES_WORDS, '0000', 'id', null, args, null);

			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 wordComplies '[alnum]'))");
		}));

		it('should return tql for OR filter', inject((TqlFilterAdapterService) => {
			//given
			const args = {
				patterns: [
					{
						value: 'Aa9',
					},
					{
						value: 'AAAAa9',
					},
				],
			};
			//when
			const filter = TqlFilterAdapterService.createFilter(MATCHES, '0000', 'id', null, args, null);
			//then
			expect(TqlFilterAdapterService.toTQL([filter])).toEqual("((0000 complies 'Aa9') or (0000 complies 'AAAAa9'))");
		}));

		it('should return tql for AND filter', inject((TqlFilterAdapterService) => {
			//given
			const args = {
				patterns: [
					{
						value: 'Aa9',
					},
				],
			};
			//when
			const filter1 = TqlFilterAdapterService.createFilter(MATCHES, '0000', 'id', null, args, null);
			const filter2 = TqlFilterAdapterService.createFilter(QUALITY, '0001', 'id', null, { invalid: false, empty: true }, null);
			//then
			expect(TqlFilterAdapterService.toTQL([filter1, filter2])).toEqual("((0000 complies 'Aa9')) and (0001 is empty)");
		}));

    });

	describe('fromTQL', () => {
		it('should return CONTAINS filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 contains 'Charles'))", columns)[0];
			// then
			expect(filter.type).toEqual(CONTAINS);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.phrase[0].value).toEqual('Charles');
		}));

		it('should return EXACT filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 = 'Ch\\\'arles'))", columns)[0];
			// then
			expect(filter.type).toEqual(EXACT);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.phrase[0].value).toEqual('Ch\'arles');
		}));

		it('should return INVALID_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("(0000 is invalid)", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('0000');
			expect(filter.args).toEqual({ invalid: true, empty: false });
		}));

		it('should return INVALID_RECORDS QUALITY filter with WILDCARD', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("(* is invalid)", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('*');
			expect(filter.args).toEqual({ invalid: true, empty: false });
		}));

		it('should return EMPTY_INVALID_RECORDS QUALITY filter with WILDCARD', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((* is empty) or (* is invalid))", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('*');
			expect(filter.args).toEqual({ invalid: true, empty: true });
		}));

		it('should return EMPTY_INVALID_RECORDS QUALITY filter with column id', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 is empty) or (0000 is invalid))", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('0000');
			expect(filter.args).toEqual({ invalid: true, empty: true });
		}));

		it('should return EMPTY_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("(0000 is empty)", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('0000');
			expect(filter.args).toEqual({ invalid: false, empty: true });
		}));

		it('should return VALID_RECORDS QUALITY filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("(0000 is valid)", columns)[0];
			// then
			expect(filter.type).toEqual(QUALITY);
			expect(filter.colId).toEqual('0000');
			expect(filter.args).toEqual({ valid: true });
		}));

		it('should return INSIDE_RANGE filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 between [1000, 2000]))", columns)[0];
			// then
			expect(filter.type).toEqual(INSIDE_RANGE);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.intervals[0].value).toEqual([1000, 2000]);
		}));

		it('should return MATCHES filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 complies 'Aa9\\\''))", columns)[0];
			// then
			expect(filter.type).toEqual(MATCHES);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.patterns[0].value).toEqual('Aa9\'');
		}));

		it('should return MATCHES_WORDS filter', inject((TqlFilterAdapterService) => {
			// when
			const filter = TqlFilterAdapterService.fromTQL("((0000 wordComplies '\\\'[alnum]\\\''))", columns)[0];
			// then
			expect(filter.type).toEqual(MATCHES_WORDS);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.patterns[0].value).toEqual('\'[alnum]\'');
		}));

		it('should return OR filter', inject((TqlFilterAdapterService) => {
			//when
			const filter = TqlFilterAdapterService.fromTQL("((0000 complies 'Aa9') or (0000 complies 'AAAAa9'))", columns)[0];

			//then
			expect(filter.type).toEqual(MATCHES);
			expect(filter.colId).toEqual('0000');
			expect(filter.args.patterns[0].value).toEqual('Aa9');
			expect(filter.args.patterns[1].value).toEqual('AAAAa9');
		}));

		it('should return AND filter', inject((TqlFilterAdapterService) => {
			//when
			const filters = TqlFilterAdapterService.fromTQL("((0000 complies 'Aa9')) and (0001 is empty)", columns);
			//then
			expect(filters[0].type).toEqual(MATCHES);
			expect(filters[0].colId).toEqual('0000');
			expect(filters[0].args.patterns[0].value).toEqual('Aa9');

			expect(filters[1].type).toEqual(QUALITY);
			expect(filters[1].colId).toEqual('0001');
			expect(filters[1].args).toEqual({ invalid: false, empty: true });
		}));

		it('should return AND filter', inject((TqlFilterAdapterService) => {
			//when
			const filters = TqlFilterAdapterService.fromTQL("(0001 is empty) and ((0000 = 'Charles'))", columns);
			//then
			expect(filters[0].type).toEqual(QUALITY);
			expect(filters[0].colId).toEqual('0001');
			expect(filters[0].args).toEqual({ invalid: false, empty: true });

			expect(filters[1].type).toEqual(EXACT);
			expect(filters[1].colId).toEqual('0000');
			expect(filters[1].args.phrase[0].value).toEqual('Charles');
		}));
	});
});
