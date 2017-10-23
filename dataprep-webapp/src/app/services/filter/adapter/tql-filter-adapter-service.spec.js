/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('TQL Filter Adapter Service', () => {
    const COL_ID = '0001';
    function getPhrase(...args) {
        return {
            phrase: args.map(a => ({ value: a })),
        };
    }

    beforeEach(angular.mock.module('data-prep.services.filter-adapter'));

    describe('create filter', () => {
        it('should create filter', inject((TqlFilterAdapterService) => {
            //given
            const type = 'contains';
            const colName = 'firstname';
            const editable = true;
            const args = {};
            const filterFn = jasmine.createSpy('filterFn');
            const removeFilterFn = jasmine.createSpy('removeFilterFn');

            //when
            const filter = TqlFilterAdapterService.createFilter(type, COL_ID, colName, editable, args, filterFn, removeFilterFn);

            //then
            expect(filter.type).toBe(type);
            expect(filter.colId).toBe(COL_ID);
            expect(filter.colName).toBe(colName);
            expect(filter.editable).toBe(editable);
            expect(filter.args).toBe(args);
            expect(filter.filterFn).toBe(filterFn);
            expect(filter.removeFilterFn).toBe(removeFilterFn);
        }));

        describe('get value', () => {
            it('should return value on CONTAINS filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'contains';
                const args = getPhrase('Charles');

                //when
                const filter = TqlFilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        value: 'Charles',
                    },
                ]);
            }));

            it('should return value on EXACT filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const args = getPhrase('Charles');

                //when
                const filter = TqlFilterAdapterService.createFilter(type, null, null, null, args, null, null);

                //then
                expect(filter.value).toEqual([
                    {
                        value: 'Charles',
                    },
                ]);
            }));
        });

        describe('to TQL', () => {
            it('should return tql corresponding to CONTAINS filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'contains';
                const args = getPhrase('Charles');

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 contains 'Charles')");
            }));

            it('should return tql corresponding to EXACT filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const args = getPhrase('Charles');

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 = 'Charles')");
            }));

            it('should return tree corresponding to EXACT multi-valued filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const args = getPhrase('Charles', 'Nico', 'Fabien');

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(((0001 = 'Charles') or (0001 = 'Nico')) or (0001 = 'Fabien'))");
            }));

            it('should return tree corresponding to INVALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'invalid_records';

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, null, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual('(0001 is invalid)');
            }));

            it('should return tree corresponding to VALID_RECORDS filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'valid_records';

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, null, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual('(0001 is valid)');
            }));

            it('should return tree corresponding to INSIDE_RANGE filter', inject((TqlFilterAdapterService) => {
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
                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual('(0001 >= 1000) and (0001 <= 2000)');
            }));

            it('should return tree corresponding to MATCHES filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'matches';
                const args = {
                    patterns: [
                        {
                            value: 'Aa9',
                        },
                    ],
                };
                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 complies to 'Aa9')");
            }));

            it('should handle empty value when operator has an operand', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const args = getPhrase('');

                const filter = TqlFilterAdapterService.createFilter(type, COL_ID, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 is empty)");
            }));
        });
    });
});
