(function () {
    'use strict';

    var suggestionsState = {
        isLoading: false
    };

    function SuggestionsState() {

        return {
            setLoading: setLoading,
            reset: reset
        };

        function setLoading(isLoading) {
            suggestionsState.isLoading = isLoading;
        }

        function reset() {
            suggestionsState.isLoading = false;
        }
    }

    angular.module('data-prep.services.state')
        .service('SuggestionsState', SuggestionsState)
        .constant('suggestionsState', suggestionsState);
})();