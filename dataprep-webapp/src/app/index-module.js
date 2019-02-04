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
import store from 'store';

import i18n, {
	fallbackLng,
	fallbackNS,
} from './i18n';
import bootstrapReact from './index.cmf';

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
import translations, { getD3Locale } from '../i18n';

const MODULE_NAME = 'data-prep';

window.MODULE_NAME = MODULE_NAME;

let preferredLanguage;

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
		Object.keys(translations)
			.forEach((translationKey) => {
				$translateProvider.translations(
					translationKey,
					translations[translationKey],
				);

				i18n.addResourceBundle(
					translationKey,
					fallbackNS,
					translations[translationKey],
					false,
					false,
				);
			});
		$translateProvider.useSanitizeValueStrategy(['escapeParameters']);
	})
	// Router config
	.config(routeConfig)
	.run(routeInterceptor);

window.bootstrapAngular = function bootstrapAngular(appSettings) {
	app
		// Debug config
		.config(($compileProvider) => {
			'ngInject';
			$compileProvider.debugInfoEnabled(false);
		})
		.config(($httpProvider) => {
			'ngInject';

			preferredLanguage =
				(appSettings.context && appSettings.context.language) ||
				fallbackLng;

			const preferredLocale =
				appSettings.context && appSettings.context.locale;
			if (preferredLocale) {
				$httpProvider.defaults.headers.common[
					'Accept-Language'
				] = preferredLocale;
			}

			moment.locale(preferredLanguage);

			if (preferredLanguage !== fallbackLng) {
				i18n.changeLanguage(preferredLanguage);
				const d3CustomLocale = getD3Locale(preferredLanguage);
				if (d3CustomLocale) {
					const localizedD3 = d3.locale(d3CustomLocale);
					d3.format = localizedD3.numberFormat;
					d3.time.format = localizedD3.timeFormat;
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

			RestURLs.register(appSettings.uris);

			// dataset encodings
			DatasetService.refreshSupportedEncodings();
		})
		.run(($translate) => {
			'ngInject';

			$translate.fallbackLanguage(fallbackLng);
			$translate.use(preferredLanguage);
		});
};

getAppConfiguration().then((appSettings) => {
	// appSettings.context.provider = 'catalog';
	// appSettings.context.country = 'FR';
	// appSettings.context.language = 'fr';
	// appSettings.context.locale = 'fr-FR';
	const { provider = 'legacy' } = appSettings.context;

	store.set('settings', appSettings);

	if (
		provider.includes('catalog') &&
		!/#\/(playground|export|version)/.test(window.location.href)
	) {
		bootstrapReact();
	}
	else {
		window.bootstrapAngular(appSettings);
		angular
			.element(document)
			.ready(() => angular.bootstrap(document, [window.MODULE_NAME]));
	}
});

/* eslint-enable angular/window-service */

export default MODULE_NAME;
