/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Progress component', () => {
	let scope;
	let element;
	let createElement;
	let stateMock;
	let controller;

	beforeEach(angular.mock.module('data-prep.step-progress', ($provide) => {
		stateMock = {
			dataset: {
				uploadingDataset:  null,
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);

		createElement = () => {
			const html = `<progress></progress>`;
			element = $compile(html)(scope);
			scope.$digest();
			controller = element.controller('progress');
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should indicates an in progress upload', inject(function ($rootScope) {
			// given
			stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 50 };

			// when
			createElement();

			// then
			expect(element.find('.upload-step').hasClass('in-progress')).toBe(true);
			expect(element.find('.profiling-step').hasClass('future')).toBe(true);
		}));

		it('should indicates an in progress profiling', inject(function ($rootScope) {
			// given
			stateMock.dataset.uploadingDataset = { id: 'mock', name: 'Mock', progress: 100 };

			// when
			createElement();

			// then
			expect(element.find('.upload-step').hasClass('complete')).toBe(true);
			expect(element.find('.profiling-step').hasClass('in-progress')).toBe(true);
			expect(element.find('.upload-step').hasClass('future')).toBe(false);
		}));
	});
});
