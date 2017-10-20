/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('TQL Filter Adapter Service', () => {

    beforeEach(angular.mock.module('data-prep.services.filter-adapter'));

    describe('create filter', () => {
        it('should create filter', inject((TqlFilterAdapterService) => {
            //given
            const type = 'contains';
            const colId = '0001';
            const colName = 'firstname';
            const editable = true;
            const args = {};
            const filterFn = jasmine.createSpy('filterFn');
            const removeFilterFn = jasmine.createSpy('removeFilterFn');

            //when
            const filter = TqlFilterAdapterService.createFilter(type, colId, colName, editable, args, filterFn, removeFilterFn);

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
            it('should return value on CONTAINS filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'contains';
                const args = {
                    phrase: [
                        {
                            value: 'Charles',
                        },
                    ],
                };

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
                const args = {
                    phrase: [
                        {
                            value: 'Charles',
                        },
                    ],
                };

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
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Charles',
                        },
                    ],
                };

                const filter = TqlFilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 contains 'Charles')");
            }));

            it('should return tql corresponding to EXACT filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Charles',
                        },
                    ],
                };

                const filter = TqlFilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(0001 = 'Charles')");
            }));

            it('should return tree corresponding to EXACT multi-valued filter', inject((TqlFilterAdapterService) => {
                //given
                const type = 'exact';
                const colId = '0001';
                const args = {
                    phrase: [
                        {
                            value: 'Charles',
                        },
                        {
                            value: 'Nico',
                        },
                        {
                            value: 'Fabien',
                        },
                    ],
                };

                const filter = TqlFilterAdapterService.createFilter(type, colId, null, null, args, null, null);

                //when
                const tql = filter.toTQL();

                //then
                expect(tql).toEqual("(((0001 = 'Charles') or (0001 = 'Nico')) or (0001 = 'Fabien'))");
            }));
        });
    });
});
