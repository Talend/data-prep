/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/*eslint-disable angular/window-service */

(() => {
    'use strict';

    var app = angular.module('data-prep',
        [
            'ngSanitize',
            'ui.router', //more advanced router
            'data-prep.app', //app root
            'data-prep.services.rest', //rest interceptors
            'data-prep.services.dataset', //for configuration
            'data-prep.services.export', //for configuration
            'data-prep.services.utils', //for configuration
            'bgDirectives'
        ])

        //Performance config
        .config(function ($httpProvider) {
            'ngInject';
            $httpProvider.useApplyAsync(true);
        })

        //Translate config
        .config(function ($translateProvider) {
            'ngInject';
            $translateProvider.useStaticFilesLoader({
                prefix: 'i18n/',
                suffix: '.json'
            });

            $translateProvider.preferredLanguage('en');
            $translateProvider.useSanitizeValueStrategy(null);
        })

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
                });

            $urlRouterProvider.otherwise('/home/datasets');
        })

        //Language from browser
        .run(function ($window, $translate) {
            var language = ($window.navigator.language === 'fr') ? 'fr' : 'en';
            $translate.use(language);
        });

    window.fetchConfiguration = function fetchConfiguration() {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');

        return $http.get('/assets/config/config.json')
            .then(function (config) {
                app
                    //Debug config
                    .config(function ($compileProvider) {
                        'ngInject';
                        $compileProvider.debugInfoEnabled(config.data.enableDebug);
                    })
                    //Configure server api urls
                    .run(function (RestURLs) {
                        'ngInject';
                        RestURLs.setServerUrl(config.data.serverUrl);
                    })
                    //Fetch dynamic configuration (export types, supported encodings, ...)
                    .run(function(ExportService, DatasetService) {
                        ExportService.refreshTypes();
                        DatasetService.refreshSupportedEncodings();
                    });

                angular.module('data-prep.services.utils')
                    .value('version', config.data.version)
                    .value('copyRights', config.data.copyRights);
            });
    };

    window.bootstrapDataPrepApplication = function bootstrapDataPrepApplication(modules) {
        angular.element(document).ready(function () {
            angular.bootstrap(document, modules);
        });
    };
})();
/*eslint-enable angular/window-service */