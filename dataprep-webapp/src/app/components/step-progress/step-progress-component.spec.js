/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Step Progress component', () => {
	let scope;
	let element;
	let createElement;
	let stateMock;
	let controller;

	beforeEach(angular.mock.module('data-prep.step-progress'));
	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile, ProgressService) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = `<step-progress></step-progress>`;
			element = $compile(html)(scope);
			scope.$digest();
			controller = element.controller('step-progress');
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render 2 steps', inject(function ($rootScope, ProgressService) {
			// given
			const mock = {
				title: 'test',
				steps: [
					{ label: '1' },
					{ label: '2' },
				]
			};
			ProgressService.start(mock);
			// when
			createElement();
			scope.$digest();

			// then
			expect(element.find('.step').length).toBe(2);
		}));

		it('should not render steps without label', inject(function ($rootScope, ProgressService) {
			// given
			const mock = {
				title: 'test',
				steps: [
					{ label: '1' },
					{},
				]
			};
			ProgressService.start(mock);

			// when
			createElement();
			scope.$digest();

			// then
			expect(element.find('.step').length).toBe(1);
		}));

		it('should indicates an in progress upload', inject(function ($rootScope, ProgressService) {
			// given
			const mock = {
				title: 'test',
				steps: [
					{ id: 'mock', name: 'Mock', state: 'IN_PROGRESS', label: 'A' },
					{ id: 'mock', name: 'Mock', type: 'INFINITE', label: 'B', state: 'FUTURE' },
				]
			};
			ProgressService.start(mock);

			// when
			createElement();
			scope.$digest();

			// then
			const steps = element.find('.step');
			expect(steps.eq(0).hasClass('in-progress')).toBe(true);
			expect(steps.eq(1).hasClass('future')).toBe(true);
		}));

		it('should indicates an in progress profiling', inject(function ($rootScope, ProgressService) {
			// given
			const mock = {
				title: 'test',
				steps: [
					{ id: 'mock', name: 'Mock', state: 'COMPLETE', label: 'A' },
					{ id: 'mock', name: 'Mock', type: 'INFINITE', state: 'IN_PROGRESS', label: 'B' },
				]
			};
			ProgressService.start(mock);

			// when
			createElement();
			scope.$digest();

			// then
			const steps = element.find('.step');
			expect(steps.eq(0).hasClass('complete')).toBe(true);
			expect(steps.eq(1).hasClass('in-progress')).toBe(true);
			expect(steps.eq(0).hasClass('future')).toBe(false);
		}));
	});
});
