/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const i18n = {
	'REFRESHING_WAIT': 'refreshing, please wait…',
	'CUSTOM_REFRESHING_WAIT': 'custom…',
};

describe('Loader component', () => {
	let scope;
	let element;
	let createElement;

	beforeEach(angular.mock.module('data-prep.loader'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = `<loader translate-once-key="translateOnceKey"></loader>`;
			element = $compile(html)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {

		it('should render loader', () => {
			// given
			createElement();

			// then
			expect(element.length).toBe(1);
			expect(element.text().trim()).toBe(i18n.REFRESHING_WAIT);
		});

		it('should render another key', () => {
			// given
			scope.translateOnceKey = 'CUSTOM_REFRESHING_WAIT';
			createElement();

			// when
			scope.$digest();

			// then
			expect(element.length).toBe(1);
			expect(element.text().trim()).toBe(i18n.CUSTOM_REFRESHING_WAIT);
		});
	});
});
