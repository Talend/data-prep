describe('Statistics state service', function () {
    'use strict';

    beforeEach(module('data-prep.services.state'));

    it('should set histogram', inject(function (statisticsState, StatisticsStateService) {
        //given
        expect(statisticsState.histogram).toBeFalsy();
        var histogram = {data : []};

        //when
        StatisticsStateService.setHistogram(histogram);

        //then
        expect(statisticsState.histogram).toBe(histogram);
    }));

    it('should set filtered histogram', inject(function (statisticsState, StatisticsStateService) {
        //given
        expect(statisticsState.filteredHistogram).toBeFalsy();
        var filteredHistogram = {data : []};

        //when
        StatisticsStateService.setFilteredHistogram(filteredHistogram);

        //then
        expect(statisticsState.filteredHistogram).toBe(filteredHistogram);
    }));

    it('should set active limits', inject(function (statisticsState, StatisticsStateService) {
        //given
        expect(statisticsState.activeLimits).toBeFalsy();
        var activeLimits = [15, 25];

        //when
        StatisticsStateService.setHistogramActiveLimits(activeLimits);

        //then
        expect(statisticsState.activeLimits).toBe(activeLimits);
    }));

    it('should set patterns', inject(function (statisticsState, StatisticsStateService) {
        //given
        expect(statisticsState.patterns).toBeFalsy();
        var patterns = {data : []};

        //when
        StatisticsStateService.setPatterns(patterns);

        //then
        expect(statisticsState.patterns).toBe(patterns);
    }));

    it('should set filtered patterns', inject(function (statisticsState, StatisticsStateService) {
        //given
        expect(statisticsState.filteredPatterns).toBeFalsy();
        var filteredPatterns = {data : []};

        //when
        StatisticsStateService.setFilteredPatterns(filteredPatterns);

        //then
        expect(statisticsState.filteredPatterns).toBe(filteredPatterns);
    }));

    it('should reset all statistics', inject(function (statisticsState, StatisticsStateService) {
        //given
        statisticsState.histogram = {};
        statisticsState.filteredHistogram = {};
        statisticsState.patterns = {};
        statisticsState.filteredPatterns = {};
        statisticsState.activeLimits = {};

        //when
        StatisticsStateService.reset();

        //then
        expect(statisticsState.histogram).toBe(null);
        expect(statisticsState.filteredHistogram).toBe(null);
        expect(statisticsState.patterns).toBe(null);
        expect(statisticsState.filteredPatterns).toBe(null);
        expect(statisticsState.activeLimits).toBe(null);
    }));
});
