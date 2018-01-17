/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import settings from '../../mocks/Settings.mock';

let stateMock;

describe('Settings service', () => {

	beforeEach(angular.mock.module('app.settings', ($provide) => {
		stateMock = {
			import: {
				importTypes: [],
			},
		};
		$provide.constant('state', stateMock);
	}));

	afterEach(inject((SettingsService) => {
		SettingsService.clearSettings();
	}));

	describe('refreshSettings', () => {
		let $httpBackend;

		beforeEach(inject(($rootScope, $injector, RestURLs) => {
			RestURLs.register({ serverUrl: '' }, settings.uris);
			$httpBackend = $injector.get('$httpBackend');
		}));

		it('should get remote settings and update local settings', inject(($rootScope, appSettings, RestURLs, SettingsService) => {
			// given
			$httpBackend
				.expectGET(RestURLs.settingsUrl)
				.respond(200, settings);

			expect(appSettings).toEqual({ views: {}, actions: {}, uris: {}, help: {}, analytics: {} });

			// when
			SettingsService.refreshSettings();
			$httpBackend.flush();

			// then
			expect(appSettings).toEqual({
				...settings,
				analytics: {},
			});
		}));
	});

	describe('setSettings', () => {
		it('should merge settings', inject((appSettings, SettingsService) => {
			expect(appSettings).toEqual({ views: {}, actions: {}, uris: {}, help: {}, analytics: {} });

			const newSettings = {
				views: {
					myCustomView: {}
				},
				actions: {},
				uris: {},
				help: {},
				analytics: {},
			};

			// when
			SettingsService.setSettings(newSettings);

			// then
			expect(appSettings).toEqual(newSettings);
		}));
	});

	describe('clearSettings', () => {
		it('should reset settings', inject((appSettings, SettingsService) => {
			// given
			SettingsService.setSettings(settings);

			// when
			SettingsService.clearSettings();

			// then
			expect(appSettings.views).toEqual({});
			expect(appSettings.actions).toEqual({});
			expect(appSettings.uris).toEqual({});
			expect(appSettings.help).toEqual({});
			expect(appSettings.analytics).toEqual({});
		}));
	});
});
