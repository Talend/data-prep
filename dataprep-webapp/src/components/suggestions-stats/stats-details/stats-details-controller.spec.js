describe('Stats-details controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.stats-details'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('StatsDetailsCtrl', {
                $scope: scope
            });
        };
    }));

    it('should bind statistics getter to StatisticsService.statistics', inject(function (StatisticsService) {
        //given
        var ctrl = createController();
        expect(ctrl.statistics).toBeFalsy();

        var stats = {};

        //when
        StatisticsService.statistics = stats;

        //then
        expect(ctrl.statistics).toBe(stats);
    }));

    it('should bind boxplotData getter to StatisticsService.boxplotData', inject(function (StatisticsService) {
        //given
        var ctrl = createController();
        expect(ctrl.statistics).toBeFalsy();

        var data = {};

        //when
        StatisticsService.boxPlot = data;

        //then
        expect(ctrl.boxPlot).toBe(data);
    }));

    it('should bind patternFrequencyTable getter to SuggestionService.currentColumn.patternFrequencyTable', inject(function (SuggestionService) {
        //given
        var ctrl = createController();
        expect(ctrl.patternFrequencyTable).toBeFalsy();

        var patternFrequencyTable = {};

        //when
        SuggestionService.currentColumn = {statistics: {patternFrequencyTable: patternFrequencyTable}};

        //then
        expect(ctrl.patternFrequencyTable).toBe(patternFrequencyTable);
    }));

});