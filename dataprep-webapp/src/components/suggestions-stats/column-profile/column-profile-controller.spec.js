describe('ColumnProfile controller', function () {
    'use strict';

    var createController, scope;

    var stateMock;

    beforeEach(module('data-prep.column-profile', function($provide) {
        stateMock = {playground: {grid: {}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('ColumnProfileCtrl', {
                $scope: scope
            });
        };
    }));

    describe('filter', function() {
        beforeEach(inject(function(FilterService, StatisticsService) {
            spyOn(FilterService, 'addFilterAndDigest').and.returnValue();
            spyOn(StatisticsService, 'addRangeFilter').and.returnValue();
        }));

        it('should add a new "exact" filter', inject(function (FilterService) {
            //given
            var ctrl = createController();
            var obj = {'data': 'Ulysse', 'occurrences': 5};

            stateMock.playground.grid.selectedColumn = {
                id: '0001',
                name: 'firstname'
            };

            //when
            ctrl.addBarchartFilter(obj);

            //then
            expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('exact', '0001', 'firstname', {phrase: 'Ulysse', caseSensitive: true});
        }));

        it('should add a new "range" filter from StatisticsService', inject(function (StatisticsService) {
            //given
            var ctrl = createController();
            var obj = {data : {min: '5', max: '15'}};

            //when
            ctrl.addRangeFilter(obj);

            //then
            expect(StatisticsService.addRangeFilter).toHaveBeenCalledWith(obj.data);
        }));

        it('should add a new "empty_records" filter from exact_filter on barchart click callback', inject(function (StatisticsService, FilterService) {
            //given
            var ctrl = createController();
            var obj = {'data': '', 'occurrences': 5};

            stateMock.playground.grid.selectedColumn = {
                id: '0001',
                name: 'firstname'
            };

            //when
            ctrl.addBarchartFilter(obj);

            //then
            expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('empty_records', '0001', 'firstname');
        }));
    });

    describe('external bindings', function() {
        it('should bind histogram getter to StatisticsService.histogram', inject(function (StatisticsService) {
            //given
            var data = {};
            var ctrl = createController();

            //when
            StatisticsService.histogram = data;

            //then
            expect(ctrl.histogram).toBe(data);
        }));

        it('should bind aggregationColumns getter to StatisticsService.getAggregationColumns()', inject(function (StatisticsService) {
            //given
            var ctrl = createController();

            var numericColumns = [{id: '0001'}, {id: '0002'}];
            spyOn(StatisticsService, 'getAggregationColumns').and.returnValue(numericColumns);

            //then
            expect(ctrl.aggregationColumns).toBe(numericColumns);
        }));
    });

    describe('aggregation', function() {
        it('should get the current aggregation name', inject(function(StatisticsService) {
            //given
            var ctrl = createController();
            StatisticsService.histogram = {
                aggregation: 'MAX'
            };

            //when
            var aggregation = ctrl.getCurrentAggregation();

            //then
            expect(aggregation).toBe('MAX');
        }));

        it('should get the default aggregation name when there is no histogram', inject(function(StatisticsService) {
            //given
            var ctrl = createController();
            StatisticsService.histogram = null;

            //when
            var aggregation = ctrl.getCurrentAggregation();

            //then
            expect(aggregation).toBe('LINE_COUNT');
        }));

        it('should get the default aggregation name when histogram is not an aggregation', inject(function(StatisticsService) {
            //given
            var ctrl = createController();
            StatisticsService.histogram = {data: []};

            //when
            var aggregation = ctrl.getCurrentAggregation();

            //then
            expect(aggregation).toBe('LINE_COUNT');
        }));

        it('should change aggregation chart', inject(function(StatisticsService) {
            //given
            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            var ctrl = createController();

            var column = {id: '0001'};
            var aggregation = {name: 'MAX'};

            //when
            ctrl.changeAggregation(column, aggregation);

            //then
            expect(StatisticsService.processAggregation).toHaveBeenCalledWith(column, aggregation);
        }));

        it('should do nothing if the current histogram is already the wanted aggregation', inject(function(StatisticsService) {
            //given
            var column = {id: '0001'};
            var aggregation = {name: 'MAX'};

            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            StatisticsService.histogram = {
                aggregation: aggregation,
                aggregationColumn: column
            };

            var ctrl = createController();

            //when
            ctrl.changeAggregation(column, aggregation);

            //then
            expect(StatisticsService.processAggregation).not.toHaveBeenCalled();
        }));
    });

    describe('statistics', function() {
        beforeEach(inject(function($q, PlaygroundService) {
            spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());
        }));

        it('should manage refresh progress flag', function() {
            //given
            var ctrl = createController();
            expect(ctrl.refreshInProgress).toBe(false);

            //when
            ctrl.refresh();
            expect(ctrl.refreshInProgress).toBe(true);
            scope.$digest();

            //then
            expect(ctrl.refreshInProgress).toBe(false);
        });

        it('should trigger statistics refresh', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();
            expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

            //when
            ctrl.refresh();

            //then
            expect(PlaygroundService.updateStatistics).toHaveBeenCalled();
        }));
    });
});