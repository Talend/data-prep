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

describe('Playground directive', () => {
	let scope;
	let createElement;
	let element;
	let ctrl;
	let stateMock;

	const metadata = {
		id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
		name: 'US States',
		author: 'anonymousUser',
		created: '02-03-2015 14:52',
		records: '3',
	};

	const sortList = [
		{ id: 'name', name: 'NAME_SORT', property: 'name' },
		{ id: 'date', name: 'DATE_SORT', property: 'created' },
	];

	const orderList = [
		{ id: 'asc', name: 'ASC_ORDER' },
		{ id: 'desc', name: 'DESC_ORDER' },
	];

	const exportTypes = [
		{
			"mimeType": "text/csv",
			"extension": ".csv",
			"id": "CSV",
			"needParameters": "true",
			"defaultExport": "false",
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
				}
			]
		},
		{
			"mimeType": "application/vnd.ms-excel",
			"extension": ".xlsx",
			"id": "XLSX",
			"needParameters": "true",
			"defaultExport": "true",
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

	beforeEach(angular.mock.module('data-prep.playground', ($provide) => {
		stateMock = {
			playground: {
				visible: true,
				filter: { gridFilters: [] },
				lookup: {
					visibility: false,
					datasets: [],
					sortList: sortList,
					orderList: orderList,
				},
				grid: {
					selectedColumns: [{ id: '0001' }],
					selectedLine: { '0001': '1' },
				},
				recipe: { current: { steps: [] } },
			},
			inventory: {
				datasets: [],
				sortList: sortList,
				orderList: orderList,
				folder: {
					metadata: {
						id: 'abcd'
					}
				}
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

	beforeEach(inject(($rootScope, $compile, $q, DatasetService) => {
		scope = $rootScope.$new();
		spyOn(DatasetService, 'getCompatiblePreparations').and.returnValue($q.when());

		createElement = () => {
			element = angular.element('<playground></playground>');
			angular.element('body').append(element);
			$compile(element)(scope);
			scope.$digest();

			ctrl = element.controller('playground');
			spyOn(ctrl, 'beforeClose').and.returnValue();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('loading', () => {
		it('should NOT render playground header and playground', () => {
			// given
			stateMock.playground.isLoading = true;

			// when
			createElement();

			// then
			expect(element.find('playground-header').length).toBe(0);
			expect(element.find('.playground').length).toBe(0);
		});
	});

	describe('suggestions', () => {
		it('should render right slidable panel', () => {
			// given
			stateMock.playground.dataset = metadata;

			// when
			createElement();

			// then: check right slidable is displayed transformations with right slide action
			expect(element.find('.playground-suggestions').eq(0).hasClass('slide-hide')).toBe(false);
			expect(element.find('.playground-suggestions').eq(0).find('.action').eq(0).hasClass('right')).toBe(true);
		});

		it('should render splitter inside slidable panel', () => {
			// given
			stateMock.playground.dataset = metadata;

			// when
			createElement();

			// then
			expect(element.find('.suggestions-stats-content').length).toBe(1);
			expect(element.find('#help-suggestions').length).toBe(1);
			expect(element.find('#help-stats').length).toBe(1);
		});
	});

	describe('recipe header', () => {
		beforeEach(inject((StateService) => {
			stateMock.playground.nameEditionMode = true;
			spyOn(StateService, 'setNameEditionMode').and.callFake((value) => {
				stateMock.playground.nameEditionMode = value;
			});
		}));

		it('should render left slidable panel', () => {
			// given
			stateMock.playground.dataset = metadata;

			// when
			createElement();

			// then : check left slidable is hidden recipe with left slide action
			expect(element.find('.playground-recipe').eq(0).hasClass('slide-hide')).toBe(true);
			expect(element.find('.playground-recipe').eq(0).find('.action').eq(0).hasClass('right')).toBe(false);
		});

		it('should hide preparation picker modal', () => {
			// given
			stateMock.playground.isPreprationPickerVisible = false;

			// when
			createElement();

			expect(angular.element('body').find('preparation-picker').length).toBe(0);
		});

		it('should display preparation picker modal', inject(() => {
			// given
			stateMock.playground.dataset = metadata;
			stateMock.playground.isPreprationPickerVisible = true;

			// when
			createElement();

			expect(angular.element('body').find('preparation-picker').length).toBe(1);
		}));
	});

	describe('dataset parameters', () => {
		it('should render dataset parameters', () => {
			// given
			stateMock.playground.dataset = metadata;

			// when
			createElement();

			// then : check dataset parameters is present
			const playground = element.find('.playground').eq(0);
			expect(playground.find('.dataset-parameters').length).toBe(1);
		});
	});

	describe('datagrid', () => {
		it('should render datagrid with filters', () => {
			// given
			stateMock.playground.dataset = metadata;

			// when
			createElement();

			// then : check datagrid and filters are present
			const playground = element.find('.playground').eq(0);
			expect(playground.eq(0).find('filter-bar').length).toBe(1);
			expect(playground.eq(0).find('filter-bar').find('#filter-search').length).toBe(1);
			expect(playground.eq(0).find('datagrid').length).toBe(1);
		});
	});

	describe('ESC management', () => {
		it('should close playground on escape key', inject(($timeout) => {
			// given
			createElement();

			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			// when
			element.find('.playground-container').eq(0).trigger(event);
			$timeout.flush();

			// then
			expect(ctrl.beforeClose).toHaveBeenCalled();
		}));

		it('should NOT close playground on non escape key', inject(($timeout) => {
			// given
			createElement();

			const event = angular.element.Event('keydown');
			event.keyCode = 14;

			// when
			element.find('.playground-container').eq(0).trigger(event);
			$timeout.flush();

			// then
			expect(ctrl.beforeClose).not.toHaveBeenCalled();
		}));

		it('should NOT close playground when event target is on input element', inject(($timeout) => {
			// given
			createElement();

			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			// when
			element.find('.playground-container input').eq(0).trigger(event);
			$timeout.flush();

			// then
			expect(ctrl.beforeClose).not.toHaveBeenCalled();
		}));

		it('should focus on playground container when event target is on input element', inject(($timeout) => {
			// given
			createElement();
			angular.element('body').append(element);
			const container = element.find('.playground-container').eq(0)[0];
			expect(document.activeElement).not.toBe(container);

			const event = angular.element.Event('keydown');
			event.keyCode = 27;

			// when
			element.find('.playground-container input').eq(0).trigger(event);
			$timeout.flush();

			// then
			expect(document.activeElement).toBe(container);
		}));
	});
});
