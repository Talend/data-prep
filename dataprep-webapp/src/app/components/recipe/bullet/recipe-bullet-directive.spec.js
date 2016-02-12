/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Single recipeBullet directive ', function () {
    'use strict';

    var createElement, element, scope, steps;

    function getSvgElementAttribute(elementName, elementIndex, attr) {
        var allSvg = element.find('svg');

        var result = [];
        allSvg.each(function (index) {
            result.push(allSvg.eq(index).find(elementName).eq(elementIndex).attr(attr));
        });

        return result;
    }

    function getTopCablesDimensions() {
        return getSvgElementAttribute('path', 0, 'd');
    }

    function getBottomCablesDimensions() {
        return getSvgElementAttribute('path', 1, 'd');
    }

    function getCircleYPosition() {
        return getSvgElementAttribute('circle', 0, 'cy');
    }

    beforeEach(angular.mock.module('data-prep.recipe-bullet'));
    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(inject(function ($rootScope, $compile, $timeout, RecipeService, RecipeBulletService) {
        steps = [
            {
                column: {id: 'col2'},
                transformation: {name: 'uppercase', label: 'To uppercase', category: 'case', parameters: [], items: []},
                inactive: false
            }
        ];


        createElement = function () {
            scope = $rootScope.$new();
            scope.step0 = steps[0];
            var template = '<recipe-bullet style="height: 100px;" step="step0"></recipe-bullet>';
            element = $compile(template)(scope);
            $timeout.flush();
            scope.$digest();
        };

        spyOn(RecipeBulletService, 'stepHoverStart').and.returnValue();
        spyOn(RecipeBulletService, 'stepHoverEnd').and.returnValue();
        spyOn(RecipeBulletService, 'toggleStep').and.returnValue();
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    describe('Middle bullet ', function () {

        beforeEach(inject(function ($rootScope, $compile, $timeout, RecipeService) {

            spyOn(RecipeService, 'isFirstStep').and.callFake(function () {
                return false;
            });
            spyOn(RecipeService, 'isLastStep').and.callFake(function () {
                return false;
            });
            spyOn(RecipeService, 'getActiveThresholdStepIndex').and.callFake(function () {
                return 1;
            });
            spyOn(RecipeService, 'getStepIndex').and.callFake(function () {
                return 1;
            });

        }));

        it('should init circle position', function () {
            //when
            createElement();

            //then
            var positions = getCircleYPosition();
            expect(positions[0]).toBe('21');
        });

        it('should init top cable dimensions', function () {
            //when
            createElement();


            //then
            var dimensions = getTopCablesDimensions();
            expect(dimensions[0]).toBe('M 15 0 L 15 11 Z');
        });

        it('should init bottom cable dimensions', function () {
            //when
            createElement();
            //then
            var dimensions = getBottomCablesDimensions();
            expect(dimensions[0]).toBe('M 15 31 L 15 105 Z');
        });

    });
});


describe('Multi recipeBullet directive', function () {
    'use strict';

    var createElement, element, scope, steps;

    function getSvgElementAttribute(elementName, elementIndex, attr) {
        var allSvg = element.find('svg');
        var result = [];
        allSvg.each(function (index) {
            result.push(allSvg.eq(index).find(elementName).eq(elementIndex).attr(attr));
        });

        return result;
    }

    function getTopCablesClasses() {
        return getSvgElementAttribute('path', 0, 'class');
    }

    function getBottomCablesClasses() {
        return getSvgElementAttribute('path', 1, 'class');
    }

    function getCircleClasses() {
        return getSvgElementAttribute('circle', 0, 'class');
    }

    beforeEach(angular.mock.module('data-prep.recipe-bullet'));
    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(inject(function ($rootScope, $compile, $timeout, RecipeService, RecipeBulletService) {
        steps = [
            {
                column: {id: 'col2'},
                transformation: {name: 'uppercase', label: 'To uppercase', category: 'case', parameters: [], items: []},
                inactive: false
            },
            {
                column: {id: 'col1'},
                transformation: {
                    name: 'lowerercase',
                    label: 'To uppercase',
                    category: 'case',
                    parameters: [],
                    items: []
                },
                inactive: false
            },
            {
                column: {id: 'col3'},
                transformation: {name: 'negate', label: 'To uppercase', category: 'case', parameters: [], items: []},
                inactive: true
            },
            {
                column: {id: 'col4'},
                transformation: {
                    name: 'propercase',
                    label: 'To uppercase',
                    category: 'case',
                    parameters: [],
                    items: []
                },
                inactive: true
            },
            {
                column: {id: 'col1'},
                transformation: {name: 'rename', label: 'To uppercase', category: 'case', parameters: [], items: []},
                inactive: false
            }
        ];


        createElement = function () {
            scope = $rootScope.$new();
            _.forEach(steps, function (step, index) {
                scope['step' + index] = step;
            });

            element = angular.element(
                '<div class="recipe">' +
                '	<div style="height: 50px;"><recipe-bullet step="step0"></recipe-bullet></div>' +
                '	<div style="height: 40px;"><recipe-bullet step="step1"></recipe-bullet></div>' +
                '	<div style="height: 30px;"><recipe-bullet step="step2"></recipe-bullet></div>' +
                '	<div style="height: 20px;"><recipe-bullet step="step3"></recipe-bullet></div>' +
                '	<div style="height: 10px;"><recipe-bullet step="step4"></recipe-bullet></div>' +
                '</div>');

            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };

        spyOn(RecipeService, 'isFirstStep').and.callFake(function (step) {
            return step === steps[0];
        });
        spyOn(RecipeService, 'isLastStep').and.callFake(function (step) {
            return step === steps[4];
        });
        spyOn(RecipeService, 'getActiveThresholdStepIndex').and.callFake(function () {
            return 1;
        });
        spyOn(RecipeService, 'getStepIndex').and.callFake(function (step) {
            return steps.indexOf(step);
        });
        spyOn(RecipeBulletService, 'stepHoverStart').and.returnValue();
        spyOn(RecipeBulletService, 'stepHoverEnd').and.returnValue();
        spyOn(RecipeBulletService, 'toggleStep').and.returnValue();
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render all bullets', function () {
        //when
        createElement();

        //then
        expect(element.find('svg').length).toBe(5);
        expect(element.find('.all-svg-cls').eq(0).attr('class').indexOf('maillon-circle') > -1).toBe(true);
        expect(element.find('.all-svg-cls').eq(1).attr('class').indexOf('maillon-circle') > -1).toBe(true);
        expect(element.find('.all-svg-cls').eq(2).attr('class').indexOf('maillon-circle-disabled') > -1).toBe(true);
        expect(element.find('.all-svg-cls').eq(3).attr('class').indexOf('maillon-circle-disabled') > -1).toBe(true);
        expect(element.find('.all-svg-cls').eq(4).attr('class').indexOf('maillon-circle') > -1).toBe(true);
    });

    it('should hide top cable on first step only', function () {
        //when
        createElement();

        //then
        var classes = getTopCablesClasses();

        expect(classes[0] && classes[0].indexOf('ng-hide') > -1).toBeTruthy();
        for (var i = 1; i < 5; i++) {
            expect(classes[i] && classes[i].indexOf('ng-hide') > -1).toBeFalsy();
        }
    });

    it('should hide bottom cable on last step only', function () {
        //when
        createElement();

        //then
        var classes = getBottomCablesClasses();

        for (var i = 0; i < 4; i++) {
            expect(classes[i] && classes[i].indexOf('ng-hide') > -1).toBeFalsy();
        }
        expect(classes[4] && classes[4].indexOf('ng-hide') > -1).toBeTruthy();
    });

    it('should call hover start actions on mouseover', inject(function (RecipeBulletService) {
        //given
        createElement();
        var event = new angular.element.Event('mouseenter');

        //when
        element.find('recipe-bullet').eq(3).trigger(event);

        //then
        expect(RecipeBulletService.stepHoverStart).toHaveBeenCalled();
    }));

    it('should set "inactive hover class" from threshold to inactive step on mouseover', function () {
        //given
        createElement();
        var event = new angular.element.Event('mouseenter');

        //when
        element.find('recipe-bullet').eq(3).trigger(event);

        //then
        var classes = getCircleClasses();
        expect(classes[0]).toBeFalsy();
        expect(classes[1]).toBeFalsy();
        expect(classes[2]).toBe('maillon-circle-disabled-hovered');
        expect(classes[3]).toBe('maillon-circle-disabled-hovered');
        expect(classes[4]).toBeFalsy();
    });

    it('should set "active hover class" from active step to the end on mouseover', function () {
        //given
        createElement();
        var event = new angular.element.Event('mouseenter');

        //when
        element.find('recipe-bullet').eq(1).trigger(event);

        //then
        var classes = getCircleClasses();
        expect(classes[0]).toBeFalsy();
        expect(classes[1]).toBe('maillon-circle-enabled-hovered');
        expect(classes[2]).toBe('maillon-circle-enabled-hovered');
        expect(classes[3]).toBe('maillon-circle-enabled-hovered');
        expect(classes[4]).toBe('maillon-circle-enabled-hovered');
    });

    it('should call hover end actions on mouseleave', inject(function (RecipeBulletService) {
        //given
        createElement();
        var event = new angular.element.Event('mouseleave');

        //when
        element.find('recipe-bullet').eq(3).trigger(event);

        //then
        expect(RecipeBulletService.stepHoverEnd).toHaveBeenCalled();
    }));

    it('should remove "inactive hover class" on mouseleave', function() {
        //given
        createElement();
        var enterEvent = new angular.element.Event('mouseenter');
        element.find('recipe-bullet').eq(3).trigger(enterEvent);

        var classes = getCircleClasses();
        expect(classes[2]).toBe('maillon-circle-disabled-hovered');
        expect(classes[3]).toBe('maillon-circle-disabled-hovered');

        var leaveEvent = new angular.element.Event('mouseleave');

        //when
        element.find('recipe-bullet').eq(3).trigger(leaveEvent);

        //then
        classes = getCircleClasses();
        expect(classes[2]).toBeFalsy();
        expect(classes[3]).toBeFalsy();
    });

});