/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Recipe Bullet service', function () {
    'use strict';

    var previousStep = {column:{id:'0003'}};
    var lastActiveStep = {inactive: false};

    var preparationId = '4635fa41864b74ef64';
    var stateMock;

    beforeEach(angular.mock.module('data-prep.services.recipe', function($provide) {
        stateMock = {playground: {preparation: {id: preparationId}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $q, $timeout,
                                PlaygroundService, RecipeBulletService, RecipeService, PreparationService, PreviewService) {
        spyOn($rootScope, '$emit').and.returnValue();
        spyOn(RecipeService, 'getPreviousStep').and.returnValue(previousStep);
        spyOn(RecipeService, 'getActiveThresholdStepIndex').and.returnValue(3);
        spyOn(RecipeService, 'refresh').and.callFake(function () {
            RecipeService.reset();
            RecipeService.getRecipe().push(lastActiveStep);
        });
        spyOn(PreparationService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PlaygroundService, 'loadStep').and.returnValue($q.when(true));
        spyOn(PreviewService, 'getPreviewDiffRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'cancelPreview').and.returnValue(null);
        spyOn(PreviewService, 'stopPendingPreview').and.returnValue(null);
        spyOn($timeout, 'cancel').and.returnValue();
    }));

    it('should trigger append preview on inactive step hover after a delay of 300ms', inject(function ($timeout, RecipeService, PreviewService, RecipeBulletService) {
        //given
        var recipe = RecipeService.getRecipe();
        recipe.push(
            {id: '1', column:{id:'0002'}},
            {id: '2', column:{id:'0005'}},
            {id: '3', column:{id:'0004'}},
            {id: '4', column:{id:'0005'}}
        );
        RecipeService.disableStepsAfter(recipe[0]);

        //when
        RecipeBulletService.stepHoverStart(recipe[2]);
        $timeout.flush(299);
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        $timeout.flush(1);

        //then
        expect(PreviewService.getPreviewDiffRecords).toHaveBeenCalledWith(preparationId, recipe[0], recipe[2], '0004');
    }));

    it('should cancel pending preview action on step hover', inject(function ($timeout, RecipeService, RecipeBulletService, PreviewService) {
        //given
        var recipe = RecipeService.getRecipe();
        recipe.push(
            {id: '1', column:{id:'0002'}},
            {id: '2', column:{id:'0005'}},
            {id: '3', column:{id:'0004'}},
            {id: '4', column:{id:'0005'}}
        );

        //when
        RecipeBulletService.stepHoverStart(recipe[2]);

        //then
        expect(PreviewService.stopPendingPreview).toHaveBeenCalled();
    }));

    it('should deactivate all the recipe', inject(function (RecipeService, PlaygroundService, RecipeBulletService) {
        //given
        var recipe = RecipeService.getRecipe();
        var step1 = {inactive: false, column:{id:'0005'}};
        var step2 = {inactive: false, column:{id:'0004'}};
        recipe.push(step1);
        recipe.push(step2);

        //when
        RecipeBulletService.toggleRecipe();

        //then
        expect(PlaygroundService.loadStep).toHaveBeenCalledWith(previousStep);
    }));

    it('should reactivate all the recipe', inject(function (RecipeService, PlaygroundService, RecipeBulletService) {
        //given
        var recipe = RecipeService.getRecipe();
        var step1 = {inactive: true, column:{id:'0005'}};
        var step2 = {inactive: true, column:{id:'0004'}};
        recipe.push(step1);
        recipe.push(step2);

        //when
        RecipeBulletService.toggleRecipe();

        //then
        expect(PlaygroundService.loadStep).toHaveBeenCalledWith(step2);
    }));

    it('should reactivate the recipe at the last active step before deactivation action', inject(function (RecipeService, PlaygroundService, RecipeBulletService) {
        //given
        var recipe = RecipeService.getRecipe();
        var step1 = {inactive: true, column:{id:'0005'}};
        var step2 = {inactive: true, column:{id:'0004'}};
        recipe.push(step1);
        recipe.push(step2);

        RecipeBulletService.lastToggled = step1;

        //when
        RecipeBulletService.toggleRecipe();

        //then
        expect(PlaygroundService.loadStep).toHaveBeenCalledWith(step1);
    }));

    it('should trigger diff preview after a 300ms', inject(function ($timeout, RecipeService, PreviewService, RecipeBulletService) {
        //given
        var recipe = RecipeService.getRecipe();
        recipe.push(
            {id: '0', column:{id:'0005'}},
            {id: '1', column:{id:'0004'}},
            {id: '2', column:{id:'0000'}},
            {id: '3', column:{id:'0001'}}
        );

        //when
        RecipeBulletService.stepHoverStart(recipe[2]);
        $timeout.flush(299);
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        $timeout.flush(1);

        //then
        expect(PreviewService.getPreviewDiffRecords).toHaveBeenCalledWith(preparationId, recipe[3], previousStep, '0000');
    }));

    it('should cancel current preview on mouse hover end after a delay of 100ms', inject(function ($timeout, PreviewService, RecipeBulletService) {
        //given
        var step = {column: {id: '0001'}};

        //when
        RecipeBulletService.stepHoverEnd(step);
        expect(PreviewService.cancelPreview).not.toHaveBeenCalled();
        $timeout.flush(100);

        //then
        expect(PreviewService.cancelPreview).toHaveBeenCalled();
    }));

    it('should cancel pending preview action on mouse hover end', inject(function ($timeout, PreviewService, RecipeBulletService) {
        //given
        var step = {column: {id: '0001'}};

        //when
        RecipeBulletService.stepHoverEnd(step);
        $timeout.flush(100);

        //then
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        expect(PreviewService.stopPendingPreview).toHaveBeenCalled();
    }));

    describe('load specific step', function() {
        it('should load current step content if the step is first inactive', inject(function (PlaygroundService, RecipeBulletService) {
            //given
            var step = {inactive: true, column:{id:'0001'}};

            //when
            RecipeBulletService.toggleStep(step);

            //then
            expect(PlaygroundService.loadStep).toHaveBeenCalledWith(step);
        }));

        it('should load previous step content if the step is first active', inject(function (PlaygroundService, RecipeBulletService) {
            //given
            var step = {inactive: false, column:{id:'0001'}};

            //when
            RecipeBulletService.toggleStep(step);

            //then
            expect(PlaygroundService.loadStep).toHaveBeenCalledWith(previousStep);
        }));
    });
});
