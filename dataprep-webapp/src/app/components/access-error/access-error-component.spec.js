/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const i18n = {
	ERROR_404_TITLE: 'Oops ...',
	ERROR_404_MESSAGE: 'The page you are looking for cannot be found',
};

describe('Http Error component', () => {
	let scope;
	let element;
	let createElement;

	beforeEach(angular.mock.module('data-prep.access-error'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = `<access-error status="status"></access-error>`;
			element = $compile(html)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should not render if no status', () => {
			// given
			createElement();

			// when
			scope.$digest();

			// then
			const component = element.find('http-error');
			expect(component.length).toBe(0);
		});

		it('should render 404 error', () => {
			// given
			scope.status = 404;
			createElement();

			// when
			scope.$digest();

			// then
			const component = element.find('http-error');
			expect(component.length).toBe(1);
			expect(component.attr('status')).toEqual('404');
			expect(component.attr('title')).toEqual(`'${i18n.ERROR_404_TITLE}'`);
			expect(component.attr('message')).toEqual(`'${i18n.ERROR_404_MESSAGE}'`);
		});
	});
});
