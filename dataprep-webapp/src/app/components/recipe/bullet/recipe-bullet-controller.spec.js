/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Recipe Bullet controller', function () {
    'use strict';

    var createController, scope, step;

    beforeEach(angular.mock.module('data-prep.recipe-bullet'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        step = {
            transformation: {stepId: '138ea798bc56', column:{id:'0002'}}
        };

        createController = function () {
            var ctrlFn = $controller('RecipeBulletCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.step = step;
            return ctrlFn();
        };

    }));

    it('should init step index', inject(function (RecipeService) {
        //given
        spyOn(RecipeService, 'getStepIndex').and.returnValue(3);

        //when
        var ctrl = createController();

        //then
        expect(ctrl.stepIndex).toBe(3);
        expect(RecipeService.getStepIndex).toHaveBeenCalledWith(step);
    }));

    it('should return true when step is the first step', inject(function (RecipeService) {
        //given
        spyOn(RecipeService, 'isFirstStep').and.returnValue(true);
        var ctrl = createController();

        //when
        var isFirst = ctrl.isStartChain();

        //then
        expect(RecipeService.isFirstStep).toHaveBeenCalledWith(step);
        expect(isFirst).toBe(true);
    }));

    it('should return false when step is NOT the first step', inject(function (RecipeService) {
        //given
        spyOn(RecipeService, 'isFirstStep').and.returnValue(false);
        var ctrl = createController();

        //when
        var isFirst = ctrl.isStartChain();

        //then
        expect(RecipeService.isFirstStep).toHaveBeenCalledWith(step);
        expect(isFirst).toBe(false);
    }));

    it('should return true when step is the last step', inject(function (RecipeService) {
        //given
        spyOn(RecipeService, 'isLastStep').and.returnValue(true);
        var ctrl = createController();

        //when
        var isLast = ctrl.isEndChain();

        //then
        expect(RecipeService.isLastStep).toHaveBeenCalledWith(step);
        expect(isLast).toBe(true);
    }));

    it('should return false when step is NOT the last step', inject(function (RecipeService) {
        //given
        spyOn(RecipeService, 'isLastStep').and.returnValue(false);
        var ctrl = createController();

        //when
        var isLast = ctrl.isEndChain();

        //then
        expect(RecipeService.isLastStep).toHaveBeenCalledWith(step);
        expect(isLast).toBe(false);
    }));

    it('should call hover start action', inject(function (RecipeService, RecipeBulletService) {
        //given
        spyOn(RecipeBulletService, 'stepHoverStart').and.returnValue();
        var ctrl = createController();

        //when
        ctrl.stepHoverStart();

        //then
        expect(RecipeBulletService.stepHoverStart).toHaveBeenCalledWith(step);
    }));

    it('should call hover end action', inject(function (RecipeBulletService) {
        //given
        spyOn(RecipeBulletService, 'stepHoverEnd').and.returnValue();
        var ctrl = createController();

        //when
        ctrl.stepHoverEnd();

        //then
        expect(RecipeBulletService.stepHoverEnd).toHaveBeenCalledWith(step);
    }));

    it('should call toggle step action', inject(function (RecipeBulletService) {
        //given
        spyOn(RecipeBulletService, 'toggleStep').and.returnValue();
        var ctrl = createController();

        //when
        ctrl.toggleStep();

        //then
        expect(RecipeBulletService.toggleStep).toHaveBeenCalledWith(step);
    }));

    describe('active step', function () {
        var stepIndex = 3;
        var activeStepThreshold = 5;
        var allSvgs = [2, 5, 8, 58, 4, 212, 87, 52];

        beforeEach(inject(function (RecipeService) {
            step.inactive = false;

            spyOn(RecipeService, 'getStepIndex').and.returnValue(stepIndex);
            spyOn(RecipeService, 'getActiveThresholdStepIndex').and.returnValue(activeStepThreshold);
        }));

        it('should get all steps after the current when step is active', function () {
            //given
            var ctrl = createController();

            //when
            var bullets = ctrl.getBulletsToChange(allSvgs);

            //then
            expect(bullets).toEqual(allSvgs.slice(stepIndex));
        });

    });

    describe('inactive step', function () {
        var stepIndex = 5;
        var activeStepThreshold = 2;
        var allSvgs = [2, 5, 8, 58, 4, 212, 87, 52];

        beforeEach(inject(function (RecipeService) {
            step.inactive = true;

            spyOn(RecipeService, 'getStepIndex').and.returnValue(stepIndex);
            spyOn(RecipeService, 'getActiveThresholdStepIndex').and.returnValue(activeStepThreshold);
        }));

        it('should get all steps after the current when step is active', function () {
            //given
            var ctrl = createController();

            //when
            var bullets = ctrl.getBulletsToChange(allSvgs);

            //then
            expect(bullets).toEqual(allSvgs.slice(3, 6));
        });

    });

});