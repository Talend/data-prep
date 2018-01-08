/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('ColumnProfile controller', () => {
    'use strict';

    let createController;
    let scope;
    let stateMock;
    const removeFilterFn = () => {
    };

    beforeEach(angular.mock.module('data-prep.column-profile', ($provide) => {
        stateMock = {
            playground: {
                grid: {},
                statistics: {},
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();

        createController = () => {
            return $controller('ColumnProfileCtrl', {
                $scope: scope,
            });
        };
    }));

    describe('filter', () => {
        beforeEach(inject(($q, FilterManagerService, StatisticsService) => {
            spyOn(FilterManagerService, 'addFilterAndDigest').and.returnValue();
            spyOn(StatisticsService, 'getRangeFilterRemoveFn').and.returnValue(removeFilterFn);
        }));

        it('should add a "exact" filter', inject((FilterManagerService) => {
            //given
            const ctrl = createController();
            const obj = { data: 'Ulysse', occurrences: 5 };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
            }];

            //when
            ctrl.addBarchartFilter(obj);

            //then
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('exact', '0001', 'firstname', {
                phrase: [
                    {
                        value: 'Ulysse',
                    },
                ],
                caseSensitive: true,
            }, null, null);
        }));

        it('should add a number "range" filter with closed intervals', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const interval = {
                min: 5,
                max: 15,
                isMaxReached: true,
            };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
                type: 'integer',
            }];

            //when
            ctrl.addRangeFilter(interval);

            //then
            expect(StatisticsService.getRangeFilterRemoveFn).toHaveBeenCalled();
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(
                'inside_range',
                '0001',
                'firstname',
                {
                    intervals: [
                        {
                            label: '[5 .. 15]',
                            value: [5, 15],
                            isMaxReached: true,
                        },
                    ],
                    type: 'integer',
                },
                removeFilterFn,
                null);
        }));

        it('should add a number "range" filter with mixed intervals', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const interval = {
                min: 5,
                max: 15,
                isMaxReached: false,
            };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
                type: 'integer',
            }];

            //when
            ctrl.addRangeFilter(interval);

            //then
            expect(StatisticsService.getRangeFilterRemoveFn).toHaveBeenCalled();
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(
                'inside_range',
                '0001',
                'firstname',
                {
                    intervals: [
                        {
                            label: '[5 .. 15[',
                            value: [5, 15],
                            isMaxReached: false,
                        },
                    ],
                    type: 'integer',
                },
                removeFilterFn,
                null);
        }));

        it('should add a number "range" filter with only one value', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const interval = {
                min: 15,
                max: 15,
                isMaxReached: true,
            };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
                type: 'integer',
            }];

            //when
            ctrl.addRangeFilter(interval);

            //then
            expect(StatisticsService.getRangeFilterRemoveFn).toHaveBeenCalled();
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(
                'inside_range',
                '0001',
                'firstname',
                {
                    intervals: [
                        {
                            label: '[15]',
                            value: [15, 15],
                            isMaxReached: true,
                        },
                    ],
                    type: 'integer',
                },
                removeFilterFn,
                null);
        }));

        it('should add a date "range" filter', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const interval = {
                min: '01-06-2015',
                max: '30-06-2015',
                label: 'Jun 2015',
                isMaxReached: undefined,
            };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
                type: 'date',
            }];

            //when
            ctrl.addRangeFilter(interval);

            //then
            expect(StatisticsService.getRangeFilterRemoveFn).toHaveBeenCalled();
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(
                'inside_range',
                '0001',
                'firstname',
                {
                    intervals: [
                        {
                            label: 'Jun 2015',
                            value: ['01-06-2015', '30-06-2015'],
                            isMaxReached: undefined,
                        },],
                    type: 'date',
                },
                removeFilterFn,
                null);
        }));

        it('should add a date "range" filter from time', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const minDateTime = new Date(2016, 0, 1).getTime();
            const maxDateTime = new Date(2016, 11, 1).getTime();
            const interval = {
                min: minDateTime,
                max: maxDateTime,
                label: undefined,
                isMaxReached: undefined,
            };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'CreationDate',
                type: 'date',
            }];

            //when
            ctrl.addRangeFilter(interval);

            //then
            expect(StatisticsService.getRangeFilterRemoveFn).toHaveBeenCalled();
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith(
                'inside_range',
                '0001',
                'CreationDate',
                {
                    intervals: [
                        {
                            label: '[2016-01-01 .. 2016-12-01[',
                            value: [minDateTime, maxDateTime],
                            isMaxReached: undefined,
                        },
                    ],
                    type: 'date',
                },
                removeFilterFn,
                null);
        }));

        it('should add a "empty_records" filter from exact_filter on barchart click callback', inject((StatisticsService, FilterManagerService) => {
            //given
            const ctrl = createController();
            const obj = { data: '', occurrences: 5 };

            stateMock.playground.grid.selectedColumns = [{
                id: '0001',
                name: 'firstname',
            }];

            //when
            ctrl.addBarchartFilter(obj);

            //then
            expect(FilterManagerService.addFilterAndDigest).toHaveBeenCalledWith('empty_records', '0001', 'firstname', null, null, null);
        }));
    });

    describe('change aggregation', () => {
        it('should process new aggregation', inject((StatisticsService) => {
            //given
            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            const ctrl = createController();

            const column = { id: '0001' };
            const aggregation = { name: 'MAX' };

            //when
            ctrl.changeAggregation(column, aggregation);

            //then
            expect(StatisticsService.processAggregation).toHaveBeenCalledWith(column, aggregation);
        }));

        it('should switch to classical chart', inject((StatisticsService) => {
            //given
            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            spyOn(StatisticsService, 'processClassicChart').and.returnValue();
            const ctrl = createController();

            //when
            ctrl.changeAggregation();

            //then
            expect(StatisticsService.processAggregation).not.toHaveBeenCalled();
            expect(StatisticsService.processClassicChart).toHaveBeenCalled();
        }));
    });
});
