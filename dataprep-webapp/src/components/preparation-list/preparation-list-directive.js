(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name data-prep.preparation-list.directive:PreparationList
     * @description This directive display the preparations list.
     * @restrict E
     */
    function PreparationList() {
        return {
            restrict: 'E',
            templateUrl: 'components/preparation-list/preparation-list.html',
            bindToController: true,
            controllerAs: 'preparationListCtrl',
            controller: 'PreparationListCtrl'
        };
    }

    angular.module('data-prep.preparation-list')
        .directive('preparationList', PreparationList);
})();