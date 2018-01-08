/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Transformation menu directive', () => {
	'use strict';
	let scope;
	let createElement;
	let element;

	const column = {
		semanticDomains: [],
		type: 'text',
	};

	const primitiveTypes = [
		{ id: 'STRING', label: 'string', labelKey: 'STRING' },
		{ id: 'INTEGER', label: 'integer', labelKey: 'INTEGER' },
		{ id: 'FLOAT', label: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', label: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', label: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{ id: 'AIRPORT', label: 'Airport', frequency: 3.03 },
		{ id: 'CITY', label: 'City', frequency: 99.24 },
	];

	beforeEach(angular.mock.module('data-prep.transformation-menu', ($provide) => {
		const stateMock = {
			playground: {
				preparation: {
					id: 'datasetId',
				},
				grid: {
					semanticDomains,
					primitiveTypes,
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(function ($q, $rootScope, $compile) {

		scope = $rootScope.$new();
		scope.column = column;

		createElement = () => {
			element = angular.element('<transform-menu column="column" menu-items="menu"></transform-menu>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render a simple action', () => {
		//given
		scope.menu = [{ label: 'uppercase' }];

		//when
		const element = createElement();

		//then
		expect(element.find('li a[ng-click="menuCtrl.select(menu, \'column\')"]').text().trim()).toBe('uppercase');
	});

	it('should render title of a simple action', () => {
		//given
		scope.menu = [{ label: 'uppercase' }];

		//when
		const element = createElement();

		//then
		expect(element.find('li a[title]').text().trim()).toBe('uppercase');
	});

	it('should render an action with parameters', () => {
		//given
		scope.menu = [{
			name: 'menuWithParam',
			label: 'menu with param',
			parameters: [
				{
					name: 'param1',
					type: 'string',
					inputType: 'text',
					default: '.',
				},
				{
					name: 'param2',
					type: 'integer',
					inputType: 'number',
					default: '5',
				},
			],
		},];

		//when
		const element = createElement();

		//then
		const menuItem = element.find('li a[ng-click="menuCtrl.select(menu, \'column\')"]');
		expect(menuItem.text().trim()).toBe('menu with param');
		expect(angular.element('body').find('.transformation-form').length).toBe(0);

		//when
		menuItem.click();

		//then
		const paramsElements = angular.element('body').find('.transformation-form');
		expect(paramsElements.length).toBe(1);
		expect(paramsElements.is(':visible')).toBe(true);
	});

	it('should render an action with simple choice', () => {
		//given
		scope.menu = [{
			name: 'menuWithParam',
			label: 'menu with param',
			items: [{
				name: 'my choice',
				values: [
					{
						name: 'noParamChoice1',
					},
					{
						name: 'noParamChoice2',
					},
				],
			},],
		},];

		//when
		const element = createElement();

		//then
		const menuItem = element.find('li a[ng-click="menuCtrl.select(menu, \'column\')"]');
		expect(menuItem.text().trim()).toBe('menu with param');
		expect(angular.element('body').find('.transformation-form').length).toBe(0);

		//when
		menuItem.click();

		//then
		const paramsElements = angular.element('body').find('.transformation-form');
		expect(paramsElements.length).toBe(1);
		expect(paramsElements.is(':visible')).toBe(true);
	});

	it('should render multiple menu items', () => {
		//given
		scope.menu = [
			{ label: 'uppercase' },
			{
				name: 'menuWithChoice',
				label: 'menu with choice',
				items: [{
					name: 'my choice',
					values: [
						{
							name: 'noParamChoice1',
						},
						{
							name: 'noParamChoice2',
						},
					],
				},],
			},
			{
				name: 'menuWithParam',
				label: 'menu with param',
				parameters: [
					{
						name: 'param1',
						type: 'string',
						inputType: 'text',
						default: '.',
					},
					{
						name: 'param2',
						type: 'integer',
						inputType: 'number',
						default: '5',
					},
				],
			},
		];

		//when
		const element = createElement();

		//then
		const menuItems = element.find('li a[ng-click="menuCtrl.select(menu, \'column\')"]');
		expect(menuItems.length).toBe(3);
		expect(menuItems.eq(0).text().trim()).toBe('uppercase');
		expect(menuItems.eq(1).text().trim()).toBe('menu with choice');
		expect(menuItems.eq(2).text().trim()).toBe('menu with param');
	});

	it('should display selected item parameters', () => {
		//given
		scope.menu = [
			{ label: 'uppercase' },
			{
				name: 'menuWithChoice',
				label: 'menu with choice',
				parameters: [
					{
						name: 'my choice',
						type: 'select',
						configuration: {
							values: [
								{
									name: 'noParamChoice1',
									value: 'noParamChoice1',
								},
								{
									name: 'noParamChoice2',
									value: 'noParamChoice2',
								},
							],
						},
					},
				],
			},
			{
				name: 'menuWithParam',
				label: 'menu with param',
				parameters: [
					{
						name: 'param1',
						type: 'string',
						inputType: 'text',
						default: '.',
					},
					{
						name: 'param2',
						type: 'integer',
						inputType: 'number',
						default: '5',
					},
				],
			},
		];

		const element = createElement();
		const menuItems = element.find('li a[ng-click="menuCtrl.select(menu, \'column\')"]');
		const menuWithChoice = menuItems.eq(1);
		const menuWithParams = menuItems.eq(2);
		expect(angular.element('body').find('.transformation-form').length).toBe(0);

		//when
		menuWithChoice.click();

		//then : expect params to render choice
		let paramsElements = angular.element('body').find('.transformation-form');
		expect(paramsElements.length).toBe(1);

		expect(paramsElements.is(':visible')).toBe(true);
		expect(paramsElements.find('button').length).toBe(1);
		expect(paramsElements.find('select').length).toBe(1);

		//when
		menuWithParams.click();

		//then : expect params to render simple params
		paramsElements = angular.element('body').find('.transformation-form');
		expect(paramsElements.length).toBe(1);

		expect(paramsElements.is(':visible')).toBe(true);
		expect(paramsElements.find('input').length).toBe(2);
		expect(paramsElements.find('button').length).toBe(1);
		expect(paramsElements.find('select').length).toBe(0);
	});
});
