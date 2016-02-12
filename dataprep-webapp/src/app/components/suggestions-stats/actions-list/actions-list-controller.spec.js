/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Actions list controller', function () {
    'use strict';

    var createController, scope;
    var stateMock;

    beforeEach(angular.mock.module('data-prep.actions-list', function ($provide) {
        stateMock = {playground: {
            grid: {},
            filter: {}
        }};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('ActionsListCtrl', {
                $scope: scope
            });
        };
    }));

    beforeEach(inject(function ($q, TransformationApplicationService, TransformationService, EarlyPreviewService) {
        spyOn(TransformationApplicationService, 'append').and.returnValue($q.when());
        spyOn(TransformationService, 'initDynamicParameters').and.returnValue($q.when());
        spyOn(EarlyPreviewService, 'activatePreview').and.returnValue();
        spyOn(EarlyPreviewService, 'deactivatePreview').and.returnValue();
        spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
        spyOn(EarlyPreviewService, 'earlyPreview').and.returnValue();
    }));

    describe('init', function () {
        it('should init vars and flags', inject(function () {
            //when
            var ctrl = createController();

            //then
            expect(ctrl.dynamicTransformation).toBe(null);
            expect(ctrl.showModalContent).toBe(null);
            expect(ctrl.dynamicFetchInProgress).toBe(false);
            expect(ctrl.showDynamicModal).toBe(false);
        }));
    });

    describe('early preview', function() {
        it('should trigger early preview with current scope', inject(function(EarlyPreviewService) {
            //given
            var transformation = {name: 'delete'};
            var ctrl = createController();
            ctrl.scope = 'column';

            //when
            ctrl.earlyPreview(transformation);

            //then
            expect(EarlyPreviewService.earlyPreview).toHaveBeenCalledWith(transformation, 'column');
        }));
    });

    describe('transform', function() {
        it('should call appendStep function on transform closure execution', inject(function (TransformationApplicationService) {
            //given
            var transformation = {name: 'tolowercase'};
            var params = {param: 'value'};
            var ctrl = createController();
            ctrl.scope = 'column';

            //when
            var closure = ctrl.transform(transformation);
            closure(params);

            //then
            expect(TransformationApplicationService.append).toHaveBeenCalledWith(transformation, 'column', params);
        }));

        it('should hide modal after step append', function () {
            //given
            var transformation = {name: 'tolowercase'};
            var params = {param: 'value'};
            var ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            var closure = ctrl.transform(transformation);
            closure(params);
            scope.$digest();

            //then
            expect(ctrl.showDynamicModal).toBe(false);
        });

        it('should append new step on static transformation selection', inject(function (TransformationApplicationService) {
            //given
            var transformation = {name: 'tolowercase'};
            var ctrl = createController();
            ctrl.scope = 'column';

            //when
            ctrl.select(transformation);

            //then
            expect(TransformationApplicationService.append).toHaveBeenCalledWith(transformation, 'column', undefined);
        }));

        it('should cancel pending preview and disable it', inject(function (EarlyPreviewService) {
            //given
            var transformation = {name: 'tolowercase'};
            var params = {param: 'value'};
            var ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            var closure = ctrl.transform(transformation);
            closure(params);

            //then
            expect(EarlyPreviewService.deactivatePreview).toHaveBeenCalled();
            expect(EarlyPreviewService.cancelPendingPreview).toHaveBeenCalled();
        }));

        it('should re-enable early preview after 500ms', inject(function ($timeout, EarlyPreviewService) {
            //given
            var transformation = {name: 'tolowercase'};
            var params = {param: 'value'};
            var ctrl = createController();
            ctrl.scope = 'column';
            ctrl.showDynamicModal = true;

            //when
            var closure = ctrl.transform(transformation);
            closure(params);
            scope.$digest();

            expect(EarlyPreviewService.activatePreview).not.toHaveBeenCalled();
            $timeout.flush(500);

            //then
            expect(EarlyPreviewService.activatePreview).toHaveBeenCalled();
        }));
    });

    describe('dynamic parameters', function() {
        beforeEach(function() {
            stateMock.playground.grid.selectedColumn = {id: '0001'};
        });

        it('should set current dynamic transformation on dynamic transformation selection', function () {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};

            var transformation = {name: 'cluster', dynamic: true};
            var ctrl = createController();
            ctrl.dynamicTransformation = null;

            //when
            ctrl.select(transformation);

            //then
            expect(ctrl.dynamicTransformation).toBe(transformation);
        });

        it('should init dynamic params on dynamic transformation selection for current dataset', inject(function (TransformationService) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            stateMock.playground.preparation = null;

            var transformation = {name: 'cluster', dynamic: true};

            var ctrl = createController();

            //when
            ctrl.select(transformation);

            //then
            expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                columnId: '0001',
                datasetId: '41fa397a8239cd051b35',
                preparationId: null
            });
        }));

        it('should init dynamic params on dynamic transformation selection for current preparation', inject(function (TransformationService) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            stateMock.playground.preparation = {id: '35da66fc454568f4a52'};

            var transformation = {name: 'cluster', dynamic: true};
            var ctrl = createController();

            //when
            ctrl.select(transformation);

            //then
            expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(transformation, {
                columnId: '0001',
                datasetId: '41fa397a8239cd051b35',
                preparationId: '35da66fc454568f4a52'
            });
        }));

        it('should update fetch progress flag during dynamic parameters init', inject(function ($rootScope) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            var transformation = {name: 'cluster', dynamic: true};

            var ctrl = createController();
            ctrl.dynamicFetchInProgress = false;

            //when
            ctrl.select(transformation);
            expect(ctrl.dynamicFetchInProgress).toBe(true);
            $rootScope.$digest();

            //then
            expect(ctrl.dynamicFetchInProgress).toBe(false);
        }));

        it('should show NO CLUSTERS WERE FOUND message', inject(function ($rootScope) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            var dynamicTransformation = {name: 'cluster', dynamic: true, cluster: {clusters: []}};
            var ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(false);
            expect(ctrl.emptyParamsMsg).toEqual('NO_CLUSTERS_ACTION_MSG');
        }));

        it('should show NO PARAMETERS WERE FOUND message', inject(function ($rootScope) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            var dynamicTransformation = {name: 'choices', dynamic: true, parameters: []};
            var ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(false);
            expect(ctrl.emptyParamsMsg).toEqual('NO_PARAMETERS_ACTION_MSG');
        }));

        it('should show dynamic cluster transformation in a modal', inject(function ($rootScope) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            var dynamicTransformation = {
                name: 'cluster',
                dynamic: true,
                cluster: {clusters: [{parameters: [], replace: {}}]}
            };
            var ctrl = createController();

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(true);
        }));

        it('should show dynamic parameters in a modal', inject(function ($rootScope) {
            //given
            stateMock.playground.dataset = {id: '41fa397a8239cd051b35'};
            var ctrl = createController();
            var dynamicTransformation = {name: 'items', dynamic: true, parameters: [{}]};

            //when
            ctrl.select(dynamicTransformation);
            $rootScope.$digest();

            //then
            expect(ctrl.showModalContent).toBe(true);
        }));
    });
});