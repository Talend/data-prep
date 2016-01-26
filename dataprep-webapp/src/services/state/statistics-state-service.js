(function() {
    'use strict';

    var statisticsState = {};

    function StatisticsStateService() {

        return {
            setBoxPlot: setBoxPlot,
            setDetails: setDetails,
            setRangeLimits: setRangeLimits,
            setHistogram: setHistogram,
            setFilteredHistogram: setFilteredHistogram,
            setHistogramActiveLimits: setHistogramActiveLimits,
            setPatterns: setPatterns,
            setFilteredPatterns: setFilteredPatterns,

            reset: reset
        };

        function setBoxPlot(boxPlot) {
            statisticsState.boxPlot = boxPlot;
        }

        function setDetails(details) {
            statisticsState.details = details;
        }

        function setRangeLimits(rangeLimits) {
            statisticsState.rangeLimits = rangeLimits;
        }

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
            statisticsState.boxPlot = null;
            statisticsState.rangeLimits = null;
            statisticsState.details = null;
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