/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datagrid header directive', () => {
	let scope;
	let createElement;
	let element;
	let ctrl;
	let stateMock;
	const types = [
		{ id: 'ANY', name: 'any', labelKey: 'ANY' },
		{ id: 'STRING', name: 'string', labelKey: 'STRING' },
		{ id: 'NUMERIC', name: 'numeric', labelKey: 'NUMERIC' },
		{ id: 'INTEGER', name: 'integer', labelKey: 'INTEGER' },
		{ id: 'DOUBLE', name: 'double', labelKey: 'DOUBLE' },
		{ id: 'FLOAT', name: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', name: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', name: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{
			"id": "AIRPORT",
			"label": "Airport",
			"frequency": 3.03,
		},
		{
			"id": "CITY",
			"label": "City",
			"frequency": 99.24,
		},
	];

	const body = angular.element('body');
	const column = {
		id: '0001',
		name: 'MostPopulousCity',
		quality: {
			empty: 5,
			invalid: 10,
			valid: 72,
		},
		type: 'string',
		statistics: {
			frequencyTable: [
				{},
			],
		},
	};

	const emptyCellsTransfo = {
		'actionScope': ['empty'],
		'name': 'delete_empty',
		'description': 'Delete rows that have empty cells',
		'label': 'Delete the Rows with Empty Cell',
	};

	const invalidCellsTransfo = {
		'actionScope': ['invalid'],
		'category': 'data cleansing',
		'name': 'clear_invalid',
		'description': 'Clear cells that contain a value recognized as invalid',
		'label': 'Clear the Cells with Invalid Values',
	};

	beforeEach(angular.mock.module('data-prep.datagrid-header', ($provide) => {
		stateMock = {
			playground: {
				preparation: {
					id: 'prepId',
				},
				suggestions: {
					transformationsForEmptyCells: [emptyCellsTransfo],
					transformationsForInvalidCells: [invalidCellsTransfo],
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			"SELECT_VALID_RECORDS": "Select rows with valid values for ",
			"SELECT_INVALID_RECORDS": "Select rows with invalid values for ",
			"SELECT_EMPTY_RECORDS": "Select rows with empty values for ",
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q, StateService, ColumnTypesService, FilterManagerService, PlaygroundService) => {
		spyOn(ColumnTypesService, 'refreshSemanticDomains').and.returnValue($q.when(semanticDomains));
		spyOn(ColumnTypesService, 'refreshTypes').and.returnValue($q.when(types));
		spyOn(FilterManagerService, 'addFilter').and.returnValue();
		spyOn(PlaygroundService, 'completeParamsAndAppend').and.returnValue();
	}));

	beforeEach(inject(($rootScope, $compile, $timeout) => {
		scope = $rootScope.$new(true);
		scope.column = column;

		createElement = () => {
			element = angular.element('<datagrid-header column="column"></datagrid-header>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
			$timeout.flush();

			ctrl = element.controller('datagridHeader');
			spyOn(ctrl, 'updateColumnName').and.returnValue();
			ctrl.columnNameEdition = { $commitViewValue: jasmine.createSpy('$commitViewValue') };
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should display column title and domain', () => {
		//given
		scope.column = {
			id: '0001',
			name: 'MostPopulousCity',
			quality: {
				empty: 5,
				invalid: 10,
				valid: 72,
			},
			type: 'string',
			domain: 'city',
			domainLabel: 'City',
		};

		//when
		createElement();

		//then
		expect(element.find('.grid-header-title').text().trim()).toBe('MostPopulousCity');
		expect(element.find('.grid-header-type').text()).toBe('City');
	});

	it('should display column title and type when there is no domain', () => {
		//when
		createElement();

		//then
		expect(element.find('.grid-header-title').text().trim()).toBe('MostPopulousCity');
		expect(element.find('.grid-header-type').text()).toBe('text');
	});

	it('should close dropdown on get transform list error', inject(($timeout, $q, TransformationService) => {
		//given
		createElement();
		spyOn(TransformationService, 'getTransformations').and.returnValue($q.when({ allTransformations: [] }));
		element.find('.grid-header-caret').click();
		const dropdown = element.find('sc-dropdown').eq(0);
		expect(dropdown.hasClass('show')).toBe(true);

		//when
		ctrl.transformationsRetrieveError = true;
		scope.$digest();
		$timeout.flush();

		//then
		expect(dropdown.hasClass('show')).toBe(false);
	}));

	it('should show input to rename column name when double click', () => {
		//given
		createElement();

		const headerTitle = element.find('.grid-header-title').eq(0);
		expect(ctrl.isEditMode).toBeFalsy();

		//when
		headerTitle.dblclick();

		//then
		expect(ctrl.isEditMode).toBeTruthy();
	});

	it('should select input text when edition mode is tuned on', inject(($window, $timeout) => {
		//given
		createElement();

		const headerTitle = element.find('.grid-header-title').eq(0);

		//when
		headerTitle.dblclick();
		$timeout.flush(100);

		//then
		const activeElement = document.activeElement; //eslint-disable-line angular/document-service
		expect(activeElement).toBe(element.find('.grid-header-title-input').eq(0)[0]);
		expect(activeElement.value).toBe('MostPopulousCity');
	}));

	it('should switch from input to text on ESC keydown', () => {
		//given
		createElement();

		ctrl.setEditMode(true);

		const event = angular.element.Event('keydown');
		event.keyCode = 27;

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.isEditMode).toBe(false);
	});

	it('should reset column name on ESC keydown', () => {
		//given
		createElement();

		ctrl.setEditMode(true);
		ctrl.newName = 'toto';

		const event = angular.element.Event('keydown');
		event.keyCode = 27;

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.newName).toBe('MostPopulousCity');
	});

	it('should switch from input to text on ENTER event without changes', () => {
		//given
		createElement();

		ctrl.setEditMode(true);

		const event = angular.element.Event('keydown');
		event.keyCode = 13;

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.isEditMode).toBe(false);
	});

	it('should submit update on ENTER with changes', () => {
		//given
		createElement();

		ctrl.setEditMode(true);
		ctrl.newName = 'MostPopulousCityInTheWorld';

		const event = angular.element.Event('keydown');
		event.keyCode = 13;

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.updateColumnName).toHaveBeenCalled();
		expect(ctrl.columnNameEdition.$commitViewValue).toHaveBeenCalled();
	});

	it('should switch from input to text on BLUR event without changes', () => {
		//given
		createElement();

		ctrl.setEditMode(true);

		const event = angular.element.Event('blur');

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.isEditMode).toBe(false);
	});

	it('should submit update on BLUR event with changes', () => {
		//given
		createElement();

		ctrl.setEditMode(true);
		ctrl.newName = 'MostPopulousCityInTheWorld';

		const event = angular.element.Event('blur');

		//when
		element.find('.grid-header-title-input').eq(0).trigger(event);

		//then
		expect(ctrl.updateColumnName).toHaveBeenCalled();
	});

	it('should stop click propagation in input', () => {
		//given
		createElement();
		const event = angular.element.Event('click');

		//when
		element.find('input').eq(0).trigger(event);

		//then
		expect(event.isPropagationStopped()).toBe(true);
		expect(event.isDefaultPrevented()).toBe(true);
	});

	it('should hide menu on left click on grid-header', () => {
		//given
		createElement();
		element.find('.dropdown-menu').addClass('show-menu');

		//when
		const event = angular.element.Event('mousedown');
		event.which = 1;
		element.find('.grid-header').eq(0).trigger(event);

		//then
		expect(element.find('.dropdown-menu').hasClass('show-menu')).toBeFalsy();
	});

	it('should show menu on right click on grid-header if menu is hidden', inject(($q, TransformationService) => {
		//given
		createElement();
		spyOn(TransformationService, 'getTransformations').and.returnValue($q.when({ allTransformations: [] }));
		expect(element.find('sc-dropdown').hasClass('show')).toBeFalsy();

		//when
		const event = angular.element.Event('mouseup');
		event.which = 3;
		element.find('.grid-header').eq(0).trigger(event);

		//then
		expect(element.find('sc-dropdown').hasClass('show')).toBeTruthy();
	}));

	it('should hide menu on right click if menu is visible', () => {
		//given
		createElement();
		element.find('.dropdown-menu').addClass('show-menu');

		//when
		const event = angular.element.Event('mousedown');
		event.which = 3;
		element.find('.grid-header').eq(0).trigger(event);

		//then
		expect(element.find('.dropdown-menu').hasClass('show-menu')).toBeFalsy();
	});

	it('should render quality filters', () => {
		// given
		createElement();

		// then
		const dropdownItems = element.find('sc-dropdown li');
		expect(dropdownItems.find('a[translate-once="DISPLAY_ROWS_VALID_VALUES"]').length).toBe(1);
		expect(dropdownItems.find('a[translate-once="DISPLAY_ROWS_INVALID_VALUES"]').length).toBe(1);
		expect(dropdownItems.find('a[translate-once="DISPLAY_ROWS_EMPTY_VALUES"]').length).toBe(1);
		expect(dropdownItems.find('a[translate-once="DISPLAY_ROWS_INVALID_EMPTY_VALUES"]').length).toBe(1);
	});

	describe('quality bar', () => {
		it('should render quality bar', () => {
			// when
			createElement();

			// then
			expect(element.find('quality-bar').length).toBe(1);
			expect(element.find('quality-bar empty-menu-items').length).toBe(1);
			expect(element.find('quality-bar invalid-menu-items').length).toBe(1);
		});
		describe('empty menu', () => {
			it('should render empty menu content', () => {
				// when
				createElement();

				// then
				expect(element.find('quality-bar empty-menu-items > li').length).toBe(3);
				expect(element.find('quality-bar empty-menu-items > li').eq(0).text().trim())
					.toBe('Select rows with empty values for MostPopulousCity');
				expect(element.find('quality-bar empty-menu-items > li').eq(2).text().trim())
					.toBe(emptyCellsTransfo.label);
			});

			it('should trigger addFilter callback on 1st item menu click', inject((FilterManagerService) => {
				// given
				createElement();

				// when
				element.find('quality-bar empty-menu-items > li').eq(0).click();

				// then
				expect(FilterManagerService.addFilter).toHaveBeenCalledWith('empty_records', column.id, column.name);
			}));

			it('should delete rows on 2nd item menu click', inject((PlaygroundService) => {
				// given
				createElement();

				// when
				element.find('quality-bar empty-menu-items > li').eq(2).click();

				// then
				expect(PlaygroundService.completeParamsAndAppend).toHaveBeenCalledWith(emptyCellsTransfo, 'column');
			}));
		});

		describe('invalid menu', () => {
			it('should render invalid menu content', () => {
				// when
				createElement();

				// then
				expect(element.find('quality-bar invalid-menu-items > li').length).toBe(3);
				expect(element.find('quality-bar invalid-menu-items > li').eq(0).text().trim())
					.toBe('Select rows with invalid values for MostPopulousCity');
				expect(element.find('quality-bar invalid-menu-items > li').eq(2).text().trim())
					.toBe(invalidCellsTransfo.label);
			});

			it('should trigger addFilter callback on 1st item menu click', inject((FilterManagerService) => {
				// given
				createElement();

				// when
				element.find('quality-bar invalid-menu-items > li').eq(0).click();

				// then
				expect(FilterManagerService.addFilter).toHaveBeenCalledWith('invalid_records', column.id, column.name);
			}));

			it('should clear invalid cells on 2nd item menu click', inject((PlaygroundService) => {
				// given
				createElement();

				// when
				element.find('quality-bar invalid-menu-items > li').eq(2).click();

				// then
				expect(PlaygroundService.completeParamsAndAppend).toHaveBeenCalledWith(invalidCellsTransfo, 'column');
			}));
		});
	});
});
