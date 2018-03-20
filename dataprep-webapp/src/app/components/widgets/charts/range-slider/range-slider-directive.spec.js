/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

function flushAllD3Transitions() {
    const now = Date.now;
    Date.now = () => Infinity;
    d3.timer.flush();
    Date.now = now;
}

describe('Range slider directive', () => {
    let createElement;
    let element;
    let scope;
    let ctrl;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new(true);
        scope.brushEnd = jasmine.createSpy('spy');

        createElement = () => {
            const html = `<range-slider id="barChart"
                id="domId"
                width="250"
                height="60"
                range-limits="rangeLimits"
                on-brush-end="brushEnd(interval)"></range-slider>`;

            element = angular.element(html);
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();

            ctrl = element.controller('rangeSlider');
        };
    }));

    afterEach(() => {
        scope.$destroy();
        if (element) {
            element.remove();
        }
    });

    describe('brush', () => {
        it('should render brush with provided number values', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                min: 0,
                max: 20,
                minBrush: 5,
                maxBrush: 15,
            };
            const fullWidth = 215; // 250 - margins

            // when
            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            // then
            expect(d3.select('.extent').attr('width')).toBe('' + (fullWidth / 2));   // brush width
            expect(d3.select('.extent').attr('x')).toBe('' + (fullWidth / 4));       // brush position = 5 on 20 -> 1/4th of the width
        }));

        it('should render brush with provided date values', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                type: 'date',
                min: 1000000000,
                max: 2000000000,
                minBrush: 1250000000,
                maxBrush: 1750000000,
            };
            const fullWidth = 215; // 250 - margins

            // when
            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            // then
            expect(d3.select('.extent').attr('width')).toBe('' + (fullWidth / 2));   // brush width
            expect(d3.select('.extent').attr('x')).toBe('' + (fullWidth / 4));       // brush position = 1250000 on 2000000 -> 1/4th of the width
        }));

        it('should render brush with number min/max when there is no provided values', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                min: 0,
                max: 20,
            };
            const fullWidth = 215; // 250 - margins

            // when
            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            // then
            expect(d3.select('.extent').attr('width')).toBe('' + fullWidth);
            expect(d3.select('.extent').attr('x')).toBe('0');
        }));

        it('should render brush with date min/max when there is no provided values', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                type: 'date',
                min: 1000000000,
                max: 2000000000,
            };
            const fullWidth = 215; // 250 - margins

            // when
            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            // then
            expect(d3.select('.extent').attr('width')).toBe('' + fullWidth);   // brush width
            expect(d3.select('.extent').attr('x')).toBe('0');       // brush position = 5 on 20 -> 1/4th of the width
        }));

        it('should remove brush when range limits is not defined', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                min: 0,
                max: 20,
                minBrush: 5,
                maxBrush: 15,
            };

            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            expect(element.find('svg').length).toBe(1);

            // when
            scope.rangeLimits = null;
            scope.$digest();
            $timeout.flush();

            // then : the previous brush should be removed in DOM
            expect(element.find('svg').length).toBe(0);
        }));

        it('should NOT rerender brush when provided values have NOT changed', inject(($timeout) => {
            // given
            scope.rangeLimits = {
                min: 0,
                max: 20,
                minBrush: 5,
                maxBrush: 15,
            };

            createElement();
            $timeout.flush();
            flushAllD3Transitions();

            expect(element.find('svg').length).toBe(1);

            // when
            scope.rangeLimits = {
                min: 0,
                max: 20,
                minBrush: 5,
                maxBrush: 15,
            };
            scope.$digest();
            // we do not flush the $timeout to trigger rerender yet

            // then : the previous brush should still be in DOM
            expect(element.find('svg').length).toBe(1);
        }));
    });

    describe('input', () => {
        it('should render number min number input with default min value', () => {
            // given
            scope.rangeLimits = {
                type: 'number',
                min: 0,
                max: 20,
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(0).length).toBe(1);
            expect(element.find('input').eq(0).val()).toBe('0');
        });

        it('should render number min number input with provided min value', () => {
            // given
            scope.rangeLimits = {
                type: 'number',
                min: 0,
                max: 20,
                minFilterVal: 10,
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(0).length).toBe(1);
            expect(element.find('input').eq(0).val()).toBe('10');
        });

        it('should render number max number input with default max value', () => {
            // given
            scope.rangeLimits = {
                type: 'number',
                min: 0,
                max: 20,
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(1).length).toBe(1);
            expect(element.find('input').eq(1).val()).toBe('20');
        });

        it('should render number max number input with provided max value', () => {
            // given
            scope.rangeLimits = {
                type: 'number',
                min: 0,
                max: 20,
                maxFilterVal: 15,
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(1).length).toBe(1);
            expect(element.find('input').eq(1).val()).toBe('15');
        });

        it('should render date min input', () => {
            // given
            scope.rangeLimits = {
                type: 'date',
                min: +new Date('2016/06/27'),
                max: +new Date('2018/06/27'),
                minFilterVal: +new Date('2016/06/27'),
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(0).length).toBe(1);
            expect(element.find('input').eq(0).val()).toBe('2016-06-27');
        });

        it('should render date max input', () => {
            // given
            scope.rangeLimits = {
                type: 'date',
                min: +new Date('2016/06/27'),
                max: +new Date('2018/06/27'),
                maxFilterVal: +new Date('2018/06/27'),
            };

            // when
            createElement();

            // then
            expect(element.find('input').eq(1).length).toBe(1);
            expect(element.find('input').eq(1).val()).toBe('2018-06-27');
        });
    });

    describe('error messages', () => {
        it('should render error message', () => {
            // given
            createElement();
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER"]').length).toBe(0);
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER_CONTENT"]').length).toBe(0);

            // when
            ctrl.invalidNumber = true;
            scope.$digest();

            // then
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER"]').length).toBe(1);
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER_CONTENT"]').length).toBe(0);
        });

        it('should render number comma error message', () => {
            // given
            createElement();
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER"]').length).toBe(0);
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER_CONTENT"]').length).toBe(0);

            // when
            ctrl.invalidNumberWithComma = true;
            scope.$digest();

            // then
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER"]').length).toBe(0);
            expect(element.find('.error[translate-once="INVALID_VALUE_RANGE_SLIDER_CONTENT"]').length).toBe(1);
        });
    });
});
