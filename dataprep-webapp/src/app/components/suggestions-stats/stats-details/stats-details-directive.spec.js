/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import i18n from '../../../../i18n/en.json';

beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
	$translateProvider.translations('en', i18n);
	$translateProvider.preferredLanguage('en');
}));

describe('stats details directive', function () {
	'use strict';

	let stateMock, scope, element, createElement, ctrl;

	beforeEach(angular.mock.module('data-prep.stats-details', function ($provide) {
		stateMock = {
			playground: {
				statistics: {},
				grid: {
					selectedColumns: [{}]
				}
			},
			statistics: {
				loading: false,
			}
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(function ($rootScope, $compile) {
		scope = $rootScope.$new();

		createElement = function () {
			scope = $rootScope.$new();
			element = angular.element('<stats-details></stats-details>');
			$compile(element)(scope);
			scope.$digest();
			ctrl = element.controller('stats-details');
		};
	}));

	afterEach(function () {
		scope.$destroy();
		element.remove();
	});

	it('should render stats', function () {
		//given
		createElement();

		stateMock.playground.statistics.details = {
			common: {
				COUNT: 4,
				DISTINCT_COUNT: 5,
				DUPLICATE_COUNT: 6,
				VALID: 9,
				EMPTY: 7,
				INVALID: 8,
			},
			specific: {
				MIN: 10,
				MAX: 11,
				MEAN: 12,
				VARIANCE: 13,
			},
		};

		//when
		ctrl.selectedTab = 'stats-tab-value';
		scope.$apply();

		//then
		const tables = element.find('.stat-table');
		expect(tables.length).toBe(2);

		const table1tr = tables.eq(0).find('tr');
		expect(table1tr.eq(0).text().trim()).toBe('Count: 4');
		expect(table1tr.eq(1).text().trim()).toBe('Distinct: 5');
		expect(table1tr.eq(2).text().trim()).toBe('Duplicate: 6');
		expect(table1tr.eq(3).text().trim()).toBe('Valid: 9');
		expect(table1tr.eq(4).text().trim()).toBe('Empty: 7');
		expect(table1tr.eq(5).text().trim()).toBe('Invalid: 8');

		const table2tr = tables.eq(1).find('tr');
		expect(table2tr.eq(0).text().trim()).toBe('MIN: 10');
		expect(table2tr.eq(1).text().trim()).toBe('MAX: 11');
		expect(table2tr.eq(2).text().trim()).toBe('Mean: 12');
		expect(table2tr.eq(3).text().trim()).toBe('Variance: 13');
	});

	it('should render a loader if statistics are computins', () => {
		createElement();

		stateMock.playground.statistics.loading = true;
		scope.$apply();

		expect(element.find('loader').length).toBe(1);
	});
});
