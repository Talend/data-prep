(function () {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.playground.directive:Playground
     * @description This directive create the playground.
     * @restrict E
     */
    function Playground() {
        return {
            restrict: 'E',
            templateUrl: 'components/playground/playground.html',
            bindToController: true,
            controllerAs: 'playgroundCtrl',
            controller: 'PlaygroundCtrl'
        };
    }

    angular.module('data-prep.playground')
        .directive('playground', Playground);
})();