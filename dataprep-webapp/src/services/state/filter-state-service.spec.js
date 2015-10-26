describe('Filter state service', function () {
    'use strict';

    beforeEach(module('data-prep.services.state'));

    describe('reset', function() {
        it('should reset grid filter', inject(function(filterState, FilterStateService) {
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

    describe('grid filters', function() {

        it('should add filter', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = {colId: '0001', args: {value: 'toto'}};
            var filter2 = {colId: '0004', args: {value: 'toto'}};
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;


            var newFilter = {colId: '0001', args: {value: 'tata'}};

            //when
            FilterStateService.addGridFilter(newFilter);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(3);
            expect(gridFilters[0]).toBe(filter1);
            expect(gridFilters[1]).toBe(filter2);
            expect(gridFilters[2]).toBe(newFilter);
        }));

        it('should update filter', inject(function (filterState, FilterStateService) {
            //given
            var oldFilter = {colId: '0001', args: {value: 'toto'}};
            var otherFilter = {colId: '0004', args: {value: 'toto'}};
            var originalGridFilters = [oldFilter, otherFilter];
            filterState.gridFilters = originalGridFilters;


            var newFilter = {colId: '0001', args: {value: 'tata'}};

            //when
            FilterStateService.updateGridFilter(oldFilter, newFilter);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(2);
            expect(gridFilters[0]).toBe(newFilter);
            expect(gridFilters[1]).toBe(otherFilter);
        }));

        it('should remove filter', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = {colId: '0001', args: {value: 'toto'}};
            var filter2 = {colId: '0004', args: {value: 'toto'}};
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;

            //when
            FilterStateService.removeGridFilter(filter1);

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(1);
            expect(gridFilters[0]).toBe(filter2);
        }));

        it('should remove all filters', inject(function (filterState, FilterStateService) {
            //given
            var filter1 = {colId: '0001', args: {value: 'toto'}};
            var filter2 = {colId: '0004', args: {value: 'toto'}};
            var originalGridFilters = [filter1, filter2];
            filterState.gridFilters = originalGridFilters;

            //when
            FilterStateService.removeAllGridFilters();

            //then
            var gridFilters = filterState.gridFilters;
            expect(gridFilters).not.toBe(originalGridFilters);
            expect(gridFilters.length).toBe(0);
        }));
    });


});
