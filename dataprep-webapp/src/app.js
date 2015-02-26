(function() {
    'use strict';

    angular.module('data-prep',
        [
            'data-prep-utils', //utils components: constants, filters, ...
            'data-prep-dataset', //dataset getter, manipulation, etc
            'data-prep-transformation', //transformation on dataset

            'talend.widget', //components widget built on bourbon (modal, dropdown, ...)

            'ui.router',
            'ngAnimate'
        ])

        .config(['$httpProvider', '$compileProvider', 'disableDebug', function ($httpProvider, $compileProvider, disableDebug) {
            $httpProvider.useApplyAsync(true);

            if(disableDebug) {
                $compileProvider.debugInfoEnabled(false);
            }
        }])

        .config(function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('nav', {
                    abstract: true,
                    templateUrl: 'app/navbar/navbar.html'
                })
                .state('nav.home', {
                    url: '/home',
                    templateUrl: 'app/home/home.html',
                    controller: 'HomeCtrl',
                    controllerAs: 'homeCtrl'
                });

            $urlRouterProvider.otherwise('/home');
        });
})();
