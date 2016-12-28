/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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

		beforeEach(inject(($rootScope, $injector) => {
			$httpBackend = $injector.get('$httpBackend');
		}));

		it('should get remote settings and update local settings', inject(($rootScope, appSettings, SettingsService) => {
			// given
			$httpBackend
				.expectGET('/assets/config/app-settings.json')
				.respond(200, settings);

			expect(appSettings).toEqual({ views: [], actions: [] });

			// when
			SettingsService.refreshSettings();
			$httpBackend.flush();

			// then
			expect(appSettings).toEqual(settings);
		}));

		it('should adapt settings and update local settings', inject(($rootScope, appSettings, SettingsService) => {
			// given
			stateMock.import.importTypes = [
				{
					defaultImport: true,
					label: 'Local File',
					model: {
						locationType: 'local',
						defaultImport: true,
						label: 'Local File',
					}
				}
			];

			$httpBackend
				.expectGET('/assets/config/app-settings.json')
				.respond(200, settings);

			expect(appSettings).toEqual({ views: [], actions: [] });

			// when
			SettingsService.refreshSettings();
			$httpBackend.flush();
			$rootScope.$apply();

			// then
			expect(appSettings.actions['dataset:create'].items).toEqual(stateMock.import.importTypes);
		}));
	});

	describe('setSettings', () => {
		it('should merge settings', inject((appSettings, SettingsService) => {
			// given
			expect(appSettings).toEqual({ views: [], actions: [] });

			const newSettings = {
				views: {
					mycurstomView: {}
				},
				actions: []
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
			appSettings.views.push({});
			appSettings.actions.push({});

			// when
			SettingsService.clearSettings();

			// then
			expect(appSettings.views).toEqual([]);
			expect(appSettings.actions).toEqual([]);
		}));
	});
});
