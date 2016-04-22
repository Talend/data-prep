/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/* eslint-disable angular/window-service */

(() => {
    'use strict';
    const app = angular.module('data-prep',
        [
            'ngSanitize',
            'ui.router', // more advanced router
            'data-prep.app', // app root
            'data-prep.services.rest', // rest interceptors
            'data-prep.services.dataset', // for configuration
            'data-prep.services.export', // for configuration
            'data-prep.services.import', // for configuration
            'data-prep.services.utils', // for configuration
            'pascalprecht.translate',
        ])

        // Performance config
        .config(($httpProvider) => {
            'ngInject';
            $httpProvider.useApplyAsync(true);
        })

        // Translate config
        .config(($translateProvider) => {
            'ngInject';
            $translateProvider.useStaticFilesLoader({
                prefix: 'i18n/',
                suffix: '.json',
            });

            $translateProvider.preferredLanguage('en');
            $translateProvider.useSanitizeValueStrategy(null);
        })

        // Router config
        .config(($stateProvider, $urlRouterProvider, $urlMatcherFactoryProvider) => {
            'ngInject';

            // override the built-in string type (which is performing the slash encoding)
            // by registering 'string' type
            const originalStringMatcher = $urlMatcherFactoryProvider.type('string');
            const overriddenStringMatcher = _.extend({}, originalStringMatcher, {
                encode: (val) => (val !== null ? val.toString() : val),
                decode: (val) => (val !== null ? val.toString() : val),
            });
            $urlMatcherFactoryProvider.type('string', overriddenStringMatcher);

            // route definitions
            $stateProvider
                .state('nav', {
                    abstract: true,
                    template: '<navbar></navbar>',
                })
                .state('nav.index', {
                    abstract: true,
                    url: '/index',
                    template: '<home></home>',
                })
                .state('nav.index.datasets', {
                    url: '/datasets',
                    views: {
                        'home-content-header': { template: '<dataset-header></dataset-header>' },
                        'home-content': { template: '<dataset-list></dataset-list>' },
                    },
                    resolve: {
                        inventory: ($q, DatasetService, PreparationService) => {
                            'ngInject';
                            return $q.all([
                                    DatasetService.init(),
                                    PreparationService.refreshPreparations(),
                                ]);
                        },
                    },
                })
                .state('nav.index.preparations', {
                    url: '/preparations/{folderPath:.*}',
                    views: {
                        'home-content-header': { template: '<preparation-header></preparation-header>' },
                        'home-content': { template: '<preparation-list></preparation-list>' },
                    },
                    resolve: {
                        inventory: ($stateParams, FolderService) => {
                            'ngInject';
                            return FolderService.init($stateParams.folderPath);
                        },
                    },
                })

                .state('playground', {
                    url: '/playground',
                    template: '<playground></playground>',
                    abstract: true,
                    resolve: {
                        inventory: ($q, DatasetService, PreparationService) => {
                            'ngInject';
                            return $q.all([
                                DatasetService.getDatasets(),
                                PreparationService.getPreparations(),
                            ]);
                        },
                    },
                })
                .state('playground.preparation', { url: '/preparation?prepid' })
                .state('playground.dataset', { url: '/dataset?datasetid' });
            $urlRouterProvider.otherwise('/index/preparations/');
        })

        // Language to use at startup (for now only english)
        .run(($window, $translate) => {
            'ngInject';
            $translate.use('en');
        });

    window.fetchConfiguration = function fetchConfiguration() {
        const initInjector = angular.injector(['ng']);
        const $http = initInjector.get('$http');

        return $http.get('/assets/config/config.json')
            .then((config) => {
                app
                // Debug config
                    .config(($compileProvider) => {
                        'ngInject';
                        $compileProvider.debugInfoEnabled(config.data.enableDebug);
                    })
                    // Configure server api urls
                    .run((RestURLs) => {
                        'ngInject';
                        RestURLs.setServerUrl(config.data.serverUrl);
                    })
                    // Fetch dynamic configuration (export types, supported encodings, ...)
                    .run((ImportService, ExportService, DatasetService) => {
                        'ngInject';
                        ImportService.initImport();
                        ExportService.refreshTypes();
                        DatasetService.refreshSupportedEncodings();

                    });

                angular.module('data-prep.services.utils')
                    .value('version', config.data.version)
                    .value('copyRights', config.data.copyRights)
                    .value('documentationSearchURL', config.data.documentationSearchURL);
            });
    };

    window.bootstrapDataPrepApplication = function bootstrapDataPrepApplication(modules) {
        angular.element(document)
            .ready(() => angular.bootstrap(document, modules));
    };
})();
/* eslint-enable angular/window-service */
