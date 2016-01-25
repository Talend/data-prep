(function() {
    'use strict';

    var statisticsState = {};

    function StatisticsStateService() {

        return {
            setHistogram: setHistogram,
            setFilteredHistogram: setFilteredHistogram,
            setHistogramActiveLimits: setHistogramActiveLimits,
            setPatterns: setPatterns,
            setFilteredPatterns: setFilteredPatterns,

            reset: reset
        };

        function setHistogram(histogram) {
            statisticsState.histogram = histogram;
            statisticsState.filteredHistogram = null;
        }

        function setFilteredHistogram(filteredHistogram) {
            statisticsState.filteredHistogram = filteredHistogram;
        }

        function setHistogramActiveLimits(activeLimits) {
            statisticsState.activeLimits = activeLimits;
        }

        function setPatterns(patterns) {
            statisticsState.patterns = patterns;
            statisticsState.filteredPatterns = null;
        }

        function setFilteredPatterns(filteredPatterns) {
            statisticsState.filteredPatterns = filteredPatterns;
        }

        function reset() {
            statisticsState.histogram = null;
            statisticsState.filteredHistogram = null;
            statisticsState.activeLimits = null;
            statisticsState.patterns = null;
            statisticsState.filteredPatterns = null;
        }
    }

    angular.module('data-prep.services.state')
        .service('StatisticsStateService', StatisticsStateService)
        .constant('statisticsState', statisticsState);
})();