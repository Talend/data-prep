import i18n from '../../../i18n/en.json';

describe('Statistics Tooltip service', function () {
	'use strict';
	let stateMock;

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.services.statistics', function ($provide) {
		stateMock = {
			playground: {
				filter: { gridFilters: [] },
				statistics: {
					histogram: {
						aggregation: null,
					},
					rangeLimits: {
						min: -10,
						max: 5,

					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('without filter', function () {
		it('should create tooltip for simple record', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = '96ebf96df2';
			const primaryValue = 5;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">5</span><br/><br/><strong>${i18n.RECORD}${i18n.COLON}</strong> <span style="color:yellow">96ebf96df2</span>`);
		}));

		it('should create tooltip for range record with min > min data values and  max < max data values', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = [-9.375, 2];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.RANGE}${i18n.COLON}</strong> <span style="color:yellow">[-9.375,2[</span>`);
		}));

		it('should create tooltip for range record with min < min data values and  max < max data values', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = [-15, 2];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.RANGE}${i18n.COLON}</strong> <span style="color:yellow">[MIN,2[</span>`);
		}));

		it('should create tooltip for range record with min < min data values and  max >= max data values', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = [-15, 10];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.RANGE}${i18n.COLON}</strong> <span style="color:yellow">[MIN,MAX]</span>`);
		}));

		it('should create tooltip for range record with min > min data values and  max >= max data values', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = [-1, 10];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.RANGE}${i18n.COLON}</strong> <span style="color:yellow">[-1,MAX]</span>`);
		}));

		it('should create tooltip for unique-value range record', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [];
			const keyLabel = 'Occurrences';
			const key = [2, 2];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.VALUE}${i18n.COLON}</strong> <span style="color:yellow">2</span>`);
		}));
	});

	describe('with filters', function () {
		it('should create tooltip for simple record', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [{}];
			const keyLabel = 'Occurrences';
			const key = '96ebf96df2';
			const primaryValue = 5;
			const secondaryValue = 1;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

			//then
			expect(tooltip).toBe(`<strong>Occurrences ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow">1 (20.0%)</span><br/><br/><strong>Occurrences ${i18n.TOOLTIP_MATCHING_FULL}${i18n.COLON}</strong> <span style="color:yellow">5</span><br/><br/><strong>${i18n.RECORD}${i18n.COLON}</strong> <span style="color:yellow">96ebf96df2</span>`);
		}));

		it('should create tooltip for aggregation chart', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [{}];
			stateMock.playground.statistics.histogram.aggregation = {};
			const keyLabel = 'Average';
			const key = '96ebf96df2';
			const primaryValue = 5;
			const secondaryValue = 1;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

			//then
			expect(tooltip).toBe(`<strong>Average ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow">5</span><br/><br/><strong>${i18n.RECORD}${i18n.COLON}</strong> <span style="color:yellow">96ebf96df2</span>`);
		}));

		it('should create tooltip for range record', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [{}];
			const keyLabel = 'Occurrences';
			const key = [-9.375, 2];
			const primaryValue = 10;
			const secondaryValue = 5;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

			//then
			expect(tooltip).toBe(`<strong>Occurrences ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow">5 (50.0%)</span><br/><br/><strong>Occurrences ${i18n.TOOLTIP_MATCHING_FULL}${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.RANGE}${i18n.COLON}</strong> <span style="color:yellow">[-9.375,2[</span>`);
		}));

		it('should create tooltip for unique-value range record', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [{}];
			const keyLabel = 'Occurrences';
			const key = [2, 2];
			const primaryValue = 10;
			const secondaryValue = 5;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

			//then
			expect(tooltip).toBe(`<strong>Occurrences ${i18n.TOOLTIP_MATCHING_FILTER}${i18n.COLON}</strong> <span style="color:yellow">5 (50.0%)</span><br/><br/><strong>Occurrences ${i18n.TOOLTIP_MATCHING_FULL}${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.VALUE}${i18n.COLON}</strong> <span style="color:yellow">2</span>`);
		}));

		it('should create tooltip without secondary data (not computed yet)', inject(function ($rootScope, StatisticsTooltipService) {
			//given
			stateMock.playground.filter.gridFilters = [{}];
			const keyLabel = 'Occurrences';
			const key = [2, 2];
			const primaryValue = 10;

			//when
			$rootScope.$digest();
			const tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

			//then
			expect(tooltip).toBe(`<strong>Occurrences${i18n.COLON}</strong> <span style="color:yellow">10</span><br/><br/><strong>${i18n.VALUE}${i18n.COLON}</strong> <span style="color:yellow">2</span>`);
		}));
	});
});
