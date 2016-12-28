/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Transformation menu component', () => {
	let scope;
	let createElement;
	let element;
	let controller;

	const primitiveTypes = [
		{ id: 'STRING', label: 'string', labelKey: 'STRING' },
		{ id: 'INTEGER', label: 'integer', labelKey: 'INTEGER' },
		{ id: 'FLOAT', label: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', label: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', label: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{ id: 'CITY', label: 'City', frequency: 99.24 },
		{ id: 'AIRPORT', label: 'Airport', frequency: 3.03 },
	];

	beforeEach(angular.mock.module('data-prep.type-transformation-menu'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			COLUMN_TYPE_IS: 'This column is a ',
			COLUMN_TYPE_SET: 'Set as',
			FLOAT: 'DECIMAL',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q, $rootScope, $compile) => {

		scope = $rootScope.$new();
		scope.column = {
			id: '0001',
			domain: 'CITY',
			domainLabel: 'CITY',
			domainFrequency: 18,
			type: 'string',
		};
		scope.types = primitiveTypes;
		scope.domains = semanticDomains;

		createElement = () => {
			element = angular.element(`
				<type-transform-menu
					types="types"
					domains="domains"
					column="column"
				></type-transform-menu>
            `);
			$compile(element)(scope);
			scope.$digest();
			controller = element.controller('typeTransformMenu');
			return element;
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should display domain', () => {
		//given
		createElement();

		//when
		controller.currentSimplifiedDomain = 'CITY';
		scope.$digest();

		//then (beware of the space char between "is a " and "CITY" which is not exactly a space)
		expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
		expect(element.find('>li >.info').text().trim()).toBe('CITY');
	});

	it('should display simplified type when there is no domain', () => {
		//given
		scope.column.domain = '';
		scope.column.domainLabel = '';
		scope.column.domainCount = 0;

		//when
		createElement();

		//then (beware of the space char between "is a " and "text which is not exactly a space)
		expect(element.find('>li >span').eq(0).text()).toBe('This column is a ');
		expect(element.find('>li >.info').text().trim()).toBe('text');
	});

	it('should render domain items with percentages', () => {
		//when
		createElement();

		//then
		const items = element.find('ul.submenu >li');
		expect(items.length).toBe(8);

		expect(items.eq(0).text().trim()).toBe('City 99.24 %');
		expect(items.eq(1).text().trim()).toBe('Airport 3.03 %');

		expect(items.eq(2).hasClass('divider')).toBe(true);
	});

	it('should render primitive types', () => {
		//when
		createElement();

		//then
		const items = element.find('ul.submenu >li');
		expect(items.length).toBe(8);

		expect(items.eq(2).hasClass('divider')).toBe(true);

		expect(items.eq(3).text().trim()).toBe('Set as STRING');
		expect(items.eq(4).text().trim()).toBe('Set as INTEGER');
		expect(items.eq(5).text().trim()).toBe('Set as DECIMAL');
		expect(items.eq(6).text().trim()).toBe('Set as BOOLEAN');
		expect(items.eq(7).text().trim()).toBe('Set as DATE');
	});

	describe('clicks', () => {
		it('should trigger change domain process', () => {
			// given
			createElement();
			spyOn(controller, 'changeDomain').and.returnValue();
			const items = element.find('ul.submenu >li');

			// when
			items.eq(1).click();
			scope.$digest();

			// then
			expect(controller.changeDomain).toHaveBeenCalledWith(semanticDomains[1]);
		});

		it('should trigger change type process', () => {
			// given
			createElement();
			spyOn(controller, 'changeType').and.returnValue();
			const items = element.find('ul.submenu >li');

			// when
			items.eq(3).click();
			scope.$digest();

			// then
			expect(controller.changeType).toHaveBeenCalledWith(primitiveTypes[0]);
		});
	});
});
