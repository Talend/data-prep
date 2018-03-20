/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Loader component', () => {
	let scope;
	let element;
	let createElement;

	beforeEach(angular.mock.module('data-prep.loader'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = `<loader></loader>`;
			element = $compile(html)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render loader', () => {
		// given
		createElement();

		// then
		expect(element.length).toBe(1);
	});
});
