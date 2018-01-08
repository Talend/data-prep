/*  ============================================================================
 Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE
 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France
 ============================================================================*/
describe('Name Modal component', () => {
	let scope;
	let element;
	let createElement;

	const body = angular.element('body');

	beforeEach(angular.mock.module('data-prep.home'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element(`<name-modal></name-modal>`);
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should NOT render name modal', inject((ImportService) => {
			ImportService.datasetNameModal = false;

			// when
			createElement();

			// then
			expect(body.find('.form-control').length).toBe(0);
		}));

		it('should render name modal', inject((ImportService) => {
			ImportService.datasetNameModal = true;

			// when
			createElement();

			// then
			expect(body.find('.form-control').length).toBe(1);
		}));
	});
});
