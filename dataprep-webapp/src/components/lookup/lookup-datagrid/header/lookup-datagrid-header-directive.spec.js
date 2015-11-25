describe('Dataset column header directive', function () {
	'use strict';
	var scope, createElement, element, ctrl;
	var body = angular.element('body');
	var column = {
		'id': '0001',
		'name': 'MostPopulousCity',
		'quality': {
			'empty': 5,
			'invalid': 10,
			'valid': 72
		},
		'type': 'string'
	};

	var added = {isAdded: false};

	beforeEach(module('data-prep.lookup-datagrid-header'));
	beforeEach(module('htmlTemplates'));

	beforeEach(inject(function ($rootScope, $compile, $timeout) {
		scope = $rootScope.$new(true);
		scope.column = column;
		scope.added = added;

		createElement = function () {
			element = angular.element('<lookup-datagrid-header column="column" added="added"></lookup-datagrid-header>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
			$timeout.flush();

			ctrl = element.controller('lookupDatagridHeader');
			spyOn(ctrl, 'showHideCheckbox').and.returnValue(true);
			scope.$digest();
		};
	}));


	afterEach(function () {
		scope.$destroy();
		element.remove();
	});

	it('should checkbox checked', function () {
		//given
		scope.added = {isAdded: true};

		//when
		createElement();

		//then
		expect(element.find('input[type=checkbox]').is(':checked')).toBeTruthy();
	});

	it('should checkbox UNchecked', function () {
		//given
		scope.added = {isAdded: false};

		//when
		createElement();

		//then
		expect(element.find('input[type=checkbox]').is(':checked')).toBeFalsy();
	});

	it('should display column title and type when there is no domain', function () {
		//when
		createElement();

		//then
		expect(element.find('.grid-header-title').text()).toBe('MostPopulousCity');
		expect(element.find('.grid-header-type').text()).toBe('text');
	});

});