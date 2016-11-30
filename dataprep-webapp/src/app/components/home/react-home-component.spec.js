/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Home component', () => {
	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<react-home></react-home>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should hold dataset-xls-preview', () => {
		//when
		createElement();

		//then
		expect(element.find('dataset-xls-preview').length).toBe(1);
	});

	it('should hold preparation-copy-move', () => {
		//when
		createElement();

		//then
		expect(element.find('preparation-copy-move').length).toBe(1);
	});

	it('should hold preparation-creator', () => {
		//when
		createElement();

		//then
		expect(element.find('preparation-creator').length).toBe(1);
	});

	it('should inject home insertion point', () => {
		//when
		createElement();

		//then
		expect(element.find('insertion-home').length).toBe(1);
	});
	
	it('should instanciate app layout with an ui insertion point', () => {
		//when
		createElement();

		//then
		expect(element.find('layout').length).toBe(1);
		expect(element.find('layout').eq(0).find('ui-view[name="home-content"]').length).toBe(1);
	});
});
