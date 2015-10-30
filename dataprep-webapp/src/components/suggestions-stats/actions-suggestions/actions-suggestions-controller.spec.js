/*jshint camelcase: false */
describe('Actions suggestions-stats controller', function () {
    'use strict';

    var createController, scope;

    var stateMock;

    beforeEach(module('data-prep.actions-suggestions', function($provide) {
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

    it('should init vars and flags', inject(function () {
        //when
        var ctrl = createController();

        //then
        expect(ctrl.dynamicTransformation).toBe(null);
        expect(ctrl.showModalContent).toBe(null);
        expect(ctrl.dynamicFetchInProgress).toBe(false);
        expect(ctrl.showDynamicModal).toBe(false);
    }));

    describe('bindings', function() {
        it('should bind "column" getter to SuggestionService.currentColumn', inject(function (SuggestionService) {
            //given
            var ctrl = createController();
            var column = {id: '0001', name: 'col1'};

            //when
            SuggestionService.currentColumn = column;

            //then
            expect(ctrl.column).toBe(column);
        }));

        it('should bind "tab" getter to SuggestionService.tab', inject(function (SuggestionService) {
            //given
            var ctrl = createController();
            var tab = 1;

            //when
            SuggestionService.tab = tab;

            //then
            expect(ctrl.tab).toBe(tab);
        }));
    });

    describe('with initiated state', function () {
        var column = {id: '0001', name: 'col1'};

        beforeEach(inject(function ($q, SuggestionService) {
            SuggestionService.currentColumn = column;
            stateMock.playground.dataset = {id: 'dataset_id'};
            stateMock.playground.preparation = {id: 'preparation_id'};
        }));

        describe('transform', function() {
            it('should call appendStep function on transform closure execution', inject(function (TransformationApplicationService) {
                //given
                var transformation = {name: 'tolowercase'};
                var transfoScope = 'column';
                var params = {param: 'value'};
                var ctrl = createController();

                //when
                var closure = ctrl.transform(transformation, transfoScope);
                closure(params);

                //then
                expect(TransformationApplicationService.append).toHaveBeenCalledWith(transformation, transfoScope, params);
            }));

            it('should hide modal after step append', function () {
                //given
                var transformation = {name: 'tolowercase'};
                var transfoScope = 'column';
                var params = {param: 'value'};
                var ctrl = createController();
                ctrl.showDynamicModal = true;

                //when
                var closure = ctrl.transform(transformation, transfoScope);
                closure(params);
                scope.$digest();

                //then
                expect(ctrl.showDynamicModal).toBe(false);
            });

            it('should append new step on static transformation selection', inject(function (TransformationApplicationService) {
                //given
                var transformation = {name: 'tolowercase'};
                var transfoScope = 'column';
                var ctrl = createController();

                //when
                ctrl.select(transformation, transfoScope);

                //then
                expect(TransformationApplicationService.append).toHaveBeenCalledWith(transformation, transfoScope, undefined);
            }));

            it('should cancel pending preview and disable it', inject(function (EarlyPreviewService) {
                //given
                var transformation = {name: 'tolowercase'};
                var transfoScope = 'column';
                var params = {param: 'value'};
                var ctrl = createController();
                ctrl.showDynamicModal = true;

                //when
                var closure = ctrl.transform(transformation, transfoScope);
                closure(params);

                //then
                expect(EarlyPreviewService.deactivatePreview).toHaveBeenCalled();
                expect(EarlyPreviewService.cancelPendingPreview).toHaveBeenCalled();
            }));

            it('should re-enable early preview after 500ms', inject(function (EarlyPreviewService) {
                //given
                jasmine.clock().install();

                var transformation = {name: 'tolowercase'};
                var transfoScope = 'column';
                var params = {param: 'value'};
                var ctrl = createController();
                ctrl.showDynamicModal = true;

                //when
                var closure = ctrl.transform(transformation, transfoScope);
                closure(params);
                scope.$digest();

                expect(EarlyPreviewService.activatePreview).not.toHaveBeenCalled();
                jasmine.clock().tick(500);

                //then
                expect(EarlyPreviewService.activatePreview).toHaveBeenCalled();
                jasmine.clock().uninstall();
            }));
        });

        describe('dynamic parameters', function() {
            it('should set current dynamic transformation and scope on dynamic transformation selection', function () {
                //given
                var transformation = {name: 'cluster', dynamic: true};
                var ctrl = createController();
                ctrl.dynamicTransformation = null;

                //when
                ctrl.select(transformation, 'column');

                //then
                expect(ctrl.dynamicTransformation).toBe(transformation);
                expect(ctrl.dynamicScope).toBe('column');
            });

            it('should init dynamic params on dynamic transformation selection for current dataset', inject(function (TransformationService) {
                //given
                stateMock.playground.preparation = null;
                var transformation = {name: 'cluster', dynamic: true};

                var ctrl = createController();

                //when
                ctrl.select(transformation, 'column');

                //then
                expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                    columnId: '0001',
                    datasetId: 'dataset_id',
                    preparationId: null
                });
            }));

            it('should init dynamic params on dynamic transformation selection for current preparation', inject(function (TransformationService) {
                //given
                var transformation = {name: 'cluster', dynamic: true};

                var ctrl = createController();

                //when
                ctrl.select(transformation, 'column');

                //then
                expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                    columnId: '0001',
                    datasetId: 'dataset_id',
                    preparationId: 'preparation_id'
                });
            }));

            it('should update fetch progress flag during dynamic parameters init', inject(function ($rootScope) {
                //given
                var transformation = {name: 'cluster', dynamic: true};
                var ctrl = createController();
                ctrl.dynamicFetchInProgress = false;

                //when
                ctrl.select(transformation, 'column');
                expect(ctrl.dynamicFetchInProgress).toBe(true);
                $rootScope.$digest();

                //then
                expect(ctrl.dynamicFetchInProgress).toBe(false);
            }));

            it('should show NO CLUSTERS WERE FOUND message', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {name: 'cluster', dynamic: true, cluster: {clusters: []}};

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(false);
                expect(ctrl.emptyParamsMsg).toEqual('NO_CLUSTERS_ACTION_MSG');
            });

            it('should show NO CHOICES WERE FOUND message', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {name: 'choices', dynamic: true, parameters: []};

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(false);
                expect(ctrl.emptyParamsMsg).toEqual('NO_CHOICES_ACTION_MSG');
            });

            it('should show NO SIMPLE PARAMS WERE FOUND message', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {name: 'items', dynamic: true, items: []};

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(false);
                expect(ctrl.emptyParamsMsg).toEqual('NO_PARAMS_ACTION_MSG');
            });

            it('should show dynamic cluster transformation in a modal', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {
                    name: 'cluster',
                    dynamic: true,
                    cluster: {clusters: [{parameters: [], replace: {}}]}
                };

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(true);
            });

            it('should show dynamic parameters transformation in a modal', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {name: 'items', dynamic: true, items: [1, 2]};

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(true);
            });

            it('should show dynamic choices transformation in a modal', function () {
                //given
                var ctrl = createController();
                ctrl.dynamicTransformation = {name: 'items', dynamic: true, parameters: [1, 2]};

                //when
                ctrl.checkDynamicResponse();

                //then
                expect(ctrl.showModalContent).toBe(true);
            });
        });
    });

    describe('render predicates', function() {
        describe('transformation', function() {
            it('should render all transformations when applyTransformationOnFilters flag is true', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = true;
                var transformation = {category: 'filtered'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderTransformation(transformation);

                //then
                expect(result).toBe(true);
            });

            it('should render transformations when category is not "filtered"', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var transformation = {category: 'quickfix'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderTransformation(transformation);

                //then
                expect(result).toBe(true);
            });

            it('should NOT render transformations when category is "filtered" and applyTransformationOnFilters flag is false', function() {
                //given
                stateMock.playground.filter.applyTransformationOnFilters = false;
                var transformation = {category: 'filtered'};
                var ctrl = createController();

                //when
                var result = ctrl.shouldRenderTransformation(transformation);

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
