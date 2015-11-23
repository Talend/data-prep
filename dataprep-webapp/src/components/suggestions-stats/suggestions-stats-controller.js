(function() {
    'use strict';

    function SuggestionsStatsCtrl(state) {
        this.state = state;
    }

    angular.module('data-prep.suggestions-stats')
        .controller('SuggestionsStatsCtrl', SuggestionsStatsCtrl);
})();