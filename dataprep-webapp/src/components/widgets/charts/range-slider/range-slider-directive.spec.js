describe('rangeSlider directive', function () {
    'use strict';

    var createElement, element, scope, isolateScope;
    var margins = {
        left: 25,
        right: 10
    };

    var flushAllD3Transitions = function () {
        var now = Date.now;
        Date.now = function () {
            return Infinity;
        };
        d3.timer.flush();
        Date.now = now;
    };

    beforeEach(module('talend.widget'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'INVALID_VALUE_RANGE_SLIDER': 'Invalid Entered Value',
            'INVALID_VALUE_RANGE_SLIDER_CONTENT': ': Use \".\" instead of \",\"'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new(true);
        scope.rangeLimits = {
            min: 0,
            max: 20,
            minBrush: 5,
            maxBrush: 15,
            minFilterVal: 5,
            maxFilterVal: 15
        };
        scope.brushEnd = jasmine.createSpy('spy');

        createElement = function () {
            element = angular.element('<range-slider id="barChart"' +
                'id="domId"' +
                'width="250"' +
                'height="100"' +
                'range-limits="rangeLimits"' +
                'on-brush-end="brushEnd"' +
                '></range-slider>');

            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();

            isolateScope = element.isolateScope();
        };
    }));

    beforeEach(function () {
        jasmine.clock().install();
    });

    afterEach(function () {
        jasmine.clock().uninstall();

        scope.$destroy();
        element.remove();
    });

    describe('brush', function() {
        it('should set the brush to the min/max position when there is no brush values', function () {
            //given
            scope.rangeLimits = {
                min: 0,
                max: 20
            };

            //when
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then: brush extent width should be all the range size
            var x = d3.scale.linear()
                .domain([scope.rangeLimits.min, scope.rangeLimits.max])
                .range([0, (250 - (margins.left + margins.right))]);
            expect(d3.select('.extent').attr('width')).toBe('' + (x(scope.rangeLimits.max) - x(scope.rangeLimits.min)));
            expect(d3.select('.extent').attr('x')).toBe('' + x(scope.rangeLimits.min));
        });

        it('should set the brush to the provided values', function () {
            //when
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then : brush extent width should be between [5...15]
            var x = d3.scale.linear()
                .domain([scope.rangeLimits.min, scope.rangeLimits.max])
                .range([0, (250 - (margins.left + margins.right))]);

            expect(d3.select('.extent').attr('width')).toBe('' + (x(scope.rangeLimits.maxBrush) - x(scope.rangeLimits.minBrush)));
            expect(d3.select('.extent').attr('x')).toBe('' + x(scope.rangeLimits.minBrush));

            var brush = d3.svg.brush()
                .x(x)
                .extent([6, 10]);
            d3.select('.brush').call(brush.event)
                .call(brush.extent([0, 20]));
            expect(d3.select('.extent').attr('width')).toBe('' + (x(scope.rangeLimits.max) - x(scope.rangeLimits.min)));
        });

        it('should init brush with a delta when minBrush = maxBrush', function () {
            //given
            scope.rangeLimits = {
                min: 0.000001,
                max: 20,
                minBrush: 20,
                maxBrush: 20,
                minFilterVal: 25,
                maxFilterVal: 35
            };
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then
            expect(isolateScope.brush.extent()).toEqual([20, 20.01]);
        });

        it('should the min and max labels to the provided min/max values', function () {
            //given
            scope.rangeLimits = {
                min: -50000,
                max: 20000,
                minBrush: -5,
                maxBrush: 10,
                minFilterVal: -20000,
                maxFilterVal: 10001
            };
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then
            expect(element.find('text.the-minimum-label').eq(0).text()).toBe('-50,000');
            expect(element.find('text.the-maximum-label').eq(0).text()).toBe('20,000');
        });
    });

    describe('inputs', function() {

        it('should init inputs with the provided min/max when minFilterVal and maxFilterVal are undefined (no filter)', function () {
            //given
            scope.rangeLimits = {
                min: -50000,
                max: 20000
            };
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then
            expect(document.getElementsByName('minRange')[0].value).toBe('-50000');
            expect(document.getElementsByName('maxRange')[0].value).toBe('20000');
        });

        it('should init inputs with the provided filter values', function () {
            //given
            scope.rangeLimits = {
                min: 0.000001,
                max: 20,
                minBrush: 20,
                maxBrush: 20,
                minFilterVal: 25,
                maxFilterVal: 35
            };
            createElement();
            jasmine.clock().tick(100);
            flushAllD3Transitions();

            //then
            expect(document.getElementsByName('minRange')[0].value).toBe('25');
            expect(document.getElementsByName('maxRange')[0].value).toBe('35');
        });

        describe('events', function() {

            describe('ENTER keyup', function() {
                it('should update brush with input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(isolateScope.brush.extent()).toEqual([5, 15]);

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 8;
                    maxInput[0].value = 10;

                    //when
                    var enterKeyUpEvent = new angular.element.Event('keyup');
                    enterKeyUpEvent.keyCode = 13;
                    minInput.trigger(enterKeyUpEvent);

                    //then
                    expect(isolateScope.brush.extent()).toEqual([8, 10]);
                });

                it('should call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 8;
                    maxInput[0].value = 10;

                    //when
                    var enterKeyUpEvent = new angular.element.Event('keyup');
                    enterKeyUpEvent.keyCode = 13;
                    minInput.trigger(enterKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should invert min and max if min > max at brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 10; // > max
                    maxInput[0].value = 8; // < min

                    //when
                    var enterKeyUpEvent = new angular.element.Event('keyup');
                    enterKeyUpEvent.keyCode = 13;
                    minInput.trigger(enterKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should cancel the typed value when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();
                    var minInput = element.find('input').eq(0);
                    expect(minInput[0].value).toEqual('5');

                    minInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    minInput.trigger(keyUp);

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 13;
                    minInput.trigger(enterKeyUp);

                    //then
                    expect(minInput[0].value).toEqual('5');
                });

                it('should NOT call brush end callback when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var minInput = element.find('input').eq(0);
                    minInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    minInput.trigger(keyUp);

                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 13;
                    minInput.trigger(enterKeyUp);

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                });

                it('should hide error message when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var minInput = element.find('input').eq(0);
                    minInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    minInput.trigger(keyUp);

                    expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('Invalid Entered Value');

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 13;
                    minInput.trigger(enterKeyUp);

                    //then
                    expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('');
                });
            });

            describe('TAB keyup', function() {
                it('should update brush with input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(isolateScope.brush.extent()).toEqual([5, 15]);

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 8;
                    maxInput[0].value = 10;

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(isolateScope.brush.extent()).toEqual([8, 10]);
                });

                it('should call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 8;
                    maxInput[0].value = 10;

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should invert min and max if min > max at brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var minInput = element.find('input').eq(0);
                    var maxInput = element.find('input').eq(1);

                    minInput[0].value = 10; // > max
                    maxInput[0].value = 8; // < min

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should cancel the typed value when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();
                    var maxInput = element.find('input').eq(1);
                    expect(maxInput[0].value).toEqual('15');

                    maxInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    maxInput.trigger(keyUp);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(maxInput[0].value).toEqual('15');
                });

                it('should NOT call brush end callback when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var maxInput = element.find('input').eq(1);
                    maxInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    maxInput.trigger(keyUp);

                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                });

                it('should hide error message when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var maxInput = element.find('input').eq(1);
                    maxInput[0].value = 'kjhfkjfkl';
                    var keyUp = new angular.element.Event('keyup');
                    maxInput.trigger(keyUp);

                    expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('Invalid Entered Value');

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('');
                });
            });

            describe('Blur keyup', function() {
                it('should update brush with input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(isolateScope.brush.extent()).toEqual([5, 15]);

                    var maxInput = element.find('input').eq(1);
                    maxInput[0].value = 17;

                    //when
                    var blurEvent = new angular.element.Event('blur');
                    maxInput.trigger(blurEvent);

                    //then
                    expect(isolateScope.brush.extent()).toEqual([5, 17]);
                });

                it('should call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var maxInput = element.find('input').eq(1);
                    maxInput[0].value = 17;

                    //when
                    var blurEvent = new angular.element.Event('blur');
                    maxInput.trigger(blurEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([5, 17]);
                });
            });

            describe('ESC keyup', function() {
                it('should cancel the typed input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);

                    var minInput = element.find('input').eq(0);
                    minInput[0].value = 7;

                    //when
                    var escKeyUp = new angular.element.Event('keyup');
                    escKeyUp.keyCode = 27;
                    minInput.trigger(escKeyUp);

                    //then
                    expect(element.find('input').eq(0)[0].value).toBe('5');
                });

                it('should NOT update brush', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(isolateScope.brush.extent()).toEqual([5, 15]);

                    var minInput = element.find('input').eq(0);
                    minInput[0].value = 7;

                    //when
                    var escKeyUp = new angular.element.Event('keyup');
                    escKeyUp.keyCode = 27;
                    minInput.trigger(escKeyUp);

                    //then
                    expect(isolateScope.brush.extent()).toEqual([5, 15]);
                });

                it('should NOT call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    var minInput = element.find('input').eq(0);
                    minInput[0].value = 7;

                    //when
                    var escKeyUp = new angular.element.Event('keyup');
                    escKeyUp.keyCode = 27;
                    minInput.trigger(escKeyUp);

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                });
            });
        });

        describe('errors', function() {
            it('should show error message', function () {
                //given
                createElement();
                jasmine.clock().tick(100);

                element.find('input').eq(0)[0].value = 'kjhfkjfkl';

                //when
                var event2 = new angular.element.Event('keyup');
                event2.keyCode = 104;//8
                element.find('input').eq(0).trigger(event2);

                //then
                expect(isolateScope.brush.extent()).toEqual([5, 15]);
                expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('Invalid Entered Value');
            });

            it('should show error message with comma warning', function () {
                //given
                createElement();
                jasmine.clock().tick(100);

                element.find('input').eq(0)[0].value = 'kjhfkjf,kl';

                //when
                var event2 = new angular.element.Event('keyup');
                event2.keyCode = 104;//8
                element.find('input').eq(0).trigger(event2);

                //then
                expect(isolateScope.brush.extent()).toEqual([5, 15]);
                expect(element.find('text.invalid-value-msg').eq(0).text()).toBe('Invalid Entered Value: Use "." instead of ","');
            });
        });
    });

});
