/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const statisticsState = {};

export function StatisticsStateService() {
	return {
		setBoxPlot,
		setDetails,
		setRangeLimits,
		setHistogram,
		setFilteredHistogram,
		setHistogramActiveLimits,
		setPatterns,
		setFilteredPatterns,
		setLoading,

		reset,
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

	function setLoading(loading) {
		statisticsState.loading = loading;
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
		statisticsState.loading = false;
	}
}
