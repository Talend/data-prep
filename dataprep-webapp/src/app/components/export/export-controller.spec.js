/*
 * ============================================================================
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

import settings from '../../../mocks/Settings.mock';

describe('Export controller', () => {
	'use strict';

	let scope;
	let createController;
	let form;
	let stateMock;
	let exportTypes;

	beforeEach(angular.mock.module('data-prep.export', ($provide) => {
		exportTypes = [
			{
				"mimeType": "text/csv",
				"extension": ".csv",
				"id": "CSV",
				"needParameters": true,
				"defaultExport": false,
				"enabled": true,
				"disableReason": "",
				"title": "Export to CSV",
				"parameters": [
					{
						"name": "csvSeparator",
						"type": "select",
						"implicit": false,
						"canBeBlank": true,
						"placeHolder": "",
						"configuration": {
							"values": [
								{
									"value": ";",
									"label": "Semicolon"
								},
								{
									"value": "\t",
									"label": "Tabulation"
								},
								{
									"value": " ",
									"label": "Space"
								},
								{
									"value": ",",
									"label": "Comma"
								}
							],
							"multiple": false
						},
						"radio": true,
						"description": "Select character to use as a delimiter",
						"label": "Delimiter",
						"default": ";"
					},
					{
						"name": "fileName",
						"type": "string",
						"implicit": false,
						"canBeBlank": false,
						"placeHolder": "",
						"description": "Name of the generated export file",
						"label": "Filename",
						"default": ""
					},
					{
						"name": "escapeCharacter",
						"type": "string",
						"implicit": false,
						"canBeBlank": true,
						"placeHolder": "",
						"description": "Escape character",
						"label": "Escape character",
						"default": "@"
					}
				]
			},
			{
				"mimeType": "application/vnd.ms-excel",
				"extension": ".xlsx",
				"id": "XLSX",
				"needParameters": true,
				"defaultExport": true,
				"enabled": true,
				"disableReason": "",
				"title": "Export to XLSX",
				"parameters": [
					{
						"name": "fileName",
						"type": "string",
						"implicit": false,
						"canBeBlank": false,
						"placeHolder": "",
						"description": "Name of the generated export file",
						"label": "Filename",
						"default": ""
					}
				]
			}
		];

		stateMock = {
			playground: {
				preparation: { name: 'prepname' },
				recipe: {
					current: {
						steps: [],
					},
				},
			},
			export: {
				exportTypes: exportTypes,
				defaultExportType: {
					exportType: 'XLSX'
				}
			}
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject((RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);
	}));

	beforeEach(inject(($rootScope, $controller, ExportService) => {
		form = {
			submit: () => {
			},
		};
		scope = $rootScope.$new();

		createController = () => {
			const ctrl = $controller('ExportCtrl', { $scope: scope });
			ctrl.form = form; //simulate init by directive
			return ctrl;
		};

		spyOn(form, 'submit').and.returnValue();
		spyOn(ExportService, 'getType').and.returnValue();
	}));

	describe('property binding', () => {
		it('should bind stepId getter to state', () => {
			//given
			const ctrl = createController();
			expect(ctrl.stepId).toBeFalsy();

			//when
			stateMock.playground.recipe.current.steps.push({
				transformation: {
					stepId: '48da64513c43a548e678bc99',
				},
			});

			//then
			expect(ctrl.stepId).toBe('48da64513c43a548e678bc99');
		});
	});

	describe('selectType', () => {
		it('should init parameters (type)', () => {
			//given
			const ctrl = createController();
			const csvType = exportTypes[0];

			expect(ctrl.selectedType).not.toBe(csvType);

			//when
			ctrl.selectType(csvType);

			//then
			expect(ctrl.selectedType).toBe(csvType);
			expect(ctrl.selectedType.parameters[1].value).toBe('prepname');
		});

		it('should show modal', () => {
			//given
			const ctrl = createController();
			const csvType = exportTypes[0];

			expect(ctrl.showModal).not.toBe(true);

			//when
			ctrl.selectType(csvType);

			//then
			expect(ctrl.showModal).toBe(true);
		});
	});

	describe('saveAndExport', () => {
		it('should set action in form', inject((RestURLs, $timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			expect(form.action).toBeFalsy();

			//when
			ctrl.saveAndExport();
			$timeout.flush();

			//then
			expect(form.action).toBe(RestURLs.exportUrl);
		}));

		it('should submit form', inject(($timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			//when
			ctrl.saveAndExport();
			$timeout.flush();

			//then
			expect(form.submit).toHaveBeenCalled();
		}));

		it('should extract selected parameters', () => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];
			ctrl.selectedType.parameters[1].value = 'my prep';
			ctrl.selectedType.parameters[2].value = '#';

			//when
			ctrl.saveAndExport();

			//then
			expect(ctrl.exportParams).toEqual({
				exportType: 'CSV',
				'exportParameters.csvSeparator': ';',
				'exportParameters.fileName': 'my prep',
				'exportParameters.escapeCharacter': '#',
			});
		});


		it('should allow empty parameters', () => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];
			ctrl.selectedType.parameters[1].value = 'my prep';
			ctrl.selectedType.parameters[2].value = '';

			//when
			ctrl.saveAndExport();

			//then
			expect(ctrl.exportParams).toEqual({
				exportType: 'CSV',
				'exportParameters.csvSeparator': ';',
				'exportParameters.fileName': 'my prep',
				'exportParameters.escapeCharacter': '',
			});
		});

		it('should save selected parameters', inject((ExportService) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];
			spyOn(ExportService, 'setExportParams').and.returnValue();

			//when
			ctrl.saveAndExport();

			//then
			expect(ExportService.setExportParams).toHaveBeenCalledWith({
				exportType: exportTypes[0].id,
				'exportParameters.csvSeparator': ';',
				'exportParameters.fileName': '',
				'exportParameters.escapeCharacter': '@',
			});
		}));
	});

	describe('launch default export', () => {
		it('should set action in form', inject((RestURLs, $timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			expect(form.action).toBeFalsy();

			//when
			ctrl.launchDefaultExport();
			$timeout.flush();

			//then
			expect(form.action).toBe(RestURLs.exportUrl);
		}));

		it('should submit form', inject(($timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			//when
			ctrl.launchDefaultExport();
			$timeout.flush();

			//then
			expect(form.submit).toHaveBeenCalled();
		}));

		it('should extract selected parameters', () => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			//when
			ctrl.launchDefaultExport();

			//then
			expect(ctrl.exportParams).toEqual({
				exportType: 'CSV',
				'exportParameters.csvSeparator': ';',
				'exportParameters.fileName': 'prepname',
				'exportParameters.escapeCharacter': '@',
			});
		});
	});

	describe('launchExport', () => {
		it('should extract selected parameters', () => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];
			ctrl.selectedType.parameters[1].value = 'my prep';

			//when
			ctrl.launchExport();

			//then
			expect(ctrl.exportParams).toEqual({
				exportType: 'CSV',
				'exportParameters.csvSeparator': ';',
				'exportParameters.fileName': 'my prep',
				'exportParameters.escapeCharacter': '@',
			});
		});

		it('should set action in form', inject((RestURLs, $timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			expect(form.action).toBeFalsy();

			//when
			ctrl.launchExport();
			$timeout.flush();

			//then
			expect(form.action).toBe(RestURLs.exportUrl);
		}));

		it('should submit form', inject(($timeout) => {
			//given
			const ctrl = createController();
			ctrl.selectedType = exportTypes[0];

			//when
			ctrl.launchExport();
			$timeout.flush();

			//then
			expect(form.submit).toHaveBeenCalled();
		}));
	});
});
