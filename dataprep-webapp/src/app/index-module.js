/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/* eslint-disable angular/window-service */

import angular from 'angular';
import ngSanitize from 'angular-sanitize';
import ngTranslate from 'angular-translate';
import uiRouter from 'angular-ui-router';

import APP_MODULE from './components/app/app-module';
import SERVICES_DATASET_MODULE from './services/dataset/dataset-module';
import SERVICES_EXPORT_MODULE from './services/export/export-module';
import SERVICES_IMPORT_MODULE from './services/import/import-module';
import SERVICES_REST_MODULE from './services/rest/rest-module';
import SERVICES_UTILS_MODULE from './services/utils/utils-module';
import SETTINGS_MODULE from './settings/settings-module';

const MODULE_NAME = 'data-prep';

let ws;
let wsPing;
const app = angular.module(MODULE_NAME,
	[
		ngSanitize,
		ngTranslate,
		uiRouter,
		SERVICES_REST_MODULE, // rest interceptors
		SERVICES_DATASET_MODULE, // for configuration
		SERVICES_EXPORT_MODULE, // for configuration
		SERVICES_IMPORT_MODULE, // for configuration
		SERVICES_UTILS_MODULE, // for configuration
		SETTINGS_MODULE, // app dynamic settings
		APP_MODULE, // app root
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
			encode: val => (val !== null ? val.toString() : val),
			decode: val => (val !== null ? val.toString() : val),
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
					'home-content': { template: '<home-dataset></home-dataset>' },
				},
			})
			.state('nav.index.preparations', {
				url: '/preparations/{folderId}',
				views: {
					'home-content': { template: '<home-preparation></home-preparation>' },
				},
			})
			.state('reactHome', {
				url: '/react',
				template: '<react-home></react-home>',
			})
			.state('reactHome.preparations', {
				url: '/preparations/{folderId}',
				views: {
					'home-content': { template: '<react-home-preparation></react-home-preparation>' },
				},
			})
			.state('playground', {
				url: '/playground',
				template: '<playground></playground>',
				abstract: true,
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
	const $q = initInjector.get('$q');

	return $q.all(
		[
			$http.get('/assets/config/config.json'),
			$http.get('/assets/config/app-settings.json'),
		])
		.then(result => result.map(res => res.data))
		.then(([config, appSettings]) => {
			app
			// Debug config
				.config(($compileProvider) => {
					'ngInject';
					$compileProvider.debugInfoEnabled(config.enableDebug);
				})
				// Configure server api urls
				.run((RestURLs) => {
					'ngInject';
					RestURLs.setServerUrl(config.serverUrl);
				})
				// Fetch dynamic configuration (export types, supported encodings, ...)
				.run((SettingsService, ImportService, ExportService, DatasetService) => {
					'ngInject';
					SettingsService.setSettings(appSettings);
					ImportService.initImport();
					ExportService.refreshTypes();
					DatasetService.refreshSupportedEncodings();
				})
				// Open a keepalive websocket if requested
				.run(() => {
					if (!config.serverKeepAliveUrl) return;
					function setupWebSocket() {
						clearInterval(wsPing);

						ws = new WebSocket(config.serverKeepAliveUrl);
						ws.onclose = () => {
							setTimeout(setupWebSocket, 1000);
						};

						wsPing = setInterval(() => {
							ws.send('ping');
						}, 3 * 60 * 1000);
					}

					setupWebSocket();
				});

			angular.module(SERVICES_UTILS_MODULE)
				.value('version', config.version)
				.value('copyRights', config.copyRights)
				.value('documentationSearchURL', config.documentationSearchURL);
		});
};

window.bootstrapDataPrepApplication = function bootstrapDataPrepApplication(modules) {
	angular.element(document)
		.ready(() => angular.bootstrap(document, modules));
};
/* eslint-enable angular/window-service */

export default MODULE_NAME;
