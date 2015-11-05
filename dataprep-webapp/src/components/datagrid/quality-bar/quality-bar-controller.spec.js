describe('Quality bar controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.quality-bar'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('QualityBarCtrl', {
                $scope: scope
            });
        };
    }));

    it('should calculate simple hash from quality values', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 10,
            empty: 5,
            valid: 72
        };

        //when
        var hash = ctrl.hashQuality();

        //then
        expect(hash).toBe('51072');
    });

    it('should compute rounded percentages', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 10,
            empty: 5,
            valid: 72
        };

        //when
        ctrl.computePercent();

        //then
        expect(ctrl.percent.empty).toBe(6);
        expect(ctrl.percent.invalid).toBe(11);
        expect(ctrl.percent.valid).toBe(83);
    });

    it('should compute width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 33,
            valid: 68
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(26);
        expect(ctrl.width.invalid).toBe(20);
        expect(ctrl.width.valid).toBe(54);
    });

    it('should set min width to empty width, and reduce the other width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 1,
            valid: 68
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(10);
        expect(ctrl.width.invalid).toBe(24);
        expect(ctrl.width.valid).toBe(66);
    });

    it('should set min width to invalid width, and reduce the other width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 1,
            empty: 25,
            valid: 68
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(24);
        expect(ctrl.width.invalid).toBe(10);
        expect(ctrl.width.valid).toBe(66);
    });

    it('should set min width to valid width, and reduce the other width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 68,
            valid: 1
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(66);
        expect(ctrl.width.invalid).toBe(24);
        expect(ctrl.width.valid).toBe(10);
    });

    it('should set 0 to empty width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 0,
            valid: 68
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(0);
        expect(ctrl.width.invalid).toBe(27);
        expect(ctrl.width.valid).toBe(73);
    });

    it('should set 0 to invalid width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 0,
            empty: 25,
            valid: 68
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(27);
        expect(ctrl.width.invalid).toBe(0);
        expect(ctrl.width.valid).toBe(73);
    });

    it('should set 0 to valid width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 25,
            empty: 68,
            valid: 0
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(73);
        expect(ctrl.width.invalid).toBe(27);
        expect(ctrl.width.valid).toBe(0);
    });

    it('should reduce width to the bigger only when the others are at minimal width', function () {
        //given
        var ctrl = createController();
        ctrl.quality = {
            invalid: 100000,
            empty: 1,
            valid: 1
        };

        //when
        ctrl.computePercent();
        ctrl.computeQualityWidth();

        //then
        expect(ctrl.width.empty).toBe(10);
        expect(ctrl.width.invalid).toBe(80);
        expect(ctrl.width.valid).toBe(10);
    });

    it('should apply selected transformation on the column scope', inject(function(TransformationApplicationService) {
        //given
        var ctrl = createController();
        var transfo = {name:'delete_empty', parameters:null};
        spyOn(TransformationApplicationService,'append').and.returnValue(function(){});

        //when
        ctrl.applyActionOnColumn(transfo);

        //then
        expect(TransformationApplicationService.append).toHaveBeenCalledWith(transfo, 'column');
    }));

    describe('when dealing with filters', function() {

        beforeEach(inject(function(FilterService) {
            spyOn(FilterService, 'addFilter').and.returnValue();
        }));

        it('should set filter on invalid records', inject(function(FilterService) {

            //given
            var ctrl = createController();
            var col = {
                'id': '0000',
                'name': 'MostPopulousCity',
                'quality': {
                    'empty': 0,
                    'invalid': 10,
                    'valid': 90,
                    'invalidValues': ['AA', 'AB', 'BA']
                },
                'type': 'string'
            };

            //when
            ctrl.filterInvalidRecords(col);

            //then
            expect(FilterService.addFilter).toHaveBeenCalledWith('invalid_records', col.id, col.name);
        }));

        it('should set filter on valid records', inject(function(FilterService) {

            //given
            var ctrl = createController();
            var col = {
                'id': '0000',
                'name': 'MostPopulousCity',
                'quality': {
                    'empty': 0,
                    'invalid': 10,
                    'valid': 90,
                    'invalidValues': ['AA', 'AB', 'BA']
                },
                'type': 'string'
            };

            //when
            ctrl.filterValidRecords(col);

            //then
            expect(FilterService.addFilter).toHaveBeenCalledWith('valid_records', col.id, col.name);
        }));

        it('should set filter on empty records', inject(function(FilterService) {

            //given
            var ctrl = createController();
            var col = {
                'id': '0001',
                'name': 'age',
                'quality': {
                    'empty': 10,
                    'invalid': 0,
                    'valid': 90
                },
                'type': 'integer'
            };

            //when
            ctrl.filterEmptyRecords(col);

            //then
            expect(FilterService.addFilter).toHaveBeenCalledWith('empty_records', col.id, col.name);
        }));
    });

});