/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Confirm widget service', () => {
	'use strict';

	beforeEach(angular.mock.module('talend.widget'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en_US', {
			TEXT_1: 'TEXT_1_VALUE',
			TEXT_2: 'TEXT_2_VALUE',
			TEXT_3: 'TEXT_3_VALUE : {{argValue}}',
		});
		$translateProvider.preferredLanguage('en_US');
	}));

	afterEach(inject(($timeout, TalendConfirmService) => {
		if (TalendConfirmService.element) {
			TalendConfirmService.resolve();
			$timeout.flush();
		}
	}));

	it('should create scope and confirm element with options', inject(($rootScope, TalendConfirmService) => {
		//given
		const text1 = 'TEXT_1';
		const text2 = 'TEXT_2';
		const body = angular.element('body');

		expect(TalendConfirmService.modalScope).toBeFalsy();
		expect(TalendConfirmService.element).toBeFalsy();
		expect(body.has('talend-confirm').length).toBe(0);

		//when
		TalendConfirmService.confirm([text1, text2]);
		$rootScope.$digest();

		//then
		expect(TalendConfirmService.modalScope).toBeTruthy();
		expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_2_VALUE']);
		expect(TalendConfirmService.element).toBeTruthy();
		expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

		expect(body.has('talend-confirm').length).toBe(1);
	}));

	it('should create scope and confirm element without options', inject(($rootScope, TalendConfirmService) => {
		//given
		const text1 = 'TEXT_1';
		const text2 = 'TEXT_2';
		const body = angular.element('body');

		expect(TalendConfirmService.modalScope).toBeFalsy();
		expect(TalendConfirmService.element).toBeFalsy();
		expect(body.has('talend-confirm').length).toBe(0);

		//when
		TalendConfirmService.confirm([text1, text2]);
		$rootScope.$digest();

		//then
		expect(TalendConfirmService.modalScope).toBeTruthy();
		expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_2_VALUE']);
		expect(TalendConfirmService.element).toBeTruthy();
		expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

		expect(body.has('talend-confirm').length).toBe(1);
	}));

	it('should create scope and confirm element with translate arguments', inject(($rootScope, TalendConfirmService) => {
		//given
		const text1 = 'TEXT_1';
		const text3 = 'TEXT_3';
		const body = angular.element('body');

		expect(TalendConfirmService.modalScope).toBeFalsy();
		expect(TalendConfirmService.element).toBeFalsy();
		expect(body.has('talend-confirm').length).toBe(0);

		//when
		TalendConfirmService.confirm([text1, text3], { argValue: 'my value' });
		$rootScope.$digest();

		//then
		expect(TalendConfirmService.modalScope).toBeTruthy();
		expect(TalendConfirmService.modalScope.texts).toEqual(['TEXT_1_VALUE', 'TEXT_3_VALUE : my value']);
		expect(TalendConfirmService.element).toBeTruthy();
		expect(TalendConfirmService.element.scope()).toBe(TalendConfirmService.modalScope);

		expect(body.has('talend-confirm').length).toBe(1);
	}));

	describe('with existing confirm', () => {
		let promise;
		let element;
		let scope;

		beforeEach(inject(($rootScope, TalendConfirmService) => {
			promise = TalendConfirmService.confirm();
			$rootScope.$digest();

			scope = TalendConfirmService.modalScope;
			element = TalendConfirmService.element;

			spyOn(element, 'remove').and.returnValue();
		}));

		it('should throw error on confirm create but another confirm modal is already created', inject(($timeout, TalendConfirmService) => {
			//when
			try {
				TalendConfirmService.confirm();
			} catch (error) {
				// then
				expect(error.message).toBe('A confirm popup is already created');
				TalendConfirmService.resolve();
				$timeout.flush();
				return;
			}

			throw Error('should have thrown error on second confirm() call');
		}));

		it('should resolve promise and remove/destroy scope and element', inject(($timeout, TalendConfirmService) => {
			//given
			let resolved = false;
			let scopeDestroyed = false;

			promise.then(() => {
				resolved = true;
			});

			scope.$on('$destroy', () => {
				scopeDestroyed = true;
			});

			//when
			TalendConfirmService.resolve();
			$timeout.flush();

			//then
			expect(resolved).toBe(true);
			expect(scopeDestroyed).toBe(true);
			expect(element.remove).toHaveBeenCalled();
			expect(TalendConfirmService.modalScope).toBeFalsy();
			expect(TalendConfirmService.element).toBeFalsy();
		}));

		it('should reject promise and remove/destroy scope and element', inject(($timeout, TalendConfirmService) => {
			//given
			let cause = false;
			let scopeDestroyed = false;

			promise.catch((error) => {
				cause = error;
			});

			scope.$on('$destroy', () => {
				scopeDestroyed = true;
			});

			//when
			TalendConfirmService.reject('dismiss');
			$timeout.flush();

			//then
			expect(cause).toBe('dismiss');
			expect(scopeDestroyed).toBe(true);
			expect(element.remove).toHaveBeenCalled();
			expect(TalendConfirmService.modalScope).toBeFalsy();
			expect(TalendConfirmService.element).toBeFalsy();
		}));
	});
});
