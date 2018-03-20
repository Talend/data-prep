/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset upload tile directive', function () {
	var scope;
	var createElement;
	const translations = {
		UPLOAD_PROCESSING: 'Profiling data, please wait...',
	};

	beforeEach(angular.mock.module('data-prep.dataset-upload-status'));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', translations);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(function ($rootScope, $compile) {
		scope = $rootScope.$new();
		createElement = function (directiveScope) {
			var element = angular.element('<dataset-upload-status dataset="dataset"></dataset-upload-status>');
			$compile(element)(directiveScope);
			directiveScope.$digest();
			return element;
		};
	}));

	it('should render progressing upload dataset', function () {
		// given
		scope.dataset = {
			name: 'Customers (50 lines)',
			progress: 10,
			error: false,
			type: 'file'
		};

		// when
		var element = createElement(scope);
		var name = element.find('.inventory-title').first();
		var progress = element.find('.inventory-progress').first();

		// then
		expect(name.text()).toBe('Customers (50 lines)');
		expect(progress.text().trim()).toBe('10 %');
		expect(progress.hasClass('error')).toBe(false);
	});

	it('should show profiling data message once the upload reaches the 100%', function () {
		//given
		scope.dataset = {
			name: 'Customers (50 lines)',
			progress: 100,
			error: false,
			type: 'file',
		};

		//when
		var element = createElement(scope);
		var progress = element.find('.inventory-progress').first();

		//then
		expect(progress.text().trim()).toBe(translations.UPLOAD_PROCESSING);
	});

	it('should render progressing remote dataset import', function () {
		//given
		scope.dataset = {
			name: 'remote 1',
			progress: 0,
			error: false,
			type: 'remote',
		};

		//when
		var element = createElement(scope);
		var name = element.find('.inventory-title').first();
		var progress = element.find('.inventory-progress').first();

		//then
		expect(name.text()).toBe('remote 1');
		expect(progress.hasClass('error')).toBe(false);
	});

	it('should render upload error dataset', function () {
		//given
		scope.dataset = {
			name: 'Customers (50 lines)',
			progress: 10,
			error: true,
		};

		//when
		var element = createElement(scope);
		var name = element.find('.inventory-title').first();
		var progress = element.find('.inventory-progress').first();

		//then
		expect(name.text()).toBe('Customers (50 lines)');
		expect(progress.text().trim()).not.toBe('10 %');
		expect(progress.hasClass('error')).toBe(true);
	});
});
