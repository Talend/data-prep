(function() {
    'use strict';
    /*Filter by label, description and category of actions
    * */
    function ActionsSuggestionsSearchFilter() {
        return function(arr, searchActionString){
            if(!searchActionString){
                return arr;
            }
            var result = [];
            searchActionString = searchActionString.toLowerCase();
            angular.forEach(arr, function(item){
                if(item.labelHtml.toLowerCase().indexOf(searchActionString) !== -1 ||
                    item.description.toLowerCase().indexOf(searchActionString) !== -1 ||
                    item.categoryHtml.toLowerCase().indexOf(searchActionString) !== -1){
                    result.push(item);
                }
            });
            return result;
        };
    }

    angular.module('data-prep.actions-suggestions')
        .filter('actionsSuggestionsSearchFilter', ActionsSuggestionsSearchFilter);

})();