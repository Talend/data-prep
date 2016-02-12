/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Actions suggestions-stats controller', function () {
    'use strict';

    var createController, scope;

    var stateMock;

    beforeEach(angular.mock.module('data-prep.actions-suggestions', function($provide) {
        stateMock = {playground: {filter: {}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller, $q, PlaygroundService, TransformationService,
                                EarlyPreviewService, TransformationApplicationService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('ActionsSuggestionsCtrl', {
                $scope: scope
            });
        };

        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when());
        spyOn(TransformationService, 'initDynamicParameters').and.returnValue($q.when());
        spyOn(TransformationApplicationService, 'append').and.returnValue($q.when());
        spyOn(EarlyPreviewService, 'activatePreview').and.returnValue();
        spyOn(EarlyPreviewService, 'deactivatePreview').and.returnValue();
        spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
    }));

    describe('render predicates', function() {
        describe('transformation', function() {
            it('should render all transformations when applyTransformationOnFilters flag is true', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = true;
                var transformation = {category: 'filtered'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderAction(transformation);

                //then
                expect(result).toBe(true);
            });

            it('should render transformations when category is not "filtered"', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var transformation = {category: 'quickfix'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderAction(transformation);

                //then
                expect(result).toBe(true);
            });

            it('should NOT render transformations when category is "filtered" and applyTransformationOnFilters flag is false', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var transformation = {category: 'filtered'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderAction(transformation);

                //then
                expect(result).toBe(false);
            });
        });

        describe('category', function() {
            it('should render all categories when applyTransformationOnFilters flag is true', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = true;
                var categoryTransformations = {
                    category: 'suggestion',
                    transformations: [{category: 'filtered'}]
                };
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderCategory(categoryTransformations);

                //then
                expect(result).toBeTruthy();
            });

            it('should render category when category is not "suggestion"', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var categoryTransformations = {
                    category: 'quickfix',
                    transformations: [{category: 'filtered'}]
                };
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderCategory(categoryTransformations);

                //then
                expect(result).toBeTruthy();
            });

            it('should render category when it has non "filtered" transformations', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var categoryTransformations = {
                    category: 'suggestion',
                    transformations: [{category: 'suggestion'}]
                };
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderCategory(categoryTransformations);

                //then
                expect(result).toBeTruthy();
            });

            it('should NOT render category when category is "suggestion" that only have "filtered" transformations', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var categoryTransformations = {
                    category: 'suggestion',
                    transformations: [{category: 'filtered'}]
                };
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderCategory(categoryTransformations);

                //then
                expect(result).toBeFalsy();
            });
        });
    });
});
