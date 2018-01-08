/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter state service', function () {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    describe('toogle', function () {
        it('should enable filter', inject(function (filterState, FilterStateService) {
            //when
            FilterStateService.enableFilters();

            //then
            expect(filterState.enabled).toBe(true);
        }));

        it('should disable filter', inject(function (filterState, FilterStateService) {
            //when
            FilterStateService.disableFilters();

            //then
            expect(filterState.enabled).toBe(false);
        }));
    });

    describe('reset', function () {
        it('should reset grid filter', inject(function (filterState, FilterStateService) {
            //given
            var originalFilters = [{}];
            filterState.gridFilters = originalFilters;

            //when
            FilterStateService.reset();

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalFilters);
            expect(gridFilters.length).toBe(0);
        }));
    });

    describe('grid filters', function () {
        it('should add a new filter', inject(function (filterState, FilterStateService) {
            //given
            var originalGridFilters = [];
            filterState.gridFilters = originalGridFilters;
            filterState.applyTransformationOnFilters = false;

            var newFilter = { colId: '0001', args: { value: 'tata' } };

            //when
            FilterStateService.addGridFilter(newFilter);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(1);
            expect(gridFilters[0]).toBe(newFilter);
            expect(filterState.applyTransformationOnFilters).toBe(true);
        }));

        it('should add filter to existing filters', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = { colId: '0001', args: { value: 'toto' } };
            var filter2 = { colId: '0004', args: { value: 'toto' } };
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;
            filterState.applyTransformationOnFilters = true;

            var newFilter = { colId: '0001', args: { value: 'tata' } };

            //when
            FilterStateService.addGridFilter(newFilter);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(3);
            expect(gridFilters[0]).toBe(filter1);
            expect(gridFilters[1]).toBe(filter2);
            expect(gridFilters[2]).toBe(newFilter);
            expect(filterState.applyTransformationOnFilters).toBe(true);
        }));

        it('should update filter', inject(function (filterState, FilterStateService) {
            //given
            var oldFilter = { colId: '0001', args: { value: 'toto' } };
            var otherFilter = { colId: '0004', args: { value: 'toto' } };
            var originalGridFilters = [oldFilter, otherFilter];
            filterState.gridFilters = originalGridFilters;

            var newFilter = { colId: '0001', args: { value: 'tata' } };

            //when
            FilterStateService.updateGridFilter(oldFilter, newFilter);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(2);
            expect(gridFilters[0]).toBe(newFilter);
            expect(gridFilters[1]).toBe(otherFilter);
        }));

        it('should remove not the last filter', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = { colId: '0001', args: { value: 'toto' } };
            var filter2 = { colId: '0004', args: { value: 'toto' } };
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;
            filterState.applyTransformationOnFilters = true;

            //when
            FilterStateService.removeGridFilter(filter1);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(1);
            expect(gridFilters[0]).toBe(filter2);
            expect(filterState.applyTransformationOnFilters).toBe(true);
        }));

        it('should remove the last filter', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = { colId: '0001', args: { value: 'toto' } };
            var originalGridFilters = [filter1];
            filterState.gridFilters = originalGridFilters;
            filterState.applyTransformationOnFilters = true;

            //when
            FilterStateService.removeGridFilter(filter1);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(0);
            expect(filterState.applyTransformationOnFilters).toBe(false);
        }));

        it('should remove all filters', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = { colId: '0001', args: { value: 'toto' } };
            var filter2 = { colId: '0004', args: { value: 'toto' } };
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;
            filterState.applyTransformationOnFilters = true;

            //when
            FilterStateService.removeAllGridFilters();

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(0);
            expect(filterState.applyTransformationOnFilters).toBe(false);
        }));
    });
});
