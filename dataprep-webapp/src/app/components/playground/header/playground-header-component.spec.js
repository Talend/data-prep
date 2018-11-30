/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


describe('Playground header component', () => {
	'use strict';

	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('data-prep.playground-header'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			element = angular.element(`<playground-header></playground-header>`);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('app header', () => {
		it('should render', () => {
			createElement();
			expect(element.find('app-header-bar').length).toBe(1);
		});
	});

	describe('sub  header', () => {
		it('should render', () => {
			createElement();
			expect(element.find('pure-sub-header-bar').length).toBe(1);
		});
	});
});
