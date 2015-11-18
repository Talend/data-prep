(function() {
    'use strict';

    var suggestionsStatsState = {
        visible : false
    };

    function SuggestionsStatsState() {

        return {
            show: show,
            hide: hide
        };

        function show (){
            suggestionsStatsState.visible = true;
        }
        function hide (){
            suggestionsStatsState.visible = false;
        }

    }

    angular.module('data-prep.services.state')
        .service('SuggestionsStatsState', SuggestionsStatsState)
        .constant('suggestionsStatsState', suggestionsStatsState);
})();