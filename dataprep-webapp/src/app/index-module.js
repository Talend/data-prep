/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/* eslint-disable angular/window-service */

import d3 from 'd3';
import angular from 'angular';
import ngSanitize from 'angular-sanitize';
import ngTranslate from 'angular-translate';
import uiRouter from 'angular-ui-router';

import { init } from 'i18next';

import APP_MODULE from './components/app/app-module';
import HOME_MODULE from './components/home/home-module';
import PLAYGROUND_MODULE from './components/playground/playground-module';
import SERVICES_DATASET_MODULE from './services/dataset/dataset-module';
import SERVICES_REST_MODULE from './services/rest/rest-module';
import SERVICES_STATE_MODULE from './services/state/state-module';
import SERVICES_UTILS_MODULE from './services/utils/utils-module';
import SERVICES_ERRORS_MODULE from './services/errors/errors-module';
import SETTINGS_MODULE from './settings/settings-module';

import { routeConfig, routeInterceptor } from './index-route';
import getAppConfiguration from './index-config';

import d3LocaleFr from '../lib/d3.locale.fr';

const MODULE_NAME = 'data-prep';

const I18N_DOMAIN_COMPONENTS = 'tui-components';
const I18N_DOMAIN_FORMS = 'tui-forms';

let ws;
let wsPing;

let preferredLanguage;
const fallbackLng = 'en';
export const i18n = init({
	fallbackLng, // Fallback language
	ns: [
		I18N_DOMAIN_COMPONENTS,
		I18N_DOMAIN_FORMS,
	],
	defaultNS: I18N_DOMAIN_COMPONENTS,
	fallbackNS: I18N_DOMAIN_COMPONENTS,
	debug: false,
	wait: true, // globally set to wait for loaded translations in translate hoc
});

const app = angular
	.module(MODULE_NAME, [
		ngSanitize,
		ngTranslate,
		uiRouter,
		APP_MODULE, // bootstrap: app root
		HOME_MODULE, // routing: home components
		PLAYGROUND_MODULE, // routing: playground component
		SERVICES_DATASET_MODULE, // configuration: refresh supported encodings
		SERVICES_ERRORS_MODULE, // routing: common http errors management
		SERVICES_REST_MODULE, // configuration: rest interceptors
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE, // configuration: register constants (version, ...)
		SETTINGS_MODULE, // configuration: get app settings
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
		$translateProvider.useSanitizeValueStrategy(null);
	})
	// Router config
	.config(routeConfig)
	.run(routeInterceptor);

window.fetchConfiguration = function fetchConfiguration() {
	return getAppConfiguration().then(({ config, appSettings }) => {
		app
			// Debug config
			.config(($compileProvider) => {
				'ngInject';
				$compileProvider.debugInfoEnabled(config.enableDebug);
			})
			.config(($translateProvider) => {
				'ngInject';

				preferredLanguage =
					(appSettings.context && appSettings.context.language) ||
					fallbackLng;

				$translateProvider.preferredLanguage(preferredLanguage);

				moment.locale(preferredLanguage);

				if (preferredLanguage !== fallbackLng) {
					i18n.changeLanguage(preferredLanguage);

					if (preferredLanguage === 'fr') {
						const d3locale = d3.locale(d3LocaleFr);
						d3.format = d3locale.numberFormat;
						d3.time.format = d3locale.timeFormat;
					}
					if ($.datetimepicker) {
						$.datetimepicker.setLocale(preferredLanguage);
					}
				}
			})
			// Fetch dynamic configuration
			.run((SettingsService, InventoryStateService) => {
				'ngInject';
				// base settings
				SettingsService.setSettings(appSettings);
				InventoryStateService.init();
			})
			// Configure server api urls and refresh supported encoding
			.run((DatasetService, HelpService, RestURLs) => {
				'ngInject';

				const { help } = appSettings;
				if (help) {
					HelpService.register(help);
				}

				RestURLs.register(config, appSettings.uris);

				// dataset encodings
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
			})
			.run(($translate) => {
				'ngInject';

				$translate.onReady(() => {
					i18n.addResourceBundle(
						preferredLanguage,
						I18N_DOMAIN_COMPONENTS,
						$translate.getTranslationTable(),
						false,
						false
					);
				});
			});

		angular
			.module(SERVICES_UTILS_MODULE)
			.value('copyRights', config.copyRights);
	});
};

window.bootstrapDataPrepApplication = function bootstrapDataPrepApplication(
	modules
) {
	angular.element(document).ready(() => angular.bootstrap(document, modules));
};
/* eslint-enable angular/window-service */

export default MODULE_NAME;
