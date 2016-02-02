/*jshint camelcase: false */

describe('Statistics Tooltip service', function () {
    'use strict';
    var stateMock;

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'TOOLTIP_MATCHING_FILTER': 'matching your filter',
            'TOOLTIP_MATCHING_FULL': 'in entire dataset'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(module('data-prep.services.statistics', function ($provide) {
        stateMock = {
            playground: {
                filter: {gridFilters: []},
                statistics:{
                    histogram:{
                        aggregation:null
                    }
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    describe('without filter', function () {
        it('should create tooltip for simple record', inject(function (StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [];
            var keyLabel = 'Occurrences';
            var key = '96ebf96df2';
            var primaryValue = 5;

            //when
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences: </strong><span style="color:yellow">5</span>' +
                '<br/><br/>' +
                '<strong>Record: </strong><span style="color:yellow">96ebf96df2</span>');
        }));

        it('should create tooltip for range record', inject(function (StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [];
            var keyLabel = 'Occurrences';
            var key = [-9.375, 2];
            var primaryValue = 10;

            //when
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences: </strong><span style="color:yellow">10</span>' +
                '<br/><br/>' +
                '<strong>Range: </strong><span style="color:yellow">[-9.375,2[</span>');
        }));

        it('should create tooltip for unique-value range record', inject(function (StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [];
            var keyLabel = 'Occurrences';
            var key = [2, 2];
            var primaryValue = 10;

            //when
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences: </strong><span style="color:yellow">10</span>' +
                '<br/><br/>' +
                '<strong>Value: </strong><span style="color:yellow">2</span>');
        }));
    });

    describe('with filters', function () {

        it('should create tooltip for simple record', inject(function ($rootScope, StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [{}];
            var keyLabel = 'Occurrences';
            var key = '96ebf96df2';
            var primaryValue = 5;
            var secondaryValue = 1;

            //when
            $rootScope.$digest();
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences matching your filter: </strong><span style="color:yellow">1 (20.0%)</span>' +
                '<br/><br/>' +
                '<strong>Occurrences in entire dataset: </strong><span style="color:yellow">5</span>' +
                '<br/><br/>' +
                '<strong>Record: </strong><span style="color:yellow">96ebf96df2</span>');
        }));

        it('should create tooltip for aggregation chart', inject(function ($rootScope, StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [{}];
            stateMock.playground.statistics.histogram.aggregation = {};
            var keyLabel = 'Average';
            var key = '96ebf96df2';
            var primaryValue = 5;
            var secondaryValue = 1;

            //when
            $rootScope.$digest();
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

            //then
            expect(tooltip).toBe(
                '<strong>Average matching your filter: </strong><span style="color:yellow">5</span>' +
                '<br/><br/>' +
                '<strong>Record: </strong><span style="color:yellow">96ebf96df2</span>');
        }));

        it('should create tooltip for range record', inject(function ($rootScope, StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [{}];
            var keyLabel = 'Occurrences';
            var key = [-9.375, 2];
            var primaryValue = 10;
            var secondaryValue = 5;

            //when
            $rootScope.$digest();
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences matching your filter: </strong><span style="color:yellow">5 (50.0%)</span>' +
                '<br/><br/>' +
                '<strong>Occurrences in entire dataset: </strong><span style="color:yellow">10</span>' +
                '<br/><br/>' +
                '<strong>Range: </strong><span style="color:yellow">[-9.375,2[</span>');
        }));

        it('should create tooltip for unique-value range record', inject(function ($rootScope, StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [{}];
            var keyLabel = 'Occurrences';
            var key = [2, 2];
            var primaryValue = 10;
            var secondaryValue = 5;

            //when
            $rootScope.$digest();
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, secondaryValue);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences matching your filter: </strong><span style="color:yellow">5 (50.0%)</span>' +
                '<br/><br/>' +
                '<strong>Occurrences in entire dataset: </strong><span style="color:yellow">10</span>' +
                '<br/><br/>' +
                '<strong>Value: </strong><span style="color:yellow">2</span>');
        }));

        it('should create tooltip without secondary data (not computed yet)', inject(function ($rootScope, StatisticsTooltipService) {
            //given
            stateMock.playground.filter.gridFilters = [{}];
            var keyLabel = 'Occurrences';
            var key = [2, 2];
            var primaryValue = 10;

            //when
            $rootScope.$digest();
            var tooltip = StatisticsTooltipService.getTooltip(keyLabel, key, primaryValue, undefined);

            //then
            expect(tooltip).toBe(
                '<strong>Occurrences matching your filter: </strong><span style="color:yellow"> (0%)</span>' +
                '<br/><br/>' +
                '<strong>Occurrences in entire dataset: </strong><span style="color:yellow">10</span>' +
                '<br/><br/>' +
                '<strong>Value: </strong><span style="color:yellow">2</span>');
        }));
    });
});