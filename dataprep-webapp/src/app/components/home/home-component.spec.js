/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Home component', () => {
	let scope;
	let element;

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $compile, AboutService) => {
		scope = $rootScope.$new(true);
		spyOn(AboutService, 'loadBuilds').and.returnValue();

		element = angular.element('<home></home>');
		$compile(element)(scope);
		scope.$digest();
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should hold dataset-xls-preview', () => {
		expect(element.find('dataset-xls-preview').length).toBe(1);
	});

	it('should hold folder-creator', () => {
		expect(element.find('folder-creator').length).toBe(1);
	});

	it('should hold preparation-copy-move', () => {
		expect(element.find('preparation-copy-move').length).toBe(1);
	});

	it('should hold preparation-creator', () => {
		expect(element.find('preparation-creator').length).toBe(1);
	});

	it('should inject home insertion point', () => {
		expect(element.find('insertion-home').length).toBe(1);
	});

	it('should instanciate app layout with an ui insertion point', () => {
		expect(element.find('layout').length).toBe(1);
		expect(element.find('layout').eq(0).find('ui-view[name="home-content"]').length).toBe(1);
	});

	it('should instantiate home dataset progress bar', () => {
		expect(element.find('step-progress').length).toBe(1);
	});
});
