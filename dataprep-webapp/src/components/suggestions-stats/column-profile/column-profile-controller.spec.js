describe('ColumnProfile controller', function () {
    'use strict';

    var createController, scope;

	beforeEach(module('data-prep.column-profile'));
	beforeEach(module('data-prep.suggestions-stats'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('ColumnProfileCtrl', {
                $scope: scope
            });
            return ctrl;
        };
    }));

    it('should call addFilter Function of the StatisticsService', inject(function (StatisticsService) {
        //given
        spyOn(StatisticsService, 'addFilter').and.returnValue();
        var ctrl = createController();
        var obj = {'data': 'Ulysse', 'occurrences': 5};

        //when
        ctrl.barchartClickFn(obj);

        //then
        expect(StatisticsService.addFilter).toHaveBeenCalledWith(obj.data);
    }));

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

        it('should change aggregation chart with preparation and step id', inject(function(StatisticsService, PlaygroundService, PreparationService, RecipeService) {
            //given
            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            var ctrl = createController();

            var datasetId = '13654634856752';
            var preparationId = '5463514684';
            var stepId = '698656896987486';
            var sampleSize = 500;
            var column = {id: '0001'};
            var aggregation = {name: 'MAX'};

            PlaygroundService.currentMetadata = {id: datasetId};
            PlaygroundService.selectedSampleSize = {value: sampleSize};
            PreparationService.currentPreparationId = preparationId;
            spyOn(RecipeService, 'getLastActiveStep').and.returnValue({id: stepId});

            //when
            ctrl.changeAggregation(column, aggregation);

            //then
            expect(StatisticsService.processAggregation).toHaveBeenCalledWith(datasetId, preparationId, stepId, sampleSize, column, aggregation);
        }));

        it('should change aggregation chart with dataset id (no preparation)', inject(function(StatisticsService, PlaygroundService, PreparationService, RecipeService) {
            //given
            spyOn(StatisticsService, 'processAggregation').and.returnValue();
            var ctrl = createController();

            var datasetId = '13654634856752';
            var column = {id: '0001'};
            var sampleSize = 500;
            var aggregation = {name: 'MAX'};

            PlaygroundService.currentMetadata = {id: datasetId};
            PlaygroundService.selectedSampleSize = {value: sampleSize};
            PreparationService.currentPreparationId = null;
            spyOn(RecipeService, 'getLastActiveStep').and.callFake(function() {
                throw new Error('should NOT call RecipeService because there is no preparation');
            });

            //when
            ctrl.changeAggregation(column, aggregation);

            //then
            expect(StatisticsService.processAggregation).toHaveBeenCalledWith(datasetId, null, null, sampleSize, column, aggregation);
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
});