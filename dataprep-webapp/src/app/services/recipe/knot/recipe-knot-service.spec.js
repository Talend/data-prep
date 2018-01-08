/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Recipe Knot service', function () {
    'use strict';

    var previousStep = { column: { id: '0003' } };
    var lastActiveStep = { inactive: false };

    var preparationId = '4635fa41864b74ef64';
    var stateMock;

    beforeEach(angular.mock.module('data-prep.services.recipe', function ($provide) {
        stateMock = {
            playground: {
                preparation: { id: preparationId },
                recipe: {
                    current: {
                        steps: [],
                    },
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $q, $timeout,
                                StepUtilsService, RecipeKnotService, RecipeService, PreparationService, PreviewService) {
        spyOn($rootScope, '$emit').and.returnValue();
        spyOn(StepUtilsService, 'getActiveThresholdStepIndex').and.returnValue(3);
        spyOn(StepUtilsService, 'getPreviousStep').and.returnValue(previousStep);
        spyOn(RecipeService, 'refresh').and.callFake(() => {
            stateMock.playground.recipe.current.steps = [lastActiveStep];
        });
        spyOn(PreparationService, 'updateStep').and.returnValue($q.when());
        spyOn(PreviewService, 'getPreviewDiffRecords').and.returnValue($q.when());
        spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when());
        spyOn(PreviewService, 'cancelPreview').and.returnValue(null);
        spyOn(PreviewService, 'stopPendingPreview').and.returnValue(null);
        spyOn($timeout, 'cancel').and.returnValue();
    }));

    it('should trigger append preview on inactive step hover after a delay of 300ms', inject(($timeout, PreviewService, RecipeKnotService) => {
        //given
        const steps = [
            { id: '1', column: { id: '0002' } },
            { id: '2', column: { id: '0005' }, inactive: true },
            { id: '3', column: { id: '0004' }, inactive: true },
            { id: '4', column: { id: '0005' }, inactive: true },
        ];
        stateMock.playground.recipe.current = {
            steps: steps,
            lastActiveStep: steps[0],
        };

        //when
        RecipeKnotService.stepHoverStart(steps[2]);
        $timeout.flush(299);
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        $timeout.flush(1);

        //then
        expect(PreviewService.getPreviewDiffRecords).toHaveBeenCalledWith(preparationId, steps[0], steps[2], '0004');
    }));

    it('should cancel pending preview action on step hover', inject(($timeout, RecipeKnotService, PreviewService) => {
        //given
        const steps = [
            { id: '1', column: { id: '0002' } },
            { id: '2', column: { id: '0005' } },
            { id: '3', column: { id: '0004' } },
            { id: '4', column: { id: '0005' } },
        ];
        stateMock.playground.recipe.current = {
            steps: steps,
            lastActiveStep: steps[0],
        };

        //when
        RecipeKnotService.stepHoverStart(steps[2]);

        //then
        expect(PreviewService.stopPendingPreview).toHaveBeenCalled();
    }));

    it('should trigger diff preview after a 300ms', inject(($timeout, PreviewService, RecipeKnotService) => {
        //given
        const steps = [
            { id: '0', column: { id: '0005' } },
            { id: '1', column: { id: '0004' } },
            { id: '2', column: { id: '0000' } },
            { id: '3', column: { id: '0001' } },
        ];
        stateMock.playground.recipe.current = {
            steps: steps,
            lastActiveStep: steps[3],
        };

        //when
        RecipeKnotService.stepHoverStart(steps[2]);
        $timeout.flush(299);
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        $timeout.flush(1);

        //then
        expect(PreviewService.getPreviewDiffRecords).toHaveBeenCalledWith(preparationId, steps[3], previousStep, '0000');
    }));

    it('should cancel current preview on mouse hover end after a delay of 100ms', inject(function ($timeout, PreviewService, RecipeKnotService) {
        //given
        var step = { column: { id: '0001' } };

        //when
        RecipeKnotService.stepHoverEnd(step);
        expect(PreviewService.cancelPreview).not.toHaveBeenCalled();
        $timeout.flush(100);

        //then
        expect(PreviewService.cancelPreview).toHaveBeenCalled();
    }));

    it('should cancel pending preview action on mouse hover end', inject(function ($timeout, PreviewService, RecipeKnotService) {
        //given
        var step = { column: { id: '0001' } };

        //when
        RecipeKnotService.stepHoverEnd(step);
        $timeout.flush(100);

        //then
        expect(PreviewService.getPreviewDiffRecords).not.toHaveBeenCalled();
        expect(PreviewService.stopPendingPreview).toHaveBeenCalled();
    }));
});
