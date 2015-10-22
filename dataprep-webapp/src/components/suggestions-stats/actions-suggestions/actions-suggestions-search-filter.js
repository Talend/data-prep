(function() {
    'use strict';

    function ActionsSuggestionsSearchFilter() {
        return function(arr, searchActionString){
            if(!searchActionString){
                return arr;
            }
            var result = [];
            searchActionString = searchActionString.toLowerCase();
            angular.forEach(arr, function(item){
                if(item.label.toLowerCase().indexOf(searchActionString) !== -1 ||
                    item.description.toLowerCase().indexOf(searchActionString) !== -1 ||
                    item.category.toLowerCase().indexOf(searchActionString) !== -1){
                    result.push(item);
                }
            });
            return result;
        };
    }

    angular.module('data-prep.actions-suggestions')
        .filter('actionsSuggestionsSearchFilter', ActionsSuggestionsSearchFilter);

})();