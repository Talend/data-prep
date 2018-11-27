/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('confirm service', () => {
	beforeEach(angular.mock.module('data-prep.services.confirm'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			a: 'translation_a',
			b: 'translation_b',
			c: 'translation_c',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject((StateService) => {
		spyOn(StateService, 'showConfirmationModal');
	}));

	it('should prompt', inject(($rootScope, StateService, ConfirmService) => {
		ConfirmService.confirm('a', ['a']);
		$rootScope.$digest();
		$rootScope.$digest();
		expect(StateService.showConfirmationModal).toHaveBeenCalled();
	}));
});
