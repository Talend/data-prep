/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const i18n = {};

describe('History control directive', function () {
	'use strict';

	let createElement;
	let element;
	let scope;
	let body;

	beforeEach(angular.mock.module('data-prep.history-control'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(function ($rootScope, $compile, HistoryService) {

		body = angular.element('body');
		scope = $rootScope.$new();

		createElement = function () {
			element = angular.element('<history-control></history-control>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};

		spyOn(HistoryService, 'undo').and.returnValue();
		spyOn(HistoryService, 'redo').and.returnValue();
	}));

	afterEach(function () {
		scope.$destroy();
		element.remove();
	});

	it('should attach undo to Ctrl+Z shortcut', inject(function ($document, HistoryService) {
		//given
		createElement();

		const event = angular.element.Event('keydown');
		event.keyCode = 90;
		event.ctrlKey = true;

		//when
		$document.trigger(event);

		//then
		expect(HistoryService.undo).toHaveBeenCalled();
	}));

	it('should attach redo to Ctrl+Y shortcut', inject(function ($document, HistoryService) {
		//given
		createElement();

		const event = angular.element.Event('keydown');
		event.keyCode = 89;
		event.ctrlKey = true;

		//when
		$document.trigger(event);

		//then
		expect(HistoryService.redo).toHaveBeenCalled();
	}));

	it('should unregister listener on destroy', inject(function ($document) {
		//given
		expect($._data(angular.element($document)[0], 'events').keydown).not.toBeDefined();
		createElement();

		expect($._data(angular.element($document)[0], 'events').keydown).toBeDefined();

		//when
		scope.$destroy();

		//then
		expect($._data(angular.element($document)[0], 'events').keydown).not.toBeDefined();
	}));
});
