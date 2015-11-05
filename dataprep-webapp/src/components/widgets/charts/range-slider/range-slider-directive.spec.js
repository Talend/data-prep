describe('rangeSlider directive', function () {
    'use strict';

    var createElement, element, scope, ctrl;
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

    beforeEach(module('htmlTemplates'));
    beforeEach(module('talend.widget'));

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
                'height="60"' +
                'range-limits="rangeLimits"' +
                'on-brush-end="brushEnd"' +
                '></range-slider>');

            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();

            ctrl = element.controller('rangeSlider');
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
            expect(ctrl.brush.extent()).toEqual([20, 20.01]);
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

            //when
            ctrl.showRangeInputs = true;
            scope.$digest();

            //then
            expect(element.find('input').eq(0)[0].value).toBe('-50000');
            expect(element.find('input').eq(1)[0].value).toBe('20000');
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

            //when
            ctrl.showRangeInputs = true;
            scope.$digest();

            //then
            expect(element.find('input').eq(0)[0].value).toBe('25');
            expect(element.find('input').eq(1)[0].value).toBe('35');
        });

        describe('events', function() {

            describe('ENTER keyup', function() {
                it('should update brush with input values and call brushend callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(ctrl.brush.extent()).toEqual([5, 15]);

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10'
                    };
                    scope.$digest();

                    var minInput = element.find('input').eq(0);

                    //when
                    var enterKeyUpEvent = new angular.element.Event('keyup');
                    enterKeyUpEvent.keyCode = 13;
                    minInput.trigger(enterKeyUpEvent);
                    scope.$digest();

                    //then
                    expect(ctrl.brush.extent()).toEqual([8, 10]);
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                    expect(minInput[0].value).toBe('8');
                });

                it('should invert min and max if min > max at brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'10',
                        maxModel:'8'
                    };
                    scope.$digest();

                    var minInput = element.find('input').eq(0);

                    //when
                    var enterKeyUpEvent = new angular.element.Event('keyup');
                    enterKeyUpEvent.keyCode = 13;
                    minInput.trigger(enterKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should cancel the entered value when value is incorrect', inject(function ($timeout) {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'kjhfkjfkl',
                        maxModel:'8'
                    };
                    scope.$digest();

                    var minInput = element.find('input').eq(0);

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 13;
                    minInput.trigger(enterKeyUp);
                    scope.$digest();
                    $timeout.flush();

                    //then
                    expect(ctrl.minMaxModel.minModel).toEqual('5');
                    expect(minInput[0].value).toEqual('5');
                }));

                it('should NOT call brush end callback when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var minInput = element.find('input').eq(0);
                    ctrl.minMaxModel = {
                        minModel:'kjhfkjfkl',
                        maxModel:'15'
                    };

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 13;
                    minInput.trigger(enterKeyUp);

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                });
            });

            describe('TAB keyup', function() {
                it('should update brush with input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(ctrl.brush.extent()).toEqual([5, 15]);

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(ctrl.brush.extent()).toEqual([8, 10]);
                });

                it('should call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10'
                    };
                    scope.$digest();
                    var maxInput = element.find('input').eq(1);

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

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'10',
                        maxModel:'8'
                    };
                    scope.$digest();
                    var maxInput = element.find('input').eq(1);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([8, 10]);
                });

                it('should cancel the typed value when value is incorrect', inject(function ($timeout) {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10lkfdsfklds'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 9;
                    maxInput.trigger(tabKeyUpEvent);
                    scope.$digest();
                    $timeout.flush();

                    //then
                    expect(ctrl.minMaxModel.maxModel).toEqual('15');
                    expect(maxInput[0].value).toEqual('15');
                }));

                it('should NOT call brush end callback when value is incorrect', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    var minInput = element.find('input').eq(0);
                    ctrl.minMaxModel = {
                        minModel:'kjhfkjfkl',
                        maxModel:'15'
                    };

                    //when
                    var enterKeyUp = new angular.element.Event('keyup');
                    enterKeyUp.keyCode = 9;
                    minInput.trigger(enterKeyUp);

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                });
            });

            describe('Blur keyup', function() {
                it('should update brush with input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(ctrl.brush.extent()).toEqual([5, 15]);

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel: '5',
                        maxModel: '17'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var blurEvent = new angular.element.Event('blur');
                    maxInput.trigger(blurEvent);
                    scope.$digest();

                    //then
                    expect(ctrl.brush.extent()).toEqual([5, 17]);
                });

                it('should NOT update brush with invalid input values', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(ctrl.brush.extent()).toEqual([5, 15]);

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel: '5',
                        maxModel: '14kjhjidf7'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var blurEvent = new angular.element.Event('blur');
                    maxInput.trigger(blurEvent);
                    scope.$digest();

                    //then
                    expect(ctrl.brush.extent()).toEqual([5, 15]);
                });

                it('should call brush end callback', function () {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel: '5',
                        maxModel: '17'
                    };
                    scope.$digest();
                    var maxInput = element.find('input').eq(1);

                    //when
                    var blurEvent = new angular.element.Event('blur');
                    maxInput.trigger(blurEvent);

                    //then
                    expect(scope.brushEnd).toHaveBeenCalledWith([5, 17]);
                });
            });

            describe('ESC keyup', function() {
                it('should cancel the typed input values even with invalid value', inject(function ($timeout) {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    flushAllD3Transitions();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'1jhkjhjjhkkj0'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);
                    var minInput = element.find('input').eq(0);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 27;
                    maxInput.trigger(tabKeyUpEvent);
                    scope.$digest();
                    $timeout.flush();

                    //then
                    expect(ctrl.minMaxModel.minModel).toEqual('5');
                    expect(minInput[0].value).toEqual('5');

                    expect(ctrl.minMaxModel.maxModel).toEqual('15');
                    expect(maxInput[0].value).toEqual('15');
                }));

                it('should NOT update brush', inject(function ($timeout) {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(ctrl.brush.extent()).toEqual([5, 15]);

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 27;
                    maxInput.trigger(tabKeyUpEvent);
                    scope.$digest();
                    $timeout.flush();

                    //then
                    expect(ctrl.brush.extent()).toEqual([5, 15]);
                }));

                it('should NOT call brush end callback', inject(function ($timeout) {
                    //given
                    createElement();
                    jasmine.clock().tick(100);
                    expect(scope.brushEnd).not.toHaveBeenCalled();

                    ctrl.showRangeInputs = true;
                    ctrl.minMaxModel = {
                        minModel:'8',
                        maxModel:'10'
                    };
                    scope.$digest();

                    var maxInput = element.find('input').eq(1);

                    //when
                    var tabKeyUpEvent = new angular.element.Event('keyup');
                    tabKeyUpEvent.keyCode = 27;
                    maxInput.trigger(tabKeyUpEvent);
                    scope.$digest();
                    $timeout.flush();

                    //then
                    expect(scope.brushEnd).not.toHaveBeenCalled();
                }));
            });
        });

        describe('errors', function() {
            it('should show error message when value is incorrect', function () {
                //given
                createElement();
                jasmine.clock().tick(100);
                flushAllD3Transitions();

                ctrl.showRangeInputs = true;
                ctrl.minMaxModel = {
                    minModel:'kjhfkjfkl'
                };
                scope.$digest();

                var minInput = element.find('input').eq(0);

                //when
                var keyUp = new angular.element.Event('keyup');
                keyUp.keyCode = 56;
                minInput.trigger(keyUp);
                scope.$digest();

                //then
                expect(element.find('.error').eq(0).hasClass('ng-hide')).toBe(false);
                expect(element.find('.error').eq(1).hasClass('ng-hide')).toBe(true);
            });

            it('should hide error message when value was incorrect and the user hits ENTER', inject(function ($timeout) {
                //given
                createElement();
                jasmine.clock().tick(100);
                flushAllD3Transitions();

                ctrl.showRangeInputs = true;
                ctrl.minMaxModel = {
                    minModel:'kjhfkjfkl,'
                };
                scope.$digest();

                var minInput = element.find('input').eq(0);
                var keyUp = new angular.element.Event('keyup');
                minInput.trigger(keyUp);
                scope.$digest();

                expect(element.find('.error').eq(0).hasClass('ng-hide')).toBe(false);
                expect(element.find('.error').eq(1).hasClass('ng-hide')).toBe(false);

                //when
                var enterKeyUp = new angular.element.Event('keyup');
                enterKeyUp.keyCode = 13;
                minInput.trigger(enterKeyUp);
                scope.$digest();
                $timeout.flush();

                //then
                expect(element.find('.error').eq(0).hasClass('ng-hide')).toBe(true);
                expect(element.find('.error').eq(1).hasClass('ng-hide')).toBe(true);
                expect(minInput[0].value).toBe('5');
            }));

            it('should hide error message when value was incorrect and the user hits TAB', inject(function ($timeout) {
                //given
                createElement();
                jasmine.clock().tick(100);
                flushAllD3Transitions();

                ctrl.showRangeInputs = true;
                ctrl.minMaxModel = {
                    minModel:'kjhfkjfkl,'
                };
                scope.$digest();

                var minInput = element.find('input').eq(0);
                var keyUp = new angular.element.Event('keyup');
                minInput.trigger(keyUp);
                scope.$digest();

                expect(element.find('.error').eq(0).hasClass('ng-hide')).toBe(false);
                expect(element.find('.error').eq(1).hasClass('ng-hide')).toBe(false);

                //when
                var enterKeyUp = new angular.element.Event('keyup');
                enterKeyUp.keyCode = 9;
                minInput.trigger(enterKeyUp);
                scope.$digest();
                $timeout.flush();

                //then
                expect(element.find('.error').eq(0).hasClass('ng-hide')).toBe(true);
                expect(element.find('.error').eq(1).hasClass('ng-hide')).toBe(true);
                expect(minInput[0].value).toBe('5');
            }));
        });
    });
});
