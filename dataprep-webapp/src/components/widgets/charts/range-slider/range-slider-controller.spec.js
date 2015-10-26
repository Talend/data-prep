describe('RangeSlider controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('talend.widget'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('RangeSliderCtrl', {
                $scope: scope
            }, true);
            return ctrlFn();
        };
    }));

    it('should transform number string to number', inject(function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber(' 88');

        //then
        expect(result).toBe(88);
    }));

    it('should return null on invalid string', inject(function () {
        //given
        var ctrl = createController();

        //when
        var result = ctrl.toNumber('dqsfds10010');

        //then
        expect(result).toBe(null);
    }));

    it('should return true when string has comma', inject(function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence(',654');

        //then
        expect(hasComma).toBe(true);
    }));

    it('should return false when string has NO comma', inject(function () {
        //given
        var ctrl = createController();

        //when
        var hasComma = ctrl.checkCommaExistence('654');

        //then
        expect(hasComma).toBe(false);
    }));

    describe('decimal places', function() {
        it('should return 0 as decimal place when the input is not is the requested format', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('azeazeaz');

            //then
            expect(result).toBe(0);
        }));

        it('should return the decimal place', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70');

            //then
            expect(result).toBe(2);
        }));

        it('should return the decimal place with scientific annonation ajustement', inject(function () {
            //given
            var ctrl = createController();

            //when
            var result = ctrl.decimalPlaces('0.70e-2');

            //then
            expect(result).toBe(4);
        }));
    });

    describe('range value adaptation', function () {

        it('should switch entered min and max to respect min < max', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 50;
            var enteredMax = 20; // lower than enteredMin
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result).toEqual({min:20, max:50});
        }));

        it('should set enteredMin to the minimum if it is under minimum', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = -2;// lower than minimum
            var enteredMax = 50;
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.min).toBe(2);
        }));

        it('should set enteredMin to the maximum if it is above', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 105;// above maximum
            var enteredMax = 110;
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.min).toBe(100);
        }));

        it('should set enteredMax to the minimum if it is under', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = -1;
            var enteredMax = 0; // under minimum
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.max).toBe(2);
        }));

        it('should set enteredMax to the maximum if it is above', inject(function () {
            //given
            var ctrl = createController();
            var enteredMin = 20;
            var enteredMax = 200; // greater than maximum
            var minimum = 2;
            var maximum = 100;
            var nbDecimals = 2;

            //when
            var result = ctrl.adaptRangeValues(enteredMin, enteredMax, minimum, maximum, nbDecimals);

            //then
            expect(result.max).toBe(100);
        }));
    });
});