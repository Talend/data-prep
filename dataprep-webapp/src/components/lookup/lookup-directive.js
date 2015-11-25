(function() {
    'use strict';

    function Lookup() {
        return {
            restrict: 'E',
            templateUrl: 'components/lookup/lookup.html',
            scope: {
                visible: '='
            },
            bindToController: true,
            controllerAs: 'lookupCtrl',
            controller: 'LookupCtrl'
        };
    }

    angular.module('data-prep.lookup')
        .directive('lookup', Lookup);
})();