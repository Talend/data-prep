(function() {
    'use strict';

    var recipeState = {};

    function RecipeStateService() {
        return {
            show: show,
            hide: hide
        };

        function show() {
            recipeState.visible = true;
        }

        function hide() {
            recipeState.visible = false;
        }
    }

    angular.module('data-prep.services.state')
        .service('RecipeStateService', RecipeStateService)
        .constant('recipeState', recipeState);
})();