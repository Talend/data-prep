/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Title service', () => {
	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en_US', {
			MY_TITLE: 'MY_TITLE_VALUE',
			DATA_PREPARATION: 'DATA_PREPARATION_VALUE',
		});
		$translateProvider.preferredLanguage('en_US');
	}));

	beforeEach(angular.mock.module('data-prep.services.utils'));

	describe('set', () => {
		it('should set translated title', inject(($rootScope, $window, TitleService) => {
			//given
			const titleKey = 'MY_TITLE';

			expect($window.document.title).not.toBe('MY_TITLE_VALUE | Talend');

			//when
			TitleService.set(titleKey);
			$rootScope.$apply();

			//then
			expect($window.document.title).toBe('MY_TITLE_VALUE | Talend');
		}));
	});

	describe('setStrict', () => {
		it('should set title', inject(($window, TitleService) => {
			//given
			const titleKey = 'TITLE';

			expect($window.document.title).not.toBe('TITLE | Talend');

			//when
			TitleService.setStrict(titleKey);

			//then
			expect($window.document.title).toBe('TITLE | Talend');
		}));
	});

	describe('reset', () => {
		it('should reset title', inject(($rootScope, $window, TitleService) => {
			//given
			expect($window.document.title).not.toBe('DATA_PREPARATION_VALUE | Talend');

			//when
			TitleService.reset();
			$rootScope.$apply();

			//then
			expect($window.document.title).toBe('DATA_PREPARATION_VALUE | Talend');
		}));
	});
});
