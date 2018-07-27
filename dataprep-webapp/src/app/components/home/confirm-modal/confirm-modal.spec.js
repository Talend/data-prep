/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('confirm modal component', () => {
	let scope;
	let element;
	let createElement;
	let mock;
	let controller;
	const body = angular.element('body');


	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.home', ($provide) => {
		mock = {
			confirm: {
				visible: true,
				texts: [],
			},
		};
		$provide.constant('state', mock);
	}));

	beforeEach(inject(($rootScope, $compile, ConfirmService) => {
		scope = $rootScope.$new(true);
		spyOn(ConfirmService, 'resolve').and.returnValue();
		spyOn(ConfirmService, 'reject').and.returnValue();

		createElement = () => {
			const html = `<confirm-modal></confirm-modal>`;
			element = $compile(html)(scope);
			body.append(element);
			scope.$digest();
			controller = element.controller('confirm-modal');
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render', () => {
			mock.confirm.texts = ['a', 'b'];
			createElement();
			scope.$digest();

			expect(body.find('.confirm-modal .confirm-content').length).toBe(2);
		});

		it('should call resolve', inject(($rootScope, StateService, ConfirmService) => {
			createElement();
			scope.$digest();

			body.find('.confirm-modal .modal-primary-button').eq(0).click();

			expect(ConfirmService.resolve).toHaveBeenCalled();
		}));


		it('should call reject', inject(($rootScope, StateService, ConfirmService) => {
			createElement();
			scope.$digest();

			body.find('.confirm-modal .modal-secondary-button').eq(0).click();

			expect(ConfirmService.reject).toHaveBeenCalled();
		}));
	});
});
