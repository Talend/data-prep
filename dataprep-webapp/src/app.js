var fetchConfiguration, bootstrapDataPrepApplication;

(function () {
    'use strict';

    var app = angular.module('data-prep',
        [
            'ngSanitize',
            'ui.router', //more advanced router
            'data-prep.app', //app root
            'data-prep.services.rest', //rest interceptors
            'data-prep.services.utils', //for configuration
            'bgDirectives'
        ])

        //Performance config
        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.useApplyAsync(true);
        }])

        //Translate config
        .config(['$translateProvider', function ($translateProvider) {
            $translateProvider.useStaticFilesLoader({
                prefix: 'i18n/',
                suffix: '.json'
            });

            $translateProvider.preferredLanguage('en');
            $translateProvider.useSanitizeValueStrategy(null);
        }])

        //Router config
        .config(function ($stateProvider, $urlRouterProvider) {
            $stateProvider
                .state('nav', {
                    abstract: true,
                    template: '<navbar></navbar>'
                })
                .state('nav.home', {
                    abstract: true,
                    url: '/home',
                    template: '<home></home>'
                })
                .state('nav.home.datasets', {
                    url: '/datasets?datasetid',
                    views: {
                        'home-content': {
                            template: '<dataset-list></dataset-list>'
                        }
                    },
                    folder: 'dataset'
                })
                .state('nav.home.preparations', {
                    url: '/preparations?prepid',
                    views: {
                        'home-content': {
                            template: '<preparation-list></preparation-list>'
                        }
                    }
                })
                .state('nav.home.recentfiles', {
                    url: '/recentfiles',
                    views: {
                        'home-content': {
                            template: '<div style="padding-top:100px; text-align: center;font-size: xx-large;" translate-once="COMING_SOON"></div>'
                        }
                    }
                })
                .state('nav.home.favorites', {
                    url: '/favorites',
                    views: {
                        'home-content': {
                            template: '<div style="padding-top:100px; text-align: center;font-size: xx-large;" translate-once="COMING_SOON"></div>'
                        }
                    }
                });

            $urlRouterProvider.otherwise('/home/datasets');
        })

        //Language from browser
        .run(function ($window, $translate) {
            var language = ($window.navigator.language === 'fr') ? 'fr' : 'en';
            $translate.use(language);
        });

    fetchConfiguration = function fetchConfiguration() {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');

        return $http.get('/assets/config/config.json')
            .then(function (config) {
                app
                    //Debug config
                    .config(['$compileProvider', function ($compileProvider) {
                        $compileProvider.debugInfoEnabled(config.data.enableDebug);
                    }])
                    //Configure server api urls
                    .run(['RestURLs', function (RestURLs) {
                        RestURLs.setServerUrl(config.data.serverUrl);
                    }]);
            });
    };

    bootstrapDataPrepApplication = function bootstrapDataPrepApplication(modules) {
        angular.element(document).ready(function () {
            angular.bootstrap(document, modules);
        });
    };
})();